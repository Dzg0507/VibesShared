package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController
) {
    var postText by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPosting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImages = selectedImages + it }
    }

    LaunchedEffect(isPosting) {
        if (isPosting) {
            scope.launch {
                try {
                    createPost(postText, selectedImages)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                } catch (e: Exception) {
                    error = e.message
                    isPosting = false
                }
            }
        }
    }

    error?.let {
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = { error = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        enabled = !isPosting
                    ) {
                        Icon(Icons.Filled.ArrowBackIosNew, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { isPosting = true },
                        enabled = postText.isNotBlank() && !isPosting
                    ) {
                        Text(if (isPosting) "Posting..." else "Post")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextField(
                    value = postText,
                    onValueChange = { if (!isPosting) postText = it },
                    enabled = !isPosting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("What's on your mind?") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )

                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages) { uri ->
                        Box(
                            modifier = Modifier.size(100.dp)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    if (!isPosting) selectedImages = selectedImages - uri
                                },
                                enabled = !isPosting,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove image",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    item {
                        IconButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.medium),
                            enabled = !isPosting
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (isPosting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

suspend fun createPost(postText: String, images: List<Uri>) {
    try {
        val auth = Firebase.auth
        val storage = Firebase.storage
        val firestore = Firebase.firestore

        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

        // Upload image if exists
        val imageUrl = if (images.isNotEmpty()) {
            val imageRef = storage.reference.child("posts/${UUID.randomUUID()}")
            imageRef.putFile(images.first()).await()
            imageRef.downloadUrl.await().toString()
        } else null

        val postId = UUID.randomUUID().toString()
        val post = Post(
            postId = postId,
            userId = currentUser.uid,  // Use actual user ID
            postText = postText,
            postImage = imageUrl,  // Match your Post data class property name
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("app_data").document("posts")
            .collection("user_posts").document(postId)
            .set(post)
            .await()

    } catch (e: Exception) {
        println("Error creating post: ${e.message}")
        throw e
    }
}
