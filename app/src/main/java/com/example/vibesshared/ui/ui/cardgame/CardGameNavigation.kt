package com.example.vibesshared.ui.ui.cardgame

/**
 * Contains all navigation destinations used in the Card Game feature.
 * These constants are referenced by the main navigation setup and various ViewModels.
 */
object CardGameDestinations {
    // Main destinations
    const val MAIN_MENU = "main_menu"
    const val BATTLE = "battle"
    const val COLLECTION = "collection"
    const val SHOP_FORGE = "shop_forge"
    const val PROFILE = "profile"

    // Nested destinations
    const val DECK_BUILDING = "deck_building"
    const val DECK_EDITOR = "deck_editor"
    const val CARD_DETAILS = "card_details"

    // Route patterns with arguments
    const val DECK_EDITOR_ROUTE = "$DECK_EDITOR/{deckId}"
    const val CARD_DETAILS_ROUTE = "$CARD_DETAILS/{cardId}"
}