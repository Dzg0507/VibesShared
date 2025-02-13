package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel.PostCreationStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: PostViewModel = hiltViewModel()
) {
    var postText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) } // Keep track of posting state
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val postCreationStatus by viewModel.postCreationStatus.collectAsState()
    val scope = rememberCoroutineScope() // Get a CoroutineScope
    val context = LocalContext.current
    val selectedImages by viewModel.selectedImages.collectAsState()


    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch { // Launch coroutine
                viewModel.addImage(it)
            }
        }
    }

    fun handleImagePicker() {
        if (!isPosting) {  // Prevent actions during post creation
            imagePicker.launch("image/*")
        }
    }


    //Combine into one function.
    fun createPost() {
        isPosting = true // Indicate that posting has started
        scope.launch{
            viewModel.addPost(postText, selectedImages, context)
        }
    }

    @Composable
    fun ErrorDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }

    if (showErrorDialog) {
        ErrorDialog { showErrorDialog = false }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }, enabled = !isPosting) { // Disable during posting
                        Icon(Icons.Filled.ArrowBackIosNew, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { createPost() },
                        enabled = postText.isNotBlank() && !isPosting // Disable if posting or text is blank
                    ) {
                        Text(if (isPosting) "Posting..." else "Post") // Show "Posting..." when in progress
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
                    onValueChange = { if (!isPosting) postText = it }, // Only allow changes if not posting
                    enabled = !isPosting, // Disable text field while posting
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Allow TextField to take up remaining space
                    placeholder = { Text("What's on your mind?") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface, //Consistent colors
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, //Text colors
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
                                    if (!isPosting) {
                                        scope.launch { // Launch coroutine
                                            viewModel.removeImage(uri)
                                        }
                                    }
                                },
                                enabled = !isPosting, // Disable during posting
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
                            onClick = { handleImagePicker() },
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.medium),
                            enabled = !isPosting // Disable during posting
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isPosting) { // Show progress indicator when posting
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Blue // Or any color you prefer
                    )
                }
            }

            if (isPosting) { // Show a loading indicator over the content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LaunchedEffect(postCreationStatus) {
            when (postCreationStatus) {
                is PostCreationStatus.Success -> {
                    isPosting = false // Reset the posting state
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
                is PostCreationStatus.Error -> {
                    errorMessage = (postCreationStatus as PostCreationStatus.Error).message
                    showErrorDialog = true
                    isPosting = false // Reset posting state on error
                }
                else -> { /* Do nothing for Idle or Creating state */ }
            }
        }
    }
}