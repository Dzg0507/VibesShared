package com.example.vibesshared.ui.ui.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vibesshared.ui.ui.data.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.questDataStore: DataStore<Preferences> by preferencesDataStore(name = "quests")

@Singleton
class QuestRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questGenerator: QuestGenerator
) {

    // In-memory cache of player progress
    private val _playerProgress = MutableStateFlow(PlayerProgress())
    val playerProgress: StateFlow<PlayerProgress> = _playerProgress

    // In-memory cache of available quests
    private val _availableQuests = MutableStateFlow<List<Quest>>(emptyList())

    // In-memory cache of rewards
    private val _rewardsCache = mutableMapOf<String, Reward>()

    // Store the last awarded rewards for display
    private val _lastAwardedRewards = MutableStateFlow<List<Reward>>(emptyList())

    init {
        // Initialize with sample data for now
        initializeRepository()
    }

    private fun initializeRepository() {
        // For demo purposes, we're initializing with hardcoded data
        // In a real app, this would load from persistent storage

        // Set default player progress
        _playerProgress.value = PlayerProgress(
            level = 5,
            xp = 250,
            multiverseTokens = 50,
            completedQuests = listOf("tutorial_quest"),
            unlockedRewards = listOf("basic_reward_1", "basic_reward_2"),
            activeQuests = emptyList()
        )

        // Generate initial quests
        refreshAvailableQuests()
    }

    /**
     * Refresh the available quests using the quest generator
     */
    fun refreshAvailableQuests() {
        val playerLevel = _playerProgress.value.level

        // Combine different types of quests
        val dailyQuests = questGenerator.generateDailyQuests()
        val weeklyQuests = questGenerator.generateWeeklyQuests()
        val eventQuests = questGenerator.generateEventQuests()
        val storyQuests = questGenerator.generateStoryQuests(playerLevel)

        // Filter quests that are appropriate for the player's level
        val allQuests = (dailyQuests + weeklyQuests + eventQuests + storyQuests)
            .filter { it.requiredLevel <= playerLevel }
            .filter { it.id !in _playerProgress.value.completedQuests }

        _availableQuests.value = allQuests

        // Cache all rewards for quick lookup
        allQuests.forEach { quest ->
            quest.rewards.forEach { reward ->
                _rewardsCache[reward.id] = reward
            }
        }
    }

    /**
     * Get the current available quests for the player
     */
    fun getAvailableQuests(): List<Quest> {
        return _availableQuests.value
    }

    /**
     * Start a quest for the player
     */
    fun startQuest(questId: String) {

        // Update player progress
        _playerProgress.value = _playerProgress.value.copy(
            activeQuests = _playerProgress.value.activeQuests + questId
        )
    }

    /**
     * Complete a quest and award rewards
     */
    fun completeQuest(questId: String, score: Int) {
        val quest = _availableQuests.value.find { it.id == questId } ?: return

        // Award XP
        var totalXp = 0
        var totalTokens = 0
        val awardedRewards = mutableListOf<Reward>() // Track awarded rewards

        // Calculate rewards
        quest.rewards.forEach { reward ->
            totalXp += reward.xpValue
            totalTokens += reward.multiverseTokenValue

            // Add to the awarded rewards list for display
            awardedRewards.add(reward)

            // Add reward to unlocked rewards
            val unlockedRewards = _playerProgress.value.unlockedRewards.toMutableList()
            if (!unlockedRewards.contains(reward.id)) {
                unlockedRewards.add(reward.id)
            }

            // Update player's unlocked rewards
            _playerProgress.value = _playerProgress.value.copy(
                unlockedRewards = unlockedRewards
            )
        }

        // Add quest to completed quests
        val completedQuests = _playerProgress.value.completedQuests.toMutableList()
        completedQuests.add(questId)

        // Remove quest from active quests
        val activeQuests = _playerProgress.value.activeQuests.toMutableList()
        activeQuests.remove(questId)

        // Update player progress
        var updatedProgress = _playerProgress.value.copy(
            xp = _playerProgress.value.xp + totalXp,
            multiverseTokens = _playerProgress.value.multiverseTokens + totalTokens,
            completedQuests = completedQuests,
            activeQuests = activeQuests
        )

        // Check for level up
        while (updatedProgress.canLevelUp()) {
            val newLevel = updatedProgress.level + 1
            val xpRequired = updatedProgress.xpForNextLevel()
            val remainingXp = updatedProgress.xp - xpRequired

            updatedProgress = updatedProgress.copy(
                level = newLevel,
                xp = remainingXp
            )
        }

        _playerProgress.value = updatedProgress

        // Store the awarded rewards for display
        _lastAwardedRewards.value = awardedRewards

        // Refresh available quests after completing a quest
        refreshAvailableQuests()
    }

    /**
     * Claim a specific reward
     */
    fun claimReward(rewardId: String) {
        println("QuestRepository: Claiming reward with ID: $rewardId")
        val reward = _rewardsCache[rewardId]

        if (reward == null) {
            println("QuestRepository: Error - Reward with ID $rewardId not found in cache")
            return
        }

        println("QuestRepository: Found reward in cache: ${reward.name} (${reward.id}, type: ${reward.type})")

        // Add reward to unlocked rewards list if not already there
        val unlockedRewards = _playerProgress.value.unlockedRewards.toMutableList()

        if (!unlockedRewards.contains(rewardId)) {
            println("QuestRepository: Adding reward to unlocked rewards list")
            unlockedRewards.add(rewardId)

            // Update player progress with new unlocked rewards list
            _playerProgress.value = _playerProgress.value.copy(
                unlockedRewards = unlockedRewards,
                xp = _playerProgress.value.xp + reward.xpValue,
                multiverseTokens = _playerProgress.value.multiverseTokens + reward.multiverseTokenValue
            )

            println("QuestRepository: Player progress updated with new reward")
            println("QuestRepository: Current unlocked rewards: ${_playerProgress.value.unlockedRewards.size}")
        } else {
            println("QuestRepository: Reward already in unlocked rewards list, updating XP and tokens only")

            // Just update XP and tokens
            _playerProgress.value = _playerProgress.value.copy(
                xp = _playerProgress.value.xp + reward.xpValue,
                multiverseTokens = _playerProgress.value.multiverseTokens + reward.multiverseTokenValue
            )
        }

        // Explicitly ensure the reward is in the rewards cache for forge access
        _rewardsCache[rewardId] = reward
        println("QuestRepository: Reward added/updated in rewards cache")
    }

    /**
     * Get a reward by its ID
     */
    fun getRewardById(rewardId: String): Reward? {
        return _rewardsCache[rewardId]
    }

    /**
     * Get a snapshot of the current player progress
     */
    fun getPlayerProgressSnapshot(): PlayerProgress {
        return _playerProgress.value
    }

    /**
     * Get the player's current token count
     */
    fun getPlayerTokens(): Int {
        return _playerProgress.value.multiverseTokens
    }

    /**
     * Add tokens to the player's balance
     */
    fun addMultiverseTokens(amount: Int) {
        _playerProgress.value = _playerProgress.value.copy(
            multiverseTokens = _playerProgress.value.multiverseTokens + amount
        )
    }

    /**
     * Spend tokens from the player's balance
     */
    fun spendMultiverseTokens(amount: Int): Boolean {
        if (_playerProgress.value.multiverseTokens < amount) {
            return false
        }

        _playerProgress.value = _playerProgress.value.copy(
            multiverseTokens = _playerProgress.value.multiverseTokens - amount
        )

        return true
    }

    /**
     * Forge a new item from two existing rewards
     */
    fun forgeMultiverseItem(rewardIds: List<String>): Reward? {
        if (rewardIds.size != 2) return null

        val reward1 = _rewardsCache[rewardIds[0]]
        val reward2 = _rewardsCache[rewardIds[1]]

        if (reward1 == null || reward2 == null) return null

        println("QuestRepository: Forging new item from ${reward1.name} and ${reward2.name}")

        // Create a new forged reward
        val highestTier = if (reward1.tier.ordinal > reward2.tier.ordinal) reward1.tier else reward2.tier
        val nextTier = getNextTier(highestTier)

        // Calculate XP value for the new item
        // The new item should be worth more than the sum of its parts
        val baseXpValue = (reward1.xpValue + reward2.xpValue)
        val bonusMultiplier = 1.5f + (nextTier.ordinal * 0.2f) // Higher tier = bigger bonus
        val forgedXpValue = (baseXpValue * bonusMultiplier).toInt()

        // Calculate token value for the new item
        val forgedTokenValue = (reward1.multiverseTokenValue + reward2.multiverseTokenValue) / 2

        val forgedReward = Reward(
            id = "forged_${System.currentTimeMillis()}",
            name = "Forged ${nextTier.name} ${getRandomForgedName()}",
            description = "A powerful item created by combining ${reward1.name} and ${reward2.name}",
            tier = nextTier,
            type = getRandomRewardType(),
            rarity = (reward1.rarity + reward2.rarity) / 3, // Rarer than the average
            xpValue = forgedXpValue,
            multiverseTokenValue = forgedTokenValue
        )

        println("QuestRepository: Created new forged item: ${forgedReward.name} (${forgedReward.id})")
        println("QuestRepository: Forged item XP value: $forgedXpValue")

        // Add the forged reward to cache
        _rewardsCache[forgedReward.id] = forgedReward

        // Remove the original items from the player's unlocked rewards
        val unlockedRewards = _playerProgress.value.unlockedRewards.toMutableList()
        unlockedRewards.remove(rewardIds[0])
        unlockedRewards.remove(rewardIds[1])

        // Add the new forged item to player's unlocked rewards
        unlockedRewards.add(forgedReward.id)

        // Award XP to the player for forging
        // We give a forging bonus on top of the new item's value
        val forgingBonusXp = 50 * nextTier.ordinal // Higher tier = bigger bonus
        val totalXpGain = forgedXpValue + forgingBonusXp

        println("QuestRepository: Awarding $totalXpGain XP to player (${forgedXpValue} from item + $forgingBonusXp forging bonus)")

        // Get current player stats
        var updatedProgress = _playerProgress.value.copy(
            unlockedRewards = unlockedRewards,
            xp = _playerProgress.value.xp + totalXpGain,
            multiverseTokens = _playerProgress.value.multiverseTokens + forgedTokenValue
        )

        // Check for level up
        var leveledUp = false
        var newLevel = updatedProgress.level

        while (updatedProgress.canLevelUp()) {
            leveledUp = true
            newLevel = updatedProgress.level + 1
            val xpRequired = updatedProgress.xpForNextLevel()
            val remainingXp = updatedProgress.xp - xpRequired

            updatedProgress = updatedProgress.copy(
                level = newLevel,
                xp = remainingXp
            )

            println("QuestRepository: Player leveled up to ${newLevel}!")
        }

        // Update player progress
        _playerProgress.value = updatedProgress

        println("QuestRepository: Player progress updated. Level: ${updatedProgress.level}, XP: ${updatedProgress.xp}")
        println("QuestRepository: Removed original items and added forged item to player inventory")
        println("QuestRepository: Player now has ${unlockedRewards.size} unlocked rewards")

        // Return both the new reward and a flag indicating if the player leveled up
        return forgedReward
    }
    /**
     * Extension function for QuestRepository to add a reward directly to the player's inventory
     * This allows manually adding special rewards that can be used in the forge
     */
    fun addRewardToInventory(reward: Reward) {
        println("QuestRepository: Manually adding reward to inventory: ${reward.name} (${reward.id})")

        // Add to the rewards cache for lookup
        _rewardsCache[reward.id] = reward
        println("QuestRepository: Added to rewards cache")

        // Add to player's unlocked rewards
        val unlockedRewards = _playerProgress.value.unlockedRewards.toMutableList()
        if (!unlockedRewards.contains(reward.id)) {
            unlockedRewards.add(reward.id)
            println("QuestRepository: Added to unlocked rewards list")
        }

        // Update player progress
        _playerProgress.value = _playerProgress.value.copy(
            unlockedRewards = unlockedRewards
        )

        println("QuestRepository: Player progress updated")
        println("QuestRepository: Current unlocked rewards: ${_playerProgress.value.unlockedRewards.size}")
    }
    /**
     * Add XP to the player from streak bonuses
     * @param bonusXp Amount of XP to add
     * @return True if player leveled up as a result
     */
    fun addStreakBonusXp(bonusXp: Int): Boolean {
        if (bonusXp <= 0) return false

        println("QuestRepository: Adding $bonusXp XP from streak bonus")

        // Get current player progress
        var updatedProgress = _playerProgress.value.copy(
            xp = _playerProgress.value.xp + bonusXp
        )

        // Check for level up
        var leveledUp = false

        while (updatedProgress.canLevelUp()) {
            leveledUp = true
            val newLevel = updatedProgress.level + 1
            val xpRequired = updatedProgress.xpForNextLevel()
            val remainingXp = updatedProgress.xp - xpRequired

            updatedProgress = updatedProgress.copy(
                level = newLevel,
                xp = remainingXp
            )

            println("QuestRepository: Player leveled up to $newLevel from streak bonus XP!")
        }

        // Update player progress
        _playerProgress.value = updatedProgress

        println("QuestRepository: Player progress updated from streak. Level: ${updatedProgress.level}, XP: ${updatedProgress.xp}")

        return leveledUp
    }

    /**
     * Load player progress from DataStore
     * In a real app, this would be called during initialization
     */
    suspend fun loadPlayerProgress() {
        val preferences = context.questDataStore.data.first()

        val level = preferences[intPreferencesKey("player_level")] ?: 1
        val xp = preferences[intPreferencesKey("player_xp")] ?: 0
        val tokens = preferences[intPreferencesKey("player_tokens")] ?: 0

        val completedQuests = preferences[stringSetPreferencesKey("completed_quests")]?.toList() ?: emptyList()
        val unlockedRewards = preferences[stringSetPreferencesKey("unlocked_rewards")]?.toList() ?: emptyList()
        val activeQuests = preferences[stringSetPreferencesKey("active_quests")]?.toList() ?: emptyList()

        _playerProgress.value = PlayerProgress(
            level = level,
            xp = xp,
            multiverseTokens = tokens,
            completedQuests = completedQuests,
            unlockedRewards = unlockedRewards,
            activeQuests = activeQuests
        )
    }

    // Helper methods

    private fun getNextTier(tier: RewardTier): RewardTier {
        return when (tier) {
            RewardTier.COMMON -> RewardTier.RARE
            RewardTier.RARE -> RewardTier.EPIC
            RewardTier.EPIC -> RewardTier.LEGENDARY
            RewardTier.LEGENDARY -> RewardTier.MYTHICAL
            RewardTier.MYTHICAL -> RewardTier.MYTHICAL // Can't go higher than mythical
        }
    }

    private fun getRandomForgedName(): String {
        val prefixes = listOf("Essence", "Fragment", "Core", "Heart", "Soul", "Spirit", "Aura")
        val suffixes = listOf("Power", "Infinity", "Creation", "Eternity", "Knowledge", "Wisdom", "Mastery")

        return "${prefixes.random()} of ${suffixes.random()}"
    }

    private fun getRandomRewardType(): RewardType {
        return RewardType.values().random()
    }
}