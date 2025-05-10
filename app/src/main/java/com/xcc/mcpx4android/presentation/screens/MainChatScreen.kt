package com.xcc.mcpx4android.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xcc.mcpx4android.presentation.components.AppDrawer
import com.xcc.mcpx4android.presentation.model.*
import com.xcc.mcpx4android.R
import com.xcc.mcpx4android.presentation.viewmodel.MainChatViewModel
import com.xcc.mcpx4android.presentation.viewmodel.McpServerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatScreen(
    mainChatViewModel: MainChatViewModel = viewModel(),
    mcpServerViewModel: McpServerViewModel = viewModel(),
    onNavigateToMcpConfig: () -> Unit
) {
    val uiState by mainChatViewModel.uiState.collectAsState()
    var userPrompt by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onClose = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onMcpConfigClick = onNavigateToMcpConfig
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AI 助手") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                val isEnabled = uiState is UiState.Success || uiState is UiState.Initial
                MessageInput(
                    prompt = userPrompt,
                    onPromptChange = { userPrompt = it },
                    onSendClick = {
                        if (userPrompt.isNotBlank()) {
                            mainChatViewModel.sendPrompt(userPrompt)
                            userPrompt = ""
                        }
                    },
                    enabled = isEnabled
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val currentState = uiState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                            Text("Initializing Model...", modifier = Modifier.padding(top = 60.dp))
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Error: ${currentState.errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is UiState.Success -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                reverseLayout = false
                            ) {
                                items(currentState.chatMessages, key = { it.id }) { message ->
                                    ChatMessageItem(message)
                                }
                            }

                            LaunchedEffect(currentState.chatMessages.size) {
                                if (currentState.chatMessages.isNotEmpty()) {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(currentState.chatMessages.lastIndex)
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Initial -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Enter a prompt to start chatting.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == Sender.USER
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val labelText = if (isUser) "User" else "Assist"
    val labelColor = textColor.copy(alpha = 0.7f) // Slightly faded label
    val horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom // Align avatar/spacer and bubble
    ) {
        Column( // Use Column to stack Label and Content
            modifier = Modifier
                .widthIn(max = 300.dp) // Limit bubble width
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Label Text
            Text(
                text = labelText,
                color = labelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp) // Space between label and content
            )

            // Message Content
            when (message.sender) {
                Sender.USER -> {
                    Text(text = message.text, color = textColor, fontSize = 16.sp)
                }
                Sender.AI -> {
                    when (message.state) {
                        AiMessageState.LOADING -> {
                            CircularProgressIndicator(modifier = Modifier
                                .padding(vertical = 8.dp)
                                .width(20.dp), strokeWidth = 2.dp)
                        }
                        AiMessageState.TOOL_CALL -> {
                            Text(
                                text = "Calling Tool: ${message.toolCallInfo ?: "..."}",
                                color = textColor.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                        AiMessageState.SUCCESS -> {
                            Text(text = message.text, color = textColor, fontSize = 16.sp)
                        }
                        AiMessageState.ERROR -> {
                            Text(
                                text = "Error: ${message.errorMessage ?: "Failed to get response"}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp
                            )
                        }
                        null -> { // Should not happen for AI, but handle defensively
                             Text(text = message.text, color = textColor, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MessageInput(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Use surface color for background
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            label = { Text(stringResource(R.string.label_prompt)) },
            modifier = Modifier.weight(1f),
            maxLines = 5 // Allow multi-line input but limit height
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                onSendClick()
            },
            enabled = enabled && prompt.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.action_go),
                tint = if (prompt.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
