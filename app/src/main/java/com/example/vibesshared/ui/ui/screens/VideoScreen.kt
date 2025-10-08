package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.video.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoService = remember { VideoService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(VideoTab.CALL) }
    var videoStories by remember { mutableStateOf<List<VideoStory>>(emptyList()) }
    var videoEffects by remember { mutableStateOf<List<VideoEffect>>(emptyList()) }
    var videoFilters by remember { mutableStateOf<List<VideoFilter>>(emptyList()) }
    var showCallDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(Unit) {
        videoStories = videoService.getVideoStories()
        videoEffects = videoService.getVideoEffects()
        videoFilters = videoService.getVideoFilters()
    }
    
    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = gradientColors))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Video Hub", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCallDialog = true }) {
                            Icon(imageVector = Icons.Filled.VideoCall, contentDescription = "Start Call", tint = Color.White)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showStoryDialog = true },
                    containerColor = LimeGreen
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Story", tint = Color.White)
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ) {
                    VideoTab.values().forEachIndexed { index, tab ->
                        Tab(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            text = { Text(tab.title, color = Color.White) },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title, tint = Color.White) }
                        )
                    }
                }
                
                // Content based on selected tab
                when (currentTab) {
                    VideoTab.CALL -> CallTab(videoService = videoService)
                    VideoTab.STORIES -> StoriesTab(stories = videoStories)
                    VideoTab.EFFECTS -> EffectsTab(effects = videoEffects)
                    VideoTab.FILTERS -> FiltersTab(filters = videoFilters)
                }
            }
        }
        
        // Call Dialog
        if (showCallDialog) {
            VideoCallDialog(
                videoService = videoService,
                onDismiss = { showCallDialog = false },
                onStartCall = { call ->
                    showCallDialog = false
                }
            )
        }
        
        // Story Dialog
        if (showStoryDialog) {
            VideoStoryDialog(
                videoService = videoService,
                onDismiss = { showStoryDialog = false },
                onCreate = { story ->
                    showStoryDialog = false
                }
            )
        }
    }
}

@Composable
fun CallTab(videoService: VideoService) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Video Calls",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Recent calls placeholder
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideoCall,
                        contentDescription = "Video Call",
                        tint = ElectricPurple,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start a Video Call",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Connect with friends and family",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StoriesTab(stories: List<VideoStory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Video Stories",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(stories) { story ->
            VideoStoryCard(story = story)
        }
    }
}

@Composable
fun VideoStoryCard(story: VideoStory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = story.thumbnailUrl,
                contentDescription = "Story Thumbnail",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.caption,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${story.duration}s • ${story.views} views",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Likes",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${story.likes}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = Icons.Filled.Comment,
                        contentDescription = "Comments",
                        tint = VividBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${story.comments}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Shares",
                        tint = ElectricPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${story.shares}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = ElectricPurple
                    )
                }
                
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EffectsTab(effects: List<VideoEffect>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Video Effects",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Effects by category
        val effectsByCategory = effects.groupBy { it.category }
        
        effectsByCategory.forEach { (category, categoryEffects) ->
            item {
                Text(
                    text = category.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryEffects) { effect ->
                        EffectCard(effect = effect)
                    }
                }
            }
        }
    }
}

@Composable
fun EffectCard(effect: VideoEffect) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = effect.icon,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = effect.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            if (effect.isPremium) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐ Premium",
                    fontSize = 10.sp,
                    color = SunsetOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FiltersTab(filters: List<VideoFilter>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Video Filters",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Filters by category
        val filtersByCategory = filters.groupBy { it.category }
        
        filtersByCategory.forEach { (category, categoryFilters) ->
            item {
                Text(
                    text = category.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryFilters) { filter ->
                        FilterCard(filter = filter)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterCard(filter: VideoFilter) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = filter.icon,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = filter.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            if (filter.isPremium) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐ Premium",
                    fontSize = 10.sp,
                    color = SunsetOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VideoCallDialog(
    videoService: VideoService,
    onDismiss: () -> Unit,
    onStartCall: (VideoCall) -> Unit
) {
    var callType by remember { mutableStateOf(VideoCallType.ONE_ON_ONE) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Video Call") },
        text = {
            Column {
                Text(
                    text = "Call Type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                callType = VideoCallType.ONE_ON_ONE
                
                Text(
                    text = "Call your friends and family with high-quality video calls!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val call = videoService.startVideoCall(
                            callerId = "current_user",
                            receiverId = "friend_1",
                            callType = callType
                        )
                        onStartCall(call)
                    }
                }
            ) {
                Text("Start Call")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun VideoStoryDialog(
    videoService: VideoService,
    onDismiss: () -> Unit,
    onCreate: (VideoStory) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Video Story") },
        text = {
            Column {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                    Text("Make story public")
                }
                
                Text(
                    text = "Stories will be visible for 24 hours",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val storyData = VideoStoryData(
                            videoUrl = "https://example.com/video/story.mp4",
                            thumbnailUrl = "https://picsum.photos/200/300",
                            duration = 30,
                            caption = caption,
                            filters = "Warm",
                            music = "Background Music",
                            effects = "Sparkles",
                            isPublic = isPublic,
                            tags = listOf("#story", "#vibes")
                        )
                        
                        val story = videoService.createVideoStory("current_user", storyData)
                        onCreate(story)
                    }
                }
            ) {
                Text("Create Story")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class VideoTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALL("Calls", Icons.Filled.VideoCall),
    STORIES("Stories", Icons.Filled.AutoStories),
    EFFECTS("Effects", Icons.Filled.EmojiEmotions),
    FILTERS("Filters", Icons.Filled.FilterList)
}