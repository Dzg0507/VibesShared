package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vibesshared.ui.ui.components.FloatingParticlesBackground
import com.example.vibesshared.ui.ui.components.HolographicProfile
import com.example.vibesshared.ui.ui.components.ParticleEffect
import com.example.vibesshared.ui.ui.data.PostWithUser
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.enums.GreetingPreference
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.utils.formatTimestamp
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel
import kotlinx.coroutines.delay

// Neon Color Palette
val NeonGreen = Color(0xFF39FF14)
val NeonPink = Color(0xFFFF00FF)
val NeonBlue = Color(0xFF00FFFF)
val NeonYellow = Color(0xFFFFFF00)
val DarkBackground = Color(0xFF0A0A0A)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavHostController,
    greetingPreference: GreetingPreference,
    viewModel: PostViewModel = hiltViewModel()
) {
    val postsWithUsers by viewModel.postsFlow.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userProfile by viewModel.currentUserProfile.collectAsState()

    val greetings = listOf(
        "What's sparking, %s?",
        "What's new, %s?",
        "What's up, %s?",
        "What's happening, %s?",
        "What's on your mind, %s?",
        "%s! Great to see you again.",
        "Welcome back, %s! What's your vibe today?",
        "Hey %s, let's spark some joy!",
        "%s, ready to share your unique perspective?",
        "The world is waiting to hear from you, %s.",
        "What's your story today, %s?",
        "Let your creativity shine, %s!",
        "Inspire us with your vibes, %s.",
        "Share your thoughts and connect with others, %s.",
        "What's your spark of inspiration, %s?",
        "Hey there, %s! What's got you feeling creative?",
        "Welcome to the Vibe Zone, %s!",
        "Let's make some noise, %s!",
        "%s, you're a vibe! Share it with the world.",
        "What's your mood today, %s?",
        "Ready to spread some good vibes, %s?",
        "The stage is yours, %s! Share your spark."
    )

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingParticlesBackground()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                AnimatedCreatePostCard(
                    navController = navController,
                    userProfile = userProfile,
                    greetings = greetings,
                    greetingPreference = greetingPreference
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(postsWithUsers) { postWithUser ->
                NeonPostCard(
                    postWithUser = postWithUser,
                    onLikeClick = { postId -> viewModel.likePost(postId) },
                    onCommentClick = { navController.navigate(Screen.Comments.createRoute(postWithUser.post.postId)) },
                    navController = navController
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (postsWithUsers.isEmpty() &&!isLoading) {
                item {
                    Text(
                        text = "BE THE FIRST TO SPARK THE FEED!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        color = NeonPink,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        PulsatingNeonLoader(isLoading = isLoading)
    }
}

@Composable
fun PulsatingNeonLoader(isLoading: Boolean) {
    if (isLoading) {
        val infiniteTransition = rememberInfiniteTransition()
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .scale(pulse),
                color = NeonGreen,
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun AnimatedCreatePostCard(
    navController: NavHostController,
    userProfile: UserProfile? = null,
    greetings: List<String>,
    greetingPreference: GreetingPreference
) {
    val infiniteTransition = rememberInfiniteTransition()
    val borderWidth by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val elevation by animateDpAsState(
        targetValue = 24.dp,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    var randomGreeting by remember(userProfile, greetingPreference) {
        mutableStateOf(
            greetings.random().format(
                when (greetingPreference) {
                    GreetingPreference.FIRST_NAME -> userProfile?.firstName
                    GreetingPreference.LAST_NAME -> userProfile?.lastName
                    GreetingPreference.USER_NAME -> userProfile?.userName
                    GreetingPreference.FULL_NAME -> "${userProfile?.firstName} ${userProfile?.lastName}"
                }
            )
        )
    }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(15000)
            randomGreeting = greetings.random().format(
                when (greetingPreference) {
                    GreetingPreference.FIRST_NAME -> userProfile?.firstName
                    GreetingPreference.LAST_NAME -> userProfile?.lastName
                    GreetingPreference.USER_NAME -> userProfile?.userName
                    GreetingPreference.FULL_NAME -> "${userProfile?.firstName} ${userProfile?.lastName}"
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(32.dp),
                spotColor = NeonPink
            )
            .border(
                width = borderWidth.dp,
                brush = Brush.linearGradient(listOf(NeonGreen, NeonBlue, NeonPink)),
                shape = RoundedCornerShape(32.dp)
            )

            .clickable { navController.navigate(Screen.CreatePost.route)},

    ) {
        if (true) {
            key(randomGreeting) {
            }
        }


        ParticleEffect(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(DarkBackground.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (userProfile!= null) {
                    Box(modifier = Modifier.size(64.dp)) {
                        HolographicProfile(
                            imageUrl = userProfile.profilePictureUrl?: "",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(NeonGreen, DarkBackground),
                                    radius = 80f
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, NeonGreen, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Post",
                            tint = NeonGreen,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (userProfile!= null) {
                    Text(
                        text = randomGreeting,
                        color = NeonYellow,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "IGNITE A NEW SPARK...",
                        color = NeonBlue,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (userProfile!= null) "Tap to ignite a new vibe..." else "Share your thoughts with the world!",
                color = NeonBlue,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonGreen, NeonBlue, NeonPink),
                            startX = 0f,
                            endX = 1000f
                        ),
                        alpha = 0.5f
                    )
            )
        }
    }
}

//... (NeonPostCard, NeonIconButton)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonPostCard(
    postWithUser: PostWithUser,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    navController: NavHostController
) {
    val post = postWithUser.post
    val user = postWithUser.user
    var isLiked by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 16.dp else 8.dp,
        animationSpec = tween(300)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val parallaxOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                translationY = parallaxOffset
                rotationZ = parallaxOffset * 0.2f
            }
            .hoverable(interactionSource)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = NeonGreen
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(NeonPink, NeonBlue)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Profile.createRoute(user.userId))
                }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profilePictureUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, NeonGreen, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        color = NeonYellow,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimestamp(post.timestamp),
                        color = NeonBlue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = post.postText ?: "",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (post.postImage.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.postImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, NeonGreen, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NeonIconButton(
                    icon = Icons.Filled.Favorite,
                    count = post.likes.size,
                    color = if (isLiked) NeonPink else Color.White,
                    onClick = {
                        onLikeClick(post.postId)
                        isLiked = !isLiked
                    }
                )

                NeonIconButton(
                    icon = Icons.Filled.ChatBubble,
                    count = post.commentCount,
                    color = NeonBlue,
                    onClick = { onCommentClick(post.postId) }
                )
            }
        }
    }
}

@Composable
fun NeonIconButton(
    icon: ImageVector,
    count: Int,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.8f else 1f)
    val haptic = LocalHapticFeedback.current // Access haptic feedback

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress) // Trigger haptic feedback
                onClick()
                isPressed = false
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

