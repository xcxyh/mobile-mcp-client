package com.xcc.mcpx4android.mcpx

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.xcc.mcpx4android.BuildConfig
import com.xcc.mcpx4android.mcp.McpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.lang.Exception

/**
 * Represents the different states during chat response generation.
 */
sealed interface ChatResponseState {
    /**
     * Indicates that a tool (function) is being called.
     * @param toolName The name of the tool being called.
     * @param args The arguments passed to the tool.
     */
    data class ToolCall(val toolName: String, val args: Map<String, Any?>) : ChatResponseState

    /**
     * Indicates the final successful text response.
     * @param text The response text from the model.
     */
    data class Success(val text: String) : ChatResponseState

    /**
     * Indicates an error occurred during the process.
     * @param message The error message.
     */
    data class Error(val message: String) : ChatResponseState
}


class ChatSession(
    private val functionRepository: FunctionRepository,
    private val mcpClient: McpClient,
) {

    private lateinit var chat: Chat

    suspend fun init() {
      val tool = mcpClient.getTool()
      val serverTool = Tool(functionRepository.functionDeclarations.values.toList())

      val generativeModel = GenerativeModel(
            // Use Gemini 2.0
            modelName = "gemini-2.0-flash-exp",
            // Use the API Key we configured in local.properties
            apiKey = BuildConfig.apiKey,
            // Plug our tools
            tools = listOf(serverTool, tool),
            // Configure a useful system prompt.
            systemInstruction = content {
                text(
                    """
You are a helpful AI assistant with access to various external tools and APIs. Your goal is to complete tasks thoroughly and autonomously by making full use of these tools. Here are your core operating principles:

1.  **Take initiative** - Don't wait for user permission to use tools. If a tool would help complete the task, use it immediately.
2.  **Chain multiple tools together** - Many tasks require multiple tool calls in sequence. Plan out and execute the full chain of calls needed to achieve the goal.
3.  **Handle errors gracefully** - If a tool call fails, try alternative approaches or tools rather than asking the user what to do.
4.  **Make reasonable assumptions** - When tool calls require parameters, use your best judgment to provide appropriate values rather than asking the user.
5.  **Show your work** - After completing tool calls, explain what you did and show relevant results, but focus on the final outcome the user wanted.
6.  **Be thorough** - Use tools repeatedly as needed until you're confident you've fully completed the task. Don't stop at partial solutions.
7.  **Always response in Chinese** - Respond to the user in Chinese, even if the task is in another language. Translate if necessary.

Your responses should focus on results rather than asking questions. Only ask the user for clarification if the task itself is unclear or impossible with the tools available.
"""
                )
            }
        )
        // Initiate a chat session
        chat = generativeModel.startChat()
    }

    // Returns a Flow that emits different states of the response generation.
    fun send(prompt: String): Flow<ChatResponseState> = flow {
        try {
            // Send the first chat message and wait for the response
            var response = chat.sendMessage(prompt)

            // Process function calls in a loop
            while (response.functionCalls.isNotEmpty()) {
                val functionCalls = response.functionCalls // Get the list of calls

                // Emit ToolCall state for each function call *before* executing it
                functionCalls.forEach { functionCall ->
                    emit(ChatResponseState.ToolCall(functionCall.name, functionCall.args))
                }

                // Execute all function calls and collect responses
                val functionResponses = functionCalls.map { functionCall ->
                    try {
                        val toolResult = if (functionRepository.functionDeclarations.containsKey(functionCall.name)) {
                            functionRepository.call(functionCall.name, functionCall.args)
                        } else {
                            mcpClient.call(functionCall.name, functionCall.args)
                        }
                        FunctionResponsePart(functionCall.name, toolResult)
                    } catch (e: Exception) {
                        // If a specific tool call fails, create an error response part
                        FunctionResponsePart(functionCall.name, JSONObject(mapOf("error" to (e.localizedMessage ?: "Tool execution failed"))))
                    }
                }

                // Send back a message with all the FunctionResponseParts
                response = chat.sendMessage(
                    content(role = "function") {
                        functionResponses.forEach { part(it) }
                    }
                )
            }

            // No more function calls, emit the final text response if available
            response.text?.let {
                emit(ChatResponseState.Success(it))
            } ?: emit(ChatResponseState.Error("Model finished processing but returned no text content."))

        } catch (e: Exception) {
            // Emit error state if any exception occurs during the process
            emit(ChatResponseState.Error(e.localizedMessage ?: "An unknown error occurred"))
        }
    }
}
