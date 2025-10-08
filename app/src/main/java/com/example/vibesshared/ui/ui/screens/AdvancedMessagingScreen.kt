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
import com.example.vibesshared.ui.ui.messaging.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedMessagingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messagingService = remember { AdvancedMessagingService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(MessagingTab.VOICE) }
    var scheduledMessages by remember { mutableStateOf<List<ScheduledMessage>>(emptyList()) }
    var messageTemplates by remember { mutableStateOf<List<MessageTemplate>>(emptyList()) }
    var messageReactions by remember { mutableStateOf<List<MessageReactionData>>(emptyList()) }
    var messageAnalytics by remember { mutableStateOf<MessageAnalytics?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showEncryptionDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(Unit) {
        scheduledMessages = messagingService.getScheduledMessages("current_user")
        messageTemplates = messagingService.getMessageTemplates("current_user")
        messageReactions = messagingService.getMessageReactions("sample_message")
        messageAnalytics = messagingService.getMessageAnalytics("sample_chat", TimeRange.WEEK)
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
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "Advanced Messaging",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Advanced Messaging", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showScheduleDialog = true }) {
                            Icon(imageVector = Icons.Filled.Schedule, contentDescription = "Schedule Message", tint = Color.White)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showEncryptionDialog = true },
                    containerColor = LimeGreen
                ) {
                    Icon(imageVector = Icons.Filled.Security, contentDescription = "Encrypt Message", tint = Color.White)
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
                    MessagingTab.values().forEachIndexed { index, tab ->
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
                    MessagingTab.VOICE -> VoiceTab(messagingService = messagingService)
                    MessagingTab.REACTIONS -> ReactionsTab(reactions = messageReactions)
                    MessagingTab.SCHEDULED -> ScheduledTab(scheduled = scheduledMessages, onCreateSchedule = { showScheduleDialog = true })
                    MessagingTab.TEMPLATES -> TemplatesTab(templates = messageTemplates, onCreateTemplate = { showTemplateDialog = true })
                    MessagingTab.ANALYTICS -> AnalyticsTab(analytics = messageAnalytics)
                }
            }
        }
        
        // Schedule Message Dialog
        if (showScheduleDialog) {
            ScheduleMessageDialog(
                messagingService = messagingService,
                onDismiss = { showScheduleDialog = false },
                onSchedule = { message ->
                    showScheduleDialog = false
                }
            )
        }
        
        // Create Template Dialog
        if (showTemplateDialog) {
            CreateTemplateDialog(
                messagingService = messagingService,
                onDismiss = { showTemplateDialog = false },
                onCreate = { template ->
                    showTemplateDialog = false
                }
            )
        }
        
        // Encryption Dialog
        if (showEncryptionDialog) {
            EncryptionDialog(
                messagingService = messagingService,
                onDismiss = { showEncryptionDialog = false }
            )
        }
    }
}

@Composable
fun VoiceTab(messagingService: AdvancedMessagingService) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var transcribedText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Voice Messages",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Recording Interface
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Recording Button
                    Button(
                        onClick = {
                            isRecording = !isRecording
                            if (isRecording) {
                                // Start recording
                                scope.launch {
                                    while (isRecording) {
                                        delay(1000)
                                        recordingDuration++
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) Color.Red else ElectricPurple
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isRecording) "Recording... ${recordingDuration}s" else "Tap to record voice message",
                        fontSize = 16.sp,
                        color = if (isRecording) Color.Red else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isRecording) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Voice messages are automatically transcribed",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Transcription Display
        if (transcribedText.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Transcription",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = transcribedText,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Voice Message Features
        item {
            Text(
                text = "Voice Features",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listOf("Auto Transcription", "Voice Effects", "Background Noise Reduction", "Voice Cloning")) { feature ->
                    Card(
                        modifier = Modifier.width(150.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = feature,
                                tint = ElectricPurple,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = feature,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionsTab(reactions: List<MessageReactionData>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Message Reactions",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Reaction Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Reactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MessageReaction.values().take(8)) { reaction ->
                            Button(
                                onClick = { },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = reaction.emoji,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Recent Reactions
        item {
            Text(
                text = "Recent Reactions",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(reactions.take(10)) { reaction ->
            ReactionItem(reaction = reaction)
        }
    }
}

@Composable
fun ReactionItem(reaction: MessageReactionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reaction.reaction.emoji,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "User ${reaction.userId.takeLast(4)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = formatTime(reaction.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "View",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ScheduledTab(scheduled: List<ScheduledMessage>, onCreateSchedule: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Messages",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onCreateSchedule,
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Schedule Message",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule", color = Color.White)
                }
            }
        }
        
        if (scheduled.isEmpty()) {
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
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "No Scheduled Messages",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No scheduled messages",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Schedule messages to be sent later",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(scheduled) { message ->
                ScheduledMessageItem(message = message)
            }
        }
    }
}

@Composable
fun ScheduledMessageItem(message: ScheduledMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled for ${formatTime(message.scheduledTime)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple
                )
                
                Text(
                    text = if (message.isSent) "SENT" else "PENDING",
                    fontSize = 10.sp,
                    color = if (message.isSent) LimeGreen else SunsetOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            if (message.isSent) LimeGreen.copy(alpha = 0.1f) else SunsetOrange.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message.content,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Type: ${message.messageType.name}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = VividBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesTab(templates: List<MessageTemplate>, onCreateTemplate: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Message Templates",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onCreateTemplate,
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Template",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", color = Color.White)
                }
            }
        }
        
        items(templates) { template ->
            TemplateItem(template = template)
        }
    }
}

@Composable
fun TemplateItem(template: MessageTemplate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = template.category,
                    fontSize = 10.sp,
                    color = VividBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(VividBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = template.content,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Used ${template.usageCount} times",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = VividBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsTab(analytics: MessageAnalytics?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Message Analytics",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        analytics?.let { data ->
            // Overview Stats
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Overview (${data.timeRange.name})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Total Messages", data.totalMessages.toString())
                            StatItem("Voice Messages", data.voiceMessages.toString())
                            StatItem("Reactions", data.reactions.toString())
                        }
                    }
                }
            }
            
            // Message Types
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Message Types",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        data.messageTypes.forEach { (type, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                
                                Text(
                                    text = count.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricPurple
                                )
                            }
                        }
                    }
                }
            }
            
            // Performance Metrics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Performance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Avg Response Time", "${data.averageResponseTime}s")
                            StatItem("Most Active Hour", "${data.mostActiveHour}:00")
                        }
                    }
                }
            }
        } ?: run {
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
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = "No Analytics",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No analytics data",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Analytics will appear as you use messaging features",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricPurple
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ScheduleMessageDialog(
    messagingService: AdvancedMessagingService,
    onDismiss: () -> Unit,
    onSchedule: (ScheduledMessage) -> Unit
) {
    var messageContent by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("") }
    var messageType by remember { mutableStateOf(MessageType.TEXT) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Message") },
        text = {
            Column {
                OutlinedTextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    label = { Text("Message Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = scheduledDate,
                        onValueChange = { scheduledDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = scheduledTime,
                        onValueChange = { scheduledTime = it },
                        label = { Text("Time (HH:MM)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val scheduledTimeMillis = System.currentTimeMillis() + 3600000L // 1 hour from now
                        val message = messagingService.scheduleMessage(
                            chatId = "sample_chat",
                            senderId = "current_user",
                            content = messageContent,
                            scheduledTime = scheduledTimeMillis,
                            messageType = messageType
                        )
                        onSchedule(message)
                    }
                },
                enabled = messageContent.isNotBlank()
            ) {
                Text("Schedule")
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
fun CreateTemplateDialog(
    messagingService: AdvancedMessagingService,
    onDismiss: () -> Unit,
    onCreate: (MessageTemplate) -> Unit
) {
    var templateName by remember { mutableStateOf("") }
    var templateContent by remember { mutableStateOf("") }
    var templateCategory by remember { mutableStateOf("General") }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Template") },
        text = {
            Column {
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = templateContent,
                    onValueChange = { templateContent = it },
                    label = { Text("Template Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = templateCategory,
                    onValueChange = { templateCategory = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val template = messagingService.createMessageTemplate(
                            userId = "current_user",
                            templateName = templateName,
                            content = templateContent,
                            category = templateCategory
                        )
                        onCreate(template)
                    }
                },
                enabled = templateName.isNotBlank() && templateContent.isNotBlank()
            ) {
                Text("Create")
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
fun EncryptionDialog(
    messagingService: AdvancedMessagingService,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var encryptedText by remember { mutableStateOf("") }
    var isEncrypted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Encryption") },
        text = {
            Column {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { 
                        messageText = it
                        isEncrypted = false
                        encryptedText = ""
                    },
                    label = { Text("Message to Encrypt") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                if (isEncrypted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = encryptedText,
                        onValueChange = { },
                        label = { Text("Encrypted Message") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        enabled = false
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val encrypted = messagingService.encryptMessage(messageText, "recipient_id")
                        encryptedText = encrypted.encryptedContent
                        isEncrypted = true
                    }
                },
                enabled = messageText.isNotBlank() && !isEncrypted
            ) {
                Text(if (isEncrypted) "Encrypted!" else "Encrypt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

enum class MessagingTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    VOICE("Voice", Icons.Filled.Mic),
    REACTIONS("Reactions", Icons.Filled.EmojiEmotions),
    SCHEDULED("Scheduled", Icons.Filled.Schedule),
    TEMPLATES("Templates", Icons.Filled.ContentCopy),
    ANALYTICS("Analytics", Icons.Filled.Analytics)
}