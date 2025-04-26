package com.xcc.mcpx4android.mcpx

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.xcc.mcpx4android.BuildConfig
import com.xcc.mcpx4android.mcp.DeviceInfoMcpServer
import com.xcc.mcpx4android.mcp.McpClient

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

Your responses should focus on results rather than asking questions. Only ask the user for clarification if the task itself is unclear or impossible with the tools available.
"""
                )
            }
        )
        // Initiate a chat session
        chat = generativeModel.startChat()
    }

    // Notice this function does I/O, hence the `suspend`.
    suspend fun send(prompt: String): String? {
        // Send the first chat message and wait for the response
        var response = chat.sendMessage(prompt)

        // Continue processing until there are no other function calls
        while (response.functionCalls.isNotEmpty()) {
            // For each function call, produce a response
            val parts = response.functionCalls.map {

                if (functionRepository.functionDeclarations.containsKey(it.name)) {
                    // Lookup the function in the repository and invoke it
                    val toolResult = functionRepository.call(it.name, it.args)
                    // Return the result for that specific function
                    FunctionResponsePart(it.name, toolResult)
                } else {
                    // Lookup the function in the repository and invoke it
                    val toolResult = mcpClient.call(it.name, it.args)
                    // Return the result for that specific function
                    FunctionResponsePart(it.name, toolResult)
                }
            }
            // Send back a message with all the `FunctionResponsePart`s
            // re-assign to `response`, and continue the loop.
            response = chat.sendMessage(
                content(role = "function") {
                    parts.forEach { part(it) }
                }
            )
        }

        // No more functions left to process, return the text response.
        return response.text
    }
}