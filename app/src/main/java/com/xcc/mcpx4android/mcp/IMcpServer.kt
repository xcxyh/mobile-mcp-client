package com.xcc.mcpx4android.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server

interface IMcpServer {

    fun createServer(): Server

}