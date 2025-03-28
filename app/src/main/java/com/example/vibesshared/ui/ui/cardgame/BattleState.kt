package com.example.vibesshared.ui.ui.cardgame

// File: BattleState.kt

/**
 * Defines battle phases, event structure, result types, and animation events.
 */

/**
 * Represents the current phase of a battle.
 */
enum class BattlePhase {
    INITIAL,       // Battle setup phase
    DRAW,          // Drawing cards phase
    PLAYER_ACTION, // Player is choosing an action
    ENEMY_ACTION,  // Enemy is taking action
    RESOLUTION,    // Resolving actions/effects (potentially unused if animations handle this)
    GAME_OVER      // Battle has concluded (Replaced ENDED based on ViewModel usage)
    // Removed DRAW phase as it wasn't explicitly used in VM logic flow
}

/**
 * Records a significant event during battle for logging.
 */
data class BattleEvent(
    val turn: Int,
    val description: String,
    val type: String // e.g., "ATTACK", "ABILITY", "SYSTEM", "ERROR"
)

/**
 * Indicates the result state of a battle.
 */
enum class BattleResult {
    NOT_STARTED,
    IN_PROGRESS,
    PLAYER_VICTORY,
    ENEMY_VICTORY,
    DRAW
}

/**
 * Represents specific animation events to be triggered in the UI. (ADDED)
 */
enum class AnimationEvent {
    CARD_PLAYED,
    ATTACK,
    ABILITY_USED,
    DAMAGE_TAKEN,
    CARD_DEFEATED,
    HEAL_EFFECT, // Example for healing animation
    BUFF_EFFECT,  // Example for buff animation
    DEBUFF_EFFECT // Example for debuff animation
    // Add other specific animation types as needed
}