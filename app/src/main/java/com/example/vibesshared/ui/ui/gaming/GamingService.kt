package com.example.vibesshared.ui.ui.gaming

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.util.*

/**
 * Gaming Service for Vibes app
 * Provides mini-games, leaderboards, achievements, and social gaming features
 */
class GamingService(private val context: Context) {
    
    companion object {
        private const val TAG = "GamingService"
    }
    
    /**
     * Available mini-games
     */
    val availableGames = listOf(
        MiniGame(
            id = "emoji_match",
            name = "Emoji Match",
            description = "Match the emoji pairs in record time!",
            icon = "🎯",
            category = GameCategory.PUZZLE,
            maxPlayers = 1,
            difficulty = Difficulty.EASY,
            estimatedDuration = 60, // seconds
            highScore = 0
        ),
        MiniGame(
            id = "word_race",
            name = "Word Race",
            description = "Type words as fast as you can!",
            icon = "🏃",
            category = GameCategory.ARCADE,
            maxPlayers = 4,
            difficulty = Difficulty.MEDIUM,
            estimatedDuration = 120,
            highScore = 0
        ),
        MiniGame(
            id = "memory_cards",
            name = "Memory Cards",
            description = "Find matching pairs in this memory game",
            icon = "🧠",
            category = GameCategory.PUZZLE,
            maxPlayers = 2,
            difficulty = Difficulty.EASY,
            estimatedDuration = 90,
            highScore = 0
        ),
        MiniGame(
            id = "trivia_challenge",
            name = "Trivia Challenge",
            description = "Answer questions and climb the leaderboard!",
            icon = "🧩",
            category = GameCategory.KNOWLEDGE,
            maxPlayers = 8,
            difficulty = Difficulty.MEDIUM,
            estimatedDuration = 300,
            highScore = 0
        ),
        MiniGame(
            id = "reaction_test",
            name = "Reaction Test",
            description = "Test your reflexes and reaction time",
            icon = "⚡",
            category = GameCategory.ARCADE,
            maxPlayers = 1,
            difficulty = Difficulty.HARD,
            estimatedDuration = 30,
            highScore = 0
        ),
        MiniGame(
            id = "emoji_story",
            name = "Emoji Story",
            description = "Create stories using only emojis!",
            icon = "📖",
            category = GameCategory.CREATIVE,
            maxPlayers = 6,
            difficulty = Difficulty.EASY,
            estimatedDuration = 180,
            highScore = 0
        )
    )
    
    /**
     * Start a mini-game session
     */
    suspend fun startGame(gameId: String, playerId: String): GameSession {
        return try {
            delay(500) // Simulate game initialization
            
            val game = availableGames.find { it.id == gameId } ?: throw Exception("Game not found")
            
            val session = GameSession(
                id = UUID.randomUUID().toString(),
                gameId = gameId,
                players = listOf(GamePlayer(playerId, "Player", 0, false)),
                startTime = System.currentTimeMillis(),
                status = GameStatus.ACTIVE,
                currentLevel = 1,
                score = 0,
                timeRemaining = game.estimatedDuration
            )
            
            Log.d(TAG, "Game session started: ${session.id}")
            session
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game", e)
            throw e
        }
    }
    
    /**
     * Join a multiplayer game
     */
    suspend fun joinGame(sessionId: String, playerId: String): Boolean {
        return try {
            delay(300) // Simulate joining game
            Log.d(TAG, "Player $playerId joined game session $sessionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error joining game", e)
            false
        }
    }
    
    /**
     * Update game score
     */
    suspend fun updateScore(sessionId: String, playerId: String, score: Int): GameResult {
        return try {
            delay(100) // Simulate score update
            
            val result = GameResult(
                sessionId = sessionId,
                playerId = playerId,
                score = score,
                rank = 1,
                isNewHighScore = score > 1000,
                achievements = generateAchievements(score),
                coinsEarned = score / 10
            )
            
            Log.d(TAG, "Score updated for player $playerId: $score")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error updating score", e)
            throw e
        }
    }
    
    /**
     * Get leaderboard for a game
     */
    suspend fun getLeaderboard(gameId: String, timeFrame: LeaderboardTimeFrame): Leaderboard {
        return try {
            delay(400) // Simulate leaderboard fetch
            
            val players = (1..50).map { index ->
                LeaderboardEntry(
                    rank = index,
                    playerId = "player_$index",
                    playerName = "Player$index",
                    score = (1000..50000).random(),
                    avatar = "https://picsum.photos/100/100?id=$index",
                    isCurrentUser = index == 1
                )
            }
            
            Leaderboard(
                gameId = gameId,
                timeFrame = timeFrame,
                entries = players,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting leaderboard", e)
            Leaderboard(gameId, timeFrame, emptyList(), System.currentTimeMillis())
        }
    }
    
    /**
     * Get user achievements
     */
    suspend fun getUserAchievements(userId: String): List<Achievement> {
        return try {
            delay(300) // Simulate achievements fetch
            
            val achievements = listOf(
                Achievement(
                    id = "first_game",
                    title = "First Steps",
                    description = "Play your first game",
                    icon = "🎮",
                    category = AchievementCategory.GAMING,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L,
                    rarity = AchievementRarity.COMMON
                ),
                Achievement(
                    id = "score_master",
                    title = "Score Master",
                    description = "Reach 10,000 points in any game",
                    icon = "🏆",
                    category = AchievementCategory.SCORING,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 43200000L,
                    rarity = AchievementRarity.RARE
                ),
                Achievement(
                    id = "social_player",
                    title = "Social Player",
                    description = "Play with 10 different friends",
                    icon = "👥",
                    category = AchievementCategory.SOCIAL,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.EPIC
                ),
                Achievement(
                    id = "speed_demon",
                    title = "Speed Demon",
                    description = "Complete a game in under 30 seconds",
                    icon = "⚡",
                    category = AchievementCategory.SPEED,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.LEGENDARY
                ),
                Achievement(
                    id = "daily_player",
                    title = "Daily Player",
                    description = "Play games for 7 consecutive days",
                    icon = "📅",
                    category = AchievementCategory.CONSISTENCY,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 604800000L,
                    rarity = AchievementRarity.RARE
                )
            )
            
            achievements
        } catch (e: Exception) {
            Log.e(TAG, "Error getting achievements", e)
            emptyList()
        }
    }
    
    /**
     * Create a gaming tournament
     */
    suspend fun createTournament(tournamentConfig: TournamentConfig): Tournament {
        return try {
            delay(800) // Simulate tournament creation
            
            val tournament = Tournament(
                id = UUID.randomUUID().toString(),
                name = tournamentConfig.name,
                description = tournamentConfig.description,
                gameId = tournamentConfig.gameId,
                maxParticipants = tournamentConfig.maxParticipants,
                entryFee = tournamentConfig.entryFee,
                prizePool = tournamentConfig.prizePool,
                startTime = tournamentConfig.startTime,
                endTime = tournamentConfig.endTime,
                status = TournamentStatus.REGISTRATION_OPEN,
                participants = emptyList(),
                creatorId = tournamentConfig.creatorId
            )
            
            Log.d(TAG, "Tournament created: ${tournament.id}")
            tournament
        } catch (e: Exception) {
            Log.e(TAG, "Error creating tournament", e)
            throw e
        }
    }
    
    /**
     * Get available tournaments
     */
    suspend fun getTournaments(): List<Tournament> {
        return try {
            delay(500) // Simulate tournaments fetch
            
            val games = availableGames.take(3)
            val tournaments = (1..10).map { index ->
                Tournament(
                    id = "tournament_$index",
                    name = "${games.random().name} Championship #$index",
                    description = "Compete for glory and prizes!",
                    gameId = games.random().id,
                    maxParticipants = (8..32).random(),
                    entryFee = (0..50).random(),
                    prizePool = (100..1000).random(),
                    startTime = System.currentTimeMillis() + (index * 3600000L),
                    endTime = System.currentTimeMillis() + (index * 3600000L) + 1800000L,
                    status = listOf(TournamentStatus.REGISTRATION_OPEN, TournamentStatus.ACTIVE, TournamentStatus.COMPLETED).random(),
                    participants = (1..16).random(),
                    creatorId = "creator_$index"
                )
            }
            
            tournaments
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tournaments", e)
            emptyList()
        }
    }
    
    /**
     * Get gaming statistics for user
     */
    suspend fun getUserStats(userId: String): UserGamingStats {
        return try {
            delay(400) // Simulate stats calculation
            
            UserGamingStats(
                userId = userId,
                totalGamesPlayed = (50..500).random(),
                totalScore = (10000..500000).random(),
                averageScore = (100..1000).random(),
                bestScore = (5000..25000).random(),
                favoriteGame = availableGames.random().name,
                totalPlayTime = (3600..86400).random(), // seconds
                achievementsUnlocked = (5..25).random(),
                rank = (1..100).random(),
                coinsEarned = (100..5000).random(),
                tournamentsWon = (0..5).random()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user stats", e)
            UserGamingStats(userId, 0, 0, 0, 0, "", 0, 0, 0, 0, 0)
        }
    }
    
    /**
     * Get daily challenges
     */
    suspend fun getDailyChallenges(): List<DailyChallenge> {
        return try {
            delay(300) // Simulate challenges fetch
            
            val challenges = listOf(
                DailyChallenge(
                    id = "daily_score",
                    title = "Score Master",
                    description = "Score 5000 points in any game",
                    reward = 100,
                    progress = 0,
                    target = 5000,
                    isCompleted = false,
                    expiresAt = System.currentTimeMillis() + 86400000L // 24 hours
                ),
                DailyChallenge(
                    id = "daily_games",
                    title = "Game Enthusiast",
                    description = "Play 3 different games today",
                    reward = 50,
                    progress = 1,
                    target = 3,
                    isCompleted = false,
                    expiresAt = System.currentTimeMillis() + 86400000L
                ),
                DailyChallenge(
                    id = "daily_social",
                    title = "Social Butterfly",
                    description = "Play with 2 friends today",
                    reward = 75,
                    progress = 0,
                    target = 2,
                    isCompleted = false,
                    expiresAt = System.currentTimeMillis() + 86400000L
                )
            )
            
            challenges
        } catch (e: Exception) {
            Log.e(TAG, "Error getting daily challenges", e)
            emptyList()
        }
    }
    
    /**
     * Redeem coins for rewards
     */
    suspend fun redeemReward(rewardId: String, userId: String, coinsSpent: Int): RewardResult {
        return try {
            delay(200) // Simulate reward redemption
            
            val reward = RewardResult(
                success = true,
                rewardId = rewardId,
                rewardName = "Custom Avatar Frame",
                coinsSpent = coinsSpent,
                message = "Reward redeemed successfully!"
            )
            
            Log.d(TAG, "Reward redeemed: $rewardId for $coinsSpent coins")
            reward
        } catch (e: Exception) {
            Log.e(TAG, "Error redeeming reward", e)
            RewardResult(false, "", "", 0, "Failed to redeem reward")
        }
    }
    
    private fun generateAchievements(score: Int): List<String> {
        val achievements = mutableListOf<String>()
        
        when {
            score >= 10000 -> achievements.add("Score Master")
            score >= 5000 -> achievements.add("Halfway Hero")
            score >= 1000 -> achievements.add("Getting Started")
        }
        
        return achievements
    }
}

// Data classes for gaming
data class MiniGame(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: GameCategory,
    val maxPlayers: Int,
    val difficulty: Difficulty,
    val estimatedDuration: Int, // seconds
    val highScore: Int
)

data class GameSession(
    val id: String,
    val gameId: String,
    val players: List<GamePlayer>,
    val startTime: Long,
    val status: GameStatus,
    val currentLevel: Int,
    val score: Int,
    val timeRemaining: Int
)

data class GamePlayer(
    val id: String,
    val name: String,
    val score: Int,
    val isReady: Boolean
)

data class GameResult(
    val sessionId: String,
    val playerId: String,
    val score: Int,
    val rank: Int,
    val isNewHighScore: Boolean,
    val achievements: List<String>,
    val coinsEarned: Int
)

data class Leaderboard(
    val gameId: String,
    val timeFrame: LeaderboardTimeFrame,
    val entries: List<LeaderboardEntry>,
    val lastUpdated: Long
)

data class LeaderboardEntry(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val score: Int,
    val avatar: String,
    val isCurrentUser: Boolean
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val rarity: AchievementRarity
)

data class Tournament(
    val id: String,
    val name: String,
    val description: String,
    val gameId: String,
    val maxParticipants: Int,
    val entryFee: Int,
    val prizePool: Int,
    val startTime: Long,
    val endTime: Long,
    val status: TournamentStatus,
    val participants: Int,
    val creatorId: String
)

data class TournamentConfig(
    val name: String,
    val description: String,
    val gameId: String,
    val maxParticipants: Int,
    val entryFee: Int,
    val prizePool: Int,
    val startTime: Long,
    val endTime: Long,
    val creatorId: String
)

data class UserGamingStats(
    val userId: String,
    val totalGamesPlayed: Int,
    val totalScore: Int,
    val averageScore: Int,
    val bestScore: Int,
    val favoriteGame: String,
    val totalPlayTime: Int, // seconds
    val achievementsUnlocked: Int,
    val rank: Int,
    val coinsEarned: Int,
    val tournamentsWon: Int
)

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int, // coins
    val progress: Int,
    val target: Int,
    val isCompleted: Boolean,
    val expiresAt: Long
)

data class RewardResult(
    val success: Boolean,
    val rewardId: String,
    val rewardName: String,
    val coinsSpent: Int,
    val message: String
)

enum class GameCategory {
    PUZZLE, ARCADE, KNOWLEDGE, CREATIVE, SPORTS, RACING
}

enum class Difficulty {
    EASY, MEDIUM, HARD
}

enum class GameStatus {
    WAITING, ACTIVE, PAUSED, FINISHED
}

enum class LeaderboardTimeFrame {
    DAILY, WEEKLY, MONTHLY, ALL_TIME
}

enum class AchievementCategory {
    GAMING, SCORING, SOCIAL, SPEED, CONSISTENCY, TOURNAMENT
}

enum class AchievementRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

enum class TournamentStatus {
    REGISTRATION_OPEN, ACTIVE, COMPLETED, CANCELLED
}