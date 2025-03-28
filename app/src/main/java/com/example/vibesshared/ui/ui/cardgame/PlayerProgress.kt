package com.example.vibesshared.ui.ui.cardgame

// File: PlayerProgress.kt

/**
 * Data structure for player progression metrics.
 */
data class PlayerProgress(
    var level: Int = 1,
    var experience: Int = 0,
    var multiverseTokens: Int = 0,
    val achievements: MutableList<String> = mutableListOf(),
    var cardsCollected: Int = 0,
    var battlesWon: Int = 0,
    var battlesLost: Int = 0
) {
    /**
     * Calculate XP needed for the next level.
     *
     * @return Int amount of XP required
     */
    fun xpToNextLevel(): Int {
        // Formula: Base 1000 XP + 500 XP per level
        return 1000 + (level * 500)
    }

    /**
     * Add XP, handle level up.
     *
     * @param amount Int XP to be added
     * @return Boolean true if leveled up
     */
    fun addXp(amount: Int): Boolean {
        experience += amount

        val xpNeeded = xpToNextLevel()
        if (experience >= xpNeeded) {
            experience -= xpNeeded
            level++
            return true
        }

        return false
    }
}