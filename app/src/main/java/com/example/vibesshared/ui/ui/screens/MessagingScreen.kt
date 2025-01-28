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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.vibesshared.ui.ui.data.Message
import com.example.vibesshared.ui.ui.theme.*
import com.example.vibesshared.ui.ui.viewmodel.ChatsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(navController: NavController, chatId: String, modifier: Modifier = Modifier) {

    val viewModel: ChatsViewModel = hiltViewModel()
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    var newMessageText by remember { mutableStateOf("") }
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    var isChatDeletedByOther by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("MessagingScreen", "Selected image URI: $it")
            viewModel.sendImageMessage(chatId, it, currentUserId)
            showImagePicker = false
        }
    }

    LaunchedEffect(key1 = chatId) {
        viewModel.loadMessages(chatId)
        viewModel.markMessagesAsRead(chatId, currentUserId)
        observeChatDeletion(chatId) { deleted ->
            isChatDeletedByOther = deleted
        }
    }

    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Apply weight here
                reverseLayout = true
            ) {
                items(messages) { message ->
                    MessageItem(message, currentUserId)
                }
            }
            ChatDeletedMessage(isChatDeletedByOther)
            Spacer(modifier = Modifier.height(16.dp))
            MessageInputRow(
                newMessageText = newMessageText,
                onNewMessageTextChange = { newMessageText = it },
                onSendClick = {
                    if (!isChatDeletedByOther && newMessageText.isNotBlank()) {
                        viewModel.sendMessage(chatId, newMessageText, currentUserId)
                        newMessageText = ""
                    }

                },
                onAddImageClick = { showImagePicker = true },
                isChatDeletedByOther = isChatDeletedByOther
            )

            if (showImagePicker) {
                LaunchedEffect(key1 = Unit) {
                    imagePickerLauncher.launch("image/*")
                }
            }
        }
    }
}

@Composable
fun MessageList(messages: List<Message>, currentUserId: String) {
    LazyColumn(modifier = Modifier) {
        items(messages) { message ->
            MessageItem(message, currentUserId)
        }
    }
}

@Composable
fun ChatDeletedMessage(isChatDeletedByOther: Boolean) {
    if (isChatDeletedByOther) {
        Text(
            text = "The other user has deleted this chat. You will have to start a new chat.",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputRow(
    newMessageText: String,
    onNewMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAddImageClick: () -> Unit,
    isChatDeletedByOther: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newMessageText,
            onValueChange = onNewMessageTextChange,
            label = { Text("Enter message") },
            modifier = Modifier.weight(1f),
            readOnly = isChatDeletedByOther,
            trailingIcon = {
                FloatingActionButton(
                    onClick = onAddImageClick,
                    modifier = Modifier.size(48.dp),
                    containerColor = Color.White
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add Image",
                        tint = Color.Black
                    )
                }
            }
        )

        Button(
            onClick = onSendClick,
            modifier = Modifier.clip(CircleShape),
            enabled = !isChatDeletedByOther
        ) {
            Text("Send")
        }
    }
}

fun observeChatDeletion(chatId: String, onChatDeleted: (Boolean) -> Unit) {
    val db = Firebase.firestore
    db.collection("chats").document(chatId).addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.w("MessagingScreen", "Listen failed.", e)
            return@addSnapshotListener
        }

        if (snapshot != null && snapshot.exists()) {
            val chat =
                snapshot.toObject(com.example.vibesshared.ui.ui.data.Chat::class.java)
            val currentUserId = Firebase.auth.currentUser?.uid ?: ""
            onChatDeleted(
                chat?.deletedBy?.isNotEmpty() == true && !chat.deletedBy.contains(
                    currentUserId
                )
            )
        } else {
            Log.d("MessagingScreen", "Chat data not available.")
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId
    val isImageMessage = message.type == "image"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (isImageMessage && message.imageUrl != null) {
            // Display image message with Coil's AsyncImage
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
            // Display text message
            Text(
                text = message.text,
                modifier = Modifier
                    .background(if (isCurrentUser) Color.Cyan else Color.LightGray)
                    .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Format the timestamp
        val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
        Text(
            text = message.timestamp?.let { dateFormat.format(it) } ?: "",
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}