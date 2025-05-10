package com.xcc.mcpx4android.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xcc.mcpx4android.domain.model.McpServerInfo
import com.xcc.mcpx4android.domain.model.ServerStatus
import com.xcc.mcpx4android.presentation.viewmodel.McpServerUiState
import com.xcc.mcpx4android.presentation.viewmodel.McpServerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpServerListScreen(
    viewModel: McpServerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCP 服务器配置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 本地 MCP 服务器列表
            Text(
                text = "本地 MCP 服务器",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            McpServerList(
                servers = uiState.localServers,
                onRefreshStatus = viewModel::refreshServerStatus,
                onTestConnection = viewModel::testConnection
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 远程 MCP 服务器列表
            Text(
                text = "远程 MCP 服务器",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            McpServerList(
                servers = uiState.remoteServers,
                onRefreshStatus = viewModel::refreshServerStatus,
                onTestConnection = viewModel::testConnection
            )
        }
    }

    // 错误提示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: 显示错误提示
        }
    }
}

@Composable
private fun McpServerList(
    servers: List<McpServerInfo>,
    onRefreshStatus: (String) -> Unit,
    onTestConnection: (McpServerInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(servers) { server ->
            McpServerItem(
                server = server,
                onRefreshStatus = onRefreshStatus,
                onTestConnection = onTestConnection
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun McpServerItem(
    server: McpServerInfo,
    onRefreshStatus: (String) -> Unit,
    onTestConnection: (McpServerInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${server.host}:${server.port}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                ServerStatusIndicator(status = server.status)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = server.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (server.tools.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "可用工具：",
                        style = MaterialTheme.typography.titleSmall
                    )
                    server.tools.forEach { tool ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tool.isEnabled,
                                onCheckedChange = null
                            )
                            Column {
                                Text(text = tool.name)
                                Text(
                                    text = tool.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onRefreshStatus(server.id) }
                    ) {
                        Text("刷新状态")
                    }
                    TextButton(
                        onClick = { onTestConnection(server) }
                    ) {
                        Text("测试连接")
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerStatusIndicator(
    status: ServerStatus,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        ServerStatus.ONLINE -> MaterialTheme.colorScheme.primary to "在线"
        ServerStatus.OFFLINE -> MaterialTheme.colorScheme.error to "离线"
        ServerStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary to "连接中"
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
} 