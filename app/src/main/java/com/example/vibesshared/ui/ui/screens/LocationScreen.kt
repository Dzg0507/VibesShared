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
import com.example.vibesshared.ui.ui.location.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(LocationTab.MAP) }
    var nearbyFriends by remember { mutableStateOf<List<NearbyFriend>>(emptyList()) }
    var popularLocations by remember { mutableStateOf<List<PopularLocation>>(emptyList()) }
    var nearbyEvents by remember { mutableStateOf<List<LocationEvent>>(emptyList()) }
    var locationHistory by remember { mutableStateOf<List<CheckIn>>(emptyList()) }
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showShareLocationDialog by remember { mutableStateOf(false) }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(Unit) {
        nearbyFriends = locationService.getNearbyFriends("current_user")
        popularLocations = locationService.getPopularLocations()
        nearbyEvents = locationService.getNearbyEvents(
            Location("", "Current Location", "", Coordinates(40.7128, -74.0060), "", 0.0)
        )
        locationHistory = locationService.getLocationHistory("current_user")
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
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Location Hub",
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
                    },
                    actions = {
                        IconButton(onClick = { showCheckInDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.AddLocation,
                                contentDescription = "Check In",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCheckInDialog = true },
                    containerColor = LimeGreen
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Check In",
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
                    LocationTab.values().forEachIndexed { index, tab ->
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
                    LocationTab.MAP -> {
                        MapTab(
                            nearbyFriends = nearbyFriends,
                            popularLocations = popularLocations
                        )
                    }
                    LocationTab.FRIENDS -> {
                        FriendsTab(
                            nearbyFriends = nearbyFriends,
                            onShareLocation = { showShareLocationDialog = true }
                        )
                    }
                    LocationTab.EVENTS -> {
                        EventsTab(
                            events = nearbyEvents,
                            onCreateEvent = { showCreateEventDialog = true }
                        )
                    }
                    LocationTab.PLACES -> {
                        PlacesTab(
                            locations = popularLocations
                        )
                    }
                    LocationTab.HISTORY -> {
                        HistoryTab(
                            checkIns = locationHistory
                        )
                    }
                }
            }
        }
        
        // Check In Dialog
        if (showCheckInDialog) {
            CheckInDialog(
                locationService = locationService,
                onDismiss = { showCheckInDialog = false },
                onCheckIn = { checkIn ->
                    // Handle check in
                    showCheckInDialog = false
                }
            )
        }
        
        // Share Location Dialog
        if (showShareLocationDialog) {
            ShareLocationDialog(
                locationService = locationService,
                onDismiss = { showShareLocationDialog = false },
                onShare = { share ->
                    // Handle location sharing
                    showShareLocationDialog = false
                }
            )
        }
        
        // Create Event Dialog
        if (showCreateEventDialog) {
            CreateEventDialog(
                locationService = locationService,
                onDismiss = { showCreateEventDialog = false },
                onCreate = { event ->
                    // Handle event creation
                    showCreateEventDialog = false
                }
            )
        }
    }
}

@Composable
fun MapTab(
    nearbyFriends: List<NearbyFriend>,
    popularLocations: List<PopularLocation>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Nearby Friends",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Nearby Friends Section
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nearbyFriends.take(10)) { friend ->
                    NearbyFriendCard(friend = friend)
                }
            }
        }
        
        item {
            Text(
                text = "Popular Places",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Popular Locations
        items(popularLocations.take(10)) { location ->
            PopularLocationCard(location = location)
        }
    }
}

@Composable
fun NearbyFriendCard(friend: NearbyFriend) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = friend.avatar,
                    contentDescription = "Friend Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                
                // Online indicator
                if (friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(LimeGreen, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = friend.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${String.format("%.1f", friend.distance)} km",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Text(
                text = friend.status,
                fontSize = 10.sp,
                color = VividBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PopularLocationCard(location: PopularLocation) {
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
                model = location.photo,
                contentDescription = "Location Photo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = location.address,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = location.category,
                        fontSize = 10.sp,
                        color = ElectricPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(ElectricPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format("%.1f", location.rating),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.1f", location.distance)} km",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${location.checkInCount} check-ins",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "Navigate",
                    tint = VividBlue
                )
            }
        }
    }
}

@Composable
fun FriendsTab(
    nearbyFriends: List<NearbyFriend>,
    onShareLocation: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Friends",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onShareLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share Location",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = Color.White)
                }
            }
        }
        
        items(nearbyFriends) { friend ->
            FriendCard(friend = friend)
        }
    }
}

@Composable
fun FriendCard(friend: NearbyFriend) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = friend.avatar,
                    contentDescription = "Friend Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                
                if (friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(LimeGreen, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = friend.status,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Text(
                    text = "${String.format("%.1f", friend.distance)} km away",
                    fontSize = 12.sp,
                    color = VividBlue,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "At ${friend.currentLocation.name}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Message,
                        contentDescription = "Message",
                        tint = ElectricPurple
                    )
                }
                
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Navigate",
                        tint = VividBlue
                    )
                }
            }
        }
    }
}

@Composable
fun EventsTab(
    events: List<LocationEvent>,
    onCreateEvent: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Events",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = onCreateEvent,
                    colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Event",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", color = Color.White)
                }
            }
        }
        
        items(events) { event ->
            EventCard(event = event)
        }
    }
}

@Composable
fun EventCard(event: LocationEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        text = event.organizer,
                        fontSize = 12.sp,
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
                    Text(
                        text = "📍 ${event.location.name}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "👥 ${event.attendees}/${event.maxAttendees}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "💰 ${if (event.price == 0.0) "Free" else "$${String.format("%.2f", event.price)}"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text(
                        text = "Join Event",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlacesTab(locations: List<PopularLocation>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Popular Places",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(locations) { location ->
            PopularLocationCard(location = location)
        }
    }
}

@Composable
fun HistoryTab(checkIns: List<CheckIn>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Check-in History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(checkIns) { checkIn ->
            CheckInHistoryCard(checkIn = checkIn)
        }
    }
}

@Composable
fun CheckInHistoryCard(checkIn: CheckIn) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = checkIn.location.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = checkIn.location.address,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (checkIn.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = checkIn.message,
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = checkIn.location.category,
                        fontSize = 10.sp,
                        color = VividBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(VividBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = formatTimestamp(checkIn.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    if (checkIn.likes > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Likes",
                                tint = Color.Red,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${checkIn.likes}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "Navigate",
                    tint = VividBlue
                )
            }
        }
    }
}

@Composable
fun CheckInDialog(
    locationService: LocationService,
    onDismiss: () -> Unit,
    onCheckIn: (CheckIn) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check In") },
        text = {
            Column {
                Text(
                    text = "Central Park, New York",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("What's happening?") },
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
                    Text("Make check-in public")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val result = locationService.checkIn(
                            userId = "current_user",
                            location = Location(
                                id = "location_1",
                                name = "Central Park",
                                address = "New York, NY",
                                coordinates = Coordinates(40.7829, -73.9654),
                                category = "Park",
                                rating = 4.5
                            ),
                            message = message,
                            isPublic = isPublic
                        )
                        
                        if (result.success && result.checkIn != null) {
                            onCheckIn(result.checkIn!!)
                        }
                    }
                }
            ) {
                Text("Check In")
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
fun ShareLocationDialog(
    locationService: LocationService,
    onDismiss: () -> Unit,
    onShare: (LocationShare) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Location") },
        text = {
            Column {
                Text(
                    text = "Share your current location with friends",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Duration: 1 hour",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Text(
                    text = "Shared with: All friends",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val result = locationService.shareLocation(
                            userId = "current_user",
                            location = Location(
                                id = "current_location",
                                name = "Current Location",
                                address = "New York, NY",
                                coordinates = Coordinates(40.7128, -74.0060),
                                category = "Current",
                                rating = 0.0
                            ),
                            friendIds = listOf("friend_1", "friend_2", "friend_3"),
                            duration = 3600
                        )
                        
                        if (result.success && result.share != null) {
                            onShare(result.share!!)
                        }
                    }
                }
            ) {
                Text("Share Location")
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
fun CreateEventDialog(
    locationService: LocationService,
    onDismiss: () -> Unit,
    onCreate: (LocationEvent) -> Unit
) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventCategory by remember { mutableStateOf("Social") }
    var maxAttendees by remember { mutableStateOf("50") }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Event") },
        text = {
            Column {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val eventData = LocationEventData(
                            name = eventName,
                            description = eventDescription,
                            location = Location(
                                id = "event_location",
                                name = "Central Park",
                                address = "New York, NY",
                                coordinates = Coordinates(40.7829, -73.9654),
                                category = "Park",
                                rating = 4.5
                            ),
                            startTime = System.currentTimeMillis() + 86400000L, // Tomorrow
                            endTime = System.currentTimeMillis() + 86400000L + 7200000L, // +2 hours
                            category = eventCategory,
                            maxAttendees = maxAttendees.toIntOrNull() ?: 50,
                            isPublic = true,
                            coverImage = "https://picsum.photos/400/300?id=${UUID.randomUUID()}",
                            tags = listOf("Community", "Fun"),
                            price = 0.0
                        )
                        
                        val event = locationService.createLocationEvent(eventData, "current_user")
                        onCreate(event)
                    }
                },
                enabled = eventName.isNotBlank()
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

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

enum class LocationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MAP("Map", Icons.Filled.Map),
    FRIENDS("Friends", Icons.Filled.People),
    EVENTS("Events", Icons.Filled.Event),
    PLACES("Places", Icons.Filled.Place),
    HISTORY("History", Icons.Filled.History)
}