package com.example.vibesshared.ui.ui.cardgame

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing player's card collection and decks.
 * This is separate from the CardLibraryRepository, which manages the entire game's card library.
 */
@Singleton
class CardRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    val cardLibraryRepository: CardLibraryRepository
) {
    // Player's card collection
    private val _playerCards = MutableStateFlow<List<MultiverseCard>>(emptyList())
    val playerCards: StateFlow<List<MultiverseCard>> = _playerCards.asStateFlow()

    // Player's decks
    private val _playerDecks = MutableStateFlow<List<CardDeck>>(emptyList())
    val playerDecks: StateFlow<List<CardDeck>> = _playerDecks.asStateFlow()

    // Player's active deck
    private val _activeDeckId = MutableStateFlow<String?>(null)
    val activeDeckId: StateFlow<String?> = _activeDeckId.asStateFlow()

    // Counts of cards by ID
    private val _cardCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cardCounts: StateFlow<Map<String, Int>> = _cardCounts.asStateFlow()

    init {
        // Initialize with sample data - in a real app, this would load from persistent storage
        loadPlayerCollection()
        loadPlayerDecks()
    }

    /**
     * Load player's card collection
     */
    private fun loadPlayerCollection() {
        // Since allCards is a Flow, we can't access .value directly
        // Instead, we'll use a default list of sample cards for initialization

        // Create some sample cards for the player's collection
        val sampleCards = listOfNotNull(
            cardLibraryRepository.getCardById("cosmic_001"),
            cardLibraryRepository.getCardById("elemental_001"),
            cardLibraryRepository.getCardById("tech_001"),
            cardLibraryRepository.getCardById("mythic_001"),
            cardLibraryRepository.getCardById("quantum_001")
        ) // Filter out any null values (cards that don't exist)

        // Add some duplicates to demonstrate card counts
        val cardCounts = mutableMapOf<String, Int>()
        sampleCards.forEach { card ->
            cardCounts[card.id] = (1..3).random()
        }

        _cardCounts.value = cardCounts
        _playerCards.value = sampleCards
    }

    /**
     * Load player's decks
     */
    private fun loadPlayerDecks() {
        // In a real app, load from database

        // --- MODIFICATION START ---
        // Get card IDs directly from the library for the starter deck
        val starterCardIds = cardLibraryRepository.allCards.value // Access StateFlow value
            .shuffled() // Shuffle the library
            .take(CardDeck.MIN_CARDS) // Take the minimum required cards
            .map { it.id }
        // --- MODIFICATION END ---

        // Check if we actually got enough cards (library might be too small)
        if (starterCardIds.size < CardDeck.MIN_CARDS) {
            Log.e("CardRepository", "FATAL: Cannot create starter deck! CardLibrary has fewer than ${CardDeck.MIN_CARDS} cards.")
            // Handle this error appropriately - maybe add more cards to library or return an empty deck list
            _playerDecks.value = emptyList()
            _activeDeckId.value = null
            return
        }

        val defaultDeck = CardDeck(
            id = "default_deck",
            name = "Starter Deck",
            // Use the IDs obtained directly from the library
            cardIds = starterCardIds, // <-- Use the new list here
            theme = DeckTheme.STANDARD,
            updatedAt = System.currentTimeMillis()
        )

        _playerDecks.value = listOf(defaultDeck)
        _activeDeckId.value = defaultDeck.id
    }


    /**
     * Add a card to the player's collection
     *
     * @param cardId ID of the card to add
     * @return True if successfully added
     */
    fun addCard(cardId: String): Boolean {
        val card = cardLibraryRepository.getCardById(cardId) ?: return false

        // Update card counts
        val currentCounts = _cardCounts.value.toMutableMap()
        currentCounts[cardId] = (currentCounts[cardId] ?: 0) + 1
        _cardCounts.value = currentCounts

        // Update player cards list if this is a new card
        if (!_playerCards.value.any { it.id == cardId }) {
            _playerCards.value = _playerCards.value + card
        }

        return true
    }

    /**
     * Remove a card from the player's collection
     *
     * @param cardId ID of the card to remove
     * @return True if successfully removed
     */
    fun removeCard(cardId: String): Boolean {
        val currentCounts = _cardCounts.value.toMutableMap()
        val currentCount = currentCounts[cardId] ?: 0

        if (currentCount <= 0) {
            return false
        }

        // Decrease count
        if (currentCount == 1) {
            currentCounts.remove(cardId)
            // Also remove from player cards list
            _playerCards.value = _playerCards.value.filter { it.id != cardId }
        } else {
            currentCounts[cardId] = currentCount - 1
        }

        _cardCounts.value = currentCounts
        return true
    }

    /**
     * Get the card count for a specific card
     *
     * @param cardId ID of the card
     * @return Number of copies owned
     */
    fun getCardCount(cardId: String): Int {
        return _cardCounts.value[cardId] ?: 0
    }

    /**
     * Save a deck (create or update)
     *
     * @param deck The deck to save
     * @return True if successfully saved
     */
    fun saveDeck(deck: CardDeck): Boolean {
        // Validate deck
        if (deck.cardIds.size < CardDeck.MIN_CARDS || deck.cardIds.size > CardDeck.MAX_CARDS) {
            return false
        }

        // Check if deck exists
        val currentDecks = _playerDecks.value.toMutableList()
        val existingIndex = currentDecks.indexOfFirst { it.id == deck.id }

        if (existingIndex >= 0) {
            // Update existing deck
            currentDecks[existingIndex] = deck.copy(updatedAt = System.currentTimeMillis())
        } else {
            // Add new deck
            currentDecks.add(deck)
        }

        _playerDecks.value = currentDecks
        return true
    }

    /**
     * Delete a deck
     *
     * @param deckId ID of the deck to delete
     * @return True if successfully deleted
     */
    fun deleteDeck(deckId: String): Boolean {
        if (_playerDecks.value.none { it.id == deckId }) {
            return false
        }

        _playerDecks.value = _playerDecks.value.filter { it.id != deckId }

        // If active deck was deleted, set first available deck as active
        if (_activeDeckId.value == deckId) {
            _activeDeckId.value = _playerDecks.value.firstOrNull()?.id
        }

        return true
    }

    /**
     * Set the active deck
     *
     * @param deckId ID of the deck to set as active
     * @return True if successfully set
     */
    fun setActiveDeck(deckId: String): Boolean {
        if (_playerDecks.value.none { it.id == deckId }) {
            return false
        }

        _activeDeckId.value = deckId
        return true
    }

    /**
     * Get the active deck
     *
     * @return The active deck or null if none set
     */
    fun getActiveDeck(): CardDeck? {
        val activeId = _activeDeckId.value ?: return null
        return _playerDecks.value.find { it.id == activeId }
    }

    /**
     * Check if player has enough cards for a deck
     *
     * @param deckCardIds List of card IDs in the deck
     * @return True if player has enough copies of each card
     */
    fun hasEnoughCardsForDeck(deckCardIds: List<String>): Boolean {
        // Count how many of each card is needed
        val neededCounts = deckCardIds.groupingBy { it }.eachCount()

        // Check against player's collection
        return neededCounts.all { (cardId, neededCount) ->
            val ownedCount = _cardCounts.value[cardId] ?: 0
            ownedCount >= neededCount
        }
    }

    /**
     * Get all cards of a specific type
     *
     * @param type The card type to filter by
     * @return List of cards of that type in player's collection
     */
    fun getCardsByType(type: CardType): List<MultiverseCard> {
        return _playerCards.value.filter { it.type == type }
    }

    /**
     * Get all cards of a specific tier
     *
     * @param tier The card tier to filter by
     * @return List of cards of that tier in player's collection
     */
    fun getCardsByTier(tier: CardTier): List<MultiverseCard> {
        return _playerCards.value.filter { it.tier == tier }
    }

    /**
     * Get a random pack of cards (simulating a pack opening)
     *
     * @param packSize Number of cards in the pack
     * @return List of randomly selected cards
     */
    fun getRandomCardPack(packSize: Int = 5): List<MultiverseCard> {
        val pack = mutableListOf<MultiverseCard>()

        repeat(packSize) {
            pack.add(cardLibraryRepository.getRandomCard())
        }

        return pack
    }

    /**
     * Open a pack and add cards to collection
     *
     * @param packSize Number of cards in the pack
     * @return List of cards that were added to collection
     */
    fun openCardPack(packSize: Int = 5): List<MultiverseCard> {
        val newCards = getRandomCardPack(packSize)

        newCards.forEach { card ->
            addCard(card.id)
        }

        return newCards
    }

    // Add this function inside the CardRepository class in ui/cardgame/CardRepository.kt

    fun createDummyDeck(): CardDeck {
        // Get some random card IDs from the library (ensure enough cards exist)
        // Or define a fixed set of IDs for the dummy deck
        val dummyCardIds = cardLibraryRepository.allCards.value // Access the flow's value
            .shuffled()
            .take(CardDeck.MIN_CARDS) // Take the minimum required cards
            .map { it.id }

        // If not enough cards in library, fallback or handle error
        if (dummyCardIds.size < CardDeck.MIN_CARDS) {
            // You might want to throw an error or return a default deck with placeholders
            // For now, let's just return a deck with the cards found
            Log.w("CardRepository", "Warning: Not enough cards in library to create a full dummy deck.")
        }

        return CardDeck(
            id = "dummy_deck_${System.currentTimeMillis()}",
            name = "Opponent Deck",
            cardIds = dummyCardIds,
            theme = DeckTheme.STANDARD // Or any theme
        )
    }

// Make sure you have this import if not already present
}