package com.example.vibesshared.ui.ui.cardgame


// Import other necessary annotations or classes if required

// --- ENUMS CONSOLIDATED HERE ---

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable
import kotlin.math.abs



/**
 * Display modes for rendering cards (MOVED HERE)
 */
enum class CardDisplayMode {
    HAND,       // Card in player's hand
    BOARD,      // Card on the game board
    GRID_ITEM,  // Card in collection grid
    DECK_LIST,  // Card in deck list view
    FULL        // Full-size card detail view
}




/**
 * Card effects that can modify gameplay
 */
@Serializable
sealed class CardEffect {
    // ... (Content of CardEffect sealed class remains the same) ...
    abstract val description: String

    @Serializable data class Damage(val amount: Int) : CardEffect() { override val description: String = "Deal $amount damage" }
    @Serializable data class Heal(val amount: Int) : CardEffect() { override val description: String = "Heal $amount points" }
    @Serializable data class DrawCards(val count: Int) : CardEffect() { override val description: String = "Draw $count card${if (count > 1) "s" else ""}" }
    @Serializable data class ModifyEnergy(val amount: Int) : CardEffect() { override val description: String = "${if (amount > 0) "Gain" else "Lose"} ${
        abs(amount)
    } energy" }
    @Serializable data class ApplyStatus(val status: String, val duration: Int) : CardEffect() { override val description: String = "Apply $status for $duration turn${if (duration > 1) "s" else ""}" }
    @Serializable data class ModifyStat(val stat: String, val amount: Int, val target: String) : CardEffect() { override val description: String = "${if (amount > 0) "Increase" else "Decrease"} $target $stat by ${
        abs(amount)
    }" }
}


/**
 * Represents the thematic affiliation of a card.
 */
enum class CardType(val displayName: String, val description: String) {
    COSMIC("Cosmic", "Harnesses the power of stars and galaxies"),
    ELEMENTAL("Elemental", "Commands natural forces like fire and water"),
    TECH("Tech", "Utilizes futuristic technology and gadgets"),
    PSYCHIC("Psychic", "Wields mental powers and illusions"),
    MYTHIC("Mythic", "Derives power from ancient legends and myths"),
    QUANTUM("Quantum", "Manipulates reality through quantum mechanics"),
    TEMPORAL("Temporal", "Controls the flow of time"),
    VOID("Void", "Channels the energy of the void between dimensions");

    companion object {
        fun fromString(value: String): CardType = values().find { it.name == value } ?: COSMIC
    }
}

/**
 * Represents the rarity/power category of a card.
 * Added dropRate property.
 */
enum class CardTier(val dropRate: Float) {
    COMMON(0.50f),      // 50% chance
    UNCOMMON(0.25f),    // 25% chance
    RARE(0.15f),        // 15% chance
    EPIC(0.07f),        // 7% chance
    LEGENDARY(0.03f),   // 3% chance
    MYTHIC(0.00f);      // 0% chance (Adjust probabilities as needed)
    // Note: Ensure the logic using dropRate handles the sum correctly if it doesn't equal 1.0
}

/**
 * Represents the origin of a card in the player's collection.
 */
enum class CardSource {
    QUEST, SHOP, FORGED, STARTER, EVENT, REWARD, ACHIEVEMENT
}

/**
 * Visual themes that can be applied to a deck.
 */
enum class DeckTheme {
    STANDARD, COSMIC_ART, ELEMENTAL_FLAMES, TEMPORAL_VORTEX, QUANTUM_GRID,
    BIONIC_TECH, SPECTRAL_MIST, DIMENSIONAL_RIFT, PRIMORDIAL_ANCIENT
}

/**
 * Types of effects that can be applied to cards.
 */
enum class EffectType {
    BUFF_POWER, BUFF_DEFENSE, DEBUFF_POWER, DEBUFF_DEFENSE,
    HEAL, DAMAGE_OVER_TIME, STUN, SHIELD, REFLECT, CLEANSE
}

/**
 * Target types for abilities.
 */
enum class TargetType {
    SINGLE, SELF, ALL_ENEMIES, ALL_ALLIES, RANDOM_ENEMY, RANDOM_ALLY
}





/**
 * Represents a card in the multiverse card game - CONSOLIDATED VERSION
 */
@Serializable
data class MultiverseCard(
    val id: String,
    val name: String,
    val description: String,
    val tier: CardTier,
    val type: CardType,
    val power: Int,
    val defense: Int,
    val health: Int,
    val energy: Int,
    @DrawableRes val imageResId: Int? = null, // Use Int? consistently
    val effects: List<CardEffect> = emptyList(),
    val isCollectible: Boolean = true,
    val keywords: List<String> = emptyList(),
    val abilities: List<String> = emptyList(), // Using List<String> for now
    val statusEffects: List<EffectType> = emptyList(),
    val source: CardSource = CardSource.STARTER,
    val tokenValue: Int = 0,
    val level: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isPowerful(): Boolean = power + defense > 10

    fun calculateValue(): Int = when (tier) {
        CardTier.COMMON -> 10
        CardTier.UNCOMMON -> 25
        CardTier.RARE -> 50
        CardTier.EPIC -> 100
        CardTier.LEGENDARY -> 250
        CardTier.MYTHIC -> 500
    } + (power + defense) / 2

    fun getStatsSummary(): String {
        return "P:$power D:$defense H:$health E:$energy"
    }
}

/**
 * Represents a player's collection of cards
 */
@Serializable
data class CardCollection(
    val userId: String,
    val cards: Map<String, Int>, // Card ID -> Quantity
    val favoriteCardIds: List<String> = emptyList()
) {
    fun totalCards(): Int = cards.values.sum()
    fun uniqueCards(): Int = cards.size
}

/**
 * Represents a player's deck
 */
@Serializable
data class CardDeck(
    val id: String,
    val name: String,
    val cardIds: List<String>,
    val theme: DeckTheme = DeckTheme.STANDARD,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val MIN_CARDS = 15
        const val MAX_CARDS = 30
    }
}