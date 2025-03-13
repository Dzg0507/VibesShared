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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.VideoPlayer
import com.example.vibesshared.ui.ui.data.Chat
import com.example.vibesshared.ui.ui.data.Message
import com.example.vibesshared.ui.ui.theme.AppColors.ElectricPurple
import com.example.vibesshared.ui.ui.theme.AppColors.LimeGreen
import com.example.vibesshared.ui.ui.theme.AppColors.SunsetOrange
import com.example.vibesshared.ui.ui.theme.AppColors.VividBlue
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.utils.formatTimestamp
import com.example.vibesshared.ui.ui.viewmodel.ChatsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.collectLatest


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
    var showVideoPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen)

    // Function to launch the respective picker based on the type
    fun launchPicker(type: String) {
        when (type) {
            "image" -> showImagePicker = true
            "video" -> showVideoPicker = true
        }
    }

    // Launcher for both image and video
    val contentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Determine the type of the selected media
            val type = context.contentResolver.getType(it)?.let { contentType ->
                if (contentType.startsWith("image")) "image" else "video"
            } ?: "unknown"

            // Send the message accordingly
            when (type) {
                "image" -> viewModel.sendImageMessage(chatId, it, currentUserId)
                "video" -> viewModel.sendVideoMessage(chatId, it, currentUserId)
                else -> Log.e("MessagingScreen", "Unsupported media type")
            }
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
                isChatDeletedByOther = true
            }
        }
        DisposableHandle { listenerRegistration.remove() }
    }

    // Collect messages using collectLatest to handle optimistic updates
    LaunchedEffect(chatId) {
        viewModel.getMessages(chatId).collectLatest { messages: List<Message> ->
            viewModel.updateMessages(messages)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                }
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
                    enabled = !isChatDeletedByOther
                )

                IconButton(onClick = { launchPicker("image") }) {
                    Icon(Icons.Filled.Add, "Attach")
                }

                Button(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            viewModel.sendMessage(chatId, newMessageText, currentUserId)
                            newMessageText = ""
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
                    modifier = Modifier.padding(8.dp).align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    reverseLayout = false
                ) {
                    items(messages, key = { it.messageId }) { message ->
                        MessageItem(message, currentUserId)
                        if (message.senderId != currentUserId) {
                            viewModel.markMessagesAsRead(chatId, currentUserId)
                        }
                    }
                }

                if (showImagePicker) {
                    contentLauncher.launch("image/*")
                    showImagePicker = false
                }
                if (showVideoPicker) {
                    contentLauncher.launch("video/*")
                    showVideoPicker = false
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
        when (message.type) {
            "image" -> message.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Image Message",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            "video" -> message.videoUrl?.let {
                // Use your actual VideoPlayer composable here
                VideoPlayer(videoUrl = it)
            }
            else -> Text(
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