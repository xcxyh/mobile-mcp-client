package com.xcc.mcpx4android.mcp

import android.util.Log
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Tool
import com.google.gson.Gson
import com.xcc.mcpx4android.mcpx.ParsedSchema
import io.modelcontextprotocol.kotlin.sdk.ClientCapabilities
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.json.JSONObject

class McpClient {

    private val TAG = "McpClient"

    private var client: Client? = null
    private val clientTransport = InMemoryClientTransport()
    private val serverTransport = InMemoryServerTransport()

    suspend fun startClient() {
        kotlin.runCatching {
            // 创建Client
            val clientInfo = Implementation(
                name = "Android MCP Client",
                version = "1.0.0"
            )
            val clientCapabilities = ClientCapabilities()
            val clientOptions = ClientOptions(
                capabilities = clientCapabilities,
                enforceStrictCapabilities = true
            )
            // 创建Client并连接到Transport
            client = Client(clientInfo, clientOptions)
            serverTransport.connectClient(clientTransport)
            clientTransport.connectServer(serverTransport)
            Log.i(TAG, "startClient")
            client?.connect(clientTransport)
            Log.i(TAG, "startClient end")
        }.onFailure {
            Log.e(TAG, "start client fail: ${it.message.toString()}")
        }
    }

    suspend fun connectServer(server: Server) {
        Log.i(TAG, "connectServer")
        server.connect(serverTransport)
        Log.i(TAG, "connectServer end")
    }

    suspend fun getTool(): Tool {
        val functions = client?.listTools()?.tools?.map {
            val parsedSchema = ParsedSchema.parse(Gson().toJson(it))
            FunctionDeclaration(
                name = it.name,
                description = it.description ?: "",
                parameters = parsedSchema.parameters,
                requiredParameters = parsedSchema.requiredParameters
            )
        } ?: emptyList()
        Log.i(TAG, "functions: ${functions.map { it.name }.toString()}")
        return Tool(functions)
    }

    suspend fun call(name: String, args: Map<String, String?>): JSONObject {
        val result = client?.callTool(name, args)
        Log.i(TAG, "call tool result: ${result?.content}")
        return JSONObject(mapOf("result" to Gson().toJson(result)))
    }

    suspend fun closeClient() {
        client?.close()
    }

}