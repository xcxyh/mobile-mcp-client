package com.xcc.mcpx4android.domain.repository

import com.xcc.mcpx4android.domain.model.McpServerInfo
import kotlinx.coroutines.flow.Flow

interface McpServerRepository {
    fun getLocalMcpServers(): Flow<List<McpServerInfo>>
    fun getRemoteMcpServers(): Flow<List<McpServerInfo>>
    suspend fun refreshServerStatus(serverId: String)
    suspend fun testConnection(server: McpServerInfo): Boolean
} 