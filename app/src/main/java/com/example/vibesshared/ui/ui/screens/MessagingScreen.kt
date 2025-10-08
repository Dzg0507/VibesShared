package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.data.Message
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.VividBlue
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
    val context = LocalContext.current
    val viewModel: ChatsViewModel = viewModel(factory = ChatsViewModel.provideFactory(context))
    val messages by viewModel.messages.collectAsState()
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
        }
    }

    // Load messages when the screen is launched
    LaunchedEffect(key1 = chatId) {
        viewModel.loadMessages(chatId)
        viewModel.markMessagesAsRead(chatId, currentUserId)
    }

    // Fetch the chat when the screen is launched or chatId changes
    LaunchedEffect(key1 = chatId) {
        val db = Firebase.firestore
        db.collection("chats").document(chatId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("MessagingScreen", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chat = snapshot.toObject(com.example.vibesshared.ui.ui.data.Chat::class.java)
                isChatDeletedByOther = chat?.deletedBy?.isNotEmpty() == true && !chat.deletedBy.contains(currentUserId)
            } else {
                Log.d("MessagingScreen", "Chat data not available.")
            }
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
            // Display chat messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageItem(message, currentUserId)
                }
            }

            // Display the chat deleted message if necessary
            if (isChatDeletedByOther) {
                Text(
                    text = "The other user has deleted this chat. You will have to start a new chat.",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field and send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    label = { Text("Enter message") },
                    modifier = Modifier.weight(1f),
                    readOnly = isChatDeletedByOther,
                    trailingIcon = {
                        // Updated to show FAB with an add image icon
                        FloatingActionButton(
                            onClick = { showImagePicker = true },
                            modifier = Modifier.size(48.dp),
                            containerColor = Color.White // Set background color to white
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Add Image",
                                tint = Color.Black // Set icon color to black
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        if (!isChatDeletedByOther && newMessageText.isNotBlank()) {
                            viewModel.sendMessage(chatId, newMessageText, currentUserId)
                            newMessageText = ""
                        } else if (isChatDeletedByOther) {
                            // Optionally, inform the user that they cannot send messages to a deleted chat
                            Log.d("MessagingScreen", "Cannot send message. Chat deleted by other user.")
                        }
                    },
                    modifier = Modifier.clip(CircleShape),
                    enabled = !isChatDeletedByOther // Disable send button if chat is deleted by other
                ) {
                    Text("Send")
                }
            }

            // Trigger the image picker when showImagePicker is true
            if (showImagePicker) {
                LaunchedEffect(key1 = chatId) {
                    imagePickerLauncher.launch("image/*")
                    showImagePicker = false
                }
            }
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
            color = Color.Gray
        )
    }
}