package com.xcc.mcpx4android

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

  init {
    _uiState.value = UiState.Loading
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        generativeModel = ChatSession(ToolFetcher.fetchFunctions())
        _uiState.value = UiState.Initial
      }
    }
  }

  fun sendPrompt(
    bitmap: Bitmap,
    prompt: String
  ) {
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