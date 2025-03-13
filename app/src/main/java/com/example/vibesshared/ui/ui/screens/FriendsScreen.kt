package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.data.FriendRequest
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.theme.AppColors.ElectricPurple
import com.example.vibesshared.ui.ui.theme.AppColors.LimeGreen
import com.example.vibesshared.ui.ui.theme.AppColors.VividBlue
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.viewmodel.FriendsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun FriendsScreen(
    navController: NavHostController,
    viewModel: FriendsViewModel = hiltViewModel(),
    currentUserId: String
) {
    var expandedFriend by remember { mutableStateOf<UserProfile?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val snackbarHostState = remember { SnackbarHostState() }
    val friendRequestStatuses by viewModel.friendRequestStatuses.collectAsState()
    val searchResults by viewModel.users.collectAsState()
    val friendsList by viewModel.friends.collectAsState()
    val receivedRequests by viewModel.receivedFriendRequests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToMessaging.collect { chatId ->
            navController.navigate(Screen.Messaging.createRoute(chatId))
        }
    }
    LaunchedEffect(currentUserId) {
        viewModel.getFriends()
        viewModel.loadReceivedFriendRequests(currentUserId)
    }
    LaunchedEffect(searchQuery, selectedTabIndex) {
        if (selectedTabIndex == 2) viewModel.searchUsers(searchQuery)
    }

    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue)
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val currentOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = with(LocalDensity.current) { screenWidthDp.toPx() } * gradientColors.size,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = currentOffset - with(LocalDensity.current) { screenWidthDp.toPx() },
                    endX = currentOffset
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Friends", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBackIosNew, "Back", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    listOf("Friends", "Requests", "Find Users").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> FriendsListContent(friendsList, expandedFriend, { friend ->
                        expandedFriend = if (expandedFriend == friend) null else friend
                    }, navController, viewModel)

                    1 -> RequestListContent(
                        receivedRequests,
                        currentUserId,
                        { viewModel.acceptFriendRequest(it); viewModel.getFriends(); viewModel.loadReceivedFriendRequests(currentUserId) },
                        { viewModel.rejectFriendRequest(it.requestId); viewModel.loadReceivedFriendRequests(currentUserId) },
                        navController
                    )
                    2 -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            label = { Text("Search users") },
                            leadingIcon = { Icon(Icons.Default.Search, "Search") }
                        )
                        UserListContent(
                            searchResults,
                            expandedFriend,
                            { user -> expandedFriend = if (expandedFriend == user) null else user },
                            navController,
                            viewModel,
                            friendRequestStatuses
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsListContent(
    friendsList: List<UserProfile>,
    expandedFriend: UserProfile?,
    onExpandClick: (UserProfile) -> Unit,
    navController: NavHostController,
    viewModel: FriendsViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        items(friendsList) { friend ->
            FriendCard(
                friend = friend,
                isExpanded = expandedFriend == friend,
                onExpandClick = { onExpandClick(friend) },
                navController = navController,
                viewModel = viewModel,
                isFriendTab = true,
                friendRequestStatus = null
            )
        }
    }
}

@Composable
fun UserListContent(
    userList: List<UserProfile>,
    expandedUser: UserProfile?,
    onExpandClick: (UserProfile) -> Unit,
    navController: NavHostController,
    viewModel: FriendsViewModel,
    friendRequestStatuses: Map<String, Result<Unit>?>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        items(userList) { user ->
            val friendRequestStatus = friendRequestStatuses[user.userId]
            FriendCard(
                friend = user,
                isExpanded = expandedUser == user,
                onExpandClick = { onExpandClick(user) },
                navController = navController,
                viewModel = viewModel,
                isFriendTab = false,
                friendRequestStatus = friendRequestStatus
            )
        }
    }
}

@Composable
fun FriendCard(
    friend: UserProfile,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    navController: NavHostController,
    viewModel: FriendsViewModel,
    isFriendTab: Boolean,
    friendRequestStatus: Result<Unit>?
) {
    val cardColors = listOf(Color(0xFFDC8686), Color(0xFF8572CB), Color(0xFFF4EAE0), Color(0xFF6D5D6E), Color(0xFF393646))
    var cardColor by remember { mutableStateOf(LimeGreen) }
    val scale by animateFloatAsState(if (isExpanded) 1.05f else 1f, label = "")

    LaunchedEffect(isExpanded) {
        if (isExpanded) cardColor = cardColors.random()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .scale(scale)
            .animateContentSize()
            .clickable { onExpandClick() }
            .shadow(8.dp, RoundedCornerShape(25.dp), spotColor = if (isExpanded) NeonPink else Color.Transparent),
        colors = CardDefaults.cardColors(containerColor = if (isExpanded) cardColor else LimeGreen),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clickable { navController.navigate(Screen.Profile.createRoute(friend.userId)) }) {
                        AsyncImage(
                            model = friend.profilePictureUrl ?: R.drawable.my_profile_icon,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(if (isExpanded) 80.dp else 60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = friend.userName ?: "No Name",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = if (isExpanded) 22.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (isExpanded) {
                            Text("Tap to close", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (!isFriendTab) {
                    val icon = when (friendRequestStatus) {
                        is Result.Loading -> Icons.Filled.Refresh
                        is Result.Success -> Icons.Filled.Check
                        is Result.Failure -> Icons.Filled.Error
                        null -> Icons.Default.PersonAdd
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = if (friendRequestStatus == null) "Add Friend" else "Request Status",
                        tint = Color.Black,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clickable(enabled = friendRequestStatus == null) {
                                viewModel.sendFriendRequest(friend.userId)
                                viewModel.resetFriendRequestStatus(friend.userId)
                            }
                    )
                }
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    VibesPortal(friend.userId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isFriendTab) {
                            Button(
                                onClick = { viewModel.createOrNavigateToChat(friend.userId) },
                                colors = ButtonDefaults.buttonColors(Color(0xFFDC8686))
                            ) {
                                Icon(Icons.Default.ChatBubble, "Message", tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Message", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VibesPortal(friendId: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.tenor_unscreen}"))
                    setOnPreparedListener { mp ->
                        mp.isLooping = true // Loop the video
                        mp.start() // Start playing immediately
                    }
                    setOnCompletionListener { it.start() } // Restart on completion
                }
            },
            modifier = Modifier.size(width = 240.dp, height = 320.dp)
        )

        val vibeMessage = remember(friendId) {
            val beginnings = listOf("Our souls", "The cosmos", "Silent waves")
            val middles = listOf("weave", "dance", "pulse")
            val ends = listOf("through the void", "in cosmic harmony")
            "${beginnings.random()} ${middles.random()} ${ends.random()}"
        }

        Text(
            text = vibeMessage,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}


@Composable
fun RequestListContent(
    receivedRequests: List<FriendRequest>,
    currentUserId: String,
    onAcceptRequest: (FriendRequest) -> Unit,
    onRejectRequest: (FriendRequest) -> Unit,
    navController: NavHostController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(receivedRequests) { request ->
            RequestCard(
                request = request,
                currentUserId = currentUserId,
                onAccept = { onAcceptRequest(request) },
                onReject = { onRejectRequest(request) },
                navController = navController
            )
        }
    }
}

@Composable
fun RequestCard(
    request: FriendRequest,
    currentUserId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    navController: NavHostController
) {
    var senderName by remember { mutableStateOf("Loading...") }
    var senderAvatar by remember { mutableStateOf("") }

    LaunchedEffect(request.senderId) {
        val senderDoc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(request.senderId)
            .get()
            .await()
        senderName = senderDoc.getString("userName") ?: "Unknown"
        senderAvatar = senderDoc.getString("profilePictureUrl") ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = senderAvatar,
                    contentDescription = "Sender Avatar",
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(senderName, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(Color.Green)) {
                    Icon(Icons.Default.Check, "Accept")
                    Spacer(Modifier.width(8.dp))
                    Text("Accept")
                }
                Button(onClick = onReject, colors = ButtonDefaults.buttonColors(Color.Red)) {
                    Icon(Icons.Default.Clear, "Reject")
                    Spacer(Modifier.width(8.dp))
                    Text("Reject")
                }
            }
        }
    }
}

private enum class PortalState {
    Closed,
    Opening,
    Open
}