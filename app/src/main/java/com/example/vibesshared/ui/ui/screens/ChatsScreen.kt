package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    LaunchedEffect(key1 = currentUserId) {
        viewModel.getUserChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
            items(chats) { chatWithUserInfo -> // Use ChatWithUserInfo
                ChatItemCard(
                    chat = chatWithUserInfo, // Pass ChatWithUserInfo
                    navController = navController,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

@Composable
fun ChatItemCard(
    chat: ChatWithUserInfo, // Receive ChatWithUserInfo
    navController: NavController,
    currentUserId: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.Messaging.createRoute(chat.chatId))
            },
        verticalAlignment = Alignment.CenterVertically // Vertically align items in the Row
    ) {
        // Display the other user's profile picture
        AsyncImage(
            model = chat.otherUser.profilePictureUrl, // Use profilePictureUrl from UserProfile
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp) // Adjust size as needed
                .clip(CircleShape), // Circular image
            contentScale = ContentScale.Crop // Crop the image to fit the circle
        )

        Spacer(modifier = Modifier.width(16.dp)) // Add space between image and text

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.otherUser.userName ?: "Unknown User", // Use userName from UserProfile
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = chat.lastMessage, // Display the last message
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}