package com.example.vibesshared.ui.ui.cardgame

// File: MainMenuViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController // Import NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject // Make sure Inject is imported

/**
 * Manages UI state for the main menu/game hub.
 */
@HiltViewModel
class MainMenuViewModel @Inject constructor( // Inject annotation needed for Hilt
    val cardRepository: CardRepository,
    cardGameProgressionRepository: CardGameProgressionRepository,
    // Removed: private val navigator: Navigator, // REMOVE Navigator dependency
) : ViewModel() {
    // From repository
    val playerProgress = cardGameProgressionRepository.playerProgress

    // From repository
    val cardCount = cardRepository.playerCards.map { it.size }

    // From repository
    val deckCount = cardRepository.playerDecks.map { it.size }

    // Badge for shop/rewards
    private val _showNewContentIndicator = MutableStateFlow(false)
    val showNewContentIndicator: StateFlow<Boolean> = _showNewContentIndicator.asStateFlow()

    init {
        // Load data and check for updates
        loadData()
    }

    /**
     * Start observing repositories.
     */
    fun loadData() {
        // In a real app, we would use coroutines to load data
        // For example:
        // viewModelScope.launch {
        //     playerProgressionRepository.loadProgress()
        //     cardRepository.loadData()
        //     checkForUpdates()
        // }
    }

    /**
     * Logic to determine showNewContentIndicator.
     */
    fun checkForUpdates() {
        // Check for new content (e.g., new shop items, rewards available)
        // This is a placeholder implementation
        _showNewContentIndicator.value = true
    }

    // --- UPDATED Navigation Functions ---
    // They now accept NavController directly

    /**
     * Trigger navigation event/action to Battle screen.
     */
    fun navigateToBattle(navController: NavController) {
        navController.navigate(CardGameDestinations.BATTLE) {
            popUpTo(CardGameDestinations.MAIN_MENU) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Trigger navigation event/action to Collection screen.
     */
    fun navigateToCollection(navController: NavController) {
        navController.navigate(CardGameDestinations.COLLECTION){
            popUpTo(CardGameDestinations.MAIN_MENU) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Trigger navigation event/action to Shop/Forge screen.
     */
    fun navigateToShopForge(navController: NavController) {
        navController.navigate(CardGameDestinations.SHOP_FORGE){
            popUpTo(CardGameDestinations.MAIN_MENU) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }

        // Reset new content indicator when visiting the shop
        _showNewContentIndicator.value = false
    }

    /**
     * Trigger navigation event/action to Profile screen.
     */
    fun navigateToProfile(navController: NavController) {
        navController.navigate(CardGameDestinations.PROFILE){
            popUpTo(CardGameDestinations.MAIN_MENU) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

// --- REMOVED Navigator Interface and Screen Enum ---
// These are no longer needed here as we use NavController directly
// and destinations are defined in CardGameDestinations / main Navigation.kt