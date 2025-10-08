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
                tournamentsWon = (0..5).random(),
                currentLevel = (1..10).random(),
                experiencePoints = (0..2500).random(),
                experienceToNextLevel = 300,
                streakDays = (0..30).random(),
                lastPlayDate = System.currentTimeMillis() - (0..86400000L).random()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user stats", e)
            UserGamingStats(userId, 0, 0, 0, 0, "", 0, 0, 0, 0, 0, 1, 0, 100, 0, System.currentTimeMillis())
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
    
    /**
     * Get user level information
     */
    suspend fun getUserLevel(userId: String): UserLevel {
        return try {
            delay(200) // Simulate level calculation
            
            val stats = getUserStats(userId)
            val currentLevel = stats.currentLevel
            
            val levelData = getLevelData(currentLevel)
            levelData
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user level", e)
            getLevelData(1) // Default to level 1
        }
    }
    
    /**
     * Award experience points
     */
    suspend fun awardExperience(userId: String, points: Int, reason: String): ExperienceAward {
        return try {
            delay(100) // Simulate experience award
            
            val experienceAward = ExperienceAward(
                userId = userId,
                pointsAwarded = points,
                reason = reason,
                timestamp = System.currentTimeMillis(),
                newLevel = null, // Will be calculated
                levelUpRewards = emptyList()
            )
            
            Log.d(TAG, "Awarded $points XP to user $userId for: $reason")
            experienceAward
        } catch (e: Exception) {
            Log.e(TAG, "Error awarding experience", e)
            throw e
        }
    }
    
    /**
     * Get level progression data
     */
    private fun getLevelData(level: Int): UserLevel {
        val levelData = mapOf(
            1 to UserLevel(1, "Rookie", 0, 100, listOf(
                LevelReward(RewardType.COINS, 50, "Welcome bonus"),
                LevelReward(RewardType.TITLE, 1, "Rookie title")
            ), "#4CAF50", "🌱"),
            2 to UserLevel(2, "Player", 100, 300, listOf(
                LevelReward(RewardType.COINS, 100, "Level up bonus"),
                LevelReward(RewardType.AVATAR_FRAME, 1, "Bronze frame")
            ), "#2196F3", "🎮"),
            3 to UserLevel(3, "Competitor", 300, 600, listOf(
                LevelReward(RewardType.COINS, 200, "Level up bonus"),
                LevelReward(RewardType.TITLE, 2, "Competitor title")
            ), "#FF9800", "⚔️"),
            4 to UserLevel(4, "Champion", 600, 1000, listOf(
                LevelReward(RewardType.COINS, 300, "Level up bonus"),
                LevelReward(RewardType.AVATAR_FRAME, 2, "Silver frame"),
                LevelReward(RewardType.GAME_UNLOCK, 1, "New game unlocked")
            ), "#9C27B0", "👑"),
            5 to UserLevel(5, "Master", 1000, 1500, listOf(
                LevelReward(RewardType.COINS, 500, "Level up bonus"),
                LevelReward(RewardType.TITLE, 3, "Master title"),
                LevelReward(RewardType.SPECIAL_ACHIEVEMENT, 1, "Master achievement")
            ), "#F44336", "🏆"),
            6 to UserLevel(6, "Legend", 1500, 2500, listOf(
                LevelReward(RewardType.COINS, 750, "Level up bonus"),
                LevelReward(RewardType.AVATAR_FRAME, 3, "Gold frame"),
                LevelReward(RewardType.TITLE, 4, "Legend title")
            ), "#FFD700", "⭐"),
            7 to UserLevel(7, "Mythic", 2500, 4000, listOf(
                LevelReward(RewardType.COINS, 1000, "Level up bonus"),
                LevelReward(RewardType.SPECIAL_ACHIEVEMENT, 2, "Mythic achievement"),
                LevelReward(RewardType.GAME_UNLOCK, 2, "Premium game unlocked")
            ), "#E91E63", "🔥"),
            8 to UserLevel(8, "Transcendent", 4000, 6000, listOf(
                LevelReward(RewardType.COINS, 1500, "Level up bonus"),
                LevelReward(RewardType.AVATAR_FRAME, 4, "Diamond frame"),
                LevelReward(RewardType.TITLE, 5, "Transcendent title")
            ), "#00BCD4", "🌟"),
            9 to UserLevel(9, "Immortal", 6000, 10000, listOf(
                LevelReward(RewardType.COINS, 2000, "Level up bonus"),
                LevelReward(RewardType.SPECIAL_ACHIEVEMENT, 3, "Immortal achievement"),
                LevelReward(RewardType.GAME_UNLOCK, 3, "Exclusive game unlocked")
            ), "#795548", "💀"),
            10 to UserLevel(10, "Godlike", 10000, Int.MAX_VALUE, listOf(
                LevelReward(RewardType.COINS, 5000, "Final bonus"),
                LevelReward(RewardType.AVATAR_FRAME, 5, "Godlike frame"),
                LevelReward(RewardType.TITLE, 6, "Godlike title"),
                LevelReward(RewardType.SPECIAL_ACHIEVEMENT, 4, "Godlike achievement")
            ), "#FF5722", "⚡")
        )
        
        return levelData[level] ?: levelData[1]!!
    }
    
    /**
     * Get enhanced achievements with more categories
     */
    suspend fun getEnhancedAchievements(userId: String): List<EnhancedAchievement> {
        return try {
            delay(300)
            
            val achievements = listOf(
                EnhancedAchievement(
                    id = "first_game",
                    title = "First Steps",
                    description = "Play your first game",
                    icon = "🎮",
                    category = AchievementCategory.GAMING,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 86400000L,
                    rarity = AchievementRarity.COMMON,
                    points = 10,
                    animationType = AnimationType.COIN_COLLECT
                ),
                EnhancedAchievement(
                    id = "score_master",
                    title = "Score Master",
                    description = "Reach 10,000 points in any game",
                    icon = "🏆",
                    category = AchievementCategory.SCORING,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 43200000L,
                    rarity = AchievementRarity.RARE,
                    points = 50,
                    animationType = AnimationType.TROPHY_CELEBRATION
                ),
                EnhancedAchievement(
                    id = "social_player",
                    title = "Social Player",
                    description = "Play with 10 different friends",
                    icon = "👥",
                    category = AchievementCategory.SOCIAL,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.EPIC,
                    points = 100,
                    animationType = AnimationType.STAR_BURST
                ),
                EnhancedAchievement(
                    id = "speed_demon",
                    title = "Speed Demon",
                    description = "Complete a game in under 30 seconds",
                    icon = "⚡",
                    category = AchievementCategory.SPEED,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.LEGENDARY,
                    points = 200,
                    animationType = AnimationType.STAR_BURST
                ),
                EnhancedAchievement(
                    id = "daily_player",
                    title = "Daily Player",
                    description = "Play games for 7 consecutive days",
                    icon = "📅",
                    category = AchievementCategory.CONSISTENCY,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis() - 604800000L,
                    rarity = AchievementRarity.RARE,
                    points = 75,
                    animationType = AnimationType.COIN_COLLECT
                ),
                EnhancedAchievement(
                    id = "perfectionist",
                    title = "Perfectionist",
                    description = "Achieve perfect score in any game",
                    icon = "💎",
                    category = AchievementCategory.SCORING,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.LEGENDARY,
                    points = 300,
                    animationType = AnimationType.TROPHY_CELEBRATION
                ),
                EnhancedAchievement(
                    id = "night_owl",
                    title = "Night Owl",
                    description = "Play games after midnight",
                    icon = "🦉",
                    category = AchievementCategory.SPECIAL,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.EPIC,
                    points = 150,
                    animationType = AnimationType.STAR_BURST
                ),
                EnhancedAchievement(
                    id = "early_bird",
                    title = "Early Bird",
                    description = "Play games before 6 AM",
                    icon = "🐦",
                    category = AchievementCategory.SPECIAL,
                    isUnlocked = false,
                    unlockedAt = null,
                    rarity = AchievementRarity.RARE,
                    points = 125,
                    animationType = AnimationType.COIN_COLLECT
                )
            )
            
            achievements
        } catch (e: Exception) {
            Log.e(TAG, "Error getting enhanced achievements", e)
            emptyList()
        }
    }
    
    /**
     * Get social gaming features
     */
    suspend fun getSocialFeatures(userId: String): SocialGamingData {
        return try {
            delay(300)
            
            SocialGamingData(
                userId = userId,
                friendsPlaying = (1..8).map { index ->
                    FriendActivity(
                        friendId = "friend_$index",
                        friendName = "Friend$index",
                        currentGame = availableGames.random().name,
                        isOnline = (0..1).random() == 1,
                        lastSeen = System.currentTimeMillis() - (index * 300000L),
                        avatar = "https://picsum.photos/100/100?id=$index"
                    )
                },
                recentChallenges = listOf(
                    FriendChallenge(
                        id = "challenge_1",
                        challengerName = "Friend1",
                        gameName = "Emoji Match",
                        challengeScore = 8500,
                        timeLimit = 24 * 60 * 60 * 1000L, // 24 hours
                        isCompleted = false
                    ),
                    FriendChallenge(
                        id = "challenge_2",
                        challengerName = "Friend3",
                        gameName = "Word Race",
                        challengeScore = 12000,
                        timeLimit = 12 * 60 * 60 * 1000L, // 12 hours
                        isCompleted = false
                    )
                ),
                teamChallenges = listOf(
                    TeamChallenge(
                        id = "team_1",
                        teamName = "Lightning Bolts",
                        currentObjective = "Score 100,000 points total",
                        progress = 75000,
                        target = 100000,
                        members = 4,
                        endTime = System.currentTimeMillis() + 86400000L
                    )
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting social features", e)
            SocialGamingData(userId, emptyList(), emptyList(), emptyList())
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
    val tournamentsWon: Int,
    val currentLevel: Int,
    val experiencePoints: Int,
    val experienceToNextLevel: Int,
    val streakDays: Int,
    val lastPlayDate: Long
)

data class UserLevel(
    val level: Int,
    val title: String,
    val minExperience: Int,
    val maxExperience: Int,
    val rewards: List<LevelReward>,
    val color: String,
    val badgeIcon: String
)

data class LevelReward(
    val type: RewardType,
    val value: Int,
    val description: String
)

enum class RewardType {
    COINS, AVATAR_FRAME, TITLE, GAME_UNLOCK, SPECIAL_ACHIEVEMENT
}

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
    GAMING, SCORING, SOCIAL, SPEED, CONSISTENCY, TOURNAMENT, SPECIAL
}

enum class AchievementRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

enum class TournamentStatus {
    REGISTRATION_OPEN, ACTIVE, COMPLETED, CANCELLED
}

// Enhanced gamification data classes
data class EnhancedAchievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val isUnlocked: Boolean,
    val unlockedAt: Long?,
    val rarity: AchievementRarity,
    val points: Int,
    val animationType: AnimationType
)

data class ExperienceAward(
    val userId: String,
    val pointsAwarded: Int,
    val reason: String,
    val timestamp: Long,
    val newLevel: Int?,
    val levelUpRewards: List<LevelReward>
)

data class SocialGamingData(
    val userId: String,
    val friendsPlaying: List<FriendActivity>,
    val recentChallenges: List<FriendChallenge>,
    val teamChallenges: List<TeamChallenge>
)

data class FriendActivity(
    val friendId: String,
    val friendName: String,
    val currentGame: String,
    val isOnline: Boolean,
    val lastSeen: Long,
    val avatar: String
)

data class FriendChallenge(
    val id: String,
    val challengerName: String,
    val gameName: String,
    val challengeScore: Int,
    val timeLimit: Long,
    val isCompleted: Boolean
)

data class TeamChallenge(
    val id: String,
    val teamName: String,
    val currentObjective: String,
    val progress: Int,
    val target: Int,
    val members: Int,
    val endTime: Long
)

enum class AnimationType {
    TROPHY_CELEBRATION, STAR_BURST, COIN_COLLECT, NONE
}