package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.UserProfile
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
    profileViewModel: ProfileViewModel = viewModel(
    )
) {
    val userProfile by profileViewModel.userProfile.collectAsState() // Correctly collect the StateFlow
    var isLoading by remember { mutableStateOf(true) }
    val vibrantPostColors = remember { List(7) { Color(Random.nextLong(0xFFFFFFFF)).copy(alpha = 0.8f) } }

    Scaffold(
        topBar = {
            VibesTopAppBar(color = MaterialTheme.colorScheme.primary)
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = 0.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                userProfile?.let { profile ->
                    UserProfileSection(profile)
                }
            }

            item { CreatePostSection(userProfile) }

            item {
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    StoryList(generateStories(3))
                }
            }

            items(generatePosts(10)) { post ->
                val postColor = vibrantPostColors[post.id % vibrantPostColors.size]
                PostCard(post, postColor)
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        delay(2000)
        isLoading = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibesTopAppBar(color: Color) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Logo",
                    modifier = Modifier.size(30.dp),
                    tint = Color.White
                )
                Text("Vibes", modifier = Modifier.padding(start = 8.dp), color = Color.White)
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = color)
    )
}

@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun StoryList(stories: List<Story>) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stories.forEach { story ->
            StoryItem(story)
        }
    }
}

@Composable
fun StoryItem(story: Story) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = story.userImage,
            contentDescription = "Story Image",
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .border(2.dp, Color.LightGray, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(story.userName, fontSize = 12.sp)
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
        targetValue = if (postText.isNotBlank()) 4.dp.value else 0.dp.value,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.value.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = userProfile?.profilePictureUri ?: "https://picsum.photos/200/300", // Placeholder
                contentDescription = "User Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
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
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Welcome, ${profile.firstName} ${profile.lastName}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Email: ${profile.email}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Bio: ${profile.bio}", style = MaterialTheme.typography.bodySmall)

            profile.profilePictureUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
fun PostCard(post: Post, backgroundColor: Color) {
    val likes = remember(post.id) { Random.nextInt(100) }
    val comments = remember(post.id) { Random.nextInt(50) }
    val shares = remember(post.id) { Random.nextInt(20) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.user.profilePictureUri,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(post.user.firstName, fontWeight = FontWeight.Bold)
                    Text(post.timestamp, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(post.postText)

            post.postImage?.let { imageUrl ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = "https://picsum.photos/200/300",
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PostActionButton(icon = Icons.Filled.Favorite, text = "$likes Likes") {
                    // TODO: Handle like action
                }
                PostActionButton(icon = Icons.AutoMirrored.Filled.Message, text = "$comments Comments") {
                    // TODO: Handle comment action
                }
                PostActionButton(icon = Icons.AutoMirrored.Filled.Send, text = "$shares Shares") {
                    // TODO: Handle share action
                }
            }
        }
    }
}


@Composable
fun PostActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 14.sp)
        }
    }
}

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
