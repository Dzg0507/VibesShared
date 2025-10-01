package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.vibesshared.ui.ui.events.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventManagementScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val eventService = remember { EventService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(EventTab.DISCOVER) }
    var trendingEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var userEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var eventGroups by remember { mutableStateOf<List<EventGroup>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Event>>(emptyList()) }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(Unit) {
        trendingEvents = eventService.getTrendingEvents()
        userEvents = eventService.getUserEvents("current_user")
        eventGroups = eventService.getEventGroups()
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
                                imageVector = Icons.Filled.Event,
                                contentDescription = "Events",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Events", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCreateEventDialog = true }) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Event", tint = Color.White)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateEventDialog = true },
                    containerColor = LimeGreen
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Event", tint = Color.White)
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.isNotBlank()) {
                            scope.launch {
                                searchResults = eventService.searchEvents(query)
                            }
                        }
                    },
                    placeholder = { Text("Search events...", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                )
                
                if (searchQuery.isNotBlank()) {
                    SearchResultsSection(results = searchResults)
                } else {
                    // Tab Row
                    TabRow(
                        selectedTabIndex = currentTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ) {
                        EventTab.values().forEachIndexed { index, tab ->
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
                        EventTab.DISCOVER -> DiscoverTab(events = trendingEvents)
                        EventTab.MY_EVENTS -> MyEventsTab(events = userEvents)
                        EventTab.GROUPS -> GroupsTab(groups = eventGroups, onCreateGroup = { showCreateGroupDialog = true })
                    }
                }
            }
        }
        
        // Create Event Dialog
        if (showCreateEventDialog) {
            CreateEventDialog(
                eventService = eventService,
                onDismiss = { showCreateEventDialog = false },
                onCreate = { event ->
                    showCreateEventDialog = false
                }
            )
        }
        
        // Create Group Dialog
        if (showCreateGroupDialog) {
            CreateGroupDialog(
                eventService = eventService,
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { group ->
                    showCreateGroupDialog = false
                }
            )
        }
    }
}

@Composable
fun DiscoverTab(events: List<Event>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Trending Events",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(events) { event ->
            EventCard(event = event)
        }
    }
}

@Composable
fun EventCard(event: Event) {
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                AsyncImage(
                    model = event.coverImage,
                    contentDescription = "Event Cover",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        text = event.organizerName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = event.category,
                    fontSize = 10.sp,
                    color = VividBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(VividBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Text(
                text = event.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Time",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatEventTime(event.startTime),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (event.isOnline) Icons.Filled.VideoCall else Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (event.isOnline) "Online Event" else event.location,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${event.rsvpCount} RSVPs",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (event.isFree) "FREE" else "$${String.format("%.0f", event.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (event.isFree) LimeGreen else ElectricPurple
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    event.tags.take(2).forEach { tag ->
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = ElectricPurple,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(ElectricPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text(
                        text = "RSVP",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MyEventsTab(events: List<Event>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Events",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (events.isEmpty()) {
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
                            imageVector = Icons.Filled.Event,
                            contentDescription = "No Events",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No events yet",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Create your first event!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(events) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Composable
fun GroupsTab(groups: List<EventGroup>, onCreateGroup: () -> Unit) {
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
                    text = "Event Groups",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onCreateGroup,
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Group",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", color = Color.White)
                }
            }
        }
        
        items(groups) { group ->
            GroupCard(group = group)
        }
    }
}

@Composable
fun GroupCard(group: EventGroup) {
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
                model = group.coverImage,
                contentDescription = "Group Cover",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = group.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.category,
                        fontSize = 10.sp,
                        color = VividBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(VividBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${group.memberCount} members",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${group.eventCount} events",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.GroupAdd,
                    contentDescription = "Join Group",
                    tint = ElectricPurple
                )
            }
        }
    }
}

@Composable
fun SearchResultsSection(results: List<Event>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Search Results",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (results.isEmpty()) {
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
                            imageVector = Icons.Filled.SearchOff,
                            contentDescription = "No Results",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No events found",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Try a different search term",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(results) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Composable
fun CreateEventDialog(
    eventService: EventService,
    onDismiss: () -> Unit,
    onCreate: (Event) -> Unit
) {
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventCategory by remember { mutableStateOf("Social") }
    var eventLocation by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var isPublic by remember { mutableStateOf(true) }
    var maxAttendees by remember { mutableStateOf("50") }
    var eventPrice by remember { mutableStateOf("0") }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Event") },
        text = {
            Column {
                OutlinedTextField(
                    value = eventTitle,
                    onValueChange = { eventTitle = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = eventCategory,
                        onValueChange = { eventCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = maxAttendees,
                        onValueChange = { maxAttendees = it },
                        label = { Text("Max Attendees") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = eventLocation,
                    onValueChange = { eventLocation = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isOnline
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isOnline,
                        onCheckedChange = { isOnline = it }
                    )
                    Text("Online Event")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                    Text("Public Event")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val eventData = EventData(
                            title = eventTitle,
                            description = eventDescription,
                            organizerId = "current_user",
                            organizerName = "You",
                            category = eventCategory,
                            startTime = System.currentTimeMillis() + 86400000L, // Tomorrow
                            endTime = System.currentTimeMillis() + 86400000L + 7200000L, // +2 hours
                            location = eventLocation,
                            isOnline = isOnline,
                            meetingLink = if (isOnline) "https://meet.example.com/room" else null,
                            maxAttendees = maxAttendees.toIntOrNull() ?: 50,
                            isPublic = isPublic,
                            coverImage = "https://picsum.photos/400/300?id=${UUID.randomUUID()}",
                            tags = listOf("Community", "Fun"),
                            price = eventPrice.toDoubleOrNull() ?: 0.0
                        )
                        
                        val event = eventService.createEvent(eventData)
                        onCreate(event)
                    }
                },
                enabled = eventTitle.isNotBlank()
            ) {
                Text("Create Event")
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
fun CreateGroupDialog(
    eventService: EventService,
    onDismiss: () -> Unit,
    onCreate: (EventGroup) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var groupCategory by remember { mutableStateOf("Social") }
    var isPublic by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Event Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = groupCategory,
                    onValueChange = { groupCategory = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                    Text("Public Group")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val groupData = EventGroupData(
                            name = groupName,
                            description = groupDescription,
                            creatorId = "current_user",
                            category = groupCategory,
                            isPublic = isPublic,
                            coverImage = "https://picsum.photos/400/200?id=${UUID.randomUUID()}",
                            tags = listOf("Community", "Events"),
                            rules = listOf("Be respectful", "No spam", "Have fun!")
                        )
                        
                        val group = eventService.createEventGroup(groupData)
                        onCreate(group)
                    }
                },
                enabled = groupName.isNotBlank()
            ) {
                Text("Create Group")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatEventTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

enum class EventTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DISCOVER("Discover", Icons.Filled.Explore),
    MY_EVENTS("My Events", Icons.Filled.Event),
    GROUPS("Groups", Icons.Filled.Group)
}