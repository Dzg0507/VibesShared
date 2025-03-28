package com.example.vibesshared.ui.ui.cardgame

import android.content.Context
import android.util.Log // Added for logging
import com.example.vibesshared.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random // Needed for sample ability logic

/**
 * Repository responsible for managing the library of all cards AND abilities in the game.
 * This serves as the source of truth for card and ability data.
 */
@Singleton
class CardLibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // --- Card Data ---
    private val _allCards = MutableStateFlow<List<MultiverseCard>>(emptyList())
    val allCards: StateFlow<List<MultiverseCard>> = _allCards.asStateFlow()
    private val cardsById = mutableMapOf<String, MultiverseCard>()

    // --- NEW: Ability Data ---
    private val _allAbilities = MutableStateFlow<Map<String, CardAbility>>(emptyMap())
    val allAbilities: StateFlow<Map<String, CardAbility>> = _allAbilities.asStateFlow()

    init {
        loadCardLibrary()
        // --- NEW: Load abilities ---
        loadAbilityLibrary()
    }

    /**
     * Loads the card library from data source
     */
    private fun loadCardLibrary() {
        val cards = createSampleCardLibrary()
        _allCards.value = cards
        cardsById.clear()
        cards.associateByTo(cardsById) { it.id }
        Log.d("CardLibraryRepo", "Loaded ${cards.size} cards into library.")
    }

    /**
     * Loads the ability library.
     * In a real app, this might load from JSON, database, etc.
     */
    private fun loadAbilityLibrary() {
        val abilities = createSampleAbilities() // Using the sample definitions
        _allAbilities.value = abilities
        Log.d("CardLibraryRepo", "Loaded ${abilities.size} abilities into library.")
    }

    /**
     * Get a card by its ID
     */
    fun getCardById(cardId: String): MultiverseCard? {
        return cardsById[cardId]
    }

    // --- NEW: Ability Lookup Functions ---

    /**
     * Get an ability definition by its unique name/ID.
     *
     * @param name The unique name/ID of the ability.
     * @return The CardAbility definition or null if not found.
     */
    fun getAbilityByName(name: String): CardAbility? {
        val ability = _allAbilities.value[name]
        if (ability == null) {
            Log.w("CardLibraryRepo", "Ability lookup failed for name: $name")
        }
        return ability
    }

    /**
     * Get all defined abilities.
     *
     * @return A list of all CardAbility definitions.
     */
    fun getAllAbilities(): List<CardAbility> {
        return _allAbilities.value.values.toList()
    }

    // --- END NEW: Ability Lookup Functions ---

    /**
     * Get cards by type
     */
    fun getCardsByType(type: CardType): List<MultiverseCard> {
        return _allCards.value.filter { it.type == type }
    }

    /**
     * Get cards by tier
     */
    fun getCardsByTier(tier: CardTier): List<MultiverseCard> {
        return _allCards.value.filter { it.tier == tier }
    }

    /**
     * Get random card from the library, respecting drop rates based on tier
     */
    fun getRandomCard(): MultiverseCard {
        // Generate a random number between 0.0 and 1.0
        val roll = Math.random()

        // Determine the tier based on drop rates
        // Assuming drop rates are defined in CardTier enum and sum close to 1.0
        var cumulativeRate = 0.0f
        val targetTier = CardTier.entries.firstOrNull { tier ->
            cumulativeRate += tier.dropRate
            roll < cumulativeRate
        } ?: CardTier.COMMON // Fallback to COMMON if rates don't sum up or logic fails

        // Get all cards of that tier and pick a random one
        val cardsOfTier = getCardsByTier(targetTier)
        return if (cardsOfTier.isNotEmpty()) {
            cardsOfTier.random()
        } else {
            // Fallback if no cards of this tier (should ideally not happen with good data)
            Log.w("CardLibraryRepo", "No cards found for tier $targetTier during random selection, falling back.")
            _allCards.value.randomOrNull() ?: createSampleCardLibrary().first() // Ensure non-null return
        }
    }

    // --- Sample Card Creation (Remains the same) ---
    private fun createSampleCardLibrary(): List<MultiverseCard> {
        val cards = mutableListOf<MultiverseCard>()

        // Cosmic Epic Card
        cards.add(
            MultiverseCard(
                id = "cosmic_001",
                name = "Celestial Guardian",
                description = "A massive entity born from the heart of a dying star",
                tier = CardTier.EPIC,
                type = CardType.COSMIC,
                power = 8,
                defense = 7,
                health = 15, // Added health
                energy = 7,
                imageResId = R.drawable.charizard, // Using placeholder
                effects = listOf(
                    CardEffect.Damage(5),
                    CardEffect.ApplyStatus("Stellar Aura", 2)
                ),
                keywords = listOf("Guardian", "Stellar", "Cosmic", "Protection", "Energy"),
                abilities = listOf("Star Shield", "Cosmic Regeneration"),
                source = CardSource.SHOP,
                tokenValue = 250,
                level = 1,
                statusEffects = listOf(
                    EffectType.BUFF_DEFENSE,
                    EffectType.SHIELD
                )
            )
        )
        // Temporal Common Card
        cards.add(
            MultiverseCard(
                id = "temporal_001",
                name = "Time Skipper",
                description = "Briefly jumps forward in time to avoid danger.",
                tier = CardTier.COMMON,
                type = CardType.TEMPORAL,
                power = 2,
                defense = 4,
                health = 7, // Added health
                energy = 3,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.ApplyStatus("Evasion", 1) // Assuming Evasion is a status effect
                ),
                keywords = listOf("Temporal", "Dodge", "Quick", "Time"),
                abilities = listOf("Short Hop"), // Example ability name
                source = CardSource.STARTER,
                tokenValue = 40,
                level = 1,
                statusEffects = listOf(EffectType.SHIELD) // Example status effect
            )
        )
        // Tech Rare Card
        cards.add(
            MultiverseCard(
                id = "tech_002",
                name = "Plasma Blaster",
                description = "A versatile weapon firing bolts of superheated plasma.",
                tier = CardTier.RARE,
                type = CardType.TECH,
                power = 7,
                defense = 3,
                health = 9, // Added health
                energy = 5,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.Damage(6)
                ),
                keywords = listOf("Tech", "Energy Weapon", "Ranged", "Plasma"),
                abilities = listOf("Overcharge Shot"), // Example ability name
                source = CardSource.SHOP,
                tokenValue = 160,
                level = 1,
                statusEffects = listOf(EffectType.DAMAGE_OVER_TIME) // Example status effect
            )
        )
        // Psychic Uncommon Card
        cards.add(
            MultiverseCard(
                id = "psychic_001",
                name = "Mind Reader",
                description = "Can anticipate the opponent's moves by peering into their thoughts.",
                tier = CardTier.UNCOMMON,
                type = CardType.PSYCHIC,
                power = 3,
                defense = 5,
                health = 11, // Added health
                energy = 4,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.ApplyStatus("Confusion", 2) // Assuming Confusion is a status effect
                ),
                keywords = listOf("Psychic", "Telepathy", "Control", "Mind"),
                abilities = listOf("Precognition", "Mental Barrier"), // Example ability names
                source = CardSource.REWARD,
                tokenValue = 80,
                level = 1,
                statusEffects = listOf(EffectType.DEBUFF_POWER) // Example status effect
            )
        )
        // Void Epic Card
        cards.add(
            MultiverseCard(
                id = "void_001",
                name = "Abyssal Horror",
                description = "A creature from the void, consuming light and hope.",
                tier = CardTier.EPIC,
                type = CardType.VOID,
                power = 9,
                defense = 6,
                health = 18, // Added health
                energy = 8,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.Damage(7),
                    CardEffect.ApplyStatus("Void Drain", 3) // Assuming Void Drain is a status effect
                ),
                keywords = listOf("Void", "Darkness", "Horror", "Consume"),
                abilities = listOf("Consume Light", "Embrace the Void"), // Example ability names
                source = CardSource.FORGED,
                tokenValue = 280,
                level = 1,
                statusEffects = listOf(EffectType.DEBUFF_DEFENSE, EffectType.DAMAGE_OVER_TIME) // Example status effects
            )
        )
        // Cosmic Uncommon Card
        cards.add(
            MultiverseCard(
                id = "cosmic_002",
                name = "Star Shard",
                description = "A fragment of a fallen star, pulsing with cosmic energy.",
                tier = CardTier.UNCOMMON,
                type = CardType.COSMIC,
                power = 4,
                defense = 4,
                health = 8, // Added health
                energy = 3,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.ModifyEnergy(2) // Gain 2 energy
                ),
                keywords = listOf("Cosmic", "Energy", "Fragment", "Power"),
                abilities = listOf("Energy Burst"), // Example ability name
                source = CardSource.STARTER,
                tokenValue = 70,
                level = 1,
                statusEffects = listOf() // No initial status effects
            )
        )
        // Elemental Common Card
        cards.add(
            MultiverseCard(
                id = "elemental_002",
                name = "Rock Golem",
                description = "A sturdy construct animated from earth and stone.",
                tier = CardTier.COMMON,
                type = CardType.ELEMENTAL,
                power = 3,
                defense = 6,
                health = 12, // Added health
                energy = 4,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(),
                keywords = listOf("Elemental", "Earth", "Defense", "Golem", "Construct"),
                abilities = listOf("Stone Skin"), // Example ability name
                source = CardSource.STARTER,
                tokenValue = 55,
                level = 1,
                statusEffects = listOf(EffectType.BUFF_DEFENSE) // Example status effect
            )
        )
        // Quantum Rare Card
        cards.add(
            MultiverseCard(
                id = "quantum_002",
                name = "Entanglement Fiend",
                description = "Twists quantum states to link allies and foes.",
                tier = CardTier.RARE,
                type = CardType.QUANTUM,
                power = 5,
                defense = 5,
                health = 13, // Added health
                energy = 6,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.ApplyStatus("Linked Fate", 2) // Example status effect
                ),
                keywords = listOf("Quantum", "Entanglement", "Link", "Support"),
                abilities = listOf("Quantum Link", "State Swap"), // Example ability names
                source = CardSource.SHOP,
                tokenValue = 170,
                level = 1,
                statusEffects = listOf()
            )
        )
        // Temporal Epic Card
        cards.add(
            MultiverseCard(
                id = "temporal_002",
                name = "Chronomancer",
                description = "A master of time, weaving temporal spells.",
                tier = CardTier.EPIC,
                type = CardType.TEMPORAL,
                power = 6,
                defense = 5,
                health = 14, // Added health
                energy = 7,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.ApplyStatus("Slow", 2), // Example status effect
                    CardEffect.DrawCards(1)
                ),
                keywords = listOf("Temporal", "Time", "Magic", "Control"),
                abilities = listOf("Time Warp", "Accelerate"), // Example ability names
                source = CardSource.REWARD,
                tokenValue = 260,
                level = 1,
                statusEffects = listOf(EffectType.STUN) // Example status effect
            )
        )
        // Tech Legendary Card
        cards.add(
            MultiverseCard(
                id = "tech_003",
                name = "Omega Sentinel",
                description = "The ultimate robotic defender, equipped with advanced weaponry.",
                tier = CardTier.LEGENDARY,
                type = CardType.TECH,
                power = 10,
                defense = 10,
                health = 25, // Added health
                energy = 9,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.Damage(8),
                    CardEffect.ApplyStatus("Adaptive Armor", 3) // Example status effect
                ),
                keywords = listOf("Tech", "Robot", "Sentinel", "Defense", "Legendary"),
                abilities = listOf("Force Field", "Laser Barrage"), // Example ability names
                source = CardSource.EVENT,
                tokenValue = 550,
                level = 1,
                statusEffects = listOf(EffectType.SHIELD, EffectType.BUFF_DEFENSE) // Example status effects
            )
        )
        // Mythic Common Card
        cards.add(
            MultiverseCard(
                id = "mythic_002", // Corrected ID from mythic_001
                name = "Griffin Hatchling",
                description = "A young griffin, fierce but still learning.",
                tier = CardTier.COMMON,
                type = CardType.MYTHIC,
                power = 4,
                defense = 3,
                health = 9, // Added health
                energy = 3,
                imageResId = R.drawable.logo1, // Placeholder Image
                effects = listOf(
                    CardEffect.Damage(3)
                ),
                keywords = listOf("Mythic", "Griffin", "Flying", "Young"),
                abilities = listOf("Peck"), // Example ability name
                source = CardSource.STARTER,
                tokenValue = 45,
                level = 1,
                statusEffects = listOf()
            )
        )
        // Elemental Rare Card
        cards.add(
            MultiverseCard(
                id = "elemental_001",
                name = "Storm Weaver",
                description = "Master of wind and lightning, commanding nature's fury",
                tier = CardTier.RARE,
                type = CardType.ELEMENTAL,
                power = 6,
                defense = 4,
                health = 10, // Added health
                energy = 5,
                imageResId = R.drawable.charizard, // Placeholder Image
                effects = listOf(
                    CardEffect.Damage(4),
                    CardEffect.DrawCards(1)
                ),
                keywords = listOf("Storm", "Weather", "Nature", "Elemental", "Control"),
                abilities = listOf("Lightning Strike", "Wind Barrier"),
                source = CardSource.SHOP,
                tokenValue = 150,
                level = 1,
                statusEffects = listOf(
                    EffectType.DAMAGE_OVER_TIME,
                    EffectType.BUFF_POWER
                )
            )
        )
        // Tech Uncommon Card
        cards.add(
            MultiverseCard(
                id = "tech_001",
                name = "Nano Swarm",
                description = "Intelligent microscopic robots that adapt and transform",
                tier = CardTier.UNCOMMON,
                type = CardType.TECH, // Changed from Elemental
                power = 4,
                defense = 5,
                health = 8, // Added health
                energy = 3,
                imageResId = R.drawable.charizard, // Placeholder Image
                effects = listOf(
                    CardEffect.ModifyEnergy(2),
                    CardEffect.ApplyStatus("Repair", 1)
                ),
                keywords = listOf(
                    "Robots",
                    "Nanotechnology",
                    "Adaptive",
                    "Repair",
                    "Intelligence"
                ),
                abilities = listOf("Self-Repair", "Transformation"),
                source = CardSource.SHOP,
                tokenValue = 75,
                level = 1,
                statusEffects = listOf(
                    EffectType.HEAL,
                    EffectType.BUFF_DEFENSE
                )
            )
        )
        // Mythic Legendary Card
        cards.add(
            MultiverseCard(
                id = "mythic_001",
                name = "Phoenix of Rebirth",
                description = "Immortal creature that rises from its own ashes",
                tier = CardTier.LEGENDARY,
                type = CardType.MYTHIC, // Changed from Temporal
                power = 9,
                defense = 8,
                health = 20, // Added health
                energy = 8,
                imageResId = R.drawable.charizard, // Placeholder Image
                effects = listOf(
                    CardEffect.Heal(5),
                    CardEffect.Damage(6)
                ),
                keywords = listOf(
                    "Phoenix",
                    "Immortal",
                    "Rebirth",
                    "Mythical",
                    "Transformation"
                ),
                abilities = listOf("Resurrection", "Eternal Flame"),
                source = CardSource.SHOP,
                tokenValue = 500,
                level = 1,
                statusEffects = listOf(
                    EffectType.HEAL,
                    EffectType.REFLECT
                )
            )
        )
        // Quantum Common Card
        cards.add(
            MultiverseCard(
                id = "quantum_001",
                name = "Probability Shifter",
                description = "Manipulates quantum probabilities to alter reality",
                tier = CardTier.COMMON,
                type = CardType.QUANTUM,
                power = 3,
                defense = 3,
                health = 6, // Added health
                energy = 2,
                imageResId = R.drawable.charizard, // Placeholder Image
                effects = listOf(
                    CardEffect.DrawCards(1),
                    CardEffect.ModifyEnergy(1)
                ),
                keywords = listOf(
                    "Quantum",
                    "Probability",
                    "Reality",
                    "Uncertainty",
                    "Physics"
                ),
                abilities = listOf("Reality Warp", "Probability Manipulation"),
                source = CardSource.FORGED,
                tokenValue = 50,
                level = 1,
                statusEffects = listOf(
                    EffectType.CLEANSE,
                    EffectType.BUFF_POWER
                )
            )
        )

        return cards
    }

    // --- NEW: Sample Ability Definitions ---
    // In a real app, this would load from a persistent source (JSON, DB, etc.)
    private fun createSampleAbilities(): Map<String, CardAbility> {
        return mapOf(
            "Star Shield" to CardAbility("Star Shield", "Grants a temporary shield.", 2, 3, { _, _ -> CardEffect.ApplyStatus(EffectType.SHIELD.name, 2) }, TargetType.SELF),
            "Cosmic Regeneration" to CardAbility("Cosmic Regeneration", "Heals over time.", 3, 4, { _, _ -> CardEffect.Heal(3) }, TargetType.SELF),
            "Lightning Strike" to CardAbility("Lightning Strike", "Deals moderate damage.", 2, 2, { _, _ -> CardEffect.Damage(4) }, TargetType.SINGLE),
            "Wind Barrier" to CardAbility("Wind Barrier", "Increases defense.", 1, 3, { _, _ -> CardEffect.ModifyStat("Defense", 2, "SELF") }, TargetType.SELF),
            "Short Hop" to CardAbility("Short Hop", "Briefly becomes harder to hit.", 1, 2, { _, _ -> CardEffect.ApplyStatus(EffectType.SHIELD.name, 1) }, TargetType.SELF),
            "Overcharge Shot" to CardAbility("Overcharge Shot", "Deals high damage.", 3, 3, { _, _ -> CardEffect.Damage(6) }, TargetType.SINGLE),
            "Precognition" to CardAbility("Precognition", "Briefly reduces opponent's power.", 2, 3, { _, _ -> CardEffect.ModifyStat("Power", -2, "TARGET") }, TargetType.SINGLE),
            "Mental Barrier" to CardAbility("Mental Barrier", "Grants a temporary shield.", 2, 3, { _, _ -> CardEffect.ApplyStatus(EffectType.SHIELD.name, 1) }, TargetType.SELF),
            "Consume Light" to CardAbility("Consume Light", "Deals damage.", 4, 4, { _, target -> CardEffect.Damage(5) }, TargetType.SINGLE), // Simplified
            "Embrace the Void" to CardAbility("Embrace the Void", "Applies damage over time.", 3, 3, { _, _ -> CardEffect.ApplyStatus(EffectType.DAMAGE_OVER_TIME.name, 3) }, TargetType.SINGLE),
            "Energy Burst" to CardAbility("Energy Burst", "Gain energy.", 0, 1, { _, _ -> CardEffect.ModifyEnergy(2) }, TargetType.SELF),
            "Stone Skin" to CardAbility("Stone Skin", "Increases defense.", 1, 3, { _, _ -> CardEffect.ModifyStat("Defense", 3, "SELF") }, TargetType.SELF),
            "Quantum Link" to CardAbility("Quantum Link", "Applies reflect status.", 3, 4, { _, _ -> CardEffect.ApplyStatus(EffectType.REFLECT.name, 2) }, TargetType.SELF),
            "State Swap" to CardAbility("State Swap", "Cleanses negative effects.", 2, 3, { _, _ -> CardEffect.ApplyStatus(EffectType.CLEANSE.name, 1) }, TargetType.SELF),
            "Time Warp" to CardAbility("Time Warp", "Applies stun.", 4, 5, { _, _ -> CardEffect.ApplyStatus(EffectType.STUN.name, 1) }, TargetType.SINGLE),
            "Accelerate" to CardAbility("Accelerate", "Draw a card.", 1, 2, { _, _ -> CardEffect.DrawCards(1) }, TargetType.SELF),
            "Force Field" to CardAbility("Force Field", "Grants a strong shield.", 5, 4, { _, _ -> CardEffect.ApplyStatus(EffectType.SHIELD.name, 3) }, TargetType.SELF),
            "Laser Barrage" to CardAbility("Laser Barrage", "Deals heavy damage.", 6, 5, { _, _ -> CardEffect.Damage(8) }, TargetType.SINGLE),
            "Peck" to CardAbility("Peck", "Deals minor damage.", 1, 1, { _, _ -> CardEffect.Damage(2) }, TargetType.SINGLE),
            "Resurrection" to CardAbility("Resurrection", "Revives with some health upon defeat (Passive - requires special handling).", 0, 99, { _, _ -> CardEffect.Heal(1) }, TargetType.SELF), // Passive concept
            "Eternal Flame" to CardAbility("Eternal Flame", "Deals damage and applies DOT.", 4, 3, { _, _ -> CardEffect.Damage(3) /* And Apply DOT - requires multi-effect lambda */ }, TargetType.SINGLE),
            "Reality Warp" to CardAbility("Reality Warp", "Random effect (requires complex logic).", 3, 3, { _, _ -> CardEffect.Damage(Random.nextInt(1,5)) }, TargetType.SINGLE),
            "Probability Manipulation" to CardAbility("Probability Manipulation", "Increases own power.", 2, 3, { _, _ -> CardEffect.ModifyStat("Power", 2, "SELF") }, TargetType.SELF),
            "Stellar Aura" to CardAbility("Stellar Aura", "Increases defense slightly.", 1, 3, { _, _ -> CardEffect.ModifyStat("Defense", 1, "SELF") }, TargetType.SELF),
            "Self-Repair" to CardAbility("Self-Repair", "Heals slightly.", 1, 2, { _, _ -> CardEffect.Heal(2) }, TargetType.SELF),
            "Transformation" to CardAbility("Transformation", "Increases power.", 2, 4, { _, _ -> CardEffect.ModifyStat("Power", 2, "SELF") }, TargetType.SELF),
            "Adaptive Armor" to CardAbility("Adaptive Armor", "Increases defense.", 3, 3, { _, _ -> CardEffect.ModifyStat("Defense", 3, "SELF") }, TargetType.SELF)
        )
    }
    // --- END NEW: Sample Ability Definitions ---

} // End of CardLibraryRepository class