package com.xcc.mcpx4android.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    onClose: () -> Unit,
    onMcpConfigClick: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "个人信息",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Divider()
        
        // 用户信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "用户头像",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                // 昵称
                Text(
                    text = "用户昵称",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // MCP 配置入口
        ListItem(
            headlineContent = { Text("MCP 配置") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "MCP 配置"
                )
            },
            modifier = Modifier.clickable {
                onMcpConfigClick()
                onClose()
            }
        )
    }
} 