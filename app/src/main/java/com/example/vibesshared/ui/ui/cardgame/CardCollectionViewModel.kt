package com.example.vibesshared.ui.ui.cardgame

// File: CardCollectionViewModel.kt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Manages UI state for card collection and deck editing screens.
 */
@HiltViewModel
class CardCollectionViewModel @Inject constructor(
    val cardRepository: CardRepository
) : ViewModel() {  // Add ViewModel() extension here

    // Filtered/sorted player cards
    private val _displayedCards = MutableStateFlow<List<MultiverseCard>>(emptyList())
    val displayedCards: StateFlow<List<MultiverseCard>> = _displayedCards.asStateFlow()

    // Current filters
    private val _activeFilters = MutableStateFlow<List<CardFilter>>(emptyList())
    val activeFilters: StateFlow<List<CardFilter>> = _activeFilters.asStateFlow()

    // Card selected for detail view
    private val _selectedCardDetails = MutableStateFlow<MultiverseCard?>(null)
    val selectedCardDetails: StateFlow<MultiverseCard?> = _selectedCardDetails.asStateFlow()

    // Player decks from repository
    val playerDecks = cardRepository.playerDecks

    // Deck being edited
    private val _selectedDeckForEditing = MutableStateFlow<CardDeck?>(null)
    val selectedDeckForEditing: StateFlow<CardDeck?> = _selectedDeckForEditing.asStateFlow()

    // Card IDs in the deck editor
    private val _deckEditorCardIds = MutableStateFlow<List<String>>(emptyList())
    val deckEditorCardIds: StateFlow<List<String>> = _deckEditorCardIds.asStateFlow()

    init {
        // Initialize displayed cards based on repository data
        cardRepository.playerCards.map { cards ->
            applyFiltersToCards(cards, _activeFilters.value)
        }
    }

    /**
     * Update activeFilters, recompute displayedCards.
     *
     * @param filter CardFilter to be applied or removed
     */
    fun applyFilter(filter: CardFilter) {
        val currentFilters = _activeFilters.value.toMutableList()

        // If filter is already applied, remove it; otherwise, add it
        val existingIndex = currentFilters.indexOfFirst { it.type == filter.type && it.value == filter.value }
        if (existingIndex >= 0) {
            currentFilters.removeAt(existingIndex)
        } else {
            currentFilters.add(filter)
        }

        _activeFilters.value = currentFilters

        // Reapply filters to cards
        _displayedCards.value = applyFiltersToCards(cardRepository.playerCards.value, currentFilters)
    }

    /**
     * Update selectedCardDetails.
     *
     * @param cardId String ID of card to select
     */
    fun selectCard(cardId: String) {
        _selectedCardDetails.value = cardRepository.playerCards.value.find { it.id == cardId }
    }

    /**
     * Update selectedDeckForEditing, load deckEditorCardIds.
     *
     * @param deckId String ID of deck to select
     */
    fun selectDeck(deckId: String) {
        val selectedDeck = cardRepository.playerDecks.value.find { it.id == deckId }
        _selectedDeckForEditing.value = selectedDeck

        // Load the card IDs for editing
        if (selectedDeck != null) {
            _deckEditorCardIds.value = selectedDeck.cardIds
        } else {
            _deckEditorCardIds.value = emptyList()
        }
    }

    /**
     * Update deckEditorCardIds.
     *
     * @param cardId String ID of card to add to deck
     */
    fun addCardToEditorDeck(cardId: String) {
        // Check if card is already in the deck or if deck is at maximum size
        val currentCardIds = _deckEditorCardIds.value
        if (currentCardIds.contains(cardId) ||
            currentCardIds.size >= CardDeck.MAX_CARDS) {
            return
        }

        // Update the card IDs
        _deckEditorCardIds.value = currentCardIds + cardId
    }

    /**
     * Update deckEditorCardIds.
     *
     * @param cardId String ID of card to remove from deck
     */
    fun removeCardFromEditorDeck(cardId: String) {
        _deckEditorCardIds.value = _deckEditorCardIds.value.filter { it != cardId }
    }

    /**
     * Validate deckEditorCardIds, create/update CardDeck, call CardRepository.saveDeck.
     *
     * @param newName String? optional new name for the deck
     */
    fun saveEditedDeck(newName: String? = null) {
        val currentDeck = _selectedDeckForEditing.value
        val cardIds = _deckEditorCardIds.value

        // Validate deck size
        if (cardIds.size < CardDeck.MIN_CARDS || cardIds.size > CardDeck.MAX_CARDS) {
            // Handle error (in a real app, we would expose this as a validation state)
            return
        }

        // Create or update the deck
        val updatedDeck = currentDeck?.// Update existing deck
        copy(
            name = newName ?: currentDeck.name,
            cardIds = cardIds
        )
            ?: // Create new deck
            CardDeck(
                id = "deck_${System.currentTimeMillis()}",
                name = newName ?: "New Deck",
                cardIds = cardIds,
                theme = DeckTheme.STANDARD,
                updatedAt = System.currentTimeMillis() // Default theme
            )

        // Save the deck
        cardRepository.saveDeck(updatedDeck)

        // Update selected deck and card IDs
        _selectedDeckForEditing.value = updatedDeck
        _deckEditorCardIds.value = updatedDeck.cardIds
    }

    /**
     * Call CardRepository.setActiveDeck.
     *
     * @param deckId String ID of deck to set as active
     */
    fun setActiveDeck(deckId: String) {
        cardRepository.setActiveDeck(deckId)
    }

    /**
     * Applies filters to a list of cards.
     *
     * @param cards List<MultiverseCard> original cards
     * @param filters List<CardFilter> filters to apply
     * @return List<MultiverseCard> filtered cards
     */
    private fun applyFiltersToCards(cards: List<MultiverseCard>, filters: List<CardFilter>): List<MultiverseCard> {
        if (filters.isEmpty()) {
            return cards
        }

        return cards.filter { card ->
            // A card must match ALL active filters
            filters.all { filter ->
                when (filter.type) {
                    FilterType.TYPE -> card.type.toString() == filter.value
                    FilterType.TIER -> card.tier.toString() == filter.value
                    FilterType.POWER_MIN -> card.power >= (filter.value.toIntOrNull() ?: 0)
                    FilterType.ENERGY_MAX -> card.energy <= (filter.value.toIntOrNull() ?: Int.MAX_VALUE)
                    // Add more filter types as needed
                }
            }
        }
    }
}

/**
 * Represents a filter that can be applied to cards.
 *
 * @property type FilterType category of filter
 * @property value String value to filter by
 */
data class CardFilter(
    val type: FilterType,
    val value: String
)

/**
 * Types of filters that can be applied to cards.
 */
enum class FilterType {
    TYPE,       // Filter by card type (COSMIC, ELEMENTAL, etc.)
    TIER,       // Filter by card tier (COMMON, RARE, etc.)
    POWER_MIN,  // Filter by minimum power
    ENERGY_MAX  // Filter by maximum energy cost
    // Add more filter types as needed
}