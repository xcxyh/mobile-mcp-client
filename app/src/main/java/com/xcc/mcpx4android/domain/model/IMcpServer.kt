package com.xcc.mcpx4android.domain.model

import io.modelcontextprotocol.kotlin.sdk.server.Server

interface IMcpServer {

    fun createServer(): Server

}