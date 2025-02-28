package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.FloatingParticlesBackground
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.theme.DarkBackground
import com.example.vibesshared.ui.ui.theme.NeonBlue
import com.example.vibesshared.ui.ui.theme.NeonGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.NeonYellow
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel.PostCreationStatus
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: PostViewModel = hiltViewModel()
) {
    var postText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val postCreationStatus by viewModel.postCreationStatus.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Vibe Type Selection
    var selectedVibeType by remember { mutableStateOf(VibeType.Text) }
    var pollOptions by remember { mutableStateOf(listOf("", "")) } // Minimum 2 options for Poll Vibe
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers for Image and Video
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { viewModel.addImage(it) } }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedVideoUri = it }
    }

    // Animation for Neon Effects
    val infiniteTransition = rememberInfiniteTransition()
    val borderWidth by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Handle Post Creation
    fun createPost() {
        isPosting = true
        scope.launch {
            when (selectedVibeType) {
                VibeType.Text -> viewModel.addPost(postText, emptyList(), context)
                VibeType.Image -> viewModel.addPost(postText, selectedImages, context)
                VibeType.Video -> viewModel.addPost(postText, emptyList(), context, selectedVideoUri)
                VibeType.Poll -> viewModel.addPollPost(postText, pollOptions.filter { it.isNotBlank() })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ignite Your Vibe", color = NeonYellow) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }, enabled = !isPosting) {
                        Icon(Icons.Filled.ArrowBackIosNew, "Back", tint = NeonGreen)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { createPost() },
                        enabled = !isPosting && (
                                (selectedVibeType == VibeType.Text && postText.isNotBlank()) ||
                                        (selectedVibeType == VibeType.Image && selectedImages.isNotEmpty()) ||
                                        (selectedVibeType == VibeType.Video && selectedVideoUri != null) ||
                                        (selectedVibeType == VibeType.Poll && pollOptions.any { it.isNotBlank() })
                                )
                    ) {
                        Text(if (isPosting) "Sparking..." else "Spark It!", color = NeonPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            FloatingParticlesBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Vibe Type Selector
                VibeTypeSelector(
                    selectedVibeType = selectedVibeType,
                    onVibeTypeSelected = { selectedVibeType = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Content Based on Vibe Type
                when (selectedVibeType) {
                    VibeType.Text -> TextVibeInput(postText, { if (!isPosting) postText = it }, isPosting)
                    VibeType.Image -> ImageVibeInput(
                        selectedImages,
                        onAddImage = { if (!isPosting) imagePicker.launch("image/*") },
                        onRemoveImage = { if (!isPosting) scope.launch { viewModel.removeImage(it) } },
                        isPosting
                    )
                    VibeType.Video -> VideoVibeInput(
                        selectedVideoUri,
                        onSelectVideo = { if (!isPosting) videoPicker.launch("video/*") },
                        onRemoveVideo = { if (!isPosting) selectedVideoUri = null },
                        isPosting
                    )
                    VibeType.Poll -> PollVibeInput(
                        pollDescription = postText,
                        onDescriptionChanged = { if (!isPosting) postText = it },
                        pollOptions = pollOptions,
                        onOptionsChanged = { if (!isPosting) pollOptions = it },
                        isPosting = isPosting
                    )
                }

                if (isPosting) {
                    NeonProgressBar(progress = uploadProgress)
                }
            }

            // Neon Error Dialog
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title = { Text("Vibe Fizzled", color = NeonYellow) },
                    text = { Text(errorMessage, color = Color.White) },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = false }) {
                            Text("Try Again", color = NeonGreen)
                        }
                    },
                    modifier = Modifier
                        .background(brush = Brush.verticalGradient(listOf(NeonBlue, NeonPink)), RoundedCornerShape(16.dp))
                        .border(2.dp, NeonGreen, RoundedCornerShape(16.dp))
                )
            }

            LaunchedEffect(postCreationStatus) {
                when (postCreationStatus) {
                    is PostCreationStatus.Success -> {
                        isPosting = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                    is PostCreationStatus.Error -> {
                        errorMessage = (postCreationStatus as PostCreationStatus.Error).message
                        showErrorDialog = true
                        isPosting = false
                    }
                    else -> {}
                }
            }
        }
    }
}

// Vibe Types Enum
enum class VibeType(val label: String) {
    Text("Text Vibe"),
    Image("Image Vibe"),
    Video("Video Vibe"),
    Poll("Poll Vibe")
}

@Composable
fun VibeTypeSelector(selectedVibeType: VibeType, onVibeTypeSelected: (VibeType) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(VibeType.values()) { vibeType ->
            val isSelected = vibeType == selectedVibeType
            Button(
                onClick = { onVibeTypeSelected(vibeType) },
                modifier = Modifier
                    .shadow(if (isSelected) 8.dp else 4.dp, RoundedCornerShape(12.dp))
                    .background(
                        brush = if (isSelected) Brush.linearGradient(listOf(NeonGreen, NeonBlue))
                        else Brush.linearGradient(listOf(DarkBackground, DarkBackground)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .scale(if (isSelected) glow else 1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text(vibeType.label, color = if (isSelected) NeonYellow else NeonBlue)
            }
        }
    }
}

@Composable
fun TextVibeInput(text: String, onTextChange: (String) -> Unit, isPosting: Boolean) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        enabled = !isPosting,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(DarkBackground, RoundedCornerShape(16.dp))
            .border(2.dp, NeonPink, RoundedCornerShape(16.dp)),
        placeholder = { Text("What's your vibe?", color = NeonBlue) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.Gray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = NeonGreen
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
}

@Composable
fun ImageVibeInput(
    selectedImages: List<Uri>,
    onAddImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    isPosting: Boolean
) {
    Column {
        TextField(
            value = "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(DarkBackground, RoundedCornerShape(16.dp))
                .border(2.dp, NeonGreen, RoundedCornerShape(16.dp)),
            placeholder = { Text("Add a caption to your images...", color = NeonBlue) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledTextColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedImages) { uri ->
                Box(modifier = Modifier.size(100.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, NeonPink, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { onRemoveImage(uri) },
                        enabled = !isPosting,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(NeonBlue.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Remove", tint = Color.White)
                    }
                }
            }
            item {
                IconButton(
                    onClick = onAddImage,
                    enabled = !isPosting,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush = Brush.linearGradient(listOf(NeonGreen, NeonBlue)))
                ) {
                    Icon(Icons.Default.Add, "Add Image", tint = NeonYellow)
                }
            }
        }
    }
}

@Composable
fun VideoVibeInput(
    selectedVideoUri: Uri?,
    onSelectVideo: () -> Unit,
    onRemoveVideo: () -> Unit,
    isPosting: Boolean
) {
    Column {
        TextField(
            value = "",
            onValueChange = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(DarkBackground, RoundedCornerShape(16.dp))
                .border(2.dp, NeonYellow, RoundedCornerShape(16.dp)),
            placeholder = { Text("Add a caption to your video...", color = NeonBlue) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledTextColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)) {
            if (selectedVideoUri != null) {
                AsyncImage(
                    model = selectedVideoUri,
                    contentDescription = "Selected Video",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, NeonYellow, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemoveVideo,
                    enabled = !isPosting,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(NeonBlue.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, "Remove", tint = Color.White)
                }
            } else {
                IconButton(
                    onClick = onSelectVideo,
                    enabled = !isPosting,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush = Brush.linearGradient(listOf(NeonGreen, NeonBlue)))
                ) {
                    Icon(Icons.Default.VideoLibrary, "Add Video", tint = NeonYellow)
                }
            }
        }
    }
}

@Composable
fun PollVibeInput(
    pollDescription: String,
    onDescriptionChanged: (String) -> Unit,
    pollOptions: List<String>,
    onOptionsChanged: (List<String>) -> Unit,
    isPosting: Boolean
) {
    Column {
        TextField(
            value = pollDescription,
            onValueChange = onDescriptionChanged,
            enabled = !isPosting,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(DarkBackground, RoundedCornerShape(16.dp))
                .border(2.dp, NeonBlue, RoundedCornerShape(16.dp)),
            placeholder = { Text("What’s your poll vibe?", color = NeonBlue) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pollOptions.forEachIndexed { index, option ->
                TextField(
                    value = option,
                    onValueChange = { newValue ->
                        val newOptions = pollOptions.toMutableList().apply { this[index] = newValue }
                        onOptionsChanged(newOptions)
                    },
                    enabled = !isPosting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, NeonPink, RoundedCornerShape(8.dp)),
                    placeholder = { Text("Option ${index + 1}", color = NeonBlue) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }
            Button(
                onClick = { if (!isPosting) onOptionsChanged(pollOptions + "") },
                enabled = !isPosting && pollOptions.size < 4,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Add Option", color = Color.Black)
            }
        }
    }
}

@Composable
fun NeonProgressBar(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition()
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )

    LinearProgressIndicator(
        progress = { progress / 100f },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .shadow(4.dp, RoundedCornerShape(4.dp))
            .scale(glow),
        color = NeonGreen,
        trackColor = NeonBlue.copy(alpha = 0.3f)
    )
}