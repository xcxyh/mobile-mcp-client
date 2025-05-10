package com.xcc.mcpx4android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcc.mcpx4android.domain.model.McpServerInfo
import com.xcc.mcpx4android.domain.repository.McpServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class McpServerUiState(
    val localServers: List<McpServerInfo> = emptyList(),
    val remoteServers: List<McpServerInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class McpServerViewModel : ViewModel() {

    private val repository: McpServerRepository = object : McpServerRepository {
        override fun getLocalMcpServers(): Flow<List<McpServerInfo>> {
            return MutableStateFlow(emptyList())
        }

        override fun getRemoteMcpServers(): Flow<List<McpServerInfo>> {
            return MutableStateFlow(emptyList())
        }

        override suspend fun refreshServerStatus(serverId: String) {

        }

        override suspend fun testConnection(server: McpServerInfo): Boolean {
           return true
        }

    }

    private val _uiState = MutableStateFlow(McpServerUiState())
    val uiState: StateFlow<McpServerUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                repository.getLocalMcpServers(),
                repository.getRemoteMcpServers()
            ) { localServers, remoteServers ->
                _uiState.value = _uiState.value.copy(
                    localServers = localServers,
                    remoteServers = remoteServers,
                    isLoading = false
                )
            }
        }
    }

    fun refreshServerStatus(serverId: String) {
        viewModelScope.launch {
            try {
                repository.refreshServerStatus(serverId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun testConnection(server: McpServerInfo) {
        viewModelScope.launch {
            try {
                val isConnected = repository.testConnection(server)
                if (!isConnected) {
                    _uiState.value = _uiState.value.copy(
                        error = "无法连接到服务器 ${server.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }
} 