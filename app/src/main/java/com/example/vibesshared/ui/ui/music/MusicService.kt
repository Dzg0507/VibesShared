package com.example.vibesshared.ui.ui.music

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.util.*

/**
 * Music Service for Vibes app
 * Provides music streaming, collaborative playlists, karaoke mode, and music sharing
 */
class MusicService(private val context: Context) {
    
    companion object {
        private const val TAG = "MusicService"
    }
    
    /**
     * Available music categories
     */
    val musicCategories = listOf(
        MusicCategory("trending", "Trending Now", "🔥"),
        MusicCategory("pop", "Pop", "🎵"),
        MusicCategory("rock", "Rock", "🎸"),
        MusicCategory("hip_hop", "Hip Hop", "🎤"),
        MusicCategory("electronic", "Electronic", "🎧"),
        MusicCategory("jazz", "Jazz", "🎷"),
        MusicCategory("classical", "Classical", "🎼"),
        MusicCategory("country", "Country", "🤠"),
        MusicCategory("rnb", "R&B", "💫"),
        MusicCategory("latin", "Latin", "🌶️")
    )
    
    /**
     * Get trending songs
     */
    suspend fun getTrendingSongs(): List<Song> {
        return try {
            delay(600) // Simulate API call
            
            val songs = (1..20).map { index ->
                Song(
                    id = "song_$index",
                    title = generateSongTitle(),
                    artist = generateArtistName(),
                    album = generateAlbumName(),
                    duration = (180..300).random(), // seconds
                    genre = musicCategories.random().name,
                    coverUrl = "https://picsum.photos/300/300?id=${index + 100}",
                    audioUrl = "https://example.com/audio/song_$index.mp3",
                    releaseDate = System.currentTimeMillis() - (index * 86400000L),
                    playCount = (1000..10000000).random(),
                    likes = (100..100000).random(),
                    isLiked = (1..4).random() == 1,
                    isPlaying = false,
                    lyrics = generateLyrics(),
                    mood = listOf("Happy", "Energetic", "Chill", "Romantic", "Sad", "Motivational").random(),
                    tempo = (60..180).random(),
                    key = listOf("C", "D", "E", "F", "G", "A", "B").random(),
                    bpm = (60..180).random()
                )
            }
            
            songs.sortedByDescending { it.playCount }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting trending songs", e)
            emptyList()
        }
    }
    
    /**
     * Search for songs
     */
    suspend fun searchSongs(query: String): List<Song> {
        return try {
            delay(400) // Simulate search
            
            val allSongs = getTrendingSongs()
            allSongs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true) ||
                song.genre.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching songs", e)
            emptyList()
        }
    }
    
    /**
     * Get songs by category
     */
    suspend fun getSongsByCategory(category: String): List<Song> {
        return try {
            delay(500) // Simulate API call
            
            val allSongs = getTrendingSongs()
            allSongs.filter { it.genre == category }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting songs by category", e)
            emptyList()
        }
    }
    
    /**
     * Create a playlist
     */
    suspend fun createPlaylist(
        name: String,
        description: String,
        isPublic: Boolean,
        creatorId: String
    ): Playlist {
        return try {
            delay(300) // Simulate playlist creation
            
            Playlist(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                creatorId = creatorId,
                isPublic = isPublic,
                songs = emptyList(),
                coverUrl = "https://picsum.photos/300/300?id=${UUID.randomUUID()}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                playCount = 0,
                likes = 0,
                followers = 0,
                tags = listOf("Custom", "Personal"),
                isCollaborative = false,
                contributors = listOf(creatorId)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating playlist", e)
            throw e
        }
    }
    
    /**
     * Get user's playlists
     */
    suspend fun getUserPlaylists(userId: String): List<Playlist> {
        return try {
            delay(400) // Simulate API call
            
            val playlists = listOf(
                Playlist(
                    id = "playlist_1",
                    name = "My Favorites",
                    description = "All my favorite songs",
                    creatorId = userId,
                    isPublic = false,
                    songs = emptyList(),
                    coverUrl = "https://picsum.photos/300/300?id=1",
                    createdAt = System.currentTimeMillis() - 86400000L,
                    updatedAt = System.currentTimeMillis(),
                    playCount = 150,
                    likes = 5,
                    followers = 2,
                    tags = listOf("Favorites"),
                    isCollaborative = false,
                    contributors = listOf(userId)
                ),
                Playlist(
                    id = "playlist_2",
                    name = "Workout Mix",
                    description = "High energy songs for workouts",
                    creatorId = userId,
                    isPublic = true,
                    songs = emptyList(),
                    coverUrl = "https://picsum.photos/300/300?id=2",
                    createdAt = System.currentTimeMillis() - 172800000L,
                    updatedAt = System.currentTimeMillis(),
                    playCount = 89,
                    likes = 12,
                    followers = 8,
                    tags = listOf("Workout", "Energy"),
                    isCollaborative = false,
                    contributors = listOf(userId)
                ),
                Playlist(
                    id = "playlist_3",
                    name = "Chill Vibes",
                    description = "Relaxing music for chill moments",
                    creatorId = userId,
                    isPublic = true,
                    songs = emptyList(),
                    coverUrl = "https://picsum.photos/300/300?id=3",
                    createdAt = System.currentTimeMillis() - 259200000L,
                    updatedAt = System.currentTimeMillis(),
                    playCount = 234,
                    likes = 18,
                    followers = 15,
                    tags = listOf("Chill", "Relaxing"),
                    isCollaborative = true,
                    contributors = listOf(userId, "friend_1", "friend_2")
                )
            )
            
            playlists
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user playlists", e)
            emptyList()
        }
    }
    
    /**
     * Add song to playlist
     */
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean {
        return try {
            delay(200) // Simulate adding song
            Log.d(TAG, "Added song $songId to playlist $playlistId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding song to playlist", e)
            false
        }
    }
    
    /**
     * Start karaoke session
     */
    suspend fun startKaraokeSession(
        songId: String,
        userId: String,
        isPublic: Boolean = false
    ): KaraokeSession {
        return try {
            delay(500) // Simulate karaoke setup
            
            KaraokeSession(
                id = UUID.randomUUID().toString(),
                songId = songId,
                hostId = userId,
                participants = listOf(KaraokeParticipant(userId, "Host", true)),
                startTime = System.currentTimeMillis(),
                isPublic = isPublic,
                status = KaraokeStatus.ACTIVE,
                lyrics = generateLyrics(),
                currentLyricIndex = 0,
                scores = mutableMapOf()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error starting karaoke session", e)
            throw e
        }
    }
    
    /**
     * Join karaoke session
     */
    suspend fun joinKaraokeSession(sessionId: String, userId: String): Boolean {
        return try {
            delay(200) // Simulate joining session
            Log.d(TAG, "User $userId joined karaoke session $sessionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error joining karaoke session", e)
            false
        }
    }
    
    /**
     * Get karaoke sessions
     */
    suspend fun getKaraokeSessions(): List<KaraokeSession> {
        return try {
            delay(400) // Simulate API call
            
            val sessions = (1..10).map { index ->
                KaraokeSession(
                    id = "karaoke_$index",
                    songId = "song_${(1..20).random()}",
                    hostId = "user_$index",
                    participants = (1..5).map { 
                        KaraokeParticipant("user_$it", "User$it", it == 1)
                    },
                    startTime = System.currentTimeMillis() - (index * 600000L),
                    isPublic = true,
                    status = listOf(KaraokeStatus.ACTIVE, KaraokeStatus.WAITING).random(),
                    lyrics = generateLyrics(),
                    currentLyricIndex = (0..50).random(),
                    scores = mutableMapOf()
                )
            }
            
            sessions
        } catch (e: Exception) {
            Log.e(TAG, "Error getting karaoke sessions", e)
            emptyList()
        }
    }
    
    /**
     * Share music with friends
     */
    suspend fun shareMusic(
        songId: String,
        fromUserId: String,
        toUserIds: List<String>,
        message: String = ""
    ): ShareResult {
        return try {
            delay(300) // Simulate sharing
            
            val shareResult = ShareResult(
                success = true,
                sharesSent = toUserIds.size,
                message = "Music shared successfully!",
                shareId = UUID.randomUUID().toString()
            )
            
            Log.d(TAG, "Music shared: $songId to ${toUserIds.size} users")
            shareResult
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing music", e)
            ShareResult(false, 0, "Failed to share music", "")
        }
    }
    
    /**
     * Get music recommendations
     */
    suspend fun getRecommendations(userId: String): List<Song> {
        return try {
            delay(500) // Simulate recommendation engine
            
            val allSongs = getTrendingSongs()
            // Simulate personalized recommendations based on user preferences
            allSongs.shuffled().take(10)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations", e)
            emptyList()
        }
    }
    
    /**
     * Like/unlike a song
     */
    suspend fun toggleLike(songId: String, userId: String): LikeResult {
        return try {
            delay(200) // Simulate like toggle
            
            val isLiked = (1..2).random() == 1 // Simulate random like state
            LikeResult(
                success = true,
                isLiked = isLiked,
                newLikeCount = (100..100000).random()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
            LikeResult(false, false, 0)
        }
    }
    
    /**
     * Get music analytics
     */
    suspend fun getMusicAnalytics(userId: String): MusicAnalytics {
        return try {
            delay(400) // Simulate analytics calculation
            
            MusicAnalytics(
                userId = userId,
                totalSongsPlayed = (500..5000).random(),
                totalPlayTime = (3600..86400).random(), // seconds
                favoriteGenre = musicCategories.random().name,
                topArtist = generateArtistName(),
                topSong = generateSongTitle(),
                playlistsCreated = (5..50).random(),
                songsLiked = (50..500).random(),
                karaokeSessions = (10..100).random(),
                averageSessionDuration = (300..1800).random()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting music analytics", e)
            MusicAnalytics(userId, 0, 0, "", "", "", 0, 0, 0, 0)
        }
    }
    
    /**
     * Create collaborative playlist
     */
    suspend fun createCollaborativePlaylist(
        name: String,
        description: String,
        creatorId: String,
        collaboratorIds: List<String>
    ): Playlist {
        return try {
            delay(400) // Simulate collaborative playlist creation
            
            Playlist(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                creatorId = creatorId,
                isPublic = true,
                songs = emptyList(),
                coverUrl = "https://picsum.photos/300/300?id=${UUID.randomUUID()}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                playCount = 0,
                likes = 0,
                followers = 0,
                tags = listOf("Collaborative", "Group"),
                isCollaborative = true,
                contributors = listOf(creatorId) + collaboratorIds
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating collaborative playlist", e)
            throw e
        }
    }
    
    private fun generateSongTitle(): String {
        val titles = listOf(
            "Midnight Dreams", "Electric Pulse", "Golden Hour", "Neon Lights",
            "Ocean Waves", "City Lights", "Starlight", "Thunderstorm",
            "Summer Breeze", "Winter Nights", "Fire and Ice", "Digital Love",
            "Cosmic Dance", "Urban Legend", "Silent Storm", "Bright Future",
            "Lost in Time", "Rising Sun", "Moonlight Sonata", "Digital Dreams"
        )
        return titles.random()
    }
    
    private fun generateArtistName(): String {
        val artists = listOf(
            "Luna Star", "Neon Pulse", "Cosmic Wave", "Electric Soul",
            "Midnight Runner", "Golden Echo", "Thunder Beat", "Ocean Drive",
            "City Lights", "Starlight Band", "Fire Storm", "Digital Dreamers",
            "Cosmic Crew", "Urban Legends", "Silent Symphony", "Bright Future",
            "Lost Time", "Rising Phoenix", "Moonlight Collective", "Digital Hearts"
        )
        return artists.random()
    }
    
    private fun generateAlbumName(): String {
        val albums = listOf(
            "Digital Dreams", "Cosmic Journey", "Electric Nights", "Golden Memories",
            "Midnight Stories", "Ocean Waves", "City Pulse", "Starlight Sessions",
            "Thunder & Lightning", "Fire & Ice", "Lost & Found", "Rising & Falling",
            "Moonlight & Memories", "Digital & Analog", "Urban & Rural", "Silent & Loud"
        )
        return albums.random()
    }
    
    private fun generateLyrics(): String {
        return """
            [Verse 1]
            In the city lights, we dance tonight
            Feeling the rhythm, everything's right
            Digital dreams in the neon glow
            This is our moment, let the music flow
            
            [Chorus]
            We're living in the future
            Dancing to the beat
            This is our story
            Making memories sweet
            
            [Verse 2]
            Through the noise and the crowd
            We stand out, we're proud
            Electric pulse in our veins
            Breaking free from the chains
            
            [Chorus]
            We're living in the future
            Dancing to the beat
            This is our story
            Making memories sweet
        """.trimIndent()
    }
}

// Data classes for music
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int, // seconds
    val genre: String,
    val coverUrl: String,
    val audioUrl: String,
    val releaseDate: Long,
    val playCount: Int,
    val likes: Int,
    val isLiked: Boolean,
    val isPlaying: Boolean,
    val lyrics: String,
    val mood: String,
    val tempo: Int,
    val key: String,
    val bpm: Int
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val creatorId: String,
    val isPublic: Boolean,
    val songs: List<Song>,
    val coverUrl: String,
    val createdAt: Long,
    val updatedAt: Long,
    val playCount: Int,
    val likes: Int,
    val followers: Int,
    val tags: List<String>,
    val isCollaborative: Boolean,
    val contributors: List<String>
)

data class MusicCategory(
    val name: String,
    val displayName: String,
    val icon: String
)

data class KaraokeSession(
    val id: String,
    val songId: String,
    val hostId: String,
    val participants: List<KaraokeParticipant>,
    val startTime: Long,
    val isPublic: Boolean,
    val status: KaraokeStatus,
    val lyrics: String,
    val currentLyricIndex: Int,
    val scores: MutableMap<String, Int>
)

data class KaraokeParticipant(
    val id: String,
    val name: String,
    val isHost: Boolean
)

data class ShareResult(
    val success: Boolean,
    val sharesSent: Int,
    val message: String,
    val shareId: String
)

data class LikeResult(
    val success: Boolean,
    val isLiked: Boolean,
    val newLikeCount: Int
)

data class MusicAnalytics(
    val userId: String,
    val totalSongsPlayed: Int,
    val totalPlayTime: Int, // seconds
    val favoriteGenre: String,
    val topArtist: String,
    val topSong: String,
    val playlistsCreated: Int,
    val songsLiked: Int,
    val karaokeSessions: Int,
    val averageSessionDuration: Int // seconds
)

enum class KaraokeStatus {
    WAITING, ACTIVE, FINISHED, CANCELLED
}