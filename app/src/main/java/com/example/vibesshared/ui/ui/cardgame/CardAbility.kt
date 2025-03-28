package com.example.vibesshared.ui.ui.cardgame

import android.util.Log // Added for logging

// File: CardAbility.kt (Updated)

/**
 * Defines the structure for special abilities and their effects.
 */
data class CardAbility(
    val name: String,
    val description: String,
    val energyCost: Int,
    val cooldown: Int,
    // Lambda now returns CardEffect (the sealed class)
    val effect: (user: CardInPlay, target: CardInPlay?) -> CardEffect,
    val targetType: TargetType
)

/**
 * Wrapper for runtime cooldown state of an ability.
 */
data class AbilityInPlay(
    val ability: CardAbility,
    var currentCooldown: Int = 0,
    var isReady: Boolean = true,
    // --- ADDED: Flag to track when cooldown finishes ---
    var wasJustReadied: Boolean = false
) {
    /**
     * Execute the ability and start cooldown.
     * Logs the activation and cooldown start.
     *
     * @param user CardInPlay that is using the ability
     * @param target CardInPlay that is the target of the ability (may be null)
     * @return CardEffect result of using the ability
     */
    fun activate(user: CardInPlay, target: CardInPlay?): CardEffect {
        if (!isReady) {
            Log.e("AbilityInPlay", "[${user.card.name}] Failed to use ${ability.name}: Ability is on cooldown ($currentCooldown turns left)")
            throw IllegalStateException("Ability ${ability.name} is on cooldown")
        }

        Log.d("AbilityInPlay", "[${user.card.name}] Activating ability: ${ability.name}")
        // Apply the ability effect
        val result: CardEffect = ability.effect(user, target)

        // Start cooldown if applicable
        if (ability.cooldown > 0) {
            currentCooldown = ability.cooldown
            isReady = false
            wasJustReadied = false // Reset flag on activation
            Log.d("AbilityInPlay", "[${user.card.name}] Ability ${ability.name} used. Cooldown set to $currentCooldown.")
        } else {
            // If no cooldown, it remains ready
            isReady = true
            wasJustReadied = false
            Log.d("AbilityInPlay", "[${user.card.name}] Ability ${ability.name} used (No Cooldown).")
        }

        return result
    }

    /**
     * Decrease cooldown counter. Sets wasJustReadied flag if cooldown completes.
     * Logs when the ability becomes ready.
     */
    fun tickCooldown() {
        // Reset the flag at the start of the tick
        wasJustReadied = false

        if (!isReady && currentCooldown > 0) {
            currentCooldown--
            Log.v("AbilityInPlay", "Ability ${ability.name} cooldown ticked to $currentCooldown.")
            if (currentCooldown == 0) {
                isReady = true
                wasJustReadied = true // Set flag: cooldown just finished this tick
                Log.d("AbilityInPlay", "Ability ${ability.name} is now ready.")
            }
        }
    }
}

// Make sure CardEffect, TargetType, CardInPlay, EffectType are defined and accessible.
// Example: Assuming EffectType is defined elsewhere
// enum class EffectType { /* ... */ }
// enum class TargetType { /* ... */ }
// sealed class CardEffect { /* ... */ }
// data class CardInPlay { /* ... */ }