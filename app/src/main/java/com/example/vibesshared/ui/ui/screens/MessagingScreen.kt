package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.data.Chat
import com.example.vibesshared.ui.ui.data.Message
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.VividBlue
import com.example.vibesshared.ui.ui.viewmodel.ChatsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DisposableHandle
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    navController: NavController,
    chatId: String,
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val currentUserId = remember { Firebase.auth.currentUser?.uid ?: "" }
    var newMessageText by remember { mutableStateOf("") }
    var isChatDeletedByOther by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendImageMessage(chatId, it, currentUserId)
            showImagePicker = false // Close picker after selection
        }
    }

    // Observe chat deletion
    LaunchedEffect(chatId, currentUserId) {
        val db = Firebase.firestore
        val chatRef = db.collection("chats").document(chatId)

        val listenerRegistration = chatRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MessagingScreen", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chat = snapshot.toObject(Chat::class.java)
                isChatDeletedByOther = chat?.participants?.contains(currentUserId) == false
            } else {
                Log.d("MessagingScreen", "Chat data not available (deleted).")
                isChatDeletedByOther = true
            }
        }
        DisposableHandle { listenerRegistration.remove() }
    }

    LaunchedEffect(key1 = chatId) {
        viewModel.getMessages(chatId)
    }

    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    label = { Text("Enter message") },
                    modifier = Modifier.weight(1f),
                    enabled = !isChatDeletedByOther,
                    trailingIcon = {
                        IconButton(onClick = { showImagePicker = true }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Image"
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            viewModel.sendMessage(chatId, newMessageText, currentUserId)
                            newMessageText = "" // Clear input after sending
                        }
                    },
                    enabled = !isChatDeletedByOther && newMessageText.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = gradientColors))
                .padding(innerPadding)
        ) {

            if (isChatDeletedByOther) {
                Text(
                    text = "This chat has been deleted by the other user.",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(messages, key = { message -> message.messageId }) { message ->
                        MessageItem(message, currentUserId)

                        if (message.senderId != currentUserId) {
                            viewModel.markMessagesAsRead(chatId, currentUserId)
                        }
                    }
                }

                if (showImagePicker) {
                    imagePickerLauncher.launch("image/*")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (message.type == "image" && message.imageUrl != null) {
            AsyncImage(
                model = message.imageUrl,
                contentDescription = "Image Message",
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = message.text,
                modifier = Modifier
                    .background(
                        if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        MessageTimestamp(timestamp = message.timestamp)
    }
}

@Composable
fun MessageTimestamp(timestamp: Timestamp?) {
    Text(
        text = formatTimestamp(timestamp),
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.padding(top = 4.dp)
    )
}

fun formatTimestamp(timestamp: Timestamp): String {
    return if (true) {
        try {
            val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a", Locale.getDefault())
            localDateTime.format(formatter)
        } catch (e: Exception) {
            "Invalid date"
        }
    } else {
        "No date available"
    }
}