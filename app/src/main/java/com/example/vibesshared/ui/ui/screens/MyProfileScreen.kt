package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.navOptions
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vibesshared.ui.ui.components.HolographicProfile
import com.example.vibesshared.ui.ui.data.Badge
import com.example.vibesshared.ui.ui.data.PostWithUser
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.theme.*
import com.example.vibesshared.ui.ui.theme.AppColors.ElectricPurple
import com.example.vibesshared.ui.ui.theme.AppColors.GoldenYellow
import com.example.vibesshared.ui.ui.theme.AppColors.LimeGreen
import com.example.vibesshared.ui.ui.theme.AppColors.SunsetOrange
import com.example.vibesshared.ui.ui.theme.AppColors.VividBlue
import com.example.vibesshared.ui.ui.viewmodel.MyProfileViewModel
import com.example.vibesshared.ui.ui.viewmodel.PostViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    navController: NavController,
    viewModel: MyProfileViewModel = hiltViewModel(),
    postViewModel: PostViewModel = hiltViewModel() // Access PostViewModel here to fetch posts
) {
    val userId = remember { Firebase.auth.currentUser?.uid }
    var userName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrlFromFirebase by remember { mutableStateOf("") }
    var badges by remember { mutableStateOf(emptyList<Badge>()) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    var isEditMode by remember { mutableStateOf(false) }
    val loadingStatus by viewModel.loadingStatus.collectAsState(initial = null)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val userProfile by viewModel.userProfile.collectAsState(initial = null)
    val newlyAwardedBadge by viewModel.newlyAwardedBadge.collectAsState(initial = null)
    val posts by postViewModel.postsFlow.collectAsState() // Use PostViewModel to get all posts

    // Find the most recent post for the user
    val recentPost = remember(posts, userId) {
        posts.filter { it.post.userId == userId }.maxByOrNull { it.post.timestamp ?: Timestamp.now() }
    }

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadUserProfile(it) }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let { user ->
            userName = user.userName ?: ""
            firstName = user.firstName ?: ""
            lastName = user.lastName ?: ""
            profilePictureUrlFromFirebase = user.profilePictureUrl ?: ""
            badges = user.badges.mapNotNull { badgeId ->
                viewModel.badgeRepository.getBadge(badgeId)?.let { badge ->
                    val acquiredDate = user.badgeDates?.get(badgeId) ?: badge.acquiredDate
                    badge.copy(acquiredDate = acquiredDate).also {
                        Log.d("MyProfileScreen", "Badge ID: $badgeId, Badge: $it, Acquired Date: ${it.acquiredDate}")
                    }
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profilePictureUri = uri
    }

    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AnimatedText(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    animateIn = animateIn,
                    delayMillis = 900,
                    fromTop = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedProfilePicture(
                    imageUrl = profilePictureUri ?: profilePictureUrlFromFirebase,
                    isEditMode = isEditMode,
                    animateIn = animateIn,
                    delayMillis = 2222,
                    onEditClick = { imagePickerLauncher.launch("image/*") },
                    fromTop = false
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Username field with neon border when not in edit mode
                AnimatedOutlinedTextField(
                    value = userName,
                    onValueChange = { if (isEditMode) userName = it },
                    label = "Username",
                    isEditMode = isEditMode,
                    animateIn = animateIn,
                    delayMillis = 1620,
                    fromTop = true,
                    neonBorderWhenNotEditing = true // Add neon border when not editable
                )

                Spacer(modifier = Modifier.height(16.dp))

                // First Name field with neon border when not in edit mode
                AnimatedOutlinedTextField(
                    value = firstName,
                    onValueChange = { if (isEditMode) firstName = it },
                    label = "First Name",
                    isEditMode = isEditMode,
                    animateIn = animateIn,
                    delayMillis = 740,
                    fromTop = false,
                    neonBorderWhenNotEditing = true // Add neon border when not editable
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Last Name field with neon border when not in edit mode
                AnimatedOutlinedTextField(
                    value = lastName,
                    onValueChange = { if (isEditMode) lastName = it },
                    label = "Last Name",
                    isEditMode = isEditMode,
                    animateIn = animateIn,
                    delayMillis = 1300,
                    fromTop = true,
                    neonBorderWhenNotEditing = true // Add neon border when not editable
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Replace "Last Post: Coming Soon!" with a miniature NeonPostCard for the recent post
                if (recentPost != null) {
                    val post = recentPost // Store the non-null value in a local variable
                    AnimatedNeonPostCardMini(
                        postWithUser = post, // Use the local variable
                        onClick = {
                            navController.navigate("${Screen.Home.route}?postId=${post.post.postId}") {
                                // Use modern navigation API to preserve back stack and bottom nav state
                                val navOptions = navOptions {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true // Preserve state of HomeScreen
                                        inclusive = false // Do not remove HomeScreen from back stack
                                    }
                                    launchSingleTop = true // Avoid adding duplicate destinations
                                }
                                navController.navigate("${Screen.Home.route}?postId=${post.post.postId}", navOptions)
                            }
                        },
                        animateIn = animateIn,
                        delayMillis = 1463,
                        fromTop = false
                    )
                } else {
                    AnimatedText(
                        text = "No Posts Yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LimeGreen,
                        animateIn = animateIn,
                        delayMillis = 1463,
                        fromTop = false
                    )
                }

                // Enhanced "Total Vibes Shared" with larger text, neon number, and increased size
                AnimatedText(
                    text = "Total Vibes Shared: ",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp), // Larger text
                    color = ElectricPurple,
                    animateIn = animateIn,
                    delayMillis = 850,
                    fromTop = true
                )
                Text(
                    text = "${userProfile?.postCount ?: 0}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp), // Larger number
                    color = NeonYellow, // Different neon color for better visibility
                    modifier = Modifier
                        .padding(start = 4.dp) // Slight padding for spacing
                        .graphicsLayer(alpha = if (animateIn) 1f else 0f) // Match animation
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Badges:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (badges.isEmpty()) {
                        item {
                            Text(
                                text = "No badges yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    } else {
                        val earnedBadges = badges.filter { badge ->
                            when (badge.badgeId) {
                                "first_login" -> true
                                "first_post" -> (userProfile?.postCount ?: 0) >= 1
                                "five_posts" -> (userProfile?.postCount ?: 0) >= 5
                                else -> false
                            }
                        }
                        items(earnedBadges.size) { index ->
                            val badge = earnedBadges[index]
                            AsyncImage(
                                model = badge.imageUrl,
                                contentDescription = badge.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable { selectedBadge = badge },
                                onError = { Log.e("MyProfileScreen", "Image load failed for badge ${badge.name}: ${it.result.throwable.message}") }
                            )
                        }
                        val unearnedBadgesCount = badges.size - earnedBadges.size
                        if (unearnedBadgesCount > 0) {
                            items(unearnedBadgesCount) {
                                Box(modifier = Modifier.size(60.dp))
                            }
                        }
                    }
                }
                Text(
                    text = "(Click Badge Image to View Details)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp, start = 8.dp)
                )

                selectedBadge?.let { badge ->
                    AlertDialog(
                        onDismissRequest = { selectedBadge = null },
                        title = { Text(text = "${badge.name} Badge!", style = MaterialTheme.typography.headlineSmall, color = Color.White) },
                        text = {
                            Column {
                                Text(text = "Entered Vibes on: ${formatTimestamp(badge.acquiredDate)}", color = Color.White)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { selectedBadge = null }) {
                                Text(text = "Close", color = Color.White)
                            }
                        },
                        containerColor = VividBlue.copy(alpha = 0.7f),
                        titleContentColor = GoldenYellow,
                        textContentColor = Color.White
                    )
                }

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
                        onDismissRequest = { viewModel.clearNewlyAwardedBadge() },
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        VividBlue.copy(alpha = 0.9f),
                                        SunsetOrange.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .shadow(16.dp, RoundedCornerShape(16.dp)),
                        title = {
                            Text(
                                text = "🎉 New Badge Earned! 🎉",
                                style = MaterialTheme.typography.headlineMedium,
                                color = GoldenYellow,
                                modifier = Modifier
                                    .graphicsLayer(alpha = alpha)
                                    .padding(bottom = 8.dp)
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
                                        .shadow(8.dp, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Woohoo! You've earned the ${badge.name} badge!",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.graphicsLayer(alpha = alpha)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Awarded on: ${formatTimestamp(badge.acquiredDate)}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.graphicsLayer(alpha = alpha)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.clearNewlyAwardedBadge() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LimeGreen,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .shadow(4.dp, RoundedCornerShape(8.dp))
                                    .graphicsLayer(alpha = alpha)
                                    .animateContentSize()
                            ) {
                                Text("Sweet!", style = MaterialTheme.typography.labelLarge)
                            }
                        },
                        containerColor = Color.Transparent // Use custom background instead
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedEditModeRow(
                    isEditMode = isEditMode,
                    onCheckedChange = { isEditMode = it },
                    animateIn = animateIn,
                    delayMillis = 800,
                    fromTop = false
                )

                AnimatedSaveButton(
                    isEditMode = isEditMode,
                    viewModel = viewModel,
                    userId = userId,
                    onSaveComplete = { isEditMode = false },
                    userName = userName,
                    firstName = firstName,
                    lastName = lastName,
                    profilePictureUri = profilePictureUri
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { navController.navigate(Screen.TriviaGame.route) }) {
                    Text("Play Trivia")
                }

                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = message, color = Color.Red)
                }
            }
        }

        if (loadingStatus == MyProfileViewModel.LoadingStatus.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground.copy(alpha = 0.7f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SunsetOrange)
            }
        }
    }
}

// New composable for a miniature NeonPostCard with navigation to HomeScreen and scrolling
@Composable
private fun AnimatedNeonPostCardMini(
    postWithUser: PostWithUser,
    onClick: () -> Unit,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-400).dp else 100.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Slightly smaller width for miniature version
                .wrapContentHeight()
                .padding(vertical = 8.dp)
                .graphicsLayer {
                    translationY = with(density) { animatedOffsetY.toPx() }
                }
                .clickable(onClick = onClick)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = NeonGreen
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp) // Reduced padding for miniature size
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(NeonPink, NeonBlue)),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(postWithUser.user.profilePictureUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(32.dp) // Smaller size for miniature
                            .clip(CircleShape)
                            .border(1.dp, NeonGreen, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "${postWithUser.user.firstName} ${postWithUser.user.lastName}",
                            color = NeonYellow,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatTimestamp(postWithUser.post.timestamp),
                            color = NeonBlue,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = postWithUser.post.postText ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Optionally add a small image or video preview if available
                if (postWithUser.post.postImages.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(postWithUser.post.postImages.first())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp) // Smaller height for miniature
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, NeonGreen, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Optionally handle poll data or video previews similarly, scaled down
                if (postWithUser.post.pollData != null) {
                    Text(
                        text = "Poll: ${postWithUser.post.getPollDescription()}",
                        color = NeonBlue,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

// Updated AnimatedOutlinedTextField to include neon border when not in edit mode
@Composable
private fun AnimatedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isEditMode: Boolean,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean,
    neonBorderWhenNotEditing: Boolean // New parameter for neon border
) {
    val density = LocalDensity.current
    val offsetX = if (fromTop) (-700).dp else 100.dp
    val animatedOffsetX by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetX,
        animationSpec = tween(durationMillis = 750, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = if (isEditMode) Color.Gray else NeonBlue) }, // Neon label color when not editing
            enabled = isEditMode,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = with(density) { animatedOffsetX.toPx() }
                }
                .then(
                    if (!isEditMode && neonBorderWhenNotEditing) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(listOf(NeonGreen, NeonPink)),
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LimeGreen,
                unfocusedTextColor = if (isEditMode) VividBlue else NeonYellow, // Neon text color when not editing
                focusedBorderColor = SunsetOrange,
                unfocusedBorderColor = if (isEditMode) NeonPink else Color.Transparent, // Hide default border when not editing
                cursorColor = LimeGreen
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// Rest of the file (AnimatedSaveButton, AnimatedText, etc.) remains unchanged
@Composable
private fun AnimatedSaveButton(
    isEditMode: Boolean,
    viewModel: MyProfileViewModel,
    userId: String?,
    onSaveComplete: () -> Unit,
    userName: String,
    firstName: String,
    lastName: String,
    profilePictureUri: Uri?
) {
    val offsetY by animateDpAsState(
        targetValue = if (isEditMode) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 300)
    )

    if (isEditMode) {
        Button(
            onClick = {
                userId?.let {
                    viewModel.saveUserProfile(
                        userId = it,
                        userName = userName,
                        firstName = firstName,
                        lastName = lastName,
                        profilePictureUri = profilePictureUri
                    )
                    onSaveComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .shadow(8.dp, RoundedCornerShape(8.dp))
        ) {
            Text(text = "Save Profile")
        }
    }
}

@Composable
private fun AnimatedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-100).dp else 1800.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            modifier = Modifier.graphicsLayer {
                translationY = with(density) { animatedOffsetY.toPx() }
            }
        )
    }
}

@Composable
private fun AnimatedProfilePicture(
    imageUrl: Any?,
    isEditMode: Boolean,
    animateIn: Boolean,
    delayMillis: Int,
    onEditClick: () -> Unit,
    fromTop: Boolean
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-400).dp else 100.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 1200, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    translationY = with(density) { animatedOffsetY.toPx() }
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            HolographicProfile(
                imageUrl = imageUrl.toString(),
                modifier = Modifier.fillMaxSize()
            )

            if (isEditMode) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .background(DarkBackground.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedEditModeRow(
    isEditMode: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-400).dp else 100.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = with(density) { animatedOffsetY.toPx() }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Edit Mode", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isEditMode,
                onCheckedChange = onCheckedChange
            )
        }
    }
}