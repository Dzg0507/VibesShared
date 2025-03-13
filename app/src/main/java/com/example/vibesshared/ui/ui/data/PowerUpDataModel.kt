package com.example.vibesshared.ui.ui.data

// Enum for Power-Up Types
enum class PowerUpType {
    TIME_FREEZE,     // Pauses the timer for a duration
    TIME_BOOST,      // Adds extra time to the clock
    FIFTY_FIFTY,     // Removes two incorrect answers
    CORRECT_ANSWER,  // Automatically answers the current question correctly
    HINT,            // Provides a hint for the current question
    SKIP_QUESTION,   // Skips the current question without penalty
    DOUBLE_POINTS    // Doubles the points for the current question
}

// Data class for Power-Ups
data class PowerUp(
    val id: String,
    val name: String,
    val description: String,
    val type: PowerUpType,
    val iconName: String,
    val rarity: Float,  // 0.0 to 1.0, indicates how rare this power-up is
    val durationSeconds: Int = 0,  // For time-based power-ups
    val cooldownSeconds: Int = 0,  // Cooldown before it can be used again
    val tokenCost: Int = 0,        // Cost in multiverse tokens
    val unlockLevel: Int = 1       // Minimum player level to unlock
)

// Factory methods to create standard power-ups
object PowerUpFactory {

    fun createTimeFreezePowerUp(id: String = "time_freeze"): PowerUp =
        PowerUp(
            id = id,
            name = "Time Freeze",
            description = "Pauses the timer for 15 seconds",
            type = PowerUpType.TIME_FREEZE,
            iconName = "pause_circle",
            rarity = 0.3f,
            durationSeconds = 15,
            cooldownSeconds = 60,
            tokenCost = 5,
            unlockLevel = 2
        )

    fun createTimeBoostPowerUp(id: String = "time_boost"): PowerUp =
        PowerUp(
            id = id,
            name = "Time Boost",
            description = "Adds 20 seconds to the timer",
            type = PowerUpType.TIME_BOOST,
            iconName = "alarm_add",
            rarity = 0.4f,
            cooldownSeconds = 90,
            tokenCost = 3,
            unlockLevel = 1
        )

    fun createFiftyFiftyPowerUp(id: String = "fifty_fifty"): PowerUp =
        PowerUp(
            id = id,
            name = "50/50",
            description = "Removes two incorrect answers",
            type = PowerUpType.FIFTY_FIFTY,
            iconName = "filter_2",
            rarity = 0.25f,
            cooldownSeconds = 30,
            tokenCost = 4,
            unlockLevel = 3
        )

    fun createCorrectAnswerPowerUp(id: String = "correct_answer"): PowerUp =
        PowerUp(
            id = id,
            name = "Smart Answer",
            description = "Automatically answers the current question correctly",
            type = PowerUpType.CORRECT_ANSWER,
            iconName = "check_circle",
            rarity = 0.1f,
            cooldownSeconds = 120,
            tokenCost = 10,
            unlockLevel = 5
        )

    fun createHintPowerUp(id: String = "hint"): PowerUp =
        PowerUp(
            id = id,
            name = "Hint",
            description = "Provides a hint for the current question",
            type = PowerUpType.HINT,
            iconName = "lightbulb",
            rarity = 0.5f,
            cooldownSeconds = 45,
            tokenCost = 2,
            unlockLevel = 1
        )

    fun createSkipQuestionPowerUp(id: String = "skip_question"): PowerUp =
        PowerUp(
            id = id,
            name = "Skip Question",
            description = "Skips the current question without penalty",
            type = PowerUpType.SKIP_QUESTION,
            iconName = "skip_next",
            rarity = 0.2f,
            cooldownSeconds = 60,
            tokenCost = 6,
            unlockLevel = 4
        )

    fun createDoublePointsPowerUp(id: String = "double_points"): PowerUp =
        PowerUp(
            id = id,
            name = "Double Points",
            description = "Doubles the points for the current question",
            type = PowerUpType.DOUBLE_POINTS,
            iconName = "exposure_plus_2",
            rarity = 0.15f,
            cooldownSeconds = 90,
            tokenCost = 8,
            unlockLevel = 7
        )

    // Get all standard power-ups
    fun getAllPowerUps(): List<PowerUp> = listOf(
        createTimeFreezePowerUp(),
        createTimeBoostPowerUp(),
        createFiftyFiftyPowerUp(),
        createCorrectAnswerPowerUp(),
        createHintPowerUp(),
        createSkipQuestionPowerUp(),
        createDoublePointsPowerUp()
    )
}

// Extension to PlayerProgress to track power-ups
data class PowerUpInventory(
    val powerUps: Map<String, Int> = mapOf(), // PowerUp ID to count
    val activeEffects: List<ActivePowerUpEffect> = emptyList()
)

// Represents an active power-up effect with an expiration time
data class ActivePowerUpEffect(
    val powerUpId: String,
    val activationTime: Long,
    val expirationTime: Long
)

// Extend PlayerProgress
data class PlayerProgressWithPowerUps(
    val baseProgress: PlayerProgress = PlayerProgress(),
    val powerUpInventory: PowerUpInventory = PowerUpInventory()
) {
    val level: Int get() = baseProgress.level
    val xp: Int get() = baseProgress.xp
    val multiverseTokens: Int get() = baseProgress.multiverseTokens
    val completedQuests: List<String> get() = baseProgress.completedQuests
    val unlockedRewards: List<String> get() = baseProgress.unlockedRewards
    val activeQuests: List<String> get() = baseProgress.activeQuests

    fun hasPowerUp(powerUpId: String): Boolean =
        (powerUpInventory.powerUps[powerUpId] ?: 0) > 0

    fun getPowerUpCount(powerUpId: String): Int =
        powerUpInventory.powerUps[powerUpId] ?: 0
}