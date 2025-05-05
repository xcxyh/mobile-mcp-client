package com.xcc.mcpx4android

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcc.mcpx4android.mcp.DeviceInfoMcpServer
import com.xcc.mcpx4android.mcp.McpClient
import com.xcc.mcpx4android.mcpx.ChatSession
import com.xcc.mcpx4android.mcpx.ToolFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BakingViewModel : ViewModel() {
  private val _uiState: MutableStateFlow<UiState> =
    MutableStateFlow(UiState.Initial)
  val uiState: StateFlow<UiState> =
    _uiState.asStateFlow()

  private lateinit var generativeModel: ChatSession

  private val mcpClient = McpClient()
  private val deviceInfoMcpServer = DeviceInfoMcpServer().createServer()

  init {
    _uiState.value = UiState.Loading

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
        _uiState.value = UiState.Initial
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
    _uiState.value = UiState.Loading

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val response = generativeModel.send(prompt)
        response?.let { outputContent ->
          _uiState.value = UiState.Success(outputContent)
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.localizedMessage ?: "")
      }
    }
  }
}