package com.xcc.mcpx4android.data.datasource

import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.shared.AbstractTransport

class InMemoryServerTransport: AbstractTransport() {

    private var clientTransport: InMemoryClientTransport? = null

    fun connectClient(clientTransport: InMemoryClientTransport) {
        this.clientTransport = clientTransport
    }

    override suspend fun close() {}

    override suspend fun send(message: JSONRPCMessage) {
        clientTransport?.receiveMsg(message)
    }

    override suspend fun start() {}

    suspend fun receiveMsg(message: JSONRPCMessage) {
        _onMessage.invoke(message)
    }
}