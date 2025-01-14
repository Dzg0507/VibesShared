package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.VividBlue
import com.example.vibesshared.ui.ui.viewmodel.ChatData
import com.example.vibesshared.ui.ui.viewmodel.ChatsViewModel
import com.example.vibesshared.ui.ui.viewmodel.MessageData

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController) {
    val viewModel: ChatsViewModel = viewModel()
    val chatDataList by viewModel.chatDataList.collectAsState()
    var showNewChatDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val videoUri = remember {
        Uri.parse("android.resource://${context.packageName}/raw/anim_back2") // Replace with your video file name
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(videoUri)
                    start()
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = 1.5f, scaleY = 2.7f)
        )

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
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showNewChatDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "New Chat",
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
                items(chatDataList) { chatData ->
                    ChatItemCard(chatData, navController)
                }
            }
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onAddChat = { newChatData ->
                viewModel.createChat(newChatData)
            }
        )
    }
}

@Composable
fun ChatItemCard(chatData: ChatData, navController: NavController) {
    val isHovered by remember { mutableStateOf(false) }
    val animatedBorder = animateBorderStroke(
        targetThickness = if (isHovered) 4.dp else 2.dp,
        targetColor = if (isHovered) Color.White else Color.LightGray
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                chatData.chatId?.let { chatId ->
                    navController.navigate("messaging/$chatId")
                }
            }
            .border(animatedBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            ElectricPurple,
                            NeonPink,
                            VividBlue,
                            SunsetOrange,
                            LimeGreen
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://picsum.photos/200/300",
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chatData.members.firstOrNull() ?: "Unknown Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isHovered) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = chatData.messages.values.lastOrNull()?.content ?: "",
                    fontSize = 14.sp,
                    color = if (isHovered) Color.LightGray else Color.Gray
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            val unreadMessages = chatData.messages.count { true }
            if (unreadMessages > 0) {
                val infiniteTransition = rememberInfiniteTransition()
                val translationY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Text(
                    text = "$unreadMessages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier.offset(y = translationY.dp)
                )
            }
        }
    }
}

@Composable
fun animateBorderStroke(
    targetThickness: Dp,
    targetColor: Color
): BorderStroke {
    val infiniteTransition = rememberInfiniteTransition()
    val thickness by infiniteTransition.animateValue(
        initialValue = 2.dp,
        targetValue = targetThickness,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color by infiniteTransition.animateColor(
        initialValue = Color.LightGray,
        targetValue = targetColor,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    return BorderStroke(thickness, color)
}

@Composable
fun NewChatDialog(onDismiss: () -> Unit, onAddChat: (ChatData) -> Unit) {
    var members by remember { mutableStateOf("") }
    var messageContent by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Chat") },
        text = {
            Column {
                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it },
                    label = { Text("Members (comma-separated)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    label = { Text("Message Content") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("Sender") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val membersList = members.split(",").map { it.trim() }
                val newChatData = ChatData(
                    members = membersList,
                    messages = mapOf(
                        "message1" to MessageData(
                            content = messageContent,
                            sender = sender,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                )
                onAddChat(newChatData)
                onDismiss()
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier
            .scale(scale)
            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
    )
}