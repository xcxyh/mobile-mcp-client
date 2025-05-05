package com.xcc.mcpx4android

import java.util.UUID

/**
 * Represents the sender of a chat message.
 */
enum class Sender {
    USER, AI
}

/**
 * Represents the state of an AI message during generation.
 */
enum class AiMessageState {
    LOADING, TOOL_CALL, SUCCESS, ERROR
}

/**
 * Represents a single message in the chat history.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(), // Unique ID for LazyColumn keys
    val text: String,
    val sender: Sender,
    val state: AiMessageState? = null, // Null for user messages, set for AI messages
    val toolCallInfo: String? = null, // Details if state is TOOL_CALL
    val errorMessage: String? = null // Details if state is ERROR
)

/**
 * A sealed hierarchy describing the state of the chat screen.
 */
sealed interface UiState {
    /**
     * Empty state when the screen is first shown or after clearing history.
     */
    object Initial : UiState

    /**
     * State during the initial ViewModel setup (e.g., loading the model).
     */
    object Loading : UiState

    /**
     * State representing the active chat session with its history.
     * Individual AI messages within the list might have their own states (Loading, ToolCall, Success, Error).
     */
    data class Success(val chatMessages: List<ChatMessage> = emptyList()) : UiState

    /**
     * State representing a critical error, e.g., failure to load the model.
     * Errors related to specific message generation are handled within ChatMessage.
     */
    data class Error(val errorMessage: String) : UiState
}
