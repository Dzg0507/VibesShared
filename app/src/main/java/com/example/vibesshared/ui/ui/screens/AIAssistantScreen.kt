package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.ai.AIService
import com.example.vibesshared.ui.ui.ai.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiService = remember { AIService(context) }
    
    var currentTab by remember { mutableStateOf(AITab.CHAT) }
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var postSuggestions by remember { mutableStateOf(listOf<PostSuggestion>()) }
    var isLoading by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    // Load initial chat message and post suggestions
    LaunchedEffect(Unit) {
        chatMessages = listOf(
            ChatMessage(
                id = "1",
                text = "Hi! I'm your AI assistant. I can help you with posting content, finding friends, managing your profile, and more! What would you like help with?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
        postSuggestions = aiService.generatePostSuggestions("current_user")
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SmartToy,
                                contentDescription = "AI Assistant",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI Assistant",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                // Tab Row
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ) {
                    AITab.values().forEachIndexed { index, tab ->
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
                    AITab.CHAT -> {
                        ChatSection(
                            messages = chatMessages,
                            userMessage = userMessage,
                            onMessageChange = { userMessage = it },
                            onSendMessage = {
                                if (userMessage.isNotBlank()) {
                                    val newUserMessage = ChatMessage(
                                        id = System.currentTimeMillis().toString(),
                                        text = userMessage,
                                        isFromUser = true,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    chatMessages = chatMessages + newUserMessage
                                    
                                    // Send to AI
                                    isLoading = true
                                    isTyping = true
                                    
                                    // Simulate AI response
                                    LaunchedEffect(userMessage) {
                                        delay(1500)
                                        val response = aiService.getChatAssistantResponse(userMessage)
                                        val aiMessage = ChatMessage(
                                            id = System.currentTimeMillis().toString(),
                                            text = response.message,
                                            isFromUser = false,
                                            timestamp = System.currentTimeMillis(),
                                            suggestions = response.suggestions
                                        )
                                        chatMessages = chatMessages + aiMessage
                                        isLoading = false
                                        isTyping = false
                                        userMessage = ""
                                    }
                                }
                            },
                            isLoading = isLoading,
                            isTyping = isTyping,
                            listState = listState
                        )
                    }
                    AITab.SUGGESTIONS -> {
                        PostSuggestionsSection(
                            suggestions = postSuggestions,
                            onSuggestionClick = { suggestion ->
                                // Handle suggestion click
                            }
                        )
                    }
                    AITab.ANALYTICS -> {
                        AIAnalyticsSection()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSection(
    messages: List<ChatMessage>,
    userMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    isTyping: Boolean,
    listState: LazyListState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(message = message)
            }
            
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }
        
        // Message Input
        MessageInput(
            message = userMessage,
            onMessageChange = onMessageChange,
            onSendMessage = onSendMessage,
            isLoading = isLoading
        )
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) VividBlue else Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isFromUser) Color.White else Color.Black,
                    fontSize = 14.sp
                )
                
                message.suggestions?.let { suggestions ->
                    Spacer(modifier = Modifier.height(8.dp))
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { /* Handle suggestion click */ },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        repeat(3) { index ->
            val rotation by animateFloatAsState(
                targetValue = if (index % 2 == 0) 0f else 360f,
                animationSpec = tween(1000),
                label = "typing_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
                    .rotate(rotation)
            )
            
            if (index < 2) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "AI is typing...",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = { Text("Ask me anything...", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                cursorColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendMessage() })
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        FloatingActionButton(
            onClick = onSendMessage,
            modifier = Modifier.size(48.dp),
            containerColor = VividBlue
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PostSuggestionsSection(
    suggestions: List<PostSuggestion>,
    onSuggestionClick: (PostSuggestion) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Smart Post Suggestions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(suggestions) { suggestion ->
            PostSuggestionCard(
                suggestion = suggestion,
                onClick = { onSuggestionClick(suggestion) }
            )
        }
    }
}

@Composable
fun PostSuggestionCard(
    suggestion: PostSuggestion,
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
                Icon(
                    imageVector = when (suggestion.type) {
                        SuggestionType.TRENDING_TOPIC -> Icons.Filled.TrendingUp
                        SuggestionType.PERSONAL_QUESTION -> Icons.Filled.QuestionAnswer
                        SuggestionType.CONTENT_PROMOTION -> Icons.Filled.Promote
                        SuggestionType.COMMUNITY_BUILDING -> Icons.Filled.Group
                    },
                    contentDescription = null,
                    tint = ElectricPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = suggestion.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.content,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestion.hashtags.forEach { hashtag ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(hashtag, fontSize = 10.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estimated engagement: ${suggestion.estimatedEngagement}",
                    fontSize = 12.sp,
                    color = when (suggestion.estimatedEngagement) {
                        "High" -> LimeGreen
                        "Medium" -> SunsetOrange
                        else -> Color.Gray
                    }
                )
                
                TextButton(onClick = { onClick() }) {
                    Text("Use This", color = ElectricPurple)
                }
            }
        }
    }
}

@Composable
fun AIAnalyticsSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "AI Analytics Dashboard",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Placeholder analytics cards
        repeat(4) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Analytics Feature ${index + 1}",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Coming soon! This will show your content performance, engagement metrics, and AI insights.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// Data classes
data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val suggestions: List<String>? = null
)

enum class AITab(val title: String, val icon: ImageVector) {
    CHAT("Chat", Icons.Filled.Chat),
    SUGGESTIONS("Suggestions", Icons.Filled.Lightbulb),
    ANALYTICS("Analytics", Icons.Filled.Analytics)
}