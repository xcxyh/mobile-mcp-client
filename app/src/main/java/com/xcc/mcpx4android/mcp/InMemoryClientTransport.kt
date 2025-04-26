package com.xcc.mcpx4android.mcp

import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.shared.AbstractTransport

class InMemoryClientTransport: AbstractTransport() {

    private var serverTransPort: InMemoryServerTransport? = null

    fun connectServer(serverTransport: InMemoryServerTransport) {
        this.serverTransPort = serverTransport
    }

    override suspend fun close() {
    }

    override suspend fun send(message: JSONRPCMessage) {
        serverTransPort?.receiveMsg(message)
    }

    override suspend fun start() {
    }

    suspend fun receiveMsg(message: JSONRPCMessage) {
        _onMessage.invoke(message)
    }
}