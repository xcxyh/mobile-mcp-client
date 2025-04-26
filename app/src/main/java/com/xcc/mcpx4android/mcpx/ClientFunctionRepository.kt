package com.xcc.mcpx4android.mcpx

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.dylibso.mcpx4j.core.McpxTool
import com.google.ai.client.generativeai.type.FunctionDeclaration
import org.json.JSONObject

class ClientFunctionRepository {

    val functionDeclarations: MutableMap<String, FunctionDeclaration> = mutableMapOf()

    val mcpxTools: MutableMap<String, McpxTool> = mutableMapOf()


    init {

        val tool = object : McpxTool {
            override fun name(): String {
                return "get_device_info"
            }
            override fun description(): String {
                return "获取当前设备基本信息"
            }
            override fun inputSchema(): String {
                return ""
            }
            override fun call(jsonInput: String?): String {
                return getDeviceSystemInfo().toString()
            }
        }

        val functionDeclaration = FunctionDeclaration(
            name = "get_device_info",
            description = "获取当前设备基本信息",
            parameters = listOf(),
            requiredParameters = listOf()
        )
        functionDeclarations.put("get_device_info", functionDeclaration)
        mcpxTools.put("get_device_info", tool)

    }

    fun getDeviceSystemInfo(): Map<String, String> {
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




    fun call(name: String, args: Map<String, String?>): JSONObject {
        val tool = mcpxTools[name]
            ?: return JSONObject(
                mapOf("result" to "$name is not a valid function"))

        val jargs = JSONObject(args)
        val jsargs = JSONObject(mapOf(
            "method" to "tools/call",
            "params" to mapOf(
                "name" to name,
                "arguments" to jargs
            )))

        Log.i("mcpx4j", "invoking $name with args = $jargs")
        // Invoke the mcp.run tool with the given arguments
        val res = tool.call(jsargs.toString())
        Log.i("mcpx4j", "$name returned: $res")

        // Ensure we always return a map
        return JSONObject(mapOf("result" to res))
    }

}