package com.example.vibesshared.ui.ui.cardgame

// File: GameUtils.kt

import com.example.vibesshared.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Collection of stateless utility functions for calculations, validation, etc.
 */

/**
 * Type effectiveness map.
 * Key: Attacking type
 * Value: Map of defending type to effectiveness multiplier
 */
val TYPE_EFFECTIVENESS_MAP: Map<CardType, Map<CardType, Float>> = mapOf(
    CardType.COSMIC to mapOf(
        CardType.COSMIC to 1.0f,
        CardType.ELEMENTAL to 0.8f,
        CardType.TEMPORAL to 1.5f,
        CardType.QUANTUM to 1.2f,
        CardType.TECH to 1.0f,
        CardType.MYTHIC to 0.7f,
        CardType.PSYCHIC to 1.3f,
        CardType.VOID to 0.6f
    ),
    CardType.ELEMENTAL to mapOf(
        CardType.COSMIC to 1.2f,
        CardType.ELEMENTAL to 1.0f,
        CardType.TEMPORAL to 0.8f,
        CardType.QUANTUM to 0.7f,
        CardType.TECH to 1.5f,
        CardType.MYTHIC to 1.3f,
        CardType.PSYCHIC to 0.6f,
        CardType.VOID to 1.0f

    ),
    CardType.TEMPORAL to mapOf(
        CardType.COSMIC to 0.7f,
        CardType.ELEMENTAL to 1.2f,
        CardType.TEMPORAL to 1.0f,
        CardType.QUANTUM to 1.5f,
        CardType.TECH to 0.8f,
        CardType.MYTHIC to 0.6f,
        CardType.PSYCHIC to 1.0f,
        CardType.VOID to 1.3f
    ),
    CardType.QUANTUM to mapOf(
        CardType.COSMIC to 0.8f,
        CardType.ELEMENTAL to 1.3f,
        CardType.TEMPORAL to 0.7f,
        CardType.QUANTUM to 1.0f,
        CardType.TECH to 1.2f,
        CardType.MYTHIC to 1.0f,
        CardType.PSYCHIC to 0.6f,
        CardType.VOID to 1.5f
    ),
    CardType.TECH to mapOf(
        CardType.COSMIC to 1.0f,
        CardType.ELEMENTAL to 0.7f,
        CardType.TEMPORAL to 1.2f,
        CardType.QUANTUM to 1.5f,
        CardType.TECH to 1.3f,
        CardType.MYTHIC to 0.8f,
        CardType.PSYCHIC to 0.6f,
        CardType.VOID to 1.0f
    ),
    CardType.MYTHIC to mapOf(
        CardType.COSMIC to 1.3f,
        CardType.ELEMENTAL to 1.0f,
        CardType.TEMPORAL to 0.7f,
        CardType.QUANTUM to 1.0f,
        CardType.TECH to 0.6f,
        CardType.MYTHIC to 1.2f,
        CardType.PSYCHIC to 1.5f,
        CardType.VOID to 0.8f
    ),
    CardType.PSYCHIC to mapOf(
        CardType.COSMIC to 0.7f,
        CardType.ELEMENTAL to 1.5f,
        CardType.TEMPORAL to 1.0f,
        CardType.QUANTUM to 0.6f,
        CardType.TECH to 1.2f,
        CardType.MYTHIC to 0.8f,
        CardType.PSYCHIC to 1.3f,
        CardType.VOID to 1.0f
    ),
    CardType.VOID to mapOf(
        CardType.COSMIC to 1.5f,
        CardType.ELEMENTAL to 0.7f,
        CardType.TEMPORAL to 1.3f,
        CardType.QUANTUM to 0.8f,
        CardType.TECH to 1.0f,
        CardType.MYTHIC to 1.2f,
        CardType.PSYCHIC to 1.0f,
        CardType.VOID to 0.6f
    )
)

/**
 * Calculates damage based on attacker's power, defender's defense, and type effectiveness.
 *
 * @param attackerCard Card attacking card
 * @param defenderCard Card defending card
 * @param additionalModifiers Float any additional modifiers to apply
 * @return Int the calculated damage
 */
fun calculateDamage(
    attackerCard: MultiverseCard,
    defenderCard: MultiverseCard,
    additionalModifiers: Float = 1.0f
): Int {
    // Base damage calculation
    val baseDamage = attackerCard.power - (defenderCard.defense / 2)

    // Apply type effectiveness
    val effectiveness = getTypeEffectiveness(attackerCard.type, defenderCard.type)

    // Calculate raw damage
    val rawDamage = baseDamage * effectiveness * additionalModifiers

    // Apply small random variation (±10%)
    val randomFactor = 0.9f + Random.nextFloat() * 0.2f

    // Ensure damage is at least 1 (unless effectiveness is 0)
    return max(if (effectiveness > 0) 1 else 0, (rawDamage * randomFactor).toInt())
}

/**
 * Gets the type effectiveness multiplier for an attack.
 *
 * @param attackerType CardType the type of the attacker
 * @param defenderType CardType the type of the defender
 * @return Float effectiveness multiplier
 */
fun getTypeEffectiveness(attackerType: CardType, defenderType: CardType): Float {
    return TYPE_EFFECTIVENESS_MAP[attackerType]?.get(defenderType) ?: 1.0f
}

/**
 * Validates if a deck meets all game rules.
 *
 * @param deck CardDeck the deck to validate
 * @return Pair<Boolean, String> validation result and error message if invalid
 */
fun validateDeckComposition(deck: CardDeck): Pair<Boolean, String> {
    // Check deck size
    if (deck.cardIds.size < CardDeck.MIN_CARDS) {
        return Pair(false, "Deck must contain at least ${CardDeck.MIN_CARDS} cards.")
    }

    if (deck.cardIds.size > CardDeck.MAX_CARDS) {
        return Pair(false, "Deck cannot contain more than ${CardDeck.MAX_CARDS} cards.")
    }

    // Check for duplicate card IDs
    val uniqueCardIds = deck.cardIds.toSet()
    if (uniqueCardIds.size != deck.cardIds.size) {
        return Pair(false, "Deck contains duplicate cards.")
    }

    // Placeholder for additional rules
    // Examples of other rules to implement:
    // - Card rarity limits (e.g., max 2 legendary cards)
    // - Energy curve requirements
    // - Type distribution requirements

    return Pair(true, "Deck is valid.")
}

/**
 * Generates a new unique ID for cards, decks, etc.
 *
 * @param prefix String optional prefix for the ID
 * @return String the generated ID
 */
fun generateRandomId(prefix: String = ""): String {
    val timestamp = System.currentTimeMillis()
    val randomPart = Random.nextInt(1000, 10000)
    return "${prefix}${timestamp}_$randomPart"
}

/**
 * Calculates XP required for a given player level.
 *
 * @param level Int the level to calculate XP for
 * @return Int XP required to reach the level
 */
fun xpForLevel(level: Int): Int {
    // Base XP (level 1 requires 0 XP)
    if (level <= 1) return 0

    // Formula: Base 1000 XP + 500 * (level - 1) with exponential scaling for higher levels
    return (1000 + 500 * (level - 1) * (1.1f.pow(level - 1))).toInt()
}

/**
 * Random card generator for shop and rewards.
 *
 * @param playerLevel Int the player's current level
 * @param count Int number of cards to generate
 * @param tierBias Float bias towards higher tier cards (0.0-1.0)
 * @return List<MultiverseCard> the generated cards
 */
fun generateRandomCards(playerLevel: Int, count: Int, tierBias: Float = 0.0f): List<MultiverseCard> {
    val cards = mutableListOf<MultiverseCard>()

    // Higher level players get better cards
    val levelFactor = min(playerLevel / 20f, 1.0f)

    repeat(count) {
        // Generate a random card based on player level and tier bias
        cards.add(generateRandomCard(playerLevel, levelFactor + tierBias))
    }

    return cards
}

/**
 * Generates a single random card.
 *
 * @param playerLevel Int the player's current level
 * @param rareCardChance Float chance modifier for rare cards (0.0-1.0)
 * @return MultiverseCard the generated card
 */
private fun generateRandomCard(playerLevel: Int, rareCardChance: Float): MultiverseCard {
    // Determine tier based on player level and chance modifier
    val tier = determineCardTier(playerLevel, rareCardChance)

    // Determine type randomly
    val type = CardType.entries.toTypedArray().random()

    // Scale stats based on tier
    val tierMultiplier = when (tier) {
        CardTier.COMMON -> 1.0f
        CardTier.RARE -> 1.2f
        CardTier.EPIC -> 1.5f
        CardTier.LEGENDARY -> 2.0f
        CardTier.MYTHIC -> 2.5f
        CardTier.UNCOMMON -> 1.1f
    }

    // Base stats with some randomization
    val basePower = (3 + Random.nextInt(5)) * tierMultiplier
    val baseDefense = (2 + Random.nextInt(4)) * tierMultiplier
    val baseHealth = (8 + Random.nextInt(8)) * tierMultiplier
    val baseEnergy = max(1, Random.nextInt(1, 5))

    // Higher tiers are more valuable
    val tokenValue = when (tier) {
        CardTier.COMMON -> 50 + Random.nextInt(50)
        CardTier.RARE -> 150 + Random.nextInt(100)
        CardTier.EPIC -> 300 + Random.nextInt(200)
        CardTier.LEGENDARY -> 600 + Random.nextInt(400)
        CardTier.MYTHIC -> 1200 + Random.nextInt(800)
        CardTier.UNCOMMON -> 75 + Random.nextInt(75)
    }

    // Generate a unique ID for the card
    val id = generateRandomId("card_")

    // Create a name for the card based on type and tier
    val adjective = getRandomAdjective(tier)
    val noun = getRandomNoun(type)
    val name = "$adjective $noun"

    // Create a description for the card
    val description = "A $tier ${type.toString().lowercase()} card with mystical powers."

    // In a full implementation, abilities would be generated here
    // For simplicity, using empty ability list
    val abilities = emptyList<CardAbility>()

    // In a full implementation, special effects would be generated here
    val specialEffect = null

    return MultiverseCard(
        id = id,
        name = name,
        description = description,
        tier = tier,
        type = type,
        power = basePower.toInt(),
        defense = baseDefense.toInt(),
        health = baseHealth.toInt(),
        energy = baseEnergy,
        abilities = listOf(),
        source = CardSource.SHOP,
        tokenValue = tokenValue,
        imageResId = R.drawable.charizard,
        effects = listOf(
            CardEffect.Damage(5),
            CardEffect.ApplyStatus("Stellar Aura", 2)
        ),
        keywords = listOf("Robot", "Builder")
    )
}

/**
 * Determines a card tier based on player level and a chance modifier.
 *
 * @param playerLevel Int the player's current level
 * @param rareCardChance Float chance modifier for rare cards (0.0-1.0)
 * @return CardTier the determined tier
 */
private fun determineCardTier(playerLevel: Int, rareCardChance: Float): CardTier {
    // Base chances affected by player level
    val levelFactor = min(playerLevel / 20f, 1.0f)

    // Apply the rare card chance modifier
    val adjustedChance = rareCardChance + levelFactor

    // Roll for tier
    val roll = Random.nextFloat()

    return when {
        roll < 0.6f - (adjustedChance * 0.3f) -> CardTier.COMMON
        roll < 0.85f - (adjustedChance * 0.2f) -> CardTier.RARE
        roll < 0.95f - (adjustedChance * 0.1f) -> CardTier.EPIC
        roll < 0.995f - (adjustedChance * 0.05f) -> CardTier.LEGENDARY
        else -> CardTier.MYTHIC
    }
}

/**
 * Gets a random adjective appropriate for the card tier.
 *
 * @param tier CardTier the card tier
 * @return String a random adjective
 */
private fun getRandomAdjective(tier: CardTier): String {
    return when (tier) {
        CardTier.COMMON -> listOf(
            "Simple", "Ordinary", "Basic", "Common", "Standard",
            "Regular", "Plain", "Modest", "Humble", "Starter"
        ).random()

        CardTier.RARE -> listOf(
            "Unusual", "Rare", "Valuable", "Uncommon", "Skilled",
            "Refined", "Polished", "Exceptional", "Notable", "Distinctive"
        ).random()

        CardTier.EPIC -> listOf(
            "Epic", "Mighty", "Formidable", "Dominant", "Heroic",
            "Glorious", "Magnificent", "Majestic", "Phenomenal", "Extraordinary"
        ).random()

        CardTier.LEGENDARY -> listOf(
            "Legendary", "Ancient", "Mythic", "Immortal", "Transcendent",
            "Fabled", "Eternal", "Unforgotten", "Timeless", "Divine"
        ).random()

        CardTier.MYTHIC -> listOf(
            "Celestial", "Primordial", "Absolute", "Ultimate", "Supreme",
            "Omnipotent", "Godlike", "Apocalyptic", "Cataclysmic", "Inevitable"
        ).random()

        CardTier.UNCOMMON -> TODO()
    }
}

/**
 * Gets a random noun appropriate for the card type.
 *
 * @param type CardType the card type
 * @return String a random noun
 */
private fun getRandomNoun(type: CardType): String {
    return when (type) {
        CardType.COSMIC -> listOf(
            "Starborn", "Voidwalker", "Nebula", "Galaxy", "Pulsar",
            "Quasar", "Supernova", "Astronomer", "Constellation", "Comet"
        ).random()

        CardType.ELEMENTAL -> listOf(
            "Firestorm", "Tsunami", "Earthquake", "Hurricane", "Volcano",
            "Inferno", "Blizzard", "Thunderbolt", "Tempest", "Whirlwind"
        ).random()

        CardType.TEMPORAL -> listOf(
            "Chronologist", "Timekeeper", "Watchmaker", "Hourglass", "Pendulum",
            "Calendar", "Clock", "Timepiece", "Epoch", "Era"
        ).random()

        CardType.QUANTUM -> listOf(
            "Particle", "Wave", "Observer", "Uncertainty", "Superposition",
            "Entanglement", "Probability", "Wavefunction", "Quantum", "Schrodinger"
        ).random()

        CardType.TECH -> listOf(
            "Cyborg", "Android", "Robot", "Mech", "Automaton",
            "Drone", "Golem", "Construct", "Engine", "Machine"
        ).random()

        CardType.MYTHIC -> listOf(
            "Ghost", "Phantom", "Wraith", "Specter", "Spirit",
            "Apparition", "Shade", "Haunt", "Poltergeist", "Banshee"
        ).random()

        CardType.PSYCHIC -> listOf(
            "Portal", "Gateway", "Rift", "Vortex", "Door",
            "Passage", "Threshold", "Conduit", "Bridge", "Tunnel"
        ).random()

        CardType.VOID -> listOf(
            "Ancient", "Titan", "Elder", "Progenitor", "Ancestor",
            "Firstborn", "Origin", "Genesis", "Archetype", "Primeval"
        ).random()
    }
}

/**
 * Formats a duration in seconds to a human-readable string.
 *
 * @param seconds Int duration in seconds
 * @return String formatted time string (e.g., "2h 30m 15s")
 */
fun formatTimeString(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m ${secs}s"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}

/**
 * Clamps a value between minimum and maximum.
 *
 * @param value T the value to clamp
 * @param minValue T the minimum value
 * @param maxValue T the maximum value
 * @return T the clamped value
 */
fun <T : Comparable<T>> clamp(value: T, minValue: T, maxValue: T): T {
    return when {
        value < minValue -> minValue
        value > maxValue -> maxValue
        else -> value
    }
}