package com.xcc.mcpx4android.mcp

import com.google.ai.client.generativeai.type.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server

interface IMcpServer {

    fun createServer(): Server

    fun convertToGeminiTool(): Tool

}