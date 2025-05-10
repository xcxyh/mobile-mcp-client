package com.xcc.mcpx4android.domain.repository

import android.util.Log
import com.dylibso.mcpx4j.core.McpxTool
import com.google.ai.client.generativeai.type.FunctionDeclaration
import org.json.JSONObject

data class FunctionRepository(
    val functionDeclarations: Map<String, FunctionDeclaration>,
    val mcpxTools: Map<String, McpxTool>) {

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