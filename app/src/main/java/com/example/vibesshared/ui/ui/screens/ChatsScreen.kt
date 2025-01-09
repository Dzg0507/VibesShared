package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavHostController) {
    // This could later be moved to a ViewModel
    val chats = remember {
        mutableStateOf(listOf(
            ChatPreview("John Doe", "Hey, how are you?", "2m ago"),
            ChatPreview("Jane Smith", "See you tomorrow!", "15m ago"),
            ChatPreview("Group Chat", "Alice: Great idea!", "1h ago")
        ))
    }

    VibesSharedTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chats") },
                    actions = {
                        // Button to start a new chat
                        IconButton(onClick = { /* Handle new chat */ }) {
                            Icon(Icons.Default.Add, "New Chat")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chats.value) { chat ->
                        ChatItem(chat) {
                            // Navigate to individual chat
                            // navController.navigate("chat/${chat.name}")
                        }
                    }
                }
            }
        }
    }
}

data class ChatPreview(
    val name: String,
    val lastMessage: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}