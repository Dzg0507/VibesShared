package com.example.vibesshared.ui.ui.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vibesshared.ui.ui.data.*
import com.example.vibesshared.ui.ui.repository.*
import com.google.gson.Gson
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
    private val gson = Gson()

    // In-memory cache of player progress
    private val _playerProgress = MutableStateFlow(PlayerProgress())
    val playerProgress: StateFlow<PlayerProgress> = _playerProgress

    // In-memory cache of available quests
    private val _availableQuests = MutableStateFlow<List<Quest>>(emptyList())

    // In-memory cache of rewards
    private val _rewardsCache = mutableMapOf<String, Reward>()

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
        val quest = _availableQuests.value.find { it.id == questId } ?: return

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

        // Calculate rewards
        quest.rewards.forEach { reward ->
            totalXp += reward.xpValue
            totalTokens += reward.multiverseTokenValue

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

        // Refresh available quests after completing a quest
        refreshAvailableQuests()
    }

    /**
     * Claim a specific reward
     */
    fun claimReward(rewardId: String) {
        val reward = _rewardsCache[rewardId] ?: return

        // Update player progress
        _playerProgress.value = _playerProgress.value.copy(
            xp = _playerProgress.value.xp + reward.xpValue,
            multiverseTokens = _playerProgress.value.multiverseTokens + reward.multiverseTokenValue
        )
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

        // Create a new forged reward
        val highestTier = if (reward1.tier.ordinal > reward2.tier.ordinal) reward1.tier else reward2.tier
        val nextTier = getNextTier(highestTier)

        val forgedReward = Reward(
            id = "forged_${System.currentTimeMillis()}",
            name = "Forged ${nextTier.name} ${getRandomForgedName()}",
            description = "A powerful item created by combining ${reward1.name} and ${reward2.name}",
            tier = nextTier,
            type = getRandomRewardType(),
            rarity = (reward1.rarity + reward2.rarity) / 3, // Rarer than the average
            xpValue = (reward1.xpValue + reward2.xpValue) * 2, // Double XP value
            multiverseTokenValue = (reward1.multiverseTokenValue + reward2.multiverseTokenValue) / 2
        )

        // Add the forged reward to cache
        _rewardsCache[forgedReward.id] = forgedReward

        // Add to player's unlocked rewards
        val unlockedRewards = _playerProgress.value.unlockedRewards.toMutableList()
        unlockedRewards.add(forgedReward.id)

        // Update player progress
        _playerProgress.value = _playerProgress.value.copy(
            unlockedRewards = unlockedRewards
        )

        return forgedReward
    }

    /**
     * Persist player progress to DataStore
     * In a real app, this would be called at appropriate times
     */
    suspend fun savePlayerProgress() {
        context.questDataStore.edit { preferences ->
            preferences[intPreferencesKey("player_level")] = _playerProgress.value.level
            preferences[intPreferencesKey("player_xp")] = _playerProgress.value.xp
            preferences[intPreferencesKey("player_tokens")] = _playerProgress.value.multiverseTokens

            // Convert lists to sets for storing in preferences
            preferences[stringSetPreferencesKey("completed_quests")] =
                _playerProgress.value.completedQuests.toSet()

            preferences[stringSetPreferencesKey("unlocked_rewards")] =
                _playerProgress.value.unlockedRewards.toSet()

            preferences[stringSetPreferencesKey("active_quests")] =
                _playerProgress.value.activeQuests.toSet()
        }
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