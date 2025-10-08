package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.data.Chat
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.VividBlue
import com.example.vibesshared.ui.ui.viewmodel.ChatsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: ChatsViewModel = viewModel(factory = ChatsViewModel.provideFactory(LocalContext.current))
    val chats by viewModel.chats.collectAsState()
    var showUserSelectionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedChats by remember { mutableStateOf(setOf<String>()) }
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    // Start loading chats when the screen is launched
    LaunchedEffect(key1 = currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadChats(currentUserId)
        }
    }

    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                )
            )
    ) {
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
                        if (selectedChats.isNotEmpty()) {
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(onClick = { showUserSelectionDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "New Chat",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                // Add the button to open UserSelectionDialog here
                Button(
                    onClick = { showUserSelectionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Select User to Chat")
                }
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
                items(chats) { chat ->
                    ChatItemCard(
                        chat,
                        navController,
                        isSelected = selectedChats.contains(chat.id),
                        onSelectionChange = { isSelected ->
                            selectedChats = if (isSelected) {
                                selectedChats + chat.id
                            } else {
                                selectedChats - chat.id
                            }
                        },
                        selectedChats
                    )
                }
            }
        }

        // Show UserSelectionDialog when the state is true
        if (showUserSelectionDialog) {
            UserSelectionDialog(
                onUserSelected = { selectedUserId ->
                    viewModel.createNewChat(listOf(selectedUserId)) // Create chat with the selected user
                    showUserSelectionDialog = false
                },
                onDismiss = { showUserSelectionDialog = false }
            )
        }

        // delete confirmation dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Chats") },
                text = { Text("Are you sure you want to delete the selected chats?") },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedChats.forEach { chatId ->
                                viewModel.deleteChat(chatId)
                            }
                            selectedChats = emptySet()
                            showDeleteConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItemCard(
    chat: Chat,
    navController: NavController,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    selectedChats: Set<String>
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Create an infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Define the border animation values
    val animatedBorderColor by infiniteTransition.animateColor(
        initialValue = Color.LightGray,
        targetValue = Color.White,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // Use the animated color for the border
    val border = BorderStroke(2.dp, animatedBorderColor)

    // Determine the card's background color based on selection state
    val cardColor = if (isSelected) Color.LightGray else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // You might want to provide an indication for the long press
                onClick = {
                    if (selectedChats.isEmpty()) {
                        navController.navigate("messaging/${chat.id}")
                    } else {
                        onSelectionChange(!isSelected)
                    }
                },
                onLongClick = { onSelectionChange(!isSelected) }
            )
            .then(if (isPressed) Modifier.border(border, RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = cardColor) // Use the determined color here
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
                    text = chat.otherUserName ?: "Unknown Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // You can add unread message count here if needed
        }
    }
}

@Composable
fun UserSelectionDialog(onUserSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val viewModel: ChatsViewModel = viewModel()
    val users by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Fetch users when the dialog is launched
    LaunchedEffect(key1 = true) {
        viewModel.fetchUsers()
        Log.d("ChatsScreen", "UserSelectionDialog: LaunchedEffect - fetchUsers called")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select User") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchUsers(it)
                    },
                    label = { Text("Search") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(users) { user ->
                        Log.d("ChatsScreen", "UserItem: ${user.firstName} ${user.lastName}, userId: ${user.userId}")
                        UserItem(user = user, onUserSelected = {
                            onUserSelected(user.userId)
                            onDismiss()
                        })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UserItem(user: UserProfile, onUserSelected: (String) -> Unit) {
    Log.d("ChatsScreen", "UserItem: ${user.firstName} ${user.lastName}, userId: ${user.userId}")
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onUserSelected(user.userId) }
        .padding(8.dp)) {
        Text(text = "${user.firstName} ${user.lastName}")
    }
}

