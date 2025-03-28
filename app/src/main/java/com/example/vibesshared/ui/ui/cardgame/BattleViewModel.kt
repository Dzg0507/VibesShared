package com.example.vibesshared.ui.ui.cardgame

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val battleRepository: BattleRepository,
    private val cardRepository: CardRepository, // Assuming CardRepository provides access to CardLibraryRepository if needed
    private val savedStateHandle: SavedStateHandle
    // Removed progression repo if not directly used for battle logic itself
    // private val cardGameProgressionRepository: CardGameProgressionRepository
) : ViewModel() {

    // Game Phase
    private val _gamePhase = MutableStateFlow(BattlePhase.INITIAL)
    val gamePhase: StateFlow<BattlePhase> = _gamePhase.asStateFlow()

    // Battle state from repository
    val battleState: StateFlow<BattleResult> = battleRepository.battleState
    val playerHand: StateFlow<List<MultiverseCard>> = battleRepository.playerHand
    val opponentHand: StateFlow<List<MultiverseCard>> = battleRepository.opponentHand
    val activePlayerCard: StateFlow<CardInPlay?> = battleRepository.activePlayerCard
    val activeOpponentCard: StateFlow<CardInPlay?> = battleRepository.activeOpponentCard
    val isPlayerTurn: StateFlow<Boolean> = battleRepository.isPlayerTurn
    val turn: StateFlow<Int> = battleRepository.turn
    val battleLog = battleRepository.battleLog

    // Animation State
    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()
    private val _currentAnimation = MutableStateFlow<AnimationEvent?>(null)
    val currentAnimation: StateFlow<AnimationEvent?> = _currentAnimation.asStateFlow()

    // UI State
    private val _awaitingPlayerAction = MutableStateFlow(true) // Start true if player starts
    val awaitingPlayerAction: StateFlow<Boolean> = _awaitingPlayerAction.asStateFlow()
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()
    private val _showGameOverDialog = MutableStateFlow(false)
    val showGameOverDialog: StateFlow<Boolean> = _showGameOverDialog.asStateFlow()
    private val _gameOverData = MutableStateFlow<Pair<BattleResult, String>?>(null)
    val gameOverData: StateFlow<Pair<BattleResult, String>?> = _gameOverData.asStateFlow()

    // Player Resources
    private val _currentPlayerEnergy = MutableStateFlow(1)
    val currentPlayerEnergy: StateFlow<Int> = _currentPlayerEnergy.asStateFlow()
    private val _maxPlayerEnergy = MutableStateFlow(1) // Start with 1 max energy
    val maxPlayerEnergy: StateFlow<Int> = _maxPlayerEnergy.asStateFlow()
    private val _energyCap = MutableStateFlow(10) // Maximum possible energy

    private var turnObserverJob: Job? = null
    private var battleResultObserverJob: Job? = null
    private var statusClearJob: Job? = null // Job to manage clearing status message

    init {
        observeTurnChanges()
        observeBattleResult()

        // Ensure initial state is correct based on who starts first
        viewModelScope.launch {
            delay(100) // Small delay to allow initial flow emissions
            if (isPlayerTurn.value) {
                handleTurnTransition(true) // Manually trigger for initial turn
            } else {
                handleTurnTransition(false)
            }
        }
    }

    // --- NEW: Public function to post status messages ---
    /**
     * Displays a status message to the user for a short duration.
     * @param message The message to display.
     */
    fun postStatusMessage(message: String) {
        // Cancel any previous job that was waiting to clear the message
        statusClearJob?.cancel()
        // Set the new message
        _statusMessage.value = message
        // Launch a new job to clear the message after a delay
        statusClearJob = viewModelScope.launch {
            delay(2500) // Display message for 2.5 seconds
            // Only clear if the message hasn't been updated again in the meantime
            if (_statusMessage.value == message) {
                _statusMessage.value = null
            }
        }
    }
    // --- End New Function ---


    // --- Energy Management ---
    private fun resetPlayerEnergy() {
        if (_maxPlayerEnergy.value < _energyCap.value) {
            _maxPlayerEnergy.value += 1
            Log.d("BattleViewModel", "Max energy increased to ${_maxPlayerEnergy.value}")
        }
        _currentPlayerEnergy.value = _maxPlayerEnergy.value
        Log.d("BattleViewModel", "Player energy reset to ${_currentPlayerEnergy.value}/${_maxPlayerEnergy.value}")
    }

    private fun modifyMaxEnergy(amount: Int) {
        _maxPlayerEnergy.value = (_maxPlayerEnergy.value + amount)
            .coerceAtLeast(1)
            .coerceAtMost(_energyCap.value)
        _currentPlayerEnergy.value = _currentPlayerEnergy.value.coerceAtMost(_maxPlayerEnergy.value)
        Log.d("BattleViewModel", "Max energy modified by $amount to ${_maxPlayerEnergy.value}. Current: ${_currentPlayerEnergy.value}")
    }

    private fun addEnergy(amount: Int) {
        val newEnergy = (_currentPlayerEnergy.value + amount).coerceAtMost(_maxPlayerEnergy.value)
        if (newEnergy > _currentPlayerEnergy.value) {
            _currentPlayerEnergy.value = newEnergy
            Log.d("BattleViewModel", "Added $amount energy. Current energy: ${_currentPlayerEnergy.value}/${_maxPlayerEnergy.value}")
        }
    }
    private fun spendEnergy(cost: Int): Boolean {
        if (_currentPlayerEnergy.value >= cost) {
            _currentPlayerEnergy.value -= cost
            Log.d("BattleViewModel", "Spent $cost energy. Remaining: ${_currentPlayerEnergy.value}/${_maxPlayerEnergy.value}")
            return true
        } else {
            Log.w("BattleViewModel", "Failed to spend $cost energy. Have: ${_currentPlayerEnergy.value}")
            postStatusMessage("Not enough energy! (Cost: $cost, Have: ${_currentPlayerEnergy.value})")
            return false
        }
    }


    // --- Battle Setup ---
    fun startNewBattle() {
        Log.i("BattleViewModel", "startNewBattle called.")
        val playerDeck = cardRepository.getActiveDeck() ?: run {
            Log.e("BattleViewModel", "Cannot start battle: No active player deck found.")
            postStatusMessage("Error: No active deck selected.")
            _gamePhase.value = BattlePhase.GAME_OVER // Indicate failure
            return
        }

        // Validate deck size before proceeding
        if (playerDeck.cardIds.size < CardDeck.MIN_CARDS || playerDeck.cardIds.size > CardDeck.MAX_CARDS) {
            Log.e("BattleViewModel", "Cannot start battle: Player deck size (${playerDeck.cardIds.size}) is invalid (Min: ${CardDeck.MIN_CARDS}, Max: ${CardDeck.MAX_CARDS}).")
            postStatusMessage("Error: Invalid deck size (${playerDeck.cardIds.size} cards).")
            _gamePhase.value = BattlePhase.GAME_OVER
            return
        }

        val enemyDeck = cardRepository.createDummyDeck() // Ensure this creates a valid deck

        // Reset energy for new battle
        _maxPlayerEnergy.value = 1
        _currentPlayerEnergy.value = 1

        _gamePhase.value = BattlePhase.INITIAL
        _showGameOverDialog.value = false // Ensure game over dialog is hidden
        _gameOverData.value = null
        _isAnimating.value = false
        _currentAnimation.value = null

        // Start battle in repository (shuffles decks, deals hands etc.)
        battleRepository.startBattle(playerDeck, enemyDeck)

        // Check initial turn state after repository setup
        _awaitingPlayerAction.value = battleRepository.isPlayerTurn.value
        if (battleRepository.isPlayerTurn.value) {
            _gamePhase.value = BattlePhase.PLAYER_ACTION
            resetPlayerEnergy() // Ensure energy is correct for turn 1
            Log.i("BattleViewModel", "Battle started. Player's turn.")
            postStatusMessage("Battle Start! Your Turn.")
        } else {
            _gamePhase.value = BattlePhase.ENEMY_ACTION
            Log.i("BattleViewModel", "Battle started. Opponent's turn.")
            postStatusMessage("Battle Start! Opponent's Turn.")
            // AI turn might be triggered by the turn change observer
        }
    }

    // --- Observers ---
    private fun observeTurnChanges() {
        turnObserverJob?.cancel()
        turnObserverJob = viewModelScope.launch {
            battleRepository.isPlayerTurn.collect { isPlayerNow ->
                // Only handle transition if not already in the correct phase
                // and animation isn't blocking
                if (!_isAnimating.value) {
                    handleTurnTransition(isPlayerNow)
                } else {
                    Log.d("BattleViewModel", "Turn change observed, waiting for animation...")
                    // Launch separate job to wait for animation
                    launch {
                        while(_isAnimating.value) { delay(100) }
                        Log.d("BattleViewModel", "Animation finished, handling turn transition.")
                        handleTurnTransition(isPlayerNow)
                    }
                }
            }
        }
    }

    private fun handleTurnTransition(isPlayerTurnNow: Boolean) {
        // Avoid redundant transitions or state sets
        val targetPhase = if (isPlayerTurnNow) BattlePhase.PLAYER_ACTION else BattlePhase.ENEMY_ACTION
        if (_gamePhase.value == targetPhase && _awaitingPlayerAction.value == isPlayerTurnNow) {
            Log.v("BattleViewModel", "handleTurnTransition: Already in correct state ($targetPhase, awaiting=$isPlayerTurnNow). Skipping.")
            return
        }

        Log.d("BattleViewModel", "Handling turn transition. Is Player Turn: $isPlayerTurnNow")
        _gamePhase.value = targetPhase
        _awaitingPlayerAction.value = isPlayerTurnNow // Player awaits action only on their turn

        if (isPlayerTurnNow) {
            resetPlayerEnergy() // Reset energy at the start of player's turn
            Log.i("BattleViewModel", "Player's turn ${battleRepository.turn.value} starts. Energy: ${_currentPlayerEnergy.value}/${_maxPlayerEnergy.value}")
            postStatusMessage("Your Turn! (Turn ${battleRepository.turn.value})")
        } else {
            Log.i("BattleViewModel", "Opponent's turn ${battleRepository.turn.value} starts.")
            postStatusMessage("Opponent's Turn (Turn ${battleRepository.turn.value})")
            // AI logic is triggered within BattleRepository.endPlayerTurn -> executeOpponentTurn
        }
    }


    private fun observeBattleResult() {
        battleResultObserverJob?.cancel()
        battleResultObserverJob = viewModelScope.launch {
            battleRepository.battleState.collect { result ->
                if (result != BattleResult.IN_PROGRESS && result != BattleResult.NOT_STARTED) {
                    Log.i("BattleViewModel", "Battle result observed: $result")
                    _gamePhase.value = BattlePhase.GAME_OVER
                    _gameOverData.value = Pair(result, generateGameOverSummary(result))
                    _showGameOverDialog.value = true
                    _awaitingPlayerAction.value = false // No more actions needed
                    turnObserverJob?.cancel() // Stop observing turn changes
                }
            }
        }
    }

    // --- Player Actions ---
    fun onCardPlayed(cardId: String) {
        Log.d("BattleViewModel", "Action: onCardPlayed ID: $cardId")
        if (!isPlayerTurn.value || _isAnimating.value || !_awaitingPlayerAction.value) return

        val card = playerHand.value.find { it.id == cardId }
        val energyCost = card?.energy ?: 0

        if (spendEnergy(energyCost)) { // Spend energy first
            _awaitingPlayerAction.value = false // Prevent multiple actions before animation/repo update
            if (battleRepository.playPlayerCard(cardId)) {
                triggerAnimation(AnimationEvent.CARD_PLAYED)
                // Don't immediately set awaitingPlayerAction = true; wait for animation/turn change
            } else {
                // Card play failed in repo, refund energy? Handle error.
                Log.e("BattleViewModel", "Card play failed in repository for ID: $cardId")
                addEnergy(energyCost) // Refund energy if repo failed
                _awaitingPlayerAction.value = true // Allow action again
                postStatusMessage("Cannot play ${card?.name ?: "card"}!") // Show error from repo if possible
            }
        }
        // If spendEnergy failed, it already posted a status message
    }

    fun onAttackAction() {
        Log.d("BattleViewModel", "Action: onAttackAction")
        if (!isPlayerTurn.value || _isAnimating.value || !_awaitingPlayerAction.value) return

        val energyCost = 1 // Define attack cost
        if (spendEnergy(energyCost)) {
            _awaitingPlayerAction.value = false
            if (battleRepository.playerAttack()) {
                triggerAnimation(AnimationEvent.ATTACK)
                // Check if attack triggers immediate damage animation on target
                // Need data from repository result or observe card health
                // Example: Trigger damage anim after attack completes? Or repo handles it?
                // For now, rely on turn change or specific events from repo if implemented
            } else {
                Log.e("BattleViewModel", "Attack failed in repository.")
                addEnergy(energyCost) // Refund
                _awaitingPlayerAction.value = true
                postStatusMessage("Cannot attack!") // Show error from repo if possible
            }
        }
    }

    fun onAbilityUsed(abilityIndex: Int, targetId: String?) {
        Log.d("BattleViewModel", "Action: onAbilityUsed index: $abilityIndex, targetId: $targetId")
        if (!isPlayerTurn.value || _isAnimating.value || !_awaitingPlayerAction.value) return

        val ability = activePlayerCard.value?.abilities?.getOrNull(abilityIndex)?.ability
        val energyCost = ability?.energyCost ?: 0

        if (spendEnergy(energyCost)) {
            _awaitingPlayerAction.value = false
            if (battleRepository.playerUseAbility(abilityIndex, targetId)) {
                // TODO: Determine more specific animation based on ability effect?
                // Needs more info from repo or ability definition.
                triggerAnimation(AnimationEvent.ABILITY_USED)

                // Handle specific energy-modifying abilities AFTER successful use in repo
                // This assumes the repo doesn't handle these itself.
                when (ability?.name) {
                    "Energy Burst" -> addEnergy(2)
                    "Mana Growth" -> modifyMaxEnergy(1) // Example name
                    // Add other energy-related abilities if needed
                }
            } else {
                Log.e("BattleViewModel", "Ability use failed in repository.")
                addEnergy(energyCost) // Refund
                _awaitingPlayerAction.value = true
                postStatusMessage("Cannot use ${ability?.name ?: "ability"}!") // Show error from repo if possible
            }
        }
    }

    fun onEndTurnAction() {
        Log.d("BattleViewModel", "Action: onEndTurnAction")
        if (!isPlayerTurn.value || _isAnimating.value || !_awaitingPlayerAction.value) return

        _awaitingPlayerAction.value = false // Prevent actions while processing turn end
        battleRepository.endPlayerTurn()
        // Turn change observer will handle transition to ENEMY_ACTION
        // Do NOT manually set game phase here, let the observer do it.
    }

    // --- Animation Handling ---
    private fun triggerAnimation(event: AnimationEvent) {
        // Potentially add more complex logic: queueing animations, checking priorities etc.
        if (!_isAnimating.value) {
            Log.d("BattleViewModel", "Triggering animation: $event")
            _isAnimating.value = true
            _currentAnimation.value = event
        } else {
            Log.w("BattleViewModel", "Animation trigger ignored ($event): Already animating (${_currentAnimation.value})")
            // TODO: Implement animation queueing if needed
        }
    }

    fun onAnimationComplete() {
        val completedAnimation = _currentAnimation.value
        Log.d("BattleViewModel", "onAnimationComplete called for $completedAnimation")
        _currentAnimation.value = null
        _isAnimating.value = false

        // Re-evaluate if player action is expected based on current turn state
        // This helps if animation finished but turn hasn't officially changed yet
        // or if multiple actions per turn are allowed (which they aren't currently)
        if (battleState.value == BattleResult.IN_PROGRESS) {
            _awaitingPlayerAction.value = battleRepository.isPlayerTurn.value
            if (_awaitingPlayerAction.value){
                Log.d("BattleViewModel", "Animation complete, awaiting player action.")
            } else {
                Log.d("BattleViewModel", "Animation complete, awaiting opponent action.")
            }
            // Trigger handling of pending turn transition if one was blocked by animation
            handleTurnTransition(battleRepository.isPlayerTurn.value)
        } else {
            _awaitingPlayerAction.value = false // Battle over
        }
    }

    // --- Game Over ---
    private fun generateGameOverSummary(result: BattleResult): String {
        return when (result) {
            BattleResult.PLAYER_VICTORY -> "You were victorious!"
            BattleResult.ENEMY_VICTORY -> "You were defeated."
            BattleResult.DRAW -> "The battle ended in a draw."
            else -> "Battle concluded."
        }
    }

    fun onGameOverDialogDismiss() {
        _showGameOverDialog.value = false
        // Consider navigating away or resetting state further here
    }

    // --- Cleanup ---
    override fun onCleared() {
        Log.d("BattleViewModel", "onCleared")
        turnObserverJob?.cancel()
        battleResultObserverJob?.cancel()
        statusClearJob?.cancel()
        super.onCleared()
    }
}