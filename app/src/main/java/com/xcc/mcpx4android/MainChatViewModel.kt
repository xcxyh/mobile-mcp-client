package com.xcc.mcpx4android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcc.mcpx4android.mcp.DeviceInfoMcpServer
import com.xcc.mcpx4android.mcp.McpClient
import com.xcc.mcpx4android.mcpx.ChatResponseState
import com.xcc.mcpx4android.mcpx.ChatSession
import com.xcc.mcpx4android.mcpx.ToolFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainChatViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Loading) // Start in Loading until model is ready
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private lateinit var generativeModel: ChatSession

    private val mcpClient = McpClient()
    private val deviceInfoMcpServer = DeviceInfoMcpServer().createServer()

    init {
        // Keep Loading state until model is initialized
        viewModelScope.launch(Dispatchers.IO) {
            mcpClient.startClient()
        }
        viewModelScope.launch(Dispatchers.IO) {
            mcpClient.connectServer(deviceInfoMcpServer)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                generativeModel = ChatSession(ToolFetcher.fetchFunctions(), mcpClient).apply {
                    init()
                }
                // Model ready, create initial greeting message
                val greetingMessage = ChatMessage(
                    text = "Hello! How can I help you today?", // Your greeting text here
                    sender = Sender.AI,
                    state = AiMessageState.SUCCESS
                )
                // Switch to Success state with the greeting message
                _uiState.value = UiState.Success(listOf(greetingMessage))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            mcpClient.closeClient()
            deviceInfoMcpServer.close()
        }
    }

    fun sendPrompt(prompt: String) {
        // Get current message list or start a new one
        val currentMessages = (_uiState.value as? UiState.Success)?.chatMessages ?: return // Exit if not in Success state

        // Create user message and initial AI loading message placeholder
        val userMessage = ChatMessage(text = prompt, sender = Sender.USER)
        val aiMessagePlaceholder = ChatMessage(text = "", sender = Sender.AI, state = AiMessageState.LOADING)
        val aiMessageId = aiMessagePlaceholder.id // Store ID to update this specific message

        // Update UI immediately with user message and AI loading placeholder
        _uiState.value = UiState.Success(currentMessages + userMessage + aiMessagePlaceholder)

        viewModelScope.launch(Dispatchers.IO) {
            generativeModel.send(prompt)
                .onEach { responseState ->
                    // Update the specific AI message based on the emitted state
                    _uiState.update { currentState ->
                        if (currentState is UiState.Success) {
                            val updatedMessages = currentState.chatMessages.map { message ->
                                if (message.id == aiMessageId) {
                                    // Update the placeholder message
                                    when (responseState) {
                                        is ChatResponseState.ToolCall -> {
                                            Log.d("BakingViewModel", "ToolCall: ${responseState.toolName}")
                                            message.copy(
                                                state = AiMessageState.TOOL_CALL,
                                                // Format tool call info for display
                                                toolCallInfo = "${responseState.toolName}(${responseState.args.map { "${it.key}=${it.value}" }.joinToString()})"
                                            )
                                        }
                                        is ChatResponseState.Success -> {
                                            Log.d("BakingViewModel", "Success: ${responseState.text}")
                                            message.copy(
                                                text = responseState.text,
                                                state = AiMessageState.SUCCESS,
                                                toolCallInfo = null // Clear tool info on success
                                            )
                                        }
                                        is ChatResponseState.Error -> {
                                             Log.e("BakingViewModel", "Error state: ${responseState.message}")
                                            message.copy(
                                                text = "Error: ${responseState.message}",
                                                state = AiMessageState.ERROR,
                                                errorMessage = responseState.message,
                                                toolCallInfo = null // Clear tool info on error
                                            )
                                        }
                                    }
                                } else {
                                    message // Keep other messages unchanged
                                }
                            }
                            UiState.Success(updatedMessages)
                        } else {
                            currentState // Should not happen in this flow, but handle defensively
                        }
                    }
                }
                .catch { e ->
                    // Handle exceptions during the flow collection itself
                    Log.e("BakingViewModel", "Flow collection error", e)
                    _uiState.update { currentState ->
                        if (currentState is UiState.Success) {
                            val updatedMessages = currentState.chatMessages.map { message ->
                                if (message.id == aiMessageId) {
                                    message.copy(
                                        text = "Error: ${e.localizedMessage ?: "Flow collection failed"}",
                                        state = AiMessageState.ERROR,
                                        errorMessage = e.localizedMessage ?: "Flow collection failed",
                                        toolCallInfo = null
                                    )
                                } else {
                                    message
                                }
                            }
                            UiState.Success(updatedMessages)
                        } else {
                            currentState
                        }
                    }
                }
                .collect() // Start collecting the flow
        }
    }
}
