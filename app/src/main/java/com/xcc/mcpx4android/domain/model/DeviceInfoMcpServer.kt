package com.xcc.mcpx4android.domain.model

import android.os.Build
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions

class DeviceInfoMcpServer: IMcpServer {

    private lateinit var server: Server

    override fun createServer(): Server {
        server = Server(
            Implementation(
                name = "DeviceInfoMCP",
                version = "1.0.0",
            ),
            ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(
                        listChanged = true
                    )
                )
            )
        )
        registerTools(server)

        return server
    }

    private fun registerTools(server: Server) {
        server.addTool(
            name = "get_device_info",
            description = "获取当前设备基本信息",
            inputSchema = Tool.Input()
        ) { request ->
            val infos = getDeviceSystemInfo()
            CallToolResult(
                content = listOf(TextContent(infos.toString()))
            )
        }
    }

    private fun getDeviceSystemInfo(): Map<String, String> {
        return mapOf(
            "品牌 (Brand)" to Build.BRAND,
            "制造商 (Manufacturer)" to Build.MANUFACTURER,
            "设备名 (Device)" to Build.DEVICE,
            "产品名 (Product)" to Build.PRODUCT,
            "型号 (Model)" to Build.MODEL,
            "主板 (Board)" to Build.BOARD,
            "硬件 (Hardware)" to Build.HARDWARE,
            "Android 版本 (Version)" to Build.VERSION.RELEASE,
            "SDK 版本 (SDK_INT)" to Build.VERSION.SDK_INT.toString(),
            "主机名 (Host)" to Build.HOST,
            "构建 ID (ID)" to Build.ID,
            "构建时间 (Time)" to Build.TIME.toString(),
            "构建用户 (User)" to Build.USER,
            "版本代码 (Incremental)" to Build.VERSION.INCREMENTAL
        )
    }

}