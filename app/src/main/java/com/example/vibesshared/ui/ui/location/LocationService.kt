package com.example.vibesshared.ui.ui.location

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.util.*

/**
 * Location Service for Vibes app
 * Provides location-based features: check-ins, nearby friends, location sharing, and events
 */
class LocationService(private val context: Context) {
    
    companion object {
        private const val TAG = "LocationService"
    }
    
    /**
     * Check in at a location
     */
    suspend fun checkIn(
        userId: String,
        location: Location,
        message: String = "",
        isPublic: Boolean = true
    ): CheckInResult {
        return try {
            delay(500) // Simulate check-in processing
            
            val checkIn = CheckIn(
                id = UUID.randomUUID().toString(),
                userId = userId,
                location = location,
                message = message,
                timestamp = System.currentTimeMillis(),
                isPublic = isPublic,
                likes = 0,
                comments = emptyList(),
                tags = extractTags(message)
            )
            
            Log.d(TAG, "User $userId checked in at ${location.name}")
            
            CheckInResult(
                success = true,
                checkIn = checkIn,
                message = "Checked in successfully!",
                nearbyFriends = findNearbyFriends(userId, location)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in", e)
            CheckInResult(false, null, "Failed to check in", emptyList())
        }
    }
    
    /**
     * Get nearby friends
     */
    suspend fun getNearbyFriends(userId: String, radius: Double = 5.0): List<NearbyFriend> {
        return try {
            delay(400) // Simulate location search
            
            val friends = (1..10).map { index ->
                NearbyFriend(
                    userId = "friend_$index",
                    name = "Friend$index",
                    avatar = "https://picsum.photos/100/100?id=$index",
                    distance = (0.1..radius).random(),
                    lastSeen = System.currentTimeMillis() - (index * 3600000L),
                    isOnline = (1..3).random() == 1,
                    currentLocation = generateRandomLocation(),
                    status = listOf("Having coffee", "At work", "Shopping", "Studying", "Working out").random()
                )
            }
            
            friends.sortedBy { it.distance }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting nearby friends", e)
            emptyList()
        }
    }
    
    /**
     * Share location with friends
     */
    suspend fun shareLocation(
        userId: String,
        location: Location,
        friendIds: List<String>,
        duration: Int = 3600 // 1 hour in seconds
    ): LocationShareResult {
        return try {
            delay(300) // Simulate location sharing
            
            val share = LocationShare(
                id = UUID.randomUUID().toString(),
                sharerId = userId,
                location = location,
                sharedWith = friendIds,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis() + (duration * 1000L),
                isActive = true,
                message = "Sharing my location with you!"
            )
            
            Log.d(TAG, "User $userId shared location with ${friendIds.size} friends")
            
            LocationShareResult(
                success = true,
                share = share,
                message = "Location shared successfully!"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing location", e)
            LocationShareResult(false, null, "Failed to share location")
        }
    }
    
    /**
     * Get nearby events
     */
    suspend fun getNearbyEvents(
        location: Location,
        radius: Double = 10.0
    ): List<LocationEvent> {
        return try {
            delay(600) // Simulate event search
            
            val events = (1..15).map { index ->
                LocationEvent(
                    id = "event_$index",
                    name = generateEventName(),
                    description = generateEventDescription(),
                    location = generateRandomLocation(),
                    startTime = System.currentTimeMillis() + (index * 3600000L),
                    endTime = System.currentTimeMillis() + (index * 3600000L) + 7200000L,
                    category = listOf("Social", "Music", "Sports", "Food", "Art", "Tech", "Fitness").random(),
                    attendees = (5..200).random(),
                    maxAttendees = (50..500).random(),
                    isPublic = true,
                    organizer = "Organizer$index",
                    coverImage = "https://picsum.photos/400/300?id=${index + 200}",
                    tags = listOf("Local", "Fun", "Community"),
                    price = if ((1..3).random() == 1) 0.0 else (10.0..100.0).random(),
                    distance = (0.1..radius).random()
                )
            }
            
            events.sortedBy { it.distance }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting nearby events", e)
            emptyList()
        }
    }
    
    /**
     * Get popular locations
     */
    suspend fun getPopularLocations(): List<PopularLocation> {
        return try {
            delay(400) // Simulate API call
            
            val locations = (1..20).map { index ->
                PopularLocation(
                    id = "location_$index",
                    name = generateLocationName(),
                    address = generateAddress(),
                    category = listOf("Restaurant", "Cafe", "Park", "Mall", "Museum", "Gym", "Bar", "Library").random(),
                    rating = (3.0..5.0).random(),
                    checkInCount = (50..5000).random(),
                    photo = "https://picsum.photos/300/200?id=${index + 300}",
                    coordinates = generateRandomCoordinates(),
                    distance = (0.1..15.0).random(),
                    isOpen = (1..5).random() != 1,
                    priceRange = (1..4).random(),
                    tags = listOf("Popular", "Trending", "Local Favorite")
                )
            }
            
            locations.sortedByDescending { it.checkInCount }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting popular locations", e)
            emptyList()
        }
    }
    
    /**
     * Create a location-based event
     */
    suspend fun createLocationEvent(
        eventData: LocationEventData,
        organizerId: String
    ): LocationEvent {
        return try {
            delay(500) // Simulate event creation
            
            val event = LocationEvent(
                id = UUID.randomUUID().toString(),
                name = eventData.name,
                description = eventData.description,
                location = eventData.location,
                startTime = eventData.startTime,
                endTime = eventData.endTime,
                category = eventData.category,
                attendees = 1, // Organizer
                maxAttendees = eventData.maxAttendees,
                isPublic = eventData.isPublic,
                organizer = "You",
                coverImage = eventData.coverImage,
                tags = eventData.tags,
                price = eventData.price,
                distance = 0.0
            )
            
            Log.d(TAG, "Location event created: ${event.name}")
            event
        } catch (e: Exception) {
            Log.e(TAG, "Error creating location event", e)
            throw e
        }
    }
    
    /**
     * Get location-based recommendations
     */
    suspend fun getLocationRecommendations(
        userId: String,
        location: Location
    ): List<LocationRecommendation> {
        return try {
            delay(400) // Simulate recommendation engine
            
            val recommendations = (1..10).map { index ->
                LocationRecommendation(
                    id = "rec_$index",
                    type = listOf("Restaurant", "Activity", "Event", "Friend", "Photo Spot").random(),
                    title = generateRecommendationTitle(),
                    description = generateRecommendationDescription(),
                    location = generateRandomLocation(),
                    distance = (0.1..5.0).random(),
                    rating = (3.0..5.0).random(),
                    image = "https://picsum.photos/200/150?id=${index + 400}",
                    reason = listOf("Popular nearby", "Friends liked", "Matches your interests", "Trending").random(),
                    estimatedTime = (30..240).random() // minutes
                )
            }
            
            recommendations
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations", e)
            emptyList()
        }
    }
    
    /**
     * Get location analytics
     */
    suspend fun getLocationAnalytics(userId: String): LocationAnalytics {
        return try {
            delay(500) // Simulate analytics calculation
            
            LocationAnalytics(
                userId = userId,
                totalCheckIns = (50..500).random(),
                totalDistanceTraveled = (100..5000).random(), // km
                favoriteLocationType = listOf("Restaurant", "Cafe", "Park", "Mall").random(),
                mostVisitedLocation = generateLocationName(),
                averageDistanceFromHome = (5..50).random(),
                placesExplored = (20..200).random(),
                eventsAttended = (5..50).random(),
                friendsMetUp = (10..100).random(),
                totalTimeSpent = (100..1000).random() // hours
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location analytics", e)
            LocationAnalytics(userId, 0, 0.0, "", "", 0.0, 0, 0, 0, 0)
        }
    }
    
    /**
     * Get location history
     */
    suspend fun getLocationHistory(userId: String): List<CheckIn> {
        return try {
            delay(400) // Simulate history fetch
            
            val checkIns = (1..30).map { index ->
                CheckIn(
                    id = "checkin_$index",
                    userId = userId,
                    location = generateRandomLocation(),
                    message = generateCheckInMessage(),
                    timestamp = System.currentTimeMillis() - (index * 86400000L),
                    isPublic = true,
                    likes = (0..50).random(),
                    comments = emptyList(),
                    tags = listOf("Good times", "Amazing food", "Great atmosphere")
                )
            }
            
            checkIns.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location history", e)
            emptyList()
        }
    }
    
    private fun findNearbyFriends(userId: String, location: Location): List<NearbyFriend> {
        // Simulate finding nearby friends
        return getNearbyFriends(userId, 2.0)
    }
    
    private fun extractTags(message: String): List<String> {
        val commonTags = listOf("#goodtimes", "#food", "#friends", "#fun", "#adventure", "#exploring")
        return commonTags.filter { tag -> message.contains(tag, ignoreCase = true) }
    }
    
    private fun generateRandomLocation(): Location {
        return Location(
            id = UUID.randomUUID().toString(),
            name = generateLocationName(),
            address = generateAddress(),
            coordinates = generateRandomCoordinates(),
            category = listOf("Restaurant", "Cafe", "Park", "Mall", "Museum", "Gym").random(),
            rating = (3.0..5.0).random()
        )
    }
    
    private fun generateLocationName(): String {
        val names = listOf(
            "Central Park", "Downtown Cafe", "Riverside Restaurant", "Mountain View Mall",
            "Sunset Beach", "Golden Gate Bridge", "Times Square", "Eiffel Tower Cafe",
            "Blue Moon Bar", "Green Valley Park", "Red Brick Restaurant", "Purple Haze Lounge",
            "Ocean View Hotel", "City Lights Theater", "Garden Plaza", "Harbor Market"
        )
        return names.random()
    }
    
    private fun generateAddress(): String {
        val streets = listOf("Main St", "Oak Ave", "Pine Rd", "Elm St", "Maple Dr", "Cedar Ln")
        val cities = listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia")
        return "${(100..9999).random()} ${streets.random()}, ${cities.random()}"
    }
    
    private fun generateRandomCoordinates(): Coordinates {
        return Coordinates(
            latitude = (25.0..49.0).random(),
            longitude = (-125.0..(-66.0)).random()
        )
    }
    
    private fun generateEventName(): String {
        val events = listOf(
            "Summer Music Festival", "Food Truck Rally", "Art Gallery Opening", "Yoga in the Park",
            "Tech Meetup", "Wine Tasting", "Comedy Night", "Fitness Bootcamp", "Book Club Meeting",
            "Photography Walk", "Cooking Class", "Dance Workshop", "Board Game Night", "Hiking Adventure"
        )
        return events.random()
    }
    
    private fun generateEventDescription(): String {
        val descriptions = listOf(
            "Join us for an amazing event filled with fun activities and great people!",
            "A perfect opportunity to meet new friends and enjoy some quality time.",
            "Don't miss out on this exciting gathering in your neighborhood!",
            "Come and experience something new and memorable with the community.",
            "An event you won't want to miss - great vibes guaranteed!"
        )
        return descriptions.random()
    }
    
    private fun generateRecommendationTitle(): String {
        val titles = listOf(
            "Hidden Gem Restaurant", "Best Coffee in Town", "Perfect Photo Spot",
            "Great Place to Work", "Amazing Sunset View", "Local Favorite Bar",
            "Peaceful Park", "Trending Cafe", "Historic Location", "Fun Activity Center"
        )
        return titles.random()
    }
    
    private fun generateRecommendationDescription(): String {
        val descriptions = listOf(
            "Highly recommended by locals and visitors alike!",
            "Perfect for a relaxing afternoon or evening.",
            "Great atmosphere and friendly staff.",
            "One of the most popular spots in the area.",
            "A must-visit location with amazing reviews."
        )
        return descriptions.random()
    }
    
    private fun generateCheckInMessage(): String {
        val messages = listOf(
            "Having an amazing time here! 😊",
            "Great food and atmosphere! 🍕",
            "Perfect spot for relaxing ☕",
            "Love this place! ❤️",
            "Amazing views from here! 🌅",
            "Best coffee in town! ☕",
            "Great place to catch up with friends 👥",
            "Beautiful location! 📸"
        )
        return messages.random()
    }
}

// Data classes for location features
data class Location(
    val id: String,
    val name: String,
    val address: String,
    val coordinates: Coordinates,
    val category: String,
    val rating: Double
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class CheckIn(
    val id: String,
    val userId: String,
    val location: Location,
    val message: String,
    val timestamp: Long,
    val isPublic: Boolean,
    val likes: Int,
    val comments: List<Comment>,
    val tags: List<String>
)

data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val text: String,
    val timestamp: Long
)

data class CheckInResult(
    val success: Boolean,
    val checkIn: CheckIn?,
    val message: String,
    val nearbyFriends: List<NearbyFriend>
)

data class NearbyFriend(
    val userId: String,
    val name: String,
    val avatar: String,
    val distance: Double, // km
    val lastSeen: Long,
    val isOnline: Boolean,
    val currentLocation: Location,
    val status: String
)

data class LocationShare(
    val id: String,
    val sharerId: String,
    val location: Location,
    val sharedWith: List<String>,
    val startTime: Long,
    val endTime: Long,
    val isActive: Boolean,
    val message: String
)

data class LocationShareResult(
    val success: Boolean,
    val share: LocationShare?,
    val message: String
)

data class LocationEvent(
    val id: String,
    val name: String,
    val description: String,
    val location: Location,
    val startTime: Long,
    val endTime: Long,
    val category: String,
    val attendees: Int,
    val maxAttendees: Int,
    val isPublic: Boolean,
    val organizer: String,
    val coverImage: String,
    val tags: List<String>,
    val price: Double,
    val distance: Double
)

data class LocationEventData(
    val name: String,
    val description: String,
    val location: Location,
    val startTime: Long,
    val endTime: Long,
    val category: String,
    val maxAttendees: Int,
    val isPublic: Boolean,
    val coverImage: String,
    val tags: List<String>,
    val price: Double
)

data class PopularLocation(
    val id: String,
    val name: String,
    val address: String,
    val category: String,
    val rating: Double,
    val checkInCount: Int,
    val photo: String,
    val coordinates: Coordinates,
    val distance: Double,
    val isOpen: Boolean,
    val priceRange: Int, // 1-4 dollar signs
    val tags: List<String>
)

data class LocationRecommendation(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val location: Location,
    val distance: Double,
    val rating: Double,
    val image: String,
    val reason: String,
    val estimatedTime: Int // minutes
)

data class LocationAnalytics(
    val userId: String,
    val totalCheckIns: Int,
    val totalDistanceTraveled: Double, // km
    val favoriteLocationType: String,
    val mostVisitedLocation: String,
    val averageDistanceFromHome: Double, // km
    val placesExplored: Int,
    val eventsAttended: Int,
    val friendsMetUp: Int,
    val totalTimeSpent: Int // hours
)