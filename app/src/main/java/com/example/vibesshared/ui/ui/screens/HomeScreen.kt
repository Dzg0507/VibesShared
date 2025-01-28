@file:OptIn(InternalSerializationApi::class)

package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Try
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi


data class Post(
    val postId: String = "",
    val userId: String = "",
    val postText: String? = "",
    val postImage: String? = null,
    val postVideo: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val userName: String? = "",
    val userProfilePicture: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val auth = Firebase.auth

    PostsListener(
        onPostsUpdate = { newPosts ->
            posts = newPosts
            isLoading = false
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0A0A)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreatePost.route) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        ) { padding ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = posts,
                        key = { post -> post.postId }
                    ) { post ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { fullHeight -> fullHeight }
                            ),
                            modifier = Modifier.animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) {
                            PostCard(post = post, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostsListener(onPostsUpdate: (List<Post>) -> Unit) {
    DisposableEffect(Unit) {
        val postsCollection = Firebase.firestore
            .collection("app_data")
            .document("posts")
            .collection("user_posts")

        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { querySnapshot ->
                    val newPosts = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Post::class.java)
                    }
                    onPostsUpdate(newPosts)
                }
            }

        onDispose {
            listener.remove()
        }
    }
}

@Composable
fun PostCard(post: Post, navController: NavController) {
    val neonGreen = Color(0xFF39FF14)
    val neonRed = Color(0xFFFF0044)
    val neonBlue = Color(0xFF0B84FF)
    val bloodOrange = Color(0xFFFF4500)

    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val gradientColors = listOf(
        Color(0xFF0D0D0D),
        Color(0xFF1A1A1A).copy(alpha = 0.95f),
        neonBlue.copy(alpha = 0.1f),
        Color(0xFF200030).copy(alpha = alpha)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 32.dp,
                spotColor = bloodOrange.copy(alpha = 0.7f),
                ambientColor = bloodOrange.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .offset(y = (-4).dp)
            .border(2.dp, neonGreen, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = 500f
                    )
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.userProfilePicture ?: R.drawable.my_profile_icon,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, neonRed, CircleShape)
                            .clickable {
                                navController.navigate("profile/${post.userId}")
                            }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = post.userName ?: "Anonymous",
                        color = neonGreen,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                post.postImage?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, neonRed, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                post.postText?.let { text ->
                    Text(
                        text = text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /* Like action */ }) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Like",
                            tint = neonRed
                        )
                    }
                    IconButton(onClick = { /* Comment action */ }) {
                        Icon(
                            Icons.Filled.Try,
                            contentDescription = "Comment",
                            tint = neonGreen
                        )
                    }
                    IconButton(onClick = { /* Share action */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = neonGreen
                        )
                    }
                }
            }
        }
    }
}
