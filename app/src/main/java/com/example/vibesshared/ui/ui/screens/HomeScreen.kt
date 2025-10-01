package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.theme.*
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Story(
    val userImage: String,
    val userName: String
)

data class Post(
    val id: Int,
    val user: UserProfile,
    val postText: String,
    val postImage: String? = null,
    val timestamp: String,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var showContent by remember { mutableStateOf(false) }
    val vibrantPostColors = remember { 
        listOf(
            VibrantPurple.copy(alpha = 0.1f),
            ElectricBlue.copy(alpha = 0.1f),
            HotPink.copy(alpha = 0.1f),
            NeonGreen.copy(alpha = 0.1f),
            ElectricYellow.copy(alpha = 0.1f),
            Cyan.copy(alpha = 0.1f),
            Rose.copy(alpha = 0.1f)
        )
    }

    LaunchedEffect(key1 = Unit) {
        delay(1500)
        isLoading = false
        delay(300)
        showContent = true
    }

    Scaffold(
        topBar = {
            VibesTopAppBar(color = MaterialTheme.colorScheme.primary)
        }
    ) { padding ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(800)) + slideInHorizontally(
                initialOffsetX = { it / 3 },
                animationSpec = spring(dampingRatio = 0.8f)
            )
        ) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentPadding = PaddingValues(top = 0.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = userProfile != null,
                        enter = fadeIn(animationSpec = tween(600)) + scaleIn(animationSpec = spring(0.8f))
                    ) {
                        userProfile?.let { profile ->
                            UserProfileSection(profile)
                        }
                    }
                }

                item { 
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) + 
                               scaleIn(animationSpec = spring(0.7f, delayMillis = 200))
                    ) {
                        CreatePostSection(userProfile) 
                    }
                }
                
                item { 
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) + 
                               slideInHorizontally(animationSpec = spring(0.8f, delayMillis = 400))
                    ) {
                        EpicFeaturesSection(navController) 
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = !isLoading,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 600)) + 
                               scaleIn(animationSpec = spring(0.8f, delayMillis = 600))
                    ) {
                        StoryList(generateStories(5))
                    }
                }

                items(generatePosts(10)) { post ->
                    val postColor = vibrantPostColors[post.id % vibrantPostColors.size]
                    AnimatedVisibility(
                        visible = !isLoading,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 800 + (post.id * 100))) + 
                               scaleIn(animationSpec = spring(0.7f, delayMillis = 800 + (post.id * 100)))
                    ) {
                        PostCard(post, postColor)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            LoadingIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibesTopAppBar(color: Color) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(VibrantPurple, ElectricBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Vibes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Search, 
                    contentDescription = "Search", 
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = color
        )
    )
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = VibrantPurple,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your vibes...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StoryList(stories: List<Story>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(stories) { story ->
            StoryItem(story)
        }
    }
}

@Composable
fun StoryItem(story: Story) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f)
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true },
                    onRelease = { isPressed = false }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VibrantPurple, ElectricBlue, HotPink)
                    )
                )
                .padding(3.dp)
        ) {
            AsyncImage(
                model = story.userImage,
                contentDescription = "Story Image",
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = story.userName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun generateStories(count: Int): List<Story> {
    val stories = mutableListOf<Story>()
    for (i in 1..count) {
        stories.add(Story("https://picsum.photos/200/300", "User$i"))
    }
    return stories
}

@Composable
fun CreatePostSection(userProfile: UserProfile?) {
    var postText by remember { mutableStateOf("") }
    val animatedElevation = animateFloatAsState(
        targetValue = if (postText.isNotBlank()) 8.dp.value else 4.dp.value,
        animationSpec = spring(dampingRatio = 0.8f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = animatedElevation.value.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = VibrantPurple.copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(VibrantPurple, ElectricBlue)
                        )
                    )
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = userProfile?.profilePictureUri ?: "https://picsum.photos/200/300",
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { 
                    Text(
                        "What's on your mind?",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibrantPurple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VibrantPurple.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = VibrantPurple
                )
            }
        }
    }
}


@Composable
fun UserProfileSection(profile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ElectricBlue.copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(VibrantPurple, ElectricBlue, HotPink)
                        )
                    )
                    .padding(3.dp)
            ) {
                AsyncImage(
                    model = profile.profilePictureUri ?: "https://picsum.photos/200/300",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${profile.firstName} ${profile.lastName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                profile.bio?.let { bio ->
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post, backgroundColor: Color) {
    val likes = remember(post.id) { Random.nextInt(100) }
    val comments = remember(post.id) { Random.nextInt(50) }
    val shares = remember(post.id) { Random.nextInt(20) }
    var isLiked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = backgroundColor.copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(VibrantPurple, ElectricBlue)
                            )
                        )
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = post.user.profilePictureUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.user.firstName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.postText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            post.postImage?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = "https://picsum.photos/400/300",
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PostActionButton(
                    icon = Icons.Filled.Favorite,
                    text = "$likes",
                    isActive = isLiked,
                    onClick = { isLiked = !isLiked }
                )
                PostActionButton(
                    icon = Icons.AutoMirrored.Filled.Message,
                    text = "$comments",
                    onClick = { /*TODO*/ }
                )
                PostActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    text = "$shares",
                    onClick = { /*TODO*/ }
                )
            }
        }
    }
}


@Composable
fun PostActionButton(
    icon: ImageVector, 
    text: String, 
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.6f)
    )
    
    val iconColor = if (isActive) Rose else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val textColor = if (isActive) Rose else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true },
                    onRelease = { isPressed = false }
                )
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EpicFeaturesSection(navController: NavController) {
    val epicFeatures = listOf(
        EpicFeature("AI Assistant", "🤖", "Smart AI features", "ai_assistant"),
        EpicFeature("AR Camera", "📸", "Augmented reality", "ar_camera"),
        EpicFeature("Live Streaming", "📺", "Go live & stream", "live_streaming"),
        EpicFeature("Gaming", "🎮", "Play mini-games", "gaming"),
        EpicFeature("Music", "🎵", "Stream & share music", "music"),
        EpicFeature("Location", "📍", "Check-ins & events", "location"),
        EpicFeature("Marketplace", "🛒", "Buy & sell items", "marketplace"),
        EpicFeature("Video", "📹", "Video calls & stories", "video"),
        EpicFeature("Events", "🎉", "Create & join events", "event_management"),
        EpicFeature("Advanced Chat", "💬", "Voice, reactions & more", "advanced_messaging")
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = GradientPurple.copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "🚀",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Epic Features",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(epicFeatures) { feature ->
                    EpicFeatureCard(
                        feature = feature,
                        onClick = { navController.navigate(feature.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun EpicFeatureCard(
    feature: EpicFeature,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f)
    )
    
    val gradientColors = listOf(
        listOf(VibrantPurple, ElectricBlue),
        listOf(HotPink, Rose),
        listOf(NeonGreen, Cyan),
        listOf(ElectricYellow, SunsetOrange),
        listOf(Indigo, Violet)
    )
    
    val featureGradient = gradientColors[feature.title.hashCode() % gradientColors.size]
    
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { isPressed = true },
                    onRelease = { 
                        isPressed = false
                        onClick()
                    }
                )
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = featureGradient.first().copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            featureGradient.first().copy(alpha = 0.1f),
                            featureGradient.second().copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = feature.icon,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class EpicFeature(
    val title: String,
    val icon: String,
    val description: String,
    val route: String
)

fun generatePosts(count: Int): List<Post> {
    val sampleUsers = List(5) { index ->
        UserProfile(
            userId = "userId$index",
            firstName = "FirstName$index",
            lastName = "LastName$index",
            email = "user$index@example.com",
            bio = "Bio for user $index",
            profilePictureUri = "https://picsum.photos/id/${index + 10}/200/300"
        )
    }

    val posts = mutableListOf<Post>()
    for (i in 1..count) {
        val user = sampleUsers[Random.nextInt(sampleUsers.size)]
        posts.add(
            Post(
                id = i,
                user = user,
                postText = "Post $i content. This is a longer post to test the layout and wrapping of the text content.",
                postImage = if (i % 2 == 0) "https://picsum.photos/400/300" else null,
                timestamp = "${i * 10} min ago"
            )
        )
    }
    return posts
}
