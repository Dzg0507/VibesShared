package com.example.vibesshared.ui.ui.cardgame

import android.util.Log // Added for logging
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job // Added for timer job management
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first // Use first() to get current value
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max // For forging logic

/**
 * Manages UI state and business logic for the shop and forge screen.
 * Includes fixes for purchase and forge operations.
 */
@HiltViewModel
class CardShopViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val cardLibraryRepository: CardLibraryRepository,
    private val cardGameProgressRepository: CardGameProgressionRepository
) : ViewModel() {

    // Player tokens for purchases
    val playerTokens: StateFlow<Int> = cardGameProgressRepository.playerProgress
        .map { it.multiverseTokens }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0 // Default value
        )

    // Shop state
    private val _shopCards = MutableStateFlow<List<MultiverseCard>>(emptyList())
    val shopCards: StateFlow<List<MultiverseCard>> = _shopCards.asStateFlow()
    private val _selectedShopCard = MutableStateFlow<MultiverseCard?>(null)
    val selectedShopCard: StateFlow<MultiverseCard?> = _selectedShopCard.asStateFlow()
    private val _refreshTimerSeconds = MutableStateFlow(3600) // 1 hour default
    val refreshTimerSeconds: StateFlow<Int> = _refreshTimerSeconds.asStateFlow()
    private var refreshTimerJob: Job? = null // Job for timer

    // Forge state
    private val _selectedForgeCardIds = MutableStateFlow<List<String>>(emptyList())
    val selectedForgeCardIds: StateFlow<List<String>> = _selectedForgeCardIds.asStateFlow()
    private val _lastForgedCardResult = MutableStateFlow<MultiverseCard?>(null)
    val lastForgedCardResult: StateFlow<MultiverseCard?> = _lastForgedCardResult.asStateFlow()

    // Common state
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()
    private var statusClearJob: Job? = null
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        startRefreshTimer()
        refreshShop()
    }

    // --- Shop Logic ---

    fun selectShopCard(id: String) {
        _selectedShopCard.value = _shopCards.value.find { it.id == id }
        Log.d("CardShopVM", "Selected shop card: ${_selectedShopCard.value?.name}")
    }

    /**
     * Purchase the currently selected card.
     * Ensures tokens are spent before adding the card.
     */
    fun purchaseSelected() {
        val cardToBuy = _selectedShopCard.value ?: run {
            Log.w("CardShopVM", "Purchase attempt failed: No card selected.")
            return // Nothing selected
        }
        val purchaseCost = cardToBuy.calculateValue() // Use card's value

        Log.d("CardShopVM", "Attempting purchase of ${cardToBuy.name} for $purchaseCost tokens.")

        if (_isLoading.value) {
            Log.w("CardShopVM", "Purchase attempt ignored: Operation already in progress.")
            return // Prevent concurrent operations
        }

        viewModelScope.launch {
            _isLoading.value = true
            var purchaseSuccess = false
            try {
                // 1. Check current tokens (using first() to get latest value)
                val currentTokens = playerTokens.first() // Get current value from flow
                if (currentTokens < purchaseCost) {
                    postStatusMessage("Not enough tokens! Need $purchaseCost.")
                    Log.w("CardShopVM", "Purchase failed: Insufficient tokens (Have: $currentTokens, Need: $purchaseCost).")
                } else {
                    // 2. Spend tokens first
                    val spent = cardGameProgressRepository.spendTokens(purchaseCost)
                    if (spent) {
                        Log.d("CardShopVM", "Tokens spent successfully ($purchaseCost).")
                        // 3. Add card to collection if tokens were spent
                        val added = cardRepository.addCard(cardToBuy.id)
                        if (added) {
                            Log.i("CardShopVM", "Card '${cardToBuy.name}' purchased and added to collection.")
                            postStatusMessage("${cardToBuy.name} purchased!")
                            _selectedShopCard.value = null // Clear selection after successful purchase
                            purchaseSuccess = true
                            // Note: Removed redundant call to updateCardsCollectedCount
                        } else {
                            // This case should ideally not happen if addCard is robust, but handle it.
                            Log.e("CardShopVM", "Purchase Error: Failed to add card '${cardToBuy.name}' to repository after spending tokens!")
                            postStatusMessage("Error adding card to collection!")
                            // Attempt to refund tokens if adding failed? Requires careful implementation.
                            // cardGameProgressionRepository.addTokens(purchaseCost) // Potential refund
                        }
                    } else {
                        // spendTokens should ideally prevent this if check was done, but handle anyway
                        Log.e("CardShopVM", "Purchase Error: spendTokens failed unexpectedly.")
                        postStatusMessage("Purchase failed: Could not spend tokens.")
                    }
                }
            } catch (e: Exception) {
                Log.e("CardShopVM", "Error during purchase process for ${cardToBuy.name}", e)
                postStatusMessage("An error occurred during purchase.")
                // Consider refunding if tokens were spent but an error occurred before adding card
            } finally {
                _isLoading.value = false
                Log.d("CardShopVM", "Purchase operation finished. Success: $purchaseSuccess")
            }
        }
    }

    fun requestRefresh() {
        // Optional: Add a cost or cooldown to manual refresh?
        Log.d("CardShopVM", "Manual shop refresh requested.")
        refreshShop() // Refresh immediately
        resetRefreshTimer() // Reset the timer
    }

    private fun refreshShop() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("CardShopVM", "Refreshing shop...")
            try {
                // Use card library repo directly
                val allCards = cardLibraryRepository.allCards.first() // Get current list
                if (allCards.isEmpty()){
                    Log.e("CardShopVM", "Cannot refresh shop: Card Library is empty!")
                    postStatusMessage("Error: Card library unavailable.")
                    _isLoading.value = false
                    return@launch
                }

                // Example logic: Select 6 random cards, maybe biased by player level
                val playerLevel = cardGameProgressRepository.playerProgress.first().level
                val shopCardCount = 6
                // Simple random selection for now
                val newShopCards = allCards.shuffled().take(shopCardCount)

                _shopCards.value = newShopCards
                _selectedShopCard.value = null // Clear selection on refresh
                Log.i("CardShopVM", "Shop refreshed with ${newShopCards.size} cards.")
                // postStatusMessage("Shop refreshed!") // Maybe too noisy

            } catch (e: Exception) {
                Log.e("CardShopVM", "Failed to refresh shop", e)
                postStatusMessage("Error refreshing shop.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startRefreshTimer() {
        refreshTimerJob?.cancel() // Cancel existing timer
        refreshTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Wait 1 second
                val currentSeconds = _refreshTimerSeconds.value
                if (currentSeconds > 0) {
                    _refreshTimerSeconds.value = currentSeconds - 1
                } else {
                    Log.i("CardShopVM", "Shop refresh timer reached zero. Auto-refreshing.")
                    refreshShop()
                    _refreshTimerSeconds.value = 3600 // Reset to 1 hour
                }
            }
        }
    }

    private fun resetRefreshTimer() {
        _refreshTimerSeconds.value = 3600 // Reset to 1 hour
        startRefreshTimer() // Restart the countdown
        Log.d("CardShopVM", "Shop refresh timer reset.")
    }


    // --- Forge Logic ---

    fun selectForgeCard(id: String) {
        val currentSelection = _selectedForgeCardIds.value.toMutableList()
        if (currentSelection.contains(id)) {
            currentSelection.remove(id)
            Log.d("CardShopVM", "Deselected card $id for forging. Selection: $currentSelection")
        } else if (currentSelection.size < 2) {
            // Check if player actually owns the card before adding
            if (cardRepository.getCardCount(id) > 0) {
                currentSelection.add(id)
                Log.d("CardShopVM", "Selected card $id for forging. Selection: $currentSelection")
            } else {
                Log.w("CardShopVM", "Cannot select card $id for forging: Player does not own it.")
                postStatusMessage("You don't own that card!")
            }
        } else {
            Log.d("CardShopVM", "Cannot select card $id: Already 2 cards selected for forging.")
            postStatusMessage("Select exactly 2 cards to forge.")
        }
        _selectedForgeCardIds.value = currentSelection
    }

    /**
     * Execute forge operation.
     * Verifies ownership, removes input cards, adds result card.
     */
    fun executeForge() {
        if (_selectedForgeCardIds.value.size != 2) {
            postStatusMessage("Please select exactly 2 cards to forge.")
            return
        }
        if (_isLoading.value) {
            Log.w("CardShopVM", "Forge attempt ignored: Operation already in progress.")
            return // Prevent concurrent operations
        }

        val firstCardId = _selectedForgeCardIds.value[0]
        val secondCardId = _selectedForgeCardIds.value[1]

        Log.d("CardShopVM", "Attempting to forge $firstCardId and $secondCardId.")

        viewModelScope.launch {
            _isLoading.value = true
            var forgeSuccess = false
            try {
                // 1. Verify ownership and get card data
                val firstCardCount = cardRepository.getCardCount(firstCardId)
                val secondCardCount = cardRepository.getCardCount(secondCardId)

                // Handle trying to forge the *same* card ID twice if only one copy exists
                if (firstCardId == secondCardId && firstCardCount < 2) {
                    Log.w("CardShopVM", "Forge failed: Need at least 2 copies to forge the same card ($firstCardId).")
                    postStatusMessage("Need 2 copies to forge the same card.")
                    _isLoading.value = false
                    return@launch
                }
                if (firstCardCount < 1 || secondCardCount < 1) {
                    Log.w("CardShopVM", "Forge failed: Player does not own one or both selected cards (Count1: $firstCardCount, Count2: $secondCardCount).")
                    postStatusMessage("You don't own one of the selected cards.")
                    _isLoading.value = false
                    return@launch
                }

                val firstCard = cardLibraryRepository.getCardById(firstCardId)
                val secondCard = cardLibraryRepository.getCardById(secondCardId)

                if (firstCard == null || secondCard == null) {
                    Log.e("CardShopVM", "Forge Error: Could not find card definitions for $firstCardId or $secondCardId in library.")
                    postStatusMessage("Error retrieving card data.")
                    _isLoading.value = false
                    return@launch
                }

                // 2. Determine Forge Result (Improved Logic)
                val forgeResult = determineForgeResult(firstCard, secondCard)

                if (forgeResult == null) {
                    Log.w("CardShopVM", "Forge failed: No suitable result card could be determined.")
                    postStatusMessage("Forging failed to produce a result.")
                    // Do NOT remove input cards if forging fails to find a result
                    _isLoading.value = false
                    return@launch
                }

                Log.d("CardShopVM", "Forge result determined: ${forgeResult.name} (Tier: ${forgeResult.tier})")

                // 3. Remove the two input cards from collection
                // Use repository function which handles counts
                val removedFirst = cardRepository.removeCard(firstCardId)
                val removedSecond = cardRepository.removeCard(secondCardId)

                if (!removedFirst || !removedSecond) {
                    // This indicates a logic error or race condition if checks passed earlier
                    Log.e("CardShopVM", "Forge Error: Failed to remove input cards (Removed1: $removedFirst, Removed2: $removedSecond) after checks passed!")
                    postStatusMessage("Error removing cards for forge.")
                    // Attempt to re-add cards if removal failed partially? Complex recovery.
                    if (removedFirst) cardRepository.addCard(firstCardId) // Potential rollback
                    if (removedSecond) cardRepository.addCard(secondCardId) // Potential rollback
                    _isLoading.value = false
                    return@launch
                }
                Log.d("CardShopVM", "Successfully removed input cards: $firstCardId, $secondCardId")

                // 4. Add the new forged card to the collection
                val addedResult = cardRepository.addCard(forgeResult.id)
                if (!addedResult) {
                    Log.e("CardShopVM", "Forge Error: Failed to add result card '${forgeResult.name}' to collection after removing input cards!")
                    postStatusMessage("Error adding forged card!")
                    // Attempt rollback?
                    cardRepository.addCard(firstCardId)
                    cardRepository.addCard(secondCardId)
                    _isLoading.value = false
                    return@launch
                }

                Log.i("CardShopVM", "Forge successful! ${firstCard.name} + ${secondCard.name} -> ${forgeResult.name}")

                // 5. Update state
                _lastForgedCardResult.value = forgeResult
                _selectedForgeCardIds.value = emptyList() // Clear selection
                postStatusMessage("Forge successful: ${forgeResult.name} created!")
                forgeSuccess = true

            } catch (e: Exception) {
                Log.e("CardShopVM", "Error during forge process", e)
                postStatusMessage("An error occurred during forging.")
            } finally {
                _isLoading.value = false
                Log.d("CardShopVM", "Forge operation finished. Success: $forgeSuccess")
            }
        }
    }

    /**
     * Determines the result of forging two cards.
     * Example logic: Find a card in the library that is one tier higher
     * than the highest tier input card, possibly matching type or stats loosely.
     */
    private suspend fun determineForgeResult(card1: MultiverseCard, card2: MultiverseCard): MultiverseCard? {
        // 1. Determine target tier (e.g., one above the max tier of inputs)
        val maxInputTierOrdinal = max(card1.tier.ordinal, card2.tier.ordinal)
        val targetTierOrdinal = (maxInputTierOrdinal + 1).coerceAtMost(CardTier.MYTHIC.ordinal)
        val targetTier = CardTier.entries[targetTierOrdinal]

        Log.d("CardShopVM","Forge Target Tier: $targetTier (from inputs ${card1.tier}, ${card2.tier})")

        // 2. Get potential result cards from library of the target tier
        val potentialResults = cardLibraryRepository.getCardsByTier(targetTier)
            .filter { it.isCollectible } // Ensure the result is obtainable

        if (potentialResults.isEmpty()) {
            Log.w("CardShopVM", "No collectible cards found in library for target tier: $targetTier")
            // Optional: Try same tier? Or allow specific recipes?
            return null // No suitable card found
        }

        // 3. Select a result (e.g., randomly from the potential results)
        // More advanced: Could bias selection based on input card types/stats
        val result = potentialResults.random()
        Log.d("CardShopVM", "Selected forge result: ${result.name}")
        return result
    }


    fun clearForgeResult() {
        // Only clear selection if there's no result displayed, otherwise let user dismiss dialog
        if (_lastForgedCardResult.value == null) {
            _selectedForgeCardIds.value = emptyList()
            Log.d("CardShopVM", "Forge selection cleared.")
        } else {
            // Clear result after dialog dismissal
            _lastForgedCardResult.value = null
            Log.d("CardShopVM", "Forge result cleared after dialog.")

        }
    }

    // --- Common ---

    private fun postStatusMessage(message: String) {
        statusClearJob?.cancel()
        _statusMessage.value = message
        Log.i("CardShopVM_Status", message) // Log status messages
        statusClearJob = viewModelScope.launch {
            delay(3000) // Clear after 3 seconds
            if (_statusMessage.value == message) {
                _statusMessage.value = null
            }
        }
    }

    override fun onCleared() {
        Log.d("CardShopVM", "onCleared")
        refreshTimerJob?.cancel()
        statusClearJob?.cancel()
        super.onCleared()
    }
}