package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.streaming.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val streamingService = remember { LiveStreamingService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(StreamTab.DISCOVER) }
    var trendingStreams by remember { mutableStateOf<List<LiveStream>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedStream by remember { mutableStateOf<LiveStream?>(null) }
    var isStreaming by remember { mutableStateOf(false) }
    var showCreateStream by remember { mutableStateOf(false) }
    
    // Load trending streams
    LaunchedEffect(Unit) {
        trendingStreams = streamingService.getTrendingStreams()
        isLoading = false
    }
    
    val gradientColors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = gradientColors)
            )
    ) {
        if (selectedStream != null) {
            // Stream Viewing Screen
            StreamViewScreen(
                stream = selectedStream!!,
                streamingService = streamingService,
                onBack = { selectedStream = null },
                modifier = Modifier.fillMaxSize()
            )
        } else if (showCreateStream) {
            // Create Stream Screen
            CreateStreamScreen(
                streamingService = streamingService,
                onStreamStarted = { stream ->
                    isStreaming = true
                    showCreateStream = false
                    selectedStream = stream
                },
                onBack = { showCreateStream = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Main Discovery Screen
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Live Streaming",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showCreateStream = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = "Go Live",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showCreateStream = true },
                        containerColor = LimeGreen
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LiveTv,
                            contentDescription = "Go Live",
                            tint = Color.White
                        )
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
                        StreamTab.values().forEachIndexed { index, tab ->
                            Tab(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                text = { Text(tab.title, color = Color.White) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = Color.White
                                    )
                                }
                            )
                        }
                    }
                    
                    // Content based on selected tab
                    when (currentTab) {
                        StreamTab.DISCOVER -> {
                            DiscoverStreamsTab(
                                streams = trendingStreams,
                                isLoading = isLoading,
                                onStreamClick = { stream ->
                                    selectedStream = stream
                                }
                            )
                        }
                        StreamTab.GAMING -> {
                            CategoryStreamsTab(
                                category = "Gaming",
                                streams = trendingStreams.filter { it.category == "Gaming" },
                                onStreamClick = { stream ->
                                    selectedStream = stream
                                }
                            )
                        }
                        StreamTab.MUSIC -> {
                            CategoryStreamsTab(
                                category = "Music",
                                streams = trendingStreams.filter { it.category == "Music" },
                                onStreamClick = { stream ->
                                    selectedStream = stream
                                }
                            )
                        }
                        StreamTab.ART -> {
                            CategoryStreamsTab(
                                category = "Art",
                                streams = trendingStreams.filter { it.category == "Art" },
                                onStreamClick = { stream ->
                                    selectedStream = stream
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverStreamsTab(
    streams: List<LiveStream>,
    isLoading: Boolean,
    onStreamClick: (LiveStream) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Trending Now",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(streams) { stream ->
                StreamCard(
                    stream = stream,
                    onClick = { onStreamClick(stream) }
                )
            }
        }
    }
}

@Composable
fun CategoryStreamsTab(
    category: String,
    streams: List<LiveStream>,
    onStreamClick: (LiveStream) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "$category Streams",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (streams.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LiveTv,
                            contentDescription = "No Streams",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No $category streams at the moment",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Be the first to go live!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(streams) { stream ->
                StreamCard(
                    stream = stream,
                    onClick = { onStreamClick(stream) }
                )
            }
        }
    }
}

@Composable
fun StreamCard(
    stream: LiveStream,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = stream.thumbnailUrl,
                    contentDescription = "Stream Thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stream.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = stream.streamerName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stream.viewerCount} watching",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stream.quality,
                            fontSize = 12.sp,
                            color = VividBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stream.tags.forEach { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(tag, fontSize = 10.sp) }
                    )
                }
            }
        }
    }
}

@Composable
fun StreamViewScreen(
    stream: LiveStream,
    streamingService: LiveStreamingService,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var showChat by remember { mutableStateOf(true) }
    var showAnalytics by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Load chat messages
    LaunchedEffect(stream.id) {
        streamingService.getStreamChat(stream.id).collect { messages ->
            chatMessages = messages
        }
    }
    
    Box(modifier = modifier) {
        // Video Player Area (Placeholder)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.LiveTv,
                    contentDescription = "Live Stream",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stream.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "by ${stream.streamerName}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stream.viewerCount} viewers",
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            // Top Controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { showAnalytics = !showAnalytics },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = "Analytics",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = { showChat = !showChat },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = "Chat",
                        tint = Color.White
                    )
                }
            }
            
            // Bottom Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isFollowing = !isFollowing
                        scope.launch {
                            streamingService.followStreamer(stream.streamerName, "current_user")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) LimeGreen else VividBlue
                    )
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        isSubscribed = !isSubscribed
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubscribed) SunsetOrange else ElectricPurple
                    )
                ) {
                    Text(
                        text = if (isSubscribed) "Subscribed" else "Subscribe",
                        color = Color.White
                    )
                }
            }
        }
        
        // Chat Panel
        AnimatedVisibility(
            visible = showChat,
            enter = slideInVertically() + fadeIn()
        ) {
            ChatPanel(
                messages = chatMessages,
                onSendMessage = { message ->
                    scope.launch {
                        streamingService.sendChatMessage(
                            streamId = stream.id,
                            message = ChatMessage(
                                id = "",
                                streamId = stream.id,
                                userId = "current_user",
                                userName = "You",
                                text = message,
                                timestamp = 0
                            )
                        ) { }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(300.dp)
                    .fillMaxHeight()
            )
        }
        
        // Analytics Panel
        AnimatedVisibility(
            visible = showAnalytics,
            enter = slideInVertically() + fadeIn()
        ) {
            StreamAnalyticsPanel(
                streamId = stream.id,
                streamingService = streamingService,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(300.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun ChatPanel(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Live Chat",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages.takeLast(50)) { message ->
                    ChatMessageItem(message = message)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Text(
        text = "${message.userName}: ${message.text}",
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun StreamAnalyticsPanel(
    streamId: String,
    streamingService: LiveStreamingService,
    modifier: Modifier = Modifier
) {
    var analytics by remember { mutableStateOf<StreamAnalytics?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(streamId) {
        analytics = streamingService.getStreamAnalytics(streamId)
    }
    
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Stream Analytics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            analytics?.let { analytics ->
                AnalyticsItem("Total Viewers", "${analytics.totalViewers}")
                AnalyticsItem("Peak Viewers", "${analytics.peakViewers}")
                AnalyticsItem("Avg View Time", "${analytics.averageViewTime} min")
                AnalyticsItem("Messages", "${analytics.totalMessages}")
                AnalyticsItem("Engagement", "${(analytics.engagementRate * 100).toInt()}%")
                AnalyticsItem("Likes", "${analytics.likes}")
                AnalyticsItem("Shares", "${analytics.shares}")
                AnalyticsItem("Revenue", "$${String.format("%.2f", analytics.revenue)}")
            }
        }
    }
}

@Composable
fun AnalyticsItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CreateStreamScreen(
    streamingService: LiveStreamingService,
    onStreamStarted: (LiveStream) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var streamTitle by remember { mutableStateOf("") }
    var streamDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Gaming") }
    var isPublic by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val categories = listOf("Gaming", "Music", "Art", "Cooking", "Fitness", "Education", "Comedy", "Tech")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = listOf(ElectricPurple, NeonPink, VividBlue))
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Go Live",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = streamTitle,
                            onValueChange = { streamTitle = it },
                            label = { Text("Stream Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = streamDescription,
                            onValueChange = { streamDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        
                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isPublic,
                                onCheckedChange = { isPublic = it }
                            )
                            Text("Public Stream")
                        }
                        
                        Button(
                            onClick = {
                                if (streamTitle.isNotBlank()) {
                                    isLoading = true
                                    scope.launch {
                                        val config = StreamConfig(
                                            title = streamTitle,
                                            description = streamDescription,
                                            category = selectedCategory,
                                            isPublic = isPublic
                                        )
                                        val session = streamingService.startStream(config)
                                        
                                        // Create a LiveStream object for the stream
                                        val liveStream = LiveStream(
                                            id = session.id,
                                            title = streamTitle,
                                            category = selectedCategory,
                                            streamerName = "You",
                                            streamerAvatar = "https://picsum.photos/200/300",
                                            viewerCount = 1,
                                            thumbnailUrl = "https://picsum.photos/400/300",
                                            isLive = true,
                                            startTime = System.currentTimeMillis(),
                                            tags = listOf("#Live", "#$selectedCategory", "#VibesApp"),
                                            quality = "1080p",
                                            language = "English"
                                        )
                                        
                                        onStreamStarted(liveStream)
                                    }
                                }
                            },
                            enabled = streamTitle.isNotBlank() && !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Start Streaming", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class StreamTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DISCOVER("Discover", Icons.Filled.Explore),
    GAMING("Gaming", Icons.Filled.SportsEsports),
    MUSIC("Music", Icons.Filled.MusicNote),
    ART("Art", Icons.Filled.Palette)
}