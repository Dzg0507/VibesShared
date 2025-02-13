// FriendsScreen.kt
@file:Suppress("DEPRECATION")

package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.data.FriendRequest
import com.example.vibesshared.ui.ui.data.UserProfile
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.VividBlue
import com.example.vibesshared.ui.ui.utils.Result
import com.example.vibesshared.ui.ui.utils.Result.*
import com.example.vibesshared.ui.ui.viewmodel.FriendsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
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
    val friendRequestStatuses = remember { mutableStateMapOf<String, Result<Unit>?>() }
    val searchResults by viewModel.users.collectAsState()
    val friendsList by viewModel.friends.collectAsState()
    val receivedRequests by viewModel.receivedFriendRequests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToMessaging.collect { chatId ->
            navController.navigate(Screen.Messaging.createRoute(chatId))
        }
    }

    LaunchedEffect(key1 = currentUserId) {
        viewModel.getFriends()
        viewModel.loadReceivedFriendRequests(currentUserId)
    }

    LaunchedEffect(searchQuery, selectedTabIndex) {
        if (selectedTabIndex == 2) {
            viewModel.searchUsers(searchQuery)
        }
    }

    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue)
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val currentOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = with(LocalDensity.current) { screenWidthDp.toPx() } * gradientColors.size,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
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
                    title = {
                        Text(
                            text = "Friends",
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
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
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
                    0 -> FriendsListContent(
                        friendsList = friendsList,
                        expandedFriend = expandedFriend,
                        onExpandClick = remember { { friend -> expandedFriend = if (expandedFriend == friend) null else friend } },
                        navController = navController,
                        viewModel = viewModel
                    )
                    1 -> RequestListContent(
                        receivedRequests = receivedRequests,
                        currentUserId = currentUserId,
                        onAcceptRequest = remember { { request ->
                            viewModel.acceptFriendRequest(request)
                            viewModel.getFriends()
                            viewModel.loadReceivedFriendRequests(currentUserId)
                        } },
                        onRejectRequest = remember { { request ->
                            viewModel.rejectFriendRequest(request.requestId)
                            viewModel.loadReceivedFriendRequests(currentUserId)
                        } },
                        navController = navController
                    )
                    2 -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            label = { Text("Search users") },
                            leadingIcon = { Icon(Icons.Default.Search, "Search") }
                        )
                        UserListContent(
                            userList = searchResults,
                            expandedUser = expandedFriend,
                            onExpandClick = remember { { user -> expandedFriend = if (expandedFriend == user) null else user } },
                            navController = navController,
                            viewModel = viewModel,
                            friendRequestStatuses = friendRequestStatuses
                        )
                    }
                }

                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Composable
fun FriendsListContent(
    friendsList: List<UserProfile>,
    expandedFriend: UserProfile?,
    onExpandClick: (UserProfile) -> Unit,
    navController: NavController,
    viewModel: FriendsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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
    navController: NavController,
    viewModel: FriendsViewModel,
    friendRequestStatuses: MutableMap<String, Result<Unit>?>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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
    navController: NavController,
    viewModel: FriendsViewModel,
    isFriendTab: Boolean,
    friendRequestStatus: Result<Unit>?
) {
    val cardColors = listOf(
        Color(0xFFDC8686),
        Color(0xFF8572CB),
        Color(0xFFF4EAE0),
        Color(0xFF6D5D6E),
        Color(0xFF393646)
    )
    var cardColor by remember { mutableStateOf(LimeGreen) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) cardColor = cardColors.random()
    }

    val currentOnExpandClick by rememberUpdatedState(onExpandClick)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize()
            .clickable { currentOnExpandClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) cardColor else LimeGreen
        ),
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
                    Box(
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Profile.createRoute(friend.userId))
                        }
                    ) {
                        AsyncImage(
                            model = friend.profilePictureUrl,
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
                            text = friend.userName?: "No Name",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = if (isExpanded) 22.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (isExpanded) {
                            Text(
                                text = "Tap to close",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (!isFriendTab) {
                    val icon = when (friendRequestStatus) {
                        is Loading -> Icons.Filled.Refresh
                        is Success -> Icons.Filled.Check
                        is Failure -> Icons.Filled.Error
                        null -> Icons.Default.PersonAdd
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = "Add Friend",
                        tint = Color.Black,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(top = 16.dp)
                            .clickable(enabled = friendRequestStatus == null) {
                                viewModel.sendFriendRequest(friend.userId)
                            }
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (isFriendTab) {
                        Button(
                            onClick = remember { { viewModel.createOrNavigateToChat(friend.userId) } },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFFDC8686))
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "Message", tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("Message", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestListContent(
    receivedRequests: List<FriendRequest>,
    currentUserId: String,
    onAcceptRequest: (FriendRequest) -> Unit,
    onRejectRequest: (FriendRequest) -> Unit,
    navController: NavController
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
    navController: NavController
) {
    var senderName by remember { mutableStateOf("Loading...") }
    var senderAvatar by remember { mutableStateOf("") }

    LaunchedEffect(request.senderId) {
        val senderDoc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(request.senderId)
            .get()
            .await()
        senderName = senderDoc.getString("userName")?: "Unknown"
        senderAvatar = senderDoc.getString("profilePictureUrl")?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = senderAvatar,
                    contentDescription = "Sender Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(senderName, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(Color.Green)
                ) {
                    Icon(Icons.Default.Check, "Accept")
                    Spacer(Modifier.width(8.dp))
                    Text("Accept")
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Icon(Icons.Default.Clear, "Reject")
                    Spacer(Modifier.width(8.dp))
                    Text("Reject")
                }
            }
        }
    }
}