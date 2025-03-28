package com.example.vibesshared.ui.ui.cardgame

// File: GamePhaseManager.kt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages transitions between different game states or phases, coordinating logic flow.
 */

/**
 * Represents a general game phase or state.
 */
enum class GamePhase {
    INITIALIZING,    // Game is loading resources and setting up
    MAIN_MENU,       // Player is in the main menu
    COLLECTION,      // Player is managing their card collection
    DECK_BUILDING,   // Player is building or editing a deck
    SHOP,            // Player is in the shop purchasing cards
    FORGE,           // Player is using the forge to combine cards
    BATTLE_SETUP,    // Player is preparing for battle (selecting deck, etc.)
    BATTLE_START,    // Battle has started, drawing initial cards
    PLAYER_TURN,     // Player's turn in battle
    OPPONENT_TURN,   // Opponent's turn in battle
    BATTLE_END,      // Battle has concluded, showing results
    REWARD,          // Player is receiving rewards
    PROFILE          // Player is viewing their profile
}

/**
 * Manages the game's phase transitions.
 */
class GamePhaseManager(
    private val battleRepository: BattleRepository? = null,
    private val cardRepository: CardRepository? = null,
    private val cardGameProgressionRepository: CardGameProgressionRepository? = null,

) {
    // Current phase of the game
    private val _currentPhase = MutableStateFlow(GamePhase.INITIALIZING)
    val currentPhase: StateFlow<GamePhase> = _currentPhase.asStateFlow()

    // Flag to indicate if a phase transition is in progress
    private var isTransitioning: Boolean by mutableStateOf(false)

    // Timestamp of the last phase change (for rate limiting)
    private var lastPhaseChangeTime: Long = 0

    // Specific battle phase for more granular control during battles
    private val _battlePhase = MutableStateFlow(BattlePhase.INITIAL)
    val battlePhase: StateFlow<BattlePhase> = _battlePhase.asStateFlow()

    /**
     * Advances to the next logical phase based on current phase.
     *
     * @return Boolean indicating if the phase was successfully advanced
     */
    suspend fun advancePhase(): Boolean {
        // Rate limit phase changes (prevent rapid transitions)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPhaseChangeTime < 300) {
            return false
        }

        // Don't advance if already transitioning
        if (isTransitioning) {
            return false
        }

        val currentPhase = _currentPhase.value

        // Determine the next logical phase based on the current phase
        val nextPhase = when (currentPhase) {
            GamePhase.INITIALIZING -> GamePhase.MAIN_MENU

            GamePhase.BATTLE_SETUP -> GamePhase.BATTLE_START
            GamePhase.BATTLE_START -> GamePhase.PLAYER_TURN
            GamePhase.PLAYER_TURN -> GamePhase.OPPONENT_TURN
            GamePhase.OPPONENT_TURN -> {
                // Check battle state to determine if battle should end or return to player turn
                if (battleRepository?.battleState?.value != BattleResult.IN_PROGRESS) {
                    GamePhase.BATTLE_END
                } else {
                    GamePhase.PLAYER_TURN
                }
            }
            GamePhase.BATTLE_END -> GamePhase.REWARD
            GamePhase.REWARD -> GamePhase.MAIN_MENU

            // For other phases, advancing doesn't make sense without user input
            else -> null
        }

        // If a next phase was determined, transition to it
        return if (nextPhase != null) {
            transition(nextPhase)
            true
        } else {
            false
        }
    }

    /**
     * Transitions to a specific game phase.
     *
     * @param phase GamePhase the phase to transition to
     * @return Boolean indicating if the transition was successfully initiated
     */
    suspend fun transition(phase: GamePhase): Boolean {
        // Rate limit phase changes
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPhaseChangeTime < 300) {
            return false
        }

        // Don't transition if already transitioning
        if (isTransitioning) {
            return false
        }

        val currentPhase = _currentPhase.value

        // Check if the transition is valid
        if (!isValidTransition(currentPhase, phase)) {
            return false
        }

        // Begin the transition
        isTransitioning = true

        // Handle exit logic for current phase
        handlePhaseExit(currentPhase)

        // Update the phase
        _currentPhase.value = phase
        lastPhaseChangeTime = currentTime

        // Handle entry logic for new phase
        handlePhaseEntry(phase)

        // Transition complete
        isTransitioning = false

        return true
    }

    /**
     * Executes logic needed when entering a specific phase.
     *
     * @param phase GamePhase the phase being entered
     */
    private fun handlePhaseEntry(phase: GamePhase) {
        when (phase) {
            GamePhase.INITIALIZING -> {
                // Load necessary game data
                loadGameData()
            }

            GamePhase.MAIN_MENU -> {
                // Reset any necessary state
            }

            GamePhase.BATTLE_SETUP -> {
                // Initialize battle setup
            }

            GamePhase.BATTLE_START -> {
                // Start the battle
                _battlePhase.value = BattlePhase.INITIAL
            }

            GamePhase.PLAYER_TURN -> {
                // Set up player's turn
                _battlePhase.value = BattlePhase.PLAYER_ACTION
            }

            GamePhase.OPPONENT_TURN -> {
                // Set up opponent's turn
                _battlePhase.value = BattlePhase.ENEMY_ACTION
            }

            GamePhase.BATTLE_END -> {
                // Process battle end
                _battlePhase.value = BattlePhase.RESOLUTION
            }

            GamePhase.REWARD -> {
                // Calculate and prepare rewards
            }

            else -> {
                // Other phases may not need specific entry logic
            }
        }
    }

    /**
     * Executes cleanup logic when exiting a phase.
     *
     * @param phase GamePhase the phase being exited
     */
    private suspend fun handlePhaseExit(phase: GamePhase) {
        when (phase) {
            GamePhase.BATTLE_START -> {
                // Cleanup after battle initialization
            }

            GamePhase.PLAYER_TURN -> {
                // End player turn processing
            }

            GamePhase.OPPONENT_TURN -> {
                // End opponent turn processing
            }

            GamePhase.BATTLE_END -> {
                // Clean up battle resources
            }

            GamePhase.REWARD -> {
                // Save progress after rewards
                cardGameProgressionRepository?.saveProgress(cardGameProgressionRepository.playerProgress.value)
            }

            else -> {
                // Other phases may not need specific exit logic
            }
        }
    }

    /**
     * Checks if a transition from one phase to another is valid.
     *
     * @param from GamePhase the current phase
     * @param to GamePhase the target phase
     * @return Boolean indicating if the transition is valid
     */
    private fun isValidTransition(from: GamePhase, to: GamePhase): Boolean {
        // Define valid transitions
        return when (from) {
            GamePhase.INITIALIZING -> to == GamePhase.MAIN_MENU

            GamePhase.MAIN_MENU -> to in listOf(
                GamePhase.COLLECTION,
                GamePhase.BATTLE_SETUP,
                GamePhase.SHOP,
                GamePhase.FORGE,
                GamePhase.PROFILE
            )

            GamePhase.COLLECTION -> to in listOf(
                GamePhase.MAIN_MENU,
                GamePhase.DECK_BUILDING,
                GamePhase.BATTLE_SETUP
            )

            GamePhase.DECK_BUILDING -> to in listOf(
                GamePhase.COLLECTION,
                GamePhase.BATTLE_SETUP
            )

            GamePhase.SHOP -> to in listOf(
                GamePhase.MAIN_MENU,
                GamePhase.FORGE,
                GamePhase.COLLECTION
            )

            GamePhase.FORGE -> to in listOf(
                GamePhase.MAIN_MENU,
                GamePhase.SHOP,
                GamePhase.COLLECTION
            )

            GamePhase.BATTLE_SETUP -> to in listOf(
                GamePhase.MAIN_MENU,
                GamePhase.COLLECTION,
                GamePhase.BATTLE_START
            )

            GamePhase.BATTLE_START -> to == GamePhase.PLAYER_TURN

            GamePhase.PLAYER_TURN -> to in listOf(
                GamePhase.OPPONENT_TURN,
                GamePhase.BATTLE_END // In case of instant win/forfeit
            )

            GamePhase.OPPONENT_TURN -> to in listOf(
                GamePhase.PLAYER_TURN,
                GamePhase.BATTLE_END // In case of instant win/loss
            )

            GamePhase.BATTLE_END -> to == GamePhase.REWARD

            GamePhase.REWARD -> to == GamePhase.MAIN_MENU

            GamePhase.PROFILE -> to == GamePhase.MAIN_MENU

            else -> false
        }
    }

    /**
     * Updates the battle phase directly (for finer control during battles).
     *
     * @param phase BattlePhase the battle phase to set
     */
    fun updateBattlePhase(phase: BattlePhase) {
        // Only update if we're in a battle-related game phase
        if (_currentPhase.value in listOf(
                GamePhase.BATTLE_START,
                GamePhase.PLAYER_TURN,
                GamePhase.OPPONENT_TURN,
                GamePhase.BATTLE_END
            )
        ) {
            _battlePhase.value = phase
        }
    }

    /**
     * Returns the player to the main menu.
     *
     * @return Boolean indicating if the transition was successful
     */
    suspend fun returnToMainMenu(): Boolean {
        return transition(GamePhase.MAIN_MENU)
    }

    /**
     * Loads initial game data.
     */
    private fun loadGameData() {
        // Placeholder for loading game data
        // In a real implementation, this would load player data, cards, etc.
        // For example:
        // viewModelScope.launch {
        //     playerProgressionRepository?.loadProgress()
        //     cardRepository?.loadData()
        // }
    }
}