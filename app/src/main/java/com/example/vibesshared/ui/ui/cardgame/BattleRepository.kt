package com.example.vibesshared.ui.ui.cardgame

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the state and turn logic for an active battle instance.
 * Applies game rules, handles actions, and determines battle outcomes.
 */
@Singleton
class BattleRepository @Inject constructor(
    // Inject CardRepository, which contains CardLibraryRepository
    private val cardRepository: CardRepository
    // No longer needs direct CardLibraryRepository injection if accessed via cardRepository
    // private val cardLibraryRepository: CardLibraryRepository - Removed if using cardRepository.cardLibraryRepository
) {
    // State flows (remain the same)
    private val _battleState = MutableStateFlow<BattleResult>(BattleResult.NOT_STARTED)
    val battleState: StateFlow<BattleResult> = _battleState.asStateFlow()

    private val _playerHand = MutableStateFlow<List<MultiverseCard>>(emptyList())
    val playerHand: StateFlow<List<MultiverseCard>> = _playerHand.asStateFlow()

    private val _opponentHand = MutableStateFlow<List<MultiverseCard>>(emptyList())
    val opponentHand: StateFlow<List<MultiverseCard>> = _opponentHand.asStateFlow()

    private val _activePlayerCard = MutableStateFlow<CardInPlay?>(null)
    val activePlayerCard: StateFlow<CardInPlay?> = _activePlayerCard.asStateFlow()

    private val _activeOpponentCard = MutableStateFlow<CardInPlay?>(null)
    val activeOpponentCard: StateFlow<CardInPlay?> = _activeOpponentCard.asStateFlow()

    private val _isPlayerTurn = MutableStateFlow(true)
    val isPlayerTurn: StateFlow<Boolean> = _isPlayerTurn.asStateFlow()

    private val _turn = MutableStateFlow(1)
    val turn: StateFlow<Int> = _turn.asStateFlow()

    private val _battleLog = MutableStateFlow<List<BattleEvent>>(emptyList())
    val battleLog: StateFlow<List<BattleEvent>> = _battleLog.asStateFlow()

    // Deck tracking (remain the same)
    private val _playerDeck = MutableStateFlow<List<MultiverseCard>>(emptyList())
    private val _opponentDeck = MutableStateFlow<List<MultiverseCard>>(emptyList())

    private var lastTurnEndTime = 0L

    /**
     * Initialize state, shuffle, draw hands.
     */
    fun startBattle(playerDeck: CardDeck, opponentDeck: CardDeck) {
        Log.i("BattleRepository", "Attempting to start battle...")

        // --- Load actual cards using the injected CardRepository's library ---
        val playerCards = playerDeck.cardIds.mapNotNull { cardId ->
            cardRepository.cardLibraryRepository.getCardById(cardId)?.also {
                Log.v("BattleRepository", "Loaded player card: ${it.name} (ID: $cardId)")
            } ?: run {
                Log.e("BattleRepository", "Failed to load player card with ID: $cardId")
                null
            }
        }
        val opponentCards = opponentDeck.cardIds.mapNotNull { cardId ->
            cardRepository.cardLibraryRepository.getCardById(cardId)?.also {
                Log.v("BattleRepository", "Loaded opponent card: ${it.name} (ID: $cardId)")
            } ?: run {
                Log.e("BattleRepository", "Failed to load opponent card with ID: $cardId")
                null
            }
        }
        // --- End Card Loading ---

        // Ensure decks meet minimum size AFTER loading valid cards
        if (playerCards.size < CardDeck.MIN_CARDS || opponentCards.size < CardDeck.MIN_CARDS) {
            Log.e("BattleRepository", "Error: Not enough valid cards loaded to start battle. Player: ${playerCards.size}, Opponent: ${opponentCards.size} (Min Required: ${CardDeck.MIN_CARDS})")
            addBattleLogEvent("Error starting battle: Invalid deck composition.", "ERROR")
            _battleState.value = BattleResult.NOT_STARTED // Ensure state reflects failure
            return
        }
        Log.d("BattleRepository", "Decks validated. Player: ${playerCards.size} cards, Opponent: ${opponentCards.size} cards.")


        // Shuffle cards
        val shuffledPlayerCards = playerCards.shuffled()
        val shuffledOpponentCards = opponentCards.shuffled()

        // Set initial decks (first 5 cards go to hand, rest to deck)
        val initialHandSize = 5
        _playerDeck.value = shuffledPlayerCards.drop(initialHandSize)
        _opponentDeck.value = shuffledOpponentCards.drop(initialHandSize)

        // Draw initial hands
        _playerHand.value = shuffledPlayerCards.take(initialHandSize)
        _opponentHand.value = shuffledOpponentCards.take(initialHandSize)
        Log.d("BattleRepository", "Dealt $initialHandSize cards to player: ${_playerHand.value.map { it.name }}")
        Log.d("BattleRepository", "Dealt $initialHandSize cards to opponent.")

        // Initialize battle state
        _activePlayerCard.value = null
        _activeOpponentCard.value = null
        _battleState.value = BattleResult.IN_PROGRESS
        _isPlayerTurn.value = true // Player starts first (can be randomized later)
        _turn.value = 1
        _battleLog.value = listOf(BattleEvent(_turn.value, "Battle started!", "SYSTEM"))
        Log.i("BattleRepository", "Battle started successfully. Turn ${_turn.value} - Player's turn.")
    }

    /**
     * Play a card from the player's hand to the battlefield.
     * Instantiates CardInPlay with initialized abilities.
     */
    fun playPlayerCard(cardId: String): Boolean {
        Log.d("BattleRepository", "Attempting to play card ID: $cardId")
        if (!_isPlayerTurn.value) {
            addBattleLogEvent("Cannot play card: Not player's turn", "ERROR")
            return false
        }
        if (_activePlayerCard.value != null) {
            addBattleLogEvent("Cannot play card: Player already has an active card ('${_activePlayerCard.value?.card?.name}')", "ERROR")
            return false
        }

        val cardData = _playerHand.value.find { it.id == cardId } ?: run {
            addBattleLogEvent("Cannot play card: Card ID '$cardId' not found in hand", "ERROR")
            return false
        }

        // --- Instantiate CardInPlay with initialized abilities ---
        val initializedAbilities = CardInPlay.initializeAbilitiesInPlay(cardData, cardRepository.cardLibraryRepository)
        val cardInPlay = CardInPlay(
            card = cardData,
            currentHealth = cardData.health, // Start with full health
            abilities = initializedAbilities // Pass the initialized list
        )
        // --- End Instantiation ---

        _activePlayerCard.value = cardInPlay
        _playerHand.value = _playerHand.value.filter { it.id != cardId } // Remove from hand
        addBattleLogEvent("Player played ${cardData.name}", "PLAY_CARD")
        Log.i("BattleRepository", "Player played ${cardData.name}. Abilities: ${initializedAbilities.joinToString { it.ability.name }}.")

        // Potentially trigger "on play" effects here if cardData has them

        return true
    }

    /**
     * Player attacks the opponent's active card.
     */
    fun playerAttack(): Boolean {
        Log.d("BattleRepository", "Player initiating attack.")
        if (!_isPlayerTurn.value) {
            addBattleLogEvent("Cannot attack: Not player's turn", "ERROR")
            return false
        }

        val playerCard = _activePlayerCard.value ?: run {
            addBattleLogEvent("Cannot attack: No active player card", "ERROR")
            return false
        }
        val opponentCard = _activeOpponentCard.value ?: run {
            addBattleLogEvent("Cannot attack: No opponent card to attack", "ERROR")
            return false
        }

        if (playerCard.isStunned()) {
            addBattleLogEvent("Cannot attack: ${playerCard.card.name} is stunned!", "STUNNED")
            return false // Return false as action failed
        }

        Log.d("BattleRepository", "${playerCard.card.name} (P:${playerCard.getCurrentPower()}) attacking ${opponentCard.card.name} (D:${opponentCard.getCurrentDefense()}, H:${opponentCard.currentHealth})")
        // Player attacks opponent
        val damageDealt = opponentCard.applyDamage(playerCard.getCurrentPower())
        addBattleLogEvent(
            "${playerCard.card.name} attacked ${opponentCard.card.name} for $damageDealt damage.",
            "ATTACK"
        )

        if (opponentCard.currentHealth <= 0) {
            handleDefeat(opponentCard) // Use helper (checks victory)
        }
        // Counter-attack if opponent is still alive and not stunned
        else if (!opponentCard.isStunned()) {
            Log.d("BattleRepository", "${opponentCard.card.name} (P:${opponentCard.getCurrentPower()}) counter-attacking ${playerCard.card.name} (D:${playerCard.getCurrentDefense()}, H:${playerCard.currentHealth})")
            val counterDamage = playerCard.applyDamage(opponentCard.getCurrentPower())
            addBattleLogEvent("${opponentCard.card.name} countered for $counterDamage damage.", "COUNTER_ATTACK")
            if (playerCard.currentHealth <= 0) {
                handleDefeat(playerCard) // Use helper (checks victory)
            }
        } else if(opponentCard.isStunned()) {
            Log.d("BattleRepository", "${opponentCard.card.name} cannot counter-attack (stunned).")
            addBattleLogEvent("${opponentCard.card.name} is stunned and cannot counter-attack.", "STUNNED")
        }

        return true // Attack action completed (even if resulting in defeat)
    }

    /**
     * Player uses an ability. Delegates effect application.
     */
    fun playerUseAbility(abilityIndex: Int, targetId: String?): Boolean {
        Log.d("BattleRepository", "Player attempting to use ability index: $abilityIndex, target ID: $targetId")
        if (!_isPlayerTurn.value) {
            addBattleLogEvent("Cannot use ability: Not player's turn", "ERROR")
            return false
        }
        val playerCard = _activePlayerCard.value ?: run {
            addBattleLogEvent("Cannot use ability: No active player card", "ERROR")
            return false
        }
        if (playerCard.isStunned()) {
            addBattleLogEvent("Cannot use ability: ${playerCard.card.name} is stunned!", "STUNNED")
            return false
        }

        val abilityInPlay = playerCard.abilities.getOrNull(abilityIndex) ?: run {
            addBattleLogEvent("Cannot use ability: Invalid ability index $abilityIndex (Card has ${playerCard.abilities.size} abilities)", "ERROR")
            return false
        }

        if (!abilityInPlay.isReady) {
            addBattleLogEvent("Cannot use ability: ${abilityInPlay.ability.name} is on cooldown (${abilityInPlay.currentCooldown} turns)", "COOLDOWN")
            return false
        }

        // --- Target Resolution ---
        val target: CardInPlay? = determineTarget(abilityInPlay.ability.targetType, playerCard, targetId)

        // Check if a target is required but none was found/valid
        if (requiresTarget(abilityInPlay.ability.targetType) && target == null && abilityInPlay.ability.targetType != TargetType.SELF) {
            addBattleLogEvent("Cannot use ability ${abilityInPlay.ability.name}: Invalid or no target selected/available for type ${abilityInPlay.ability.targetType}", "ERROR")
            return false
        }
        Log.d("BattleRepository", "Ability target determined: ${target?.card?.name ?: "None/Self"}")

        try {
            // --- Ability Activation ---
            // AbilityInPlay.activate handles cooldown and returns the CardEffect
            val cardEffect = abilityInPlay.activate(playerCard, target)
            Log.i("BattleRepository", "Player used ${abilityInPlay.ability.name}. Effect: ${cardEffect.description}")

            // --- Delegate Effect Application ---
            applyCardEffect(cardEffect, playerCard, target) // Apply the effect based on its type

            // --- Post-Effect Checks ---
            // Check defeat after effects resolve (order might matter for reflect/self-damage)
            if (playerCard.currentHealth <= 0) { // Check self first
                handleDefeat(playerCard)
            }
            target?.let { // Then check target
                if (it.currentHealth <= 0) {
                    handleDefeat(it)
                }
            }
            // Final check (in case both were defeated or game ended some other way)
            checkForVictory()

            return true // Ability activation and effect application successful

        } catch (e: IllegalStateException) {
            // Catch potential "Ability is on cooldown" error if check failed somehow
            addBattleLogEvent("Error using ability ${abilityInPlay.ability.name}: ${e.message}", "ERROR")
            return false
        } catch (e: Exception) {
            // Catch unexpected errors during effect application
            Log.e("BattleRepository", "Unexpected error during player ability use", e)
            addBattleLogEvent("Unexpected error using ability ${abilityInPlay.ability.name}.", "ERROR")
            return false
        }
    }


    // --- NEW: Helper to determine the target ---
    private fun determineTarget(targetType: TargetType, user: CardInPlay, targetId: String?): CardInPlay? {
        val opponentCard = _activeOpponentCard.value
        val playerCard = _activePlayerCard.value // Assume this is always 'user' if called from player actions

        return when (targetType) {
            TargetType.SELF -> user
            TargetType.SINGLE -> {
                // Allow targeting self or opponent based on targetId
                if (targetId == user.card.id) {
                    user // Target is self
                } else if (targetId == opponentCard?.card?.id) {
                    opponentCard // Target is opponent
                } else if (targetId == playerCard?.card?.id && user != playerCard) {
                    playerCard // Target is player's card (if different from user - future case?)
                }
                else {
                    Log.w("BattleRepository", "Target ID '$targetId' provided for SINGLE target ability does not match self, opponent, or player.")
                    null // Target ID doesn't match known active cards
                }
            }
            TargetType.RANDOM_ENEMY -> opponentCard // Simple case for 1v1
            TargetType.ALL_ENEMIES -> opponentCard // Simple case for 1v1
            TargetType.RANDOM_ALLY -> user // Simple case for 1v1
            TargetType.ALL_ALLIES -> user // Simple case for 1v1
            // Add more complex logic here if multiple allies/enemies are possible
        }
    }

    // --- NEW: Helper to check if target is needed based on type ---
    private fun requiresTarget(targetType: TargetType): Boolean {
        return when (targetType) {
            TargetType.SELF -> false // Target is implicit
            // All others potentially require a target (even if randomly selected)
            TargetType.SINGLE, TargetType.RANDOM_ENEMY, TargetType.ALL_ENEMIES,
            TargetType.RANDOM_ALLY, TargetType.ALL_ALLIES -> true
        }
    }

    // --- NEW: Helper to apply the CardEffect ---
    private fun applyCardEffect(effect: CardEffect, source: CardInPlay, primaryTarget: CardInPlay?) {
        Log.d("BattleRepository.applyCardEffect", "Applying effect '${effect.description}' from ${source.card.name} to ${primaryTarget?.card?.name ?: "Self/None"}")
        when (effect) {
            is CardEffect.Damage -> {
                primaryTarget?.let {
                    val damageDealt = it.applyDamage(effect.amount) // CardInPlay handles defense/shield
                    addBattleLogEvent("${source.card.name}'s effect dealt $damageDealt damage to ${it.card.name}", "ABILITY_DAMAGE")
                } ?: addBattleLogEvent("Damage effect from ${source.card.name} had no valid target.", "WARNING")
            }
            is CardEffect.Heal -> {
                // Target is usually SELF for heals, but could be an ally
                val targetToHeal = primaryTarget ?: source // Default to source if primaryTarget is null
                // Use addEffect for healing, as it handles overheal and logging
                targetToHeal.addEffect(EffectType.HEAL, 1, effect.amount) // Duration 1 for instant application
                // addEffect logs the heal amount now
                // addBattleLogEvent("${source.card.name}'s effect healed ${effect.amount} HP for ${targetToHeal.card.name}.", "ABILITY_HEAL")
            }
            is CardEffect.DrawCards -> {
                addBattleLogEvent("${source.card.name}'s effect triggers drawing ${effect.count} card(s).", "ABILITY_DRAW")
                // Determine who draws based on the source
                if (source == _activePlayerCard.value) {
                    repeat(effect.count) { drawPlayerCard() }
                } else if (source == _activeOpponentCard.value) {
                    repeat(effect.count) { drawOpponentCard() }
                }
            }
            is CardEffect.ModifyEnergy -> {
                // This effect needs to interact with the ViewModel or energy state holder
                Log.w("BattleRepository.applyCardEffect", "ModifyEnergy effect (+${effect.amount}) not fully implemented. Needs ViewModel interaction.")
                addBattleLogEvent("${source.card.name}'s effect attempts to modify energy by ${effect.amount}.", "ABILITY_ENERGY")
                // Example (Conceptual - requires ViewModel access):
                // if (source == _activePlayerCard.value) { viewModelScope.launch { battleViewModel.addEnergy(effect.amount) } }
            }
            is CardEffect.ApplyStatus -> {
                val effectType = EffectType.entries.find { it.name.equals(effect.status, ignoreCase = true) }
                if (effectType != null) {
                    // Determine the actual target based on the original ability's intent
                    // Assuming primaryTarget was correctly determined earlier based on TargetType
                    val actualTarget = primaryTarget ?: source // Default to self if primaryTarget is null (e.g., for SELF effects)
                    actualTarget.addEffect(effectType, effect.duration, 0) // Magnitude 0 unless effect includes it
                    // addEffect handles logging
                    // addBattleLogEvent("${source.card.name} applied ${effectType.name} (Dur: ${effect.duration}) to ${actualTarget.card.name}", "EFFECT_APPLIED")
                } else {
                    addBattleLogEvent("Could not map status '${effect.status}' to EffectType for effect from ${source.card.name}.", "WARNING")
                }
            }
            is CardEffect.ModifyStat -> {
                // Determine effect type based on stat and amount
                val (effectType, magnitude) = when {
                    effect.stat.equals("POWER", ignoreCase = true) && effect.amount > 0 -> Pair(EffectType.BUFF_POWER, effect.amount)
                    effect.stat.equals("POWER", ignoreCase = true) && effect.amount < 0 -> Pair(EffectType.DEBUFF_POWER, kotlin.math.abs(effect.amount)) // Use absolute for magnitude
                    effect.stat.equals("DEFENSE", ignoreCase = true) && effect.amount > 0 -> Pair(EffectType.BUFF_DEFENSE, effect.amount)
                    effect.stat.equals("DEFENSE", ignoreCase = true) && effect.amount < 0 -> Pair(EffectType.DEBUFF_DEFENSE, kotlin.math.abs(effect.amount)) // Use absolute for magnitude
                    // Add other stats like HEALTH if needed
                    else -> Pair(null, 0)
                }
                if (effectType != null) {
                    // Determine target based on effect.target ("SELF", "TARGET", etc.)
                    // Re-evaluate target based on the ModifyStat target field if needed, otherwise use primaryTarget
                    val actualTarget = when(effect.target.uppercase()) {
                        "SELF" -> source
                        "TARGET" -> primaryTarget
                        // Add other cases if ModifyStat effect has different target types
                        else -> primaryTarget // Default to the primary ability target
                    }

                    actualTarget?.let {
                        // Apply effect with a default duration (e.g., 2 turns)
                        val duration = 2
                        it.addEffect(effectType, duration, magnitude)
                        // addEffect logs application/stacking
                        // addBattleLogEvent("${source.card.name} modified ${it.card.name}'s ${effect.stat} by ${effect.amount} for $duration turns.", "STAT_MOD")
                    } ?: addBattleLogEvent("Stat modification effect from ${source.card.name} had no valid target.", "WARNING")

                } else {
                    addBattleLogEvent("Could not map stat modification '${effect.stat}' to EffectType for effect from ${source.card.name}.", "WARNING")
                }
            }
        }
    }


    /**
     * End the player's turn and start the opponent's turn.
     */
    fun endPlayerTurn() {
        if (battleState.value != BattleResult.IN_PROGRESS) {
            Log.w("BattleRepository", "Attempted to end player turn, but battle is already over (${battleState.value}).")
            return
        }
        if (!_isPlayerTurn.value) {
            addBattleLogEvent("Cannot end turn: Not player's turn", "ERROR")
            return
        }

        lastTurnEndTime = System.currentTimeMillis()
        Log.i("BattleRepository", "Player ending turn ${turn.value} at $lastTurnEndTime")

        // --- End of Player Turn Phase ---
        addBattleLogEvent("Player ended their turn.", "END_TURN")

        // 1. Tick effects/cooldowns for the player card
        _activePlayerCard.value?.tickTurn()
        // 2. Check if player card defeated itself (e.g., DOT)
        if ((_activePlayerCard.value?.currentHealth ?: 1) <= 0) {
            handleDefeat(_activePlayerCard.value!!) // This checks victory
            if (battleState.value != BattleResult.IN_PROGRESS) return // Stop if battle ended
        }

        // 3. Draw a card for the player (if deck allows)
        drawPlayerCard()

        // --- Start of Opponent Turn Phase ---
        // 4. Switch turn state
        _isPlayerTurn.value = false

        // 5. Execute opponent's actions
        executeOpponentTurn() // This function now handles opponent's turn end internally
    }

    /**
     * Draw a card from the player's deck to their hand.
     */
    private fun drawPlayerCard() {
        if (_playerHand.value.size >= 10) { // Example: Max hand size limit
            addBattleLogEvent("Player's hand is full, cannot draw.", "DRAW_FAILED")
            // Optionally burn the card
            if (_playerDeck.value.isNotEmpty()) {
                val burntCard = _playerDeck.value.first()
                _playerDeck.value = _playerDeck.value.drop(1)
                addBattleLogEvent("Player burnt ${burntCard.name} due to full hand.", "CARD_BURN")
            }
            return
        }
        if (_playerDeck.value.isNotEmpty()) {
            val drawnCard = _playerDeck.value.first()
            _playerHand.value = (_playerHand.value + drawnCard)
            _playerDeck.value = _playerDeck.value.drop(1)
            addBattleLogEvent("Player drew ${drawnCard.name}", "DRAW_CARD")
            Log.d("BattleRepository", "Player drew ${drawnCard.name}. Hand: ${_playerHand.value.size}, Deck: ${_playerDeck.value.size}")
        } else {
            addBattleLogEvent("Player's deck is empty, no card drawn.", "DRAW_FAILED")
            // TODO: Implement fatigue damage or other "deck out" penalty?
        }
    }

    /**
     * Draw a card from the opponent's deck to their hand.
     */
    private fun drawOpponentCard() {
        if (_opponentHand.value.size >= 10) { // Example: Max hand size limit
            addBattleLogEvent("Opponent's hand is full, cannot draw.", "DRAW_FAILED")
            if (_opponentDeck.value.isNotEmpty()) {
                val burntCard = _opponentDeck.value.first()
                _opponentDeck.value = _opponentDeck.value.drop(1)
                addBattleLogEvent("Opponent burnt a card due to full hand.", "CARD_BURN")
            }
            return
        }
        if (_opponentDeck.value.isNotEmpty()) {
            val drawnCard = _opponentDeck.value.first()
            _opponentHand.value = (_opponentHand.value + drawnCard)
            _opponentDeck.value = _opponentDeck.value.drop(1)
            addBattleLogEvent("Opponent drew a card.", "DRAW_CARD")
            Log.d("BattleRepository", "Opponent drew a card. Hand: ${_opponentHand.value.size}, Deck: ${_opponentDeck.value.size}")
        } else {
            addBattleLogEvent("Opponent's deck is empty, no card drawn.", "DRAW_FAILED")
            // TODO: Implement fatigue damage for opponent?
        }
    }

    /**
     * Execute the opponent's turn AI logic.
     */
    private fun executeOpponentTurn() {
        // Prevent actions if battle ended during player's turn effects
        if (battleState.value != BattleResult.IN_PROGRESS) {
            Log.w("BattleRepository", "Skipping opponent turn execution, battle ended.")
            return
        }

        addBattleLogEvent("Opponent's turn ${turn.value} begins", "SYSTEM")
        var actionTaken = false
        val opponentCard = _activeOpponentCard.value
        val playerCard = _activePlayerCard.value

        // --- Opponent AI Decision Tree ---

        // 1. Play a card if slot is empty and has cards in hand
        if (opponentCard == null && _opponentHand.value.isNotEmpty()) {
            // Simple AI: Play the first available card
            val cardToPlay = _opponentHand.value.first()
            if (playOpponentCard(cardToPlay.id)) { // Use helper function
                actionTaken = true
                Log.d("BattleRepository", "AI Action: Played card ${cardToPlay.name}")
            }
        }

        // Get updated opponentCard state AFTER potentially playing one
        val currentOpponentCard = _activeOpponentCard.value

        // 2. Consider using an Ability (if card exists and player exists)
        // Make this check independent of whether a card was just played
        if (!actionTaken && currentOpponentCard != null && playerCard != null) {
            // Find the first ready ability
            val usableAbilityInfo = currentOpponentCard.abilities.withIndex()
                .firstOrNull { (_, ability) -> ability.isReady } // Add energy check if AI uses energy

            if (usableAbilityInfo != null) {
                val (index, abilityInPlay) = usableAbilityInfo
                // Simple AI: Target player if ability requires a target (and isn't SELF)
                val targetId = if (abilityInPlay.ability.targetType != TargetType.SELF) playerCard.card.id else null
                if (opponentUseAbility(index, targetId)) { // Use helper
                    actionTaken = true
                    Log.d("BattleRepository", "AI Action: Used ability ${abilityInPlay.ability.name}")
                }
            } else {
                Log.d("BattleRepository", "AI: No abilities ready for ${currentOpponentCard.card.name}.")
            }
        }

        // 3. Attack if possible and no other action taken yet
        // Re-check opponent card status (it might have been defeated by ability effects/reflect)
        val opponentCardAfterAbility = _activeOpponentCard.value
        if (!actionTaken && opponentCardAfterAbility != null && playerCard != null && !opponentCardAfterAbility.isStunned()) {
            if (opponentAttack()) { // Use helper
                actionTaken = true
                Log.d("BattleRepository", "AI Action: Attacked with ${opponentCardAfterAbility.card.name}")
            }
        } else if (!actionTaken && opponentCardAfterAbility?.isStunned() == true) {
            Log.d("BattleRepository", "AI Action: Attack skipped, ${opponentCardAfterAbility.card.name} is stunned.")
            addBattleLogEvent("${opponentCardAfterAbility.card.name} is stunned and cannot attack.", "STUNNED")
        }

        // --- End AI Decision Tree ---

        if (!actionTaken) {
            Log.d("BattleRepository", "AI Action: No action taken.")
            addBattleLogEvent("Opponent took no action.", "SYSTEM")
        }

        // End opponent's turn (triggers effect ticks, turn increment, and player turn start)
        // This happens regardless of whether an action was taken, unless the battle ended.
        if (battleState.value == BattleResult.IN_PROGRESS) {
            endOpponentTurn()
        } else {
            Log.i("BattleRepository", "Opponent turn finished, but battle state is ${battleState.value}, not ending turn formally.")
        }
    }

    // --- NEW: Opponent action helpers (mirror player actions) ---

    /** Plays a card from opponent's hand. Assumes cardId is valid. */
    private fun playOpponentCard(cardId: String): Boolean {
        val cardData = _opponentHand.value.find { it.id == cardId } ?: return false
        // TODO: Add energy check here if opponent uses energy

        // Instantiate CardInPlay with initialized abilities
        val initializedAbilities = CardInPlay.initializeAbilitiesInPlay(cardData, cardRepository.cardLibraryRepository)
        val cardInPlay = CardInPlay(
            card = cardData,
            currentHealth = cardData.health,
            abilities = initializedAbilities
        )

        _activeOpponentCard.value = cardInPlay
        _opponentHand.value = _opponentHand.value.filter { it.id != cardId } // Remove from hand
        addBattleLogEvent("Opponent played ${cardData.name}", "PLAY_CARD")
        Log.i("BattleRepository", "Opponent played ${cardData.name}.")
        // Potentially trigger "on play" effects

        return true
    }

    /** Opponent performs an attack. */
    private fun opponentAttack(): Boolean {
        val opponentCard = _activeOpponentCard.value ?: return false // Should exist if called
        val playerCard = _activePlayerCard.value ?: return false // Opponent needs target
        if (opponentCard.isStunned()) return false // Double check stun

        Log.d("BattleRepository", "Opponent ${opponentCard.card.name} (P:${opponentCard.getCurrentPower()}) attacking ${playerCard.card.name} (D:${playerCard.getCurrentDefense()}, H:${playerCard.currentHealth})")
        // Opponent attacks player
        val damageDealt = playerCard.applyDamage(opponentCard.getCurrentPower())
        addBattleLogEvent("Opponent's ${opponentCard.card.name} attacked ${playerCard.card.name} for $damageDealt damage.", "ATTACK")

        if (playerCard.currentHealth <= 0) {
            handleDefeat(playerCard) // checks victory
        }
        // Counter-attack if player is still alive and not stunned
        else if (!playerCard.isStunned()) {
            Log.d("BattleRepository", "Player ${playerCard.card.name} (P:${playerCard.getCurrentPower()}) counter-attacking ${opponentCard.card.name} (D:${opponentCard.getCurrentDefense()}, H:${opponentCard.currentHealth})")
            val counterDamage = opponentCard.applyDamage(playerCard.getCurrentPower())
            addBattleLogEvent("${playerCard.card.name} countered for $counterDamage damage.", "COUNTER_ATTACK")
            if (opponentCard.currentHealth <= 0) {
                handleDefeat(opponentCard) // checks victory
            }
        } else if (playerCard.isStunned()) {
            Log.d("BattleRepository", "Player ${playerCard.card.name} cannot counter-attack (stunned).")
            addBattleLogEvent("${playerCard.card.name} is stunned and cannot counter-attack.", "STUNNED")
        }
        return true
    }

    /** Opponent uses an ability. */
    private fun opponentUseAbility(abilityIndex: Int, targetId: String?): Boolean {
        Log.d("BattleRepository", "Opponent attempting ability index: $abilityIndex, target ID: $targetId")
        val opponentCard = _activeOpponentCard.value ?: return false
        if (opponentCard.isStunned()) return false

        val abilityInPlay = opponentCard.abilities.getOrNull(abilityIndex) ?: return false
        if (!abilityInPlay.isReady) return false
        // TODO: Add energy check here if opponent uses energy

        // Determine target
        val target: CardInPlay? = determineTarget(abilityInPlay.ability.targetType, opponentCard, targetId)

        // Validate target
        if (requiresTarget(abilityInPlay.ability.targetType) && target == null && abilityInPlay.ability.targetType != TargetType.SELF) {
            addBattleLogEvent("Opponent Ability ${abilityInPlay.ability.name} failed: Invalid target", "AI_ERROR")
            return false
        }
        Log.d("BattleRepository", "Opponent ability target determined: ${target?.card?.name ?: "None/Self"}")

        try {
            // Activate ability
            val cardEffect = abilityInPlay.activate(opponentCard, target)
            Log.i("BattleRepository", "Opponent used ${abilityInPlay.ability.name}. Effect: ${cardEffect.description}")

            // Apply effect
            applyCardEffect(cardEffect, opponentCard, target)

            // Post-effect checks
            if (opponentCard.currentHealth <= 0) { // Check self first
                handleDefeat(opponentCard)
            }
            target?.let { // Then check target
                if (it.currentHealth <= 0) {
                    handleDefeat(it)
                }
            }
            checkForVictory() // Final check

            return true
        } catch (e: IllegalStateException) {
            addBattleLogEvent("Opponent Ability ${abilityInPlay.ability.name} failed (state): ${e.message}", "AI_ERROR")
            return false
        } catch (e: Exception) {
            Log.e("BattleRepository", "Unexpected error during opponent ability use", e)
            addBattleLogEvent("Unexpected error using opponent ability ${abilityInPlay.ability.name}.", "AI_ERROR")
            return false
        }
    }
    // --- End Opponent action helpers ---


    /**
     * End the opponent's turn and start the player's turn.
     * Increments turn counter, ticks effects, draws card for player.
     */
    private fun endOpponentTurn() {
        if (battleState.value != BattleResult.IN_PROGRESS) {
            Log.w("BattleRepository", "Attempted to end opponent turn, but battle is already over (${battleState.value}).")
            return
        }
        Log.d("BattleRepository", "Ending opponent turn ${turn.value}. Time since player ended: ${System.currentTimeMillis() - lastTurnEndTime}ms")

        // --- End of Opponent Turn Phase ---
        addBattleLogEvent("Opponent ended their turn.", "END_TURN")

        // 1. Tick effects/cooldowns for the opponent card
        _activeOpponentCard.value?.tickTurn()
        // 2. Check if opponent card defeated itself
        if ((_activeOpponentCard.value?.currentHealth ?: 1) <= 0) {
            handleDefeat(_activeOpponentCard.value!!) // Checks victory
            if (battleState.value != BattleResult.IN_PROGRESS) return // Stop if battle ended
        }

        // --- Start of Player Turn Phase ---
        // 3. Increment turn counter
        _turn.value += 1
        Log.i("BattleRepository", "Starting Turn ${_turn.value} - Player's turn.")

        // 4. Switch turn state
        _isPlayerTurn.value = true
        addBattleLogEvent("Turn ${_turn.value} - Player's turn begins", "NEW_TURN")

        // 5. Tick player's effects/cooldowns at START of their turn (alternative: tick at end)
        // _activePlayerCard.value?.tickTurn() // Choose one: tick at start OR end

        // 6. Draw a card for the player
        drawPlayerCard()

        // 7. Check win/loss conditions again (e.g., fatigue, start-of-turn effects)
        checkForVictory()
    }

    /**
     * Handle a card being defeated: remove from board and check win condition.
     */
    private fun handleDefeat(defeatedCardInPlay: CardInPlay) {
        // Prevent multiple defeat processing for the same card in one sequence
        if ((defeatedCardInPlay == _activePlayerCard.value && _activePlayerCard.value == null) ||
            (defeatedCardInPlay == _activeOpponentCard.value && _activeOpponentCard.value == null)) {
            Log.w("BattleRepository", "Attempted to handle defeat for already removed card: ${defeatedCardInPlay.card.name}")
            return
        }

        addBattleLogEvent("${defeatedCardInPlay.card.name} was defeated!", "DEFEAT")
        var boardChanged = false
        if (defeatedCardInPlay == _activePlayerCard.value) {
            Log.i("BattleRepository", "Player card ${defeatedCardInPlay.card.name} defeated.")
            _activePlayerCard.value = null
            boardChanged = true
        } else if (defeatedCardInPlay == _activeOpponentCard.value) {
            Log.i("BattleRepository", "Opponent card ${defeatedCardInPlay.card.name} defeated.")
            _activeOpponentCard.value = null
            boardChanged = true
        } else {
            // This shouldn't happen if only active cards are passed
            Log.e("BattleRepository", "handleDefeat called with card not active on board: ${defeatedCardInPlay.card.name}")
        }

        // Only check victory if the board state actually changed
        if (boardChanged) {
            checkForVictory()
        }
    }

    /**
     * Add an event to the battle log.
     */
    private fun addBattleLogEvent(description: String, type: String) {
        // Avoid logging if battle isn't in progress (e.g., post-game messages)
        // if (_battleState.value != BattleResult.IN_PROGRESS && type != "VICTORY" && type != "DEFEAT" && type != "DRAW" && type != "ERROR") return

        val newEvent = BattleEvent(_turn.value, description, type)
        // Use takeLast to limit log size
        _battleLog.value = (_battleLog.value + newEvent).takeLast(100)
        Log.d("BattleLog", "[T${newEvent.turn}][${newEvent.type}] ${newEvent.description}")
    }

    /**
     * Check for victory or defeat conditions based on card presence and deck status.
     */
    private fun checkForVictory() {
        // Don't change state if battle already concluded
        if (_battleState.value != BattleResult.IN_PROGRESS) return

        // Check current state
        val opponentHasCard = _activeOpponentCard.value != null
        val playerHasCard = _activePlayerCard.value != null
        val opponentCanPlay = _opponentHand.value.isNotEmpty() || _opponentDeck.value.isNotEmpty()
        val playerCanPlay = _playerHand.value.isNotEmpty() || _playerDeck.value.isNotEmpty()

        // Victory conditions: Opponent has no card AND cannot play another one.
        val opponentOutOfOptions = !opponentHasCard && !opponentCanPlay
        // Defeat conditions: Player has no card AND cannot play another one.
        val playerOutOfOptions = !playerHasCard && !playerCanPlay

        Log.d("BattleRepository", "Victory Check: opponentHasCard=$opponentHasCard, playerHasCard=$playerHasCard, opponentCanPlay=$opponentCanPlay, playerCanPlay=$playerCanPlay")

        // Determine outcome
        if (opponentOutOfOptions && !playerOutOfOptions) {
            _battleState.value = BattleResult.PLAYER_VICTORY
            addBattleLogEvent("Player wins the battle!", "VICTORY")
            Log.i("BattleRepository", "GAME OVER: Player Victory")
        } else if (playerOutOfOptions && !opponentOutOfOptions) {
            _battleState.value = BattleResult.ENEMY_VICTORY
            addBattleLogEvent("Opponent wins the battle!", "DEFEAT")
            Log.i("BattleRepository", "GAME OVER: Enemy Victory")
        } else if (playerOutOfOptions && opponentOutOfOptions) {
            // Draw condition: Both players run out of options simultaneously
            _battleState.value = BattleResult.DRAW
            addBattleLogEvent("Battle ended in a draw!", "DRAW")
            Log.i("BattleRepository", "GAME OVER: Draw")
        }
        // Else: Battle continues, state remains IN_PROGRESS
    }
} // End of BattleRepository class