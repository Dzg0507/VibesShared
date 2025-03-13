package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.ChatWithUserInfo
import com.example.vibesshared.ui.ui.viewmodel.ChatsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: ChatsViewModel = hiltViewModel()
    val chats by viewModel.chats.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    LaunchedEffect(currentUserId) {
        viewModel.getUserChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, "Back", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(chats) { chatWithUserInfo ->
                ChatItemCard(chatWithUserInfo, navController, currentUserId)
            }
        }
    }
}

@Composable
fun ChatItemCard(
    chat: ChatWithUserInfo,
    navController: NavController,
    currentUserId: String
) {
    val gradient = remember(chat.otherUser.userId) {
        val seed = chat.otherUser.userId.hashCode()
        listOf(
            Color(seed and 0xFF0000 shr 16, seed and 0x00FF00 shr 8, seed and 0x0000FF),
            Color((seed * 2) and 0xFF0000 shr 16, (seed * 2) and 0x00FF00 shr 8, (seed * 2) and 0x0000FF)
        )
    }
    val moodEmoji = chat.lastMessage.let { msg ->
        when {
            msg.contains("happy", ignoreCase = true) -> "😊"
            msg.contains("sad", ignoreCase = true) -> "😢"
            msg.contains("image") -> "🖼️"
            else -> "💬"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Brush.linearGradient(gradient), RoundedCornerShape(12.dp))
            .clickable { navController.navigate(Screen.Messaging.createRoute(chat.chatId)) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chat.otherUser.profilePictureUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.otherUser.userName ?: "Unknown User",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$moodEmoji ${chat.lastMessage}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.animateContentSize() // Simple animation
            )
        }
    }
}