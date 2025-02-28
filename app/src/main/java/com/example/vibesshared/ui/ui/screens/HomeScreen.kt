package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.example.vibesshared.ui.ui.theme.DarkBackground
import com.example.vibesshared.ui.ui.theme.NeonBlue
import com.example.vibesshared.ui.ui.theme.NeonGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.NeonYellow
import com.example.vibesshared.ui.ui.utils.formatTimestamp
import com.example.vibesshared.ui.ui.viewmodel.MyProfileViewModel
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavHostController,
    greetingPreference: GreetingPreference,
    postViewModel: PostViewModel = hiltViewModel(),
    profileViewModel: MyProfileViewModel = hiltViewModel()
) {
    val postsWithUsers by postViewModel.postsFlow.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val userProfile by postViewModel.currentUserProfile.collectAsState()
    val newlyAwardedBadge by profileViewModel.newlyAwardedBadge.collectAsState(initial = null)

    val userId = remember { Firebase.auth.currentUser?.uid }

    LaunchedEffect(userId) {
        userId?.let { profileViewModel.checkAndAwardFirstLogin(it) }
    }

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

    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    // Get postId from navigation arguments
    val postId by remember {
        mutableStateOf(navController.currentBackStackEntry?.arguments?.getString("postId"))
    }

    // Use LazyListState to manage scrolling
    val listState = rememberLazyListState()

    // Scroll to the post with the matching postId when navigating
    LaunchedEffect(postId) {
        if (postId != null) {
            val index = postsWithUsers.indexOfFirst { it.post.postId == postId }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingParticlesBackground()

        LazyColumn(
            state = listState, // Use LazyListState for scrolling
            modifier = Modifier.fillMaxSize()
        ) {
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
                    onLikeClick = { postId -> postViewModel.likePost(postId) },
                    onCommentClick = { navController.navigate(Screen.Comments.createRoute(postWithUser.post.postId)) },
                    navController = navController,
                    onImageClick = { imageUrl -> fullScreenImageUrl = imageUrl },
                    onVotePoll = { postId, option -> postViewModel.voteOnPoll(postId, option) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (postsWithUsers.isEmpty() && !isLoading) {
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

        FullScreenImage(
            imageUrl = fullScreenImageUrl,
            onDismiss = { fullScreenImageUrl = null }
        )

        newlyAwardedBadge?.let { badge ->
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )

            AlertDialog(
                onDismissRequest = { profileViewModel.clearNewlyAwardedBadge() },
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonBlue, NeonPink)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, NeonGreen, RoundedCornerShape(16.dp))
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                title = {
                    Text(
                        text = "🎉 NEW VIBE UNLOCKED! 🎉",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonYellow,
                        modifier = Modifier
                            .graphicsLayer(alpha = alpha)
                            .padding(horizontal = 8.dp)
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        AsyncImage(
                            model = badge.imageUrl,
                            contentDescription = badge.name,
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    alpha = alpha
                                )
                                .border(2.dp, NeonGreen, CircleShape)
                                .shadow(8.dp, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "You’ve earned the ${badge.name} badge!",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.graphicsLayer(alpha = alpha)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Unlocked: ${formatTimestamp(badge.acquiredDate)}",
                                color = NeonBlue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.graphicsLayer(alpha = alpha)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { profileViewModel.clearNewlyAwardedBadge() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .graphicsLayer(alpha = alpha)
                    ) {
                        Text("Radical!", style = MaterialTheme.typography.labelLarge)
                    }
                },
                containerColor = Color.Transparent
            )
        }
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
            .clickable { navController.navigate(Screen.CreatePost.route) },
    ) {
        ParticleEffect(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(DarkBackground.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (userProfile != null) {
                    Box(modifier = Modifier.size(64.dp)) {
                        HolographicProfile(
                            imageUrl = userProfile.profilePictureUrl ?: "",
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

                if (userProfile != null) {
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
                text = if (userProfile != null) "Tap to ignite a new vibe..." else "Share your thoughts with the world!",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonPostCard(
    postWithUser: PostWithUser,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    navController: NavHostController,
    onImageClick: (String) -> Unit,
    onVotePoll: (String, String) -> Unit
) {
    val post = postWithUser.post
    val user = postWithUser.user
    var isLiked by remember { mutableStateOf(post.likes.contains(Firebase.auth.currentUser?.uid)) }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedPollOption by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(post.hasUserVoted) }

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
            .wrapContentHeight() // Maintain content-wrapping behavior
            .padding(horizontal = 8.dp) // Minimal external padding
            .graphicsLayer {
                translationY = parallaxOffset
                rotationZ = parallaxOffset * 0.2f
            }
            .hoverable(interactionSource)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = NeonGreen
            )
            .clickable(
                onClick = {
                    if (post.pollData != null) {
                        isExpanded = !isExpanded
                    }
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Internal padding inside the neon border
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(NeonPink, NeonBlue)),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Profile.createRoute(user.userId))
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp) // Internal padding to space from the border
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
                        .border(2.dp, NeonGreen, CircleShape)
                        .padding(4.dp), // Internal padding around profile pic
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp)) // Spacing between profile pic and text

                Column(
                    modifier = Modifier.padding(top = 4.dp) // Slight top padding for alignment
                ) {
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

            Spacer(modifier = Modifier.height(16.dp)) // Spacing between sections

            // Handle poll description as postText with "Poll:" prefix, centered
            if (post.pollData != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Ensure centering
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // Internal padding
                ) {
                    Text(
                        text = "Poll:",
                        color = NeonBlue,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = post.postText ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center, // Ensure centering
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp) // Internal padding
                    )
                }
            } else {
                Text(
                    text = post.postText ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp) // Internal padding for non-poll posts
                )
            }

            if (post.postImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Internal vertical padding
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(post.postImages.filterNotNull()) { imageUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post Image",
                            modifier = Modifier
                                .size(240.dp, 180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, NeonGreen, RoundedCornerShape(16.dp))
                                .clickable { onImageClick(imageUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (post.postVideo != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, NeonYellow, RoundedCornerShape(16.dp))
                        .clickable { /* Navigate to video player */ }
                        .padding(vertical = 8.dp, horizontal = 4.dp) // Internal padding
                ) {
                    AsyncImage(
                        model = post.postVideo,
                        contentDescription = "Video Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play Video",
                        tint = NeonYellow,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )
                }
            }

            if (post.pollData != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Ensure centering
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // Internal padding
                ) {
                    if (!isExpanded) {
                        val flashAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        // Enhanced flashing effect with all four colors
                        Text(
                            text = "Tap To Vote!",
                            color = when ((flashAlpha * 4 % 4).toInt()) { // Corrected color cycling logic
                                0 -> NeonGreen
                                1 -> NeonBlue
                                2 -> NeonPink
                                3 -> NeonYellow
                                else -> NeonGreen // Default back to NeonGreen (should not reach here)
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp), // Larger font size
                            textAlign = TextAlign.Center, // Ensure centering
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp) // Internal padding
                        )
                    } else {
                        val options = post.getPollOptions()
                        val votes = post.getPollVotes()

                        options.forEach { option ->
                            val voteCount = votes[option] ?: 0
                            val isSelected = selectedPollOption == option

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(
                                        enabled = !hasVoted,
                                        onClick = {
                                            if (!hasVoted) {
                                                onVotePoll(post.postId, option)
                                                selectedPollOption = option
                                                hasVoted = true
                                            }
                                        }
                                    )
                                    .background(
                                        if (isSelected) NeonGreen.copy(alpha = 0.3f)
                                        else DarkBackground,
                                        RoundedCornerShape(8.dp)
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSelected) NeonYellow else Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp), // Internal padding for poll options
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$voteCount votes",
                                    color = NeonGreen,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (hasVoted) {
                            Text(
                                text = "You voted: $selectedPollOption",
                                color = NeonBlue,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp), // Internal padding
                                textAlign = TextAlign.Center // Ensure centering
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp), // Internal vertical padding
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
fun FullScreenImage(
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    if (imageUrl != null) {
        var isAnimatingIn by remember { mutableStateOf(true) }
        val initialSize = 240.dp
        val fullScreenWidth = 0.9f
        val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
        val targetWidth = screenWidthDp * fullScreenWidth
        val targetHeight = targetWidth * 1.5f

        val scale by animateFloatAsState(
            targetValue = if (isAnimatingIn) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (isAnimatingIn) 800 else 400,
                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
            ),
            finishedListener = { isAnimatingIn = false }
        )
        val alpha by animateFloatAsState(
            targetValue = if (isAnimatingIn) 1f else 0f,
            animationSpec = tween(durationMillis = if (isAnimatingIn) 800 else 400)
        )

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
                    .background(Color.Black.copy(alpha = alpha))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Screen Image",
                    modifier = Modifier
                        .scale(scale)
                        .fillMaxWidth(0.9f) // Slightly less than full width for better fit
                        .clip(RoundedCornerShape(24.dp))
                        .border(4.dp, NeonGreen, RoundedCornerShape(24.dp)), // Add neon border for consistency
                    contentScale = ContentScale.Fit
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
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isHovered) 12.dp else 4.dp,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Removes the default ripple effect
                onClick = onClick
            )
            .padding(vertical = 4.dp) // Add slight padding for better touch target
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(32.dp)
                .shadow(shadowElevation, CircleShape, ambientColor = NeonGreen, spotColor = NeonGreen)
        )
        Text(
            text = count.toString(),
            color = NeonGreen,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}