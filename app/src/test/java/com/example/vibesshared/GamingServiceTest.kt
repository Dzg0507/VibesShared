package com.example.vibesshared

import android.content.Context
import com.example.vibesshared.ui.ui.gaming.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class GamingServiceTest {

    @Mock
    private lateinit var context: Context

    @Test
    fun testGamingServiceInitialization() {
        val gamingService = GamingService(context)
        assertNotNull(gamingService)
        assertTrue(gamingService.availableGames.isNotEmpty())
    }

    @Test
    fun testAvailableGames() {
        val gamingService = GamingService(context)
        val games = gamingService.availableGames
        
        assertTrue(games.size >= 6) // Should have at least 6 games
        
        // Test that each game has required fields
        games.forEach { game ->
            assertNotNull(game.id)
            assertNotNull(game.name)
            assertNotNull(game.description)
            assertNotNull(game.icon)
            assertNotNull(game.category)
            assertNotNull(game.difficulty)
            assertTrue(game.maxPlayers > 0)
            assertTrue(game.estimatedDuration > 0)
        }
    }

    @Test
    fun testGameCategories() {
        val gamingService = GamingService(context)
        val games = gamingService.availableGames
        
        // Should have games in different categories
        val categories = games.map { it.category }.distinct()
        assertTrue(categories.size >= 3) // Should have at least 3 different categories
    }

    @Test
    fun testGameDifficulties() {
        val gamingService = GamingService(context)
        val games = gamingService.availableGames
        
        // Should have games with different difficulties
        val difficulties = games.map { it.difficulty }.distinct()
        assertTrue(difficulties.contains(Difficulty.EASY))
        assertTrue(difficulties.contains(Difficulty.MEDIUM))
        assertTrue(difficulties.contains(Difficulty.HARD))
    }

    @Test
    fun testGetUserStats() = runBlocking {
        val gamingService = GamingService(context)
        val stats = gamingService.getUserStats("test_user")
        
        assertNotNull(stats)
        assertNotNull(stats.userId)
        assertTrue(stats.totalGamesPlayed >= 0)
        assertTrue(stats.totalScore >= 0)
        assertTrue(stats.currentLevel >= 1)
        assertTrue(stats.experiencePoints >= 0)
        assertTrue(stats.streakDays >= 0)
    }

    @Test
    fun testGetUserLevel() = runBlocking {
        val gamingService = GamingService(context)
        val level = gamingService.getUserLevel("test_user")
        
        assertNotNull(level)
        assertTrue(level.level >= 1)
        assertNotNull(level.title)
        assertNotNull(level.color)
        assertNotNull(level.badgeIcon)
        assertNotNull(level.rewards)
    }

    @Test
    fun testGetEnhancedAchievements() = runBlocking {
        val gamingService = GamingService(context)
        val achievements = gamingService.getEnhancedAchievements("test_user")
        
        assertNotNull(achievements)
        assertTrue(achievements.isNotEmpty())
        
        achievements.forEach { achievement ->
            assertNotNull(achievement.id)
            assertNotNull(achievement.title)
            assertNotNull(achievement.description)
            assertNotNull(achievement.icon)
            assertNotNull(achievement.category)
            assertNotNull(achievement.rarity)
            assertNotNull(achievement.animationType)
            assertTrue(achievement.points > 0)
        }
    }

    @Test
    fun testGetSocialFeatures() = runBlocking {
        val gamingService = GamingService(context)
        val socialData = gamingService.getSocialFeatures("test_user")
        
        assertNotNull(socialData)
        assertNotNull(socialData.userId)
        assertNotNull(socialData.friendsPlaying)
        assertNotNull(socialData.recentChallenges)
        assertNotNull(socialData.teamChallenges)
    }

    @Test
    fun testStartGame() = runBlocking {
        val gamingService = GamingService(context)
        val game = gamingService.availableGames.first()
        val session = gamingService.startGame(game.id, "test_user")
        
        assertNotNull(session)
        assertNotNull(session.id)
        assertTrue(session.gameId == game.id)
        assertNotNull(session.players)
        assertTrue(session.players.isNotEmpty())
        assertNotNull(session.status)
    }

    @Test
    fun testGetLeaderboard() = runBlocking {
        val gamingService = GamingService(context)
        val game = gamingService.availableGames.first()
        val leaderboard = gamingService.getLeaderboard(game.id, LeaderboardTimeFrame.WEEKLY)
        
        assertNotNull(leaderboard)
        assertNotNull(leaderboard.gameId)
        assertTrue(leaderboard.gameId == game.id)
        assertNotNull(leaderboard.entries)
        assertNotNull(leaderboard.timeFrame)
    }

    @Test
    fun testGetTournaments() = runBlocking {
        val gamingService = GamingService(context)
        val tournaments = gamingService.getTournaments()
        
        assertNotNull(tournaments)
        tournaments.forEach { tournament ->
            assertNotNull(tournament.id)
            assertNotNull(tournament.name)
            assertNotNull(tournament.description)
            assertNotNull(tournament.gameId)
            assertNotNull(tournament.status)
            assertTrue(tournament.maxParticipants > 0)
            assertTrue(tournament.participants >= 0)
        }
    }

    @Test
    fun testGetDailyChallenges() = runBlocking {
        val gamingService = GamingService(context)
        val challenges = gamingService.getDailyChallenges()
        
        assertNotNull(challenges)
        assertTrue(challenges.isNotEmpty())
        
        challenges.forEach { challenge ->
            assertNotNull(challenge.id)
            assertNotNull(challenge.title)
            assertNotNull(challenge.description)
            assertTrue(challenge.reward > 0)
            assertTrue(challenge.target > 0)
            assertTrue(challenge.progress >= 0)
            assertTrue(challenge.progress <= challenge.target)
        }
    }

    @Test
    fun testAwardExperience() = runBlocking {
        val gamingService = GamingService(context)
        val experienceAward = gamingService.awardExperience("test_user", 100, "Test achievement")
        
        assertNotNull(experienceAward)
        assertTrue(experienceAward.userId == "test_user")
        assertTrue(experienceAward.pointsAwarded == 100)
        assertNotNull(experienceAward.reason)
        assertNotNull(experienceAward.timestamp)
        assertNotNull(experienceAward.levelUpRewards)
    }
}