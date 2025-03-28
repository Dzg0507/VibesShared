package com.example.vibesshared.ui.ui.cardgame

// File: CardInPlay.kt (Final - Corrected & Complete)

import android.util.Log
// Note: Ensure CardLibraryRepository is injectable or accessible where CardInPlay is created.

/**
 * Represents the dynamic state of a card during battle, including current health,
 * active status effects (with duration and magnitude), and ability cooldowns.
 */
data class CardInPlay(
    val card: MultiverseCard,
    var currentHealth: Int = card.health,
    // Store active effects with Pair<Duration, Magnitude>
    val activeEffects: MutableMap<EffectType, Pair<Int, Int>> = mutableMapOf(),
    // Abilities are now initialized externally (e.g., by BattleRepository)
    // using initializeAbilitiesInPlay and passed to the constructor or set later.
    // This list holds the runtime state (cooldowns) for the card's abilities.
    val abilities: List<AbilityInPlay>
) {
    // Primary constructor now requires the initialized 'abilities' list.

    companion object {
        /**
         * Initializes the AbilityInPlay list for a given card using the CardLibraryRepository.
         * This function MUST be called with a valid CardLibraryRepository instance
         * when creating a CardInPlay object.
         *
         * @param card The MultiverseCard whose abilities need to be initialized.
         * @param cardLibraryRepo The repository instance containing ability definitions.
         * @return A list of AbilityInPlay objects ready for use.
         */
        fun initializeAbilitiesInPlay(
            card: MultiverseCard,
            cardLibraryRepo: CardLibraryRepository // The repository instance is required
        ): List<AbilityInPlay> {
            val abilityNames = card.abilities
            if (abilityNames.isEmpty()) {
                return emptyList() // No abilities to initialize
            }

            Log.d("CardInPlay.Companion", "Initializing abilities for card '${card.name}': ${abilityNames.joinToString()}")

            return abilityNames.mapNotNull { name ->
                // --- Actual Ability Lookup ---
                cardLibraryRepo.getAbilityByName(name)?.let { abilityDef ->
                    Log.d("CardInPlay.Companion", "Found definition for ability '$name', creating AbilityInPlay.")
                    AbilityInPlay(abilityDef) // Create runtime state wrapper
                } ?: run {
                    // Log error if an ability name on the card doesn't exist in the library
                    Log.e("CardInPlay.Companion", "CRITICAL: Ability definition NOT FOUND for name '$name' on card '${card.name}' (ID: ${card.id}). Check CardLibraryRepository ability definitions.")
                    null // Ability definition not found - skip this ability
                }
                // --- End Actual Ability Lookup ---
            }
        }
    }

    // Example Usage Hint (in BattleRepository or wherever CardInPlay is created):
    /*
    fun createCardInPlay(cardData: MultiverseCard): CardInPlay {
        val initializedAbilities = CardInPlay.initializeAbilitiesInPlay(cardData, this.cardLibraryRepository) // Pass the repo instance
        return CardInPlay(
            card = cardData,
            currentHealth = cardData.health,
            activeEffects = mutableMapOf(),
            abilities = initializedAbilities // Pass the initialized list
        )
    }
    */

    /**
     * Reduce health, considering defense, shield, and other effects.
     * Logs the damage calculation steps.
     *
     * @param amount Raw damage amount before mitigation.
     * @return Int The actual damage dealt after mitigation.
     */
    fun applyDamage(amount: Int): Int {
        if (amount <= 0) return 0

        // 1. Check for Shield
        activeEffects[EffectType.SHIELD]?.let { shieldData ->
            val remainingDuration = shieldData.first - 1
            if (remainingDuration <= 0) {
                activeEffects.remove(EffectType.SHIELD)
                Log.d("CardInPlay.applyDamage", "[${card.name}] Shield broken by attack.")
            } else {
                activeEffects[EffectType.SHIELD] = shieldData.copy(first = remainingDuration)
                Log.d("CardInPlay.applyDamage", "[${card.name}] Shield absorbed attack. Stacks left: $remainingDuration")
            }
            return 0 // Shield absorbs all damage
        }

        // 2. Calculate effective defense (considering buffs/debuffs)
        val defenseModifier = activeEffects.entries
            .filter { it.key == EffectType.BUFF_DEFENSE || it.key == EffectType.DEBUFF_DEFENSE }
            .sumOf { (type, data) -> if (type == EffectType.BUFF_DEFENSE) data.second else -data.second }
        val effectiveDefense = (card.defense + defenseModifier).coerceAtLeast(0)
        Log.v("CardInPlay.applyDamage", "[${card.name}] Effective Defense: ${card.defense} + ($defenseModifier) = $effectiveDefense")

        // 3. Calculate damage reduction (e.g., defense reduces damage by half its value)
        val damageReduction = effectiveDefense / 2 // Example formula
        Log.v("CardInPlay.applyDamage", "[${card.name}] Damage Reduction: $damageReduction (from $effectiveDefense defense)")

        // 4. Calculate effective damage (minimum 1 damage if not shielded)
        val effectiveDamage = (amount - damageReduction).coerceAtLeast(1)
        Log.v("CardInPlay.applyDamage", "[${card.name}] Effective Damage: ($amount raw - $damageReduction reduction) = $effectiveDamage (min 1)")

        // 5. Apply damage to health
        val previousHealth = currentHealth
        currentHealth = (currentHealth - effectiveDamage).coerceAtLeast(0)
        Log.d("CardInPlay.applyDamage", "[${card.name}] Took $effectiveDamage damage. HP: $previousHealth -> $currentHealth")

        // 6. Handle Reflect (if present) - This logic is likely better handled
        //    in BattleRepository after damage is dealt, as it needs the attacker info.

        return effectiveDamage
    }

    /**
     * Adds or updates a status effect on the card.
     * Handles stacking logic for duration and magnitude.
     * Applies instant effects like Heal/Cleanse directly if duration is 1.
     *
     * @param effectType The type of effect to apply.
     * @param duration The number of turns the effect should last.
     * @param magnitude The strength of the effect (e.g., amount healed, stat change).
     */
    fun addEffect(effectType: EffectType, duration: Int, magnitude: Int = 0) {
        if (duration <= 0 && effectType != EffectType.CLEANSE) { // Allow Cleanse with duration 0 or 1
            Log.w("CardInPlay.addEffect", "[${card.name}] Attempted to add effect ${effectType.name} with invalid duration: $duration")
            return
        }

        // Handle instant effects first if duration is 1
        if (duration == 1) {
            when (effectType) {
                EffectType.HEAL -> {
                    val healedAmount = (currentHealth + magnitude).coerceAtMost(card.health) - currentHealth
                    if (healedAmount > 0) {
                        currentHealth += healedAmount
                        Log.d("CardInPlay.addEffect", "[${card.name}] Instantly healed $healedAmount HP. Current HP: $currentHealth")
                    }
                    // Decide if a 1-turn "Heal" effect should still be added to the map (e.g., for display)
                    // If not, return here. Current implementation adds it below.
                    // return
                }
                EffectType.CLEANSE -> {
                    val effectsToRemove = activeEffects.keys.filter { !it.isPositive } // Use the extension property
                    if (effectsToRemove.isNotEmpty()) {
                        effectsToRemove.forEach { activeEffects.remove(it) }
                        Log.d("CardInPlay.addEffect", "[${card.name}] Instantly cleansed effects: ${effectsToRemove.joinToString()}")
                    }
                    return // Don't add Cleanse to map for instant effect
                }
                // Other effects might still be added for 1 turn duration below
                else -> {} // Continue to standard logic
            }
        }

        // --- Standard logic for adding/stacking effects ---
        val currentEffect = activeEffects[effectType]
        val newDuration: Int
        val newMagnitude: Int

        // Define stacking behavior (example: refresh duration, add magnitude)
        if (currentEffect != null) {
            // Example: Refresh/Extend duration to the new duration if longer, otherwise keep old? Add magnitude?
            // This logic depends heavily on game design rules.
            // Simple stacking: Add durations, Add magnitudes
            newDuration = currentEffect.first + duration
            newMagnitude = currentEffect.second + magnitude
            Log.d("CardInPlay.addEffect", "[${card.name}] Stacking ${effectType.name}. Old(Dur:${currentEffect.first},Mag:${currentEffect.second}), Added(Dur:$duration,Mag:$magnitude) -> Result(Dur:$newDuration,Mag:$newMagnitude)")
        } else {
            newDuration = duration
            newMagnitude = magnitude
            Log.d("CardInPlay.addEffect", "[${card.name}] Applying new effect ${effectType.name} (Dur:$newDuration, Mag:$newMagnitude)")
        }

        // Apply limits if necessary (e.g., max stacks for shield? Max debuff magnitude?)
        // Example limit:
        // if (effectType == EffectType.DEBUFF_POWER && newMagnitude < -5) { newMagnitude = -5 }

        activeEffects[effectType] = Pair(newDuration, newMagnitude)
    }


    /**
     * Processes the start-of-turn or end-of-turn ticks for effects and cooldowns.
     * Applies periodic effects like Damage Over Time.
     * Reduces duration of active effects and cooldowns.
     * Removes expired effects.
     */
    fun tickTurn() {
        Log.v("CardInPlay.tickTurn", "[${card.name}] Ticking turn. Start Effects: ${activeEffects.entries.joinToString { "${it.key}(${it.value.first},${it.value.second})" }}")

        // --- Apply Periodic Effects ---
        if (currentHealth <= 0) {
            Log.v("CardInPlay.tickTurn", "[${card.name}] Skipping periodic effects, already defeated.")
        } else {
            // Damage Over Time
            activeEffects[EffectType.DAMAGE_OVER_TIME]?.let { dotData ->
                val dotAmount = dotData.second // Get magnitude
                if (dotAmount > 0) {
                    Log.d("CardInPlay.tickTurn", "[${card.name}] Taking $dotAmount damage from DOT.")
                    applyDamage(dotAmount) // applyDamage handles shield checks etc.
                }
            }
            // Heal Over Time (if HEAL effect has duration > 1)
            activeEffects[EffectType.HEAL]?.let { hotData ->
                val hotAmount = hotData.second
                if (hotAmount > 0 && currentHealth < card.health) { // Only heal if not full health
                    val healedAmount = (currentHealth + hotAmount).coerceAtMost(card.health) - currentHealth
                    if (healedAmount > 0) {
                        currentHealth += healedAmount
                        Log.d("CardInPlay.tickTurn", "[${card.name}] Healed $healedAmount HP from Heal over Time. Current HP: $currentHealth")
                    }
                }
            }
            // Add other periodic effects here (e.g., Energy Regen - requires modifying external state)
        }


        // --- Tick Down Durations ---
        if (activeEffects.isEmpty() && abilities.all { it.isReady }) {
            Log.v("CardInPlay.tickTurn", "[${card.name}] No active effects or cooldowns to tick.")
            return // Optimization: skip if nothing to tick
        }

        val effectsToRemove = mutableListOf<EffectType>()
        // Iterate over a copy of keys to avoid ConcurrentModificationException if effects are removed
        val currentEffectKeys = activeEffects.keys.toList()

        currentEffectKeys.forEach { type ->
            activeEffects[type]?.let { data ->
                val newDuration = data.first - 1
                if (newDuration <= 0) {
                    effectsToRemove.add(type)
                    activeEffects.remove(type) // Remove directly from original map
                } else {
                    activeEffects[type] = data.copy(first = newDuration) // Update duration in original map
                }
            }
        }
        if (effectsToRemove.isNotEmpty()) {
            Log.d("CardInPlay.tickTurn", "[${card.name}] Effects expired: ${effectsToRemove.joinToString()}")
        }

        // --- Tick Down Ability Cooldowns ---
        var abilitiesNowReady = false
        abilities.forEach {
            it.tickCooldown()
            if(it.wasJustReadied) abilitiesNowReady = true
        }
        if (abilitiesNowReady) {
            val readyAbilityNames = abilities.filter { it.isReady }.joinToString { it.ability.name }
            Log.d("CardInPlay.tickTurn", "[${card.name}] Abilities now ready: $readyAbilityNames")
        }

        Log.v("CardInPlay.tickTurn", "[${card.name}] Turn tick finished. End Effects: ${activeEffects.entries.joinToString { "${it.key}(${it.value.first},${it.value.second})" }}")
    }

    // Helper to check if stunned
    fun isStunned(): Boolean = activeEffects.containsKey(EffectType.STUN)

    /**
     * Calculates the current power considering buffs and debuffs.
     * @return Int The effective power value.
     */
    fun getCurrentPower(): Int {
        val powerModifier = activeEffects.entries
            .filter { it.key == EffectType.BUFF_POWER || it.key == EffectType.DEBUFF_POWER }
            .sumOf { (type, data) -> if (type == EffectType.BUFF_POWER) data.second else -data.second }
        // Ensure power doesn't go below 0, though some games allow negative stats.
        return (card.power + powerModifier).coerceAtLeast(0)
    }

    /**
     * Calculates the current defense considering buffs and debuffs.
     * @return Int The effective defense value.
     */
    fun getCurrentDefense(): Int {
        val defenseModifier = activeEffects.entries
            .filter { it.key == EffectType.BUFF_DEFENSE || it.key == EffectType.DEBUFF_DEFENSE }
            .sumOf { (type, data) -> if (type == EffectType.BUFF_DEFENSE) data.second else -data.second }
        // Ensure defense doesn't go below 0.
        return (card.defense + defenseModifier).coerceAtLeast(0)
    }
}

/**
 * Extension property to check if an effect type is generally positive (buff, heal, shield, etc.)
 * Needed for the Cleanse logic.
 */
val EffectType.isPositive: Boolean
    get() = when (this) {
        EffectType.BUFF_POWER, EffectType.BUFF_DEFENSE, EffectType.HEAL,
        EffectType.SHIELD, EffectType.REFLECT, EffectType.CLEANSE -> true
        else -> false // Debuffs, DOT, Stun are not positive
    }

// Note: The definitions for EffectType, CardAbility, AbilityInPlay, CardEffect,
// TargetType, MultiverseCard are expected to be accessible from other files
// (e.g., MultiVerseCardModels.kt, CardAbility.kt).
// They are NOT redefined here.