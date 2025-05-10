package com.xcc.mcpx4android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xcc.mcpx4android.presentation.screens.MainChatScreen
import com.xcc.mcpx4android.presentation.screens.McpServerListScreen
import com.xcc.mcpx4android.ui.theme.Mcpx4androidTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Mcpx4androidTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          val navController = rememberNavController()

          NavHost(
            navController = navController,
            startDestination = "chat"
          ) {
            composable("chat") {
              MainChatScreen(
                onNavigateToMcpConfig = {
                  navController.navigate("mcp_config")
                }
              )
            }
            composable("mcp_config") {
              McpServerListScreen(
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                onBackClick = {
                  navController.popBackStack()
                }
              )
            }
          }
        }
      }
    }
  }
}