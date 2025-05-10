package com.xcc.mcpx4android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class McpServerInfo(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val status: ServerStatus,
    val isLocal: Boolean,
    val tools: List<McpTool> = emptyList(),
    val description: String = ""
) : Parcelable

enum class ServerStatus {
    ONLINE,
    OFFLINE,
    CONNECTING
}

@Parcelize
data class McpTool(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean = true
) : Parcelable 