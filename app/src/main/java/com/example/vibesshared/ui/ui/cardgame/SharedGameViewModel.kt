package com.example.vibesshared.ui.ui.cardgame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A ViewModel that can be shared across multiple screens for global game state.
 * Provided as a singleton to maintain state throughout the app's lifecycle.
 */
@Singleton
class SharedGameViewModel @Inject constructor() : ViewModel() {

    // Game audio settings
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    // Game music settings
    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    // Game difficulty
    private val _gameDifficulty = MutableStateFlow(GameDifficulty.NORMAL)
    val gameDifficulty: StateFlow<GameDifficulty> = _gameDifficulty.asStateFlow()

    // Current tutorial state
    private val _tutorialCompleted = MutableStateFlow(false)
    val tutorialCompleted: StateFlow<Boolean> = _tutorialCompleted.asStateFlow()

    // Global notification or event
    private val _globalEvent = MutableStateFlow<GameEvent?>(null)
    val globalEvent: StateFlow<GameEvent?> = _globalEvent.asStateFlow()

    /**
     * Toggle sound on/off
     */
    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    /**
     * Toggle music on/off
     */
    fun toggleMusic() {
        _musicEnabled.value = !_musicEnabled.value
    }

    /**
     * Set game difficulty
     */
    fun setDifficulty(difficulty: GameDifficulty) {
        _gameDifficulty.value = difficulty
    }

    /**
     * Mark tutorial as completed
     */
    fun completeTutorial() {
        _tutorialCompleted.value = true
    }

    /**
     * Broadcast a global game event
     */
    fun broadcastEvent(event: GameEvent) {
        _globalEvent.value = event
    }

    /**
     * Clear the current event after handling
     */
    fun clearEvent() {
        _globalEvent.value = null
    }
}

/**
 * Represents different game difficulty levels
 */
enum class GameDifficulty {
    EASY, NORMAL, HARD, EXPERT
}

/**
 * Represents global game events that can be broadcast across screens
 */
sealed class GameEvent {
    data class NewCardUnlocked(val cardId: String) : GameEvent()
    data class AchievementUnlocked(val achievementId: String) : GameEvent()
    data class BattleInvite(val fromPlayerId: String) : GameEvent()
    object DailyRewardAvailable : GameEvent()
    object ConnectionError : GameEvent()
}