package com.example.vibesshared.ui.ui.data

// Enum for Quest Difficulties
enum class QuestDifficulty {
    EASY, MEDIUM, HARD, ADEPT, EXPERT, LEGENDARY, MULTIVERSE_BREAKER
}

// Enum for Quest Types
enum class QuestType {
    STANDARD, TIME_ATTACK, COOPERATIVE, CHALLENGE_MODE, BOSS_BATTLE, RESEARCH_EXPEDITION
}

// Enum for Reward Tiers
enum class RewardTier {
    COMMON, RARE, EPIC, LEGENDARY, MYTHICAL
}

// Enum for Reward Types
enum class RewardType {
    UNIQUE_CHARACTER,
    COSMETIC_ITEM,
    MULTIVERSE_FRAGMENT,
    RARE_ACHIEVEMENT,
    EXCLUSIVE_BACKGROUND
}

// Data class for Quests
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: QuestDifficulty,
    val type: QuestType,
    val category: String,
    val questionCount: Int,
    val completionThreshold: Int, // Add this property
    val timeLimit: Int,
    val rewards: List<Reward>,
    val requiredLevel: Int = 1,
    val isTimeLimited: Boolean = false,
    val expirationDate: Long? = null
)

// Data class for Rewards
data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val tier: RewardTier,
    val type: RewardType,
    val rarity: Float, // Probability of obtaining (0.0 to 1.0)
    val xpValue: Int,
    val multiverseTokenValue: Int = 0
)

// Data class for Player Progress
data class PlayerProgress(
    val level: Int = 1,
    val xp: Int = 0,
    val multiverseTokens: Int = 0,
    val completedQuests: List<String> = emptyList(),
    val unlockedRewards: List<String> = emptyList(),
    val activeQuests: List<String> = emptyList()
)

// Extension function to check if a quest is available to the player
fun Quest.isAvailableToPlayer(playerProgress: PlayerProgress): Boolean {
    return playerProgress.level >= requiredLevel &&
            !playerProgress.completedQuests.contains(id) &&
            (!isTimeLimited || (expirationDate == null || System.currentTimeMillis() < expirationDate))
}

// Extension function to calculate XP needed for next level
fun PlayerProgress.xpForNextLevel(): Int {
    // Simple leveling formula: base XP increases quadratically
    return level * level * 100
}

// Extension function to check if player can level up
fun PlayerProgress.canLevelUp(): Boolean {
    return xp >= xpForNextLevel()
}