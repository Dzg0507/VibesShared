package com.example.vibesshared.ui.ui.repository

import com.example.vibesshared.ui.ui.data.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to generate quests with balanced XP and rewards
 */
@Singleton
class QuestGenerator @Inject constructor() {

    // XP multipliers for different quest difficulties
    private val xpMultipliers = mapOf(
        QuestDifficulty.EASY to 1.0f,
        QuestDifficulty.MEDIUM to 1.5f,
        QuestDifficulty.HARD to 2.25f,
        QuestDifficulty.ADEPT to 3.0f,
        QuestDifficulty.EXPERT to 4.0f,
        QuestDifficulty.LEGENDARY to 5.5f,
        QuestDifficulty.MULTIVERSE_BREAKER to 8.0f
    )

    // Base XP rewards based on quest type
    private val baseXpRewards = mapOf(
        QuestType.STANDARD to 100,
        QuestType.TIME_ATTACK to 125,
        QuestType.COOPERATIVE to 110,
        QuestType.CHALLENGE_MODE to 150,
        QuestType.BOSS_BATTLE to 200,
        QuestType.RESEARCH_EXPEDITION to 130
    )

    // Generate a daily quest set
    fun generateDailyQuests(): List<Quest> {
        val quests = mutableListOf<Quest>()

        // Easy daily quest (always available)
        quests.add(generateDailyQuest(QuestDifficulty.EASY))

        // Medium daily quest (always available)
        quests.add(generateDailyQuest(QuestDifficulty.MEDIUM))

        // Hard daily quest (level 5+)
        quests.add(generateDailyQuest(QuestDifficulty.HARD, 5))

        // Adept daily quest (level 10+)
        quests.add(generateDailyQuest(QuestDifficulty.ADEPT, 10))

        // Expert daily quest (level 15+)
        quests.add(generateDailyQuest(QuestDifficulty.EXPERT, 15))

        return quests
    }

    // Generate a set of weekly quests
    fun generateWeeklyQuests(): List<Quest> {
        val quests = mutableListOf<Quest>()

        // One weekly quest for each difficulty (except multiverse breaker)
        QuestDifficulty.values().filter { it != QuestDifficulty.MULTIVERSE_BREAKER }.forEach { difficulty ->
            val requiredLevel = when (difficulty) {
                QuestDifficulty.EASY -> 1
                QuestDifficulty.MEDIUM -> 3
                QuestDifficulty.HARD -> 7
                QuestDifficulty.ADEPT -> 12
                QuestDifficulty.EXPERT -> 18
                QuestDifficulty.LEGENDARY -> 25
                else -> 1
            }

            quests.add(generateWeeklyQuest(difficulty, requiredLevel))
        }

        return quests
    }

    // Generate special event quests
    fun generateEventQuests(): List<Quest> {
        val quests = mutableListOf<Quest>()

        // Current date to check for events
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Special holiday events
        if (month == Calendar.DECEMBER && (day in 20..25)) {
            // Winter holiday event
            quests.add(generateHolidayQuest("Winter Wisdom", "history"))
        } else if (month == Calendar.OCTOBER && day >= 25) {
            // Halloween event
            quests.add(generateHolidayQuest("Spooky Smarts", "entertainment"))
        } else if (month == Calendar.FEBRUARY && day in 10..14) {
            // Valentine's event
            quests.add(generateHolidayQuest("Romantic Riddles", "science"))
        }

        // Add monthly challenge quest (legendary difficulty)
        quests.add(generateMonthlyChallenge())

        return quests
    }

    // Generate story/campaign quests
    fun generateStoryQuests(playerLevel: Int): List<Quest> {
        val quests = mutableListOf<Quest>()

        // Chapter 1: The Beginning (levels 1-5)
        if (playerLevel >= 1) {
            quests.addAll(listOf(
                generateStoryQuest("Multiverse Awakening", "The first step into the multiverse", QuestDifficulty.EASY, "science", 1),
                generateStoryQuest("Dimensional Echoes", "Strange echoes from parallel worlds", QuestDifficulty.EASY, "history", 2),
                generateStoryQuest("Quantum Queries", "Master the basics of quantum knowledge", QuestDifficulty.MEDIUM, "science", 3)
            ))
        }

        // Chapter 2: The Expansion (levels 6-10)
        if (playerLevel >= 6) {
            quests.addAll(listOf(
                generateStoryQuest("Temporal Tangle", "Navigate the streams of time", QuestDifficulty.MEDIUM, "history", 6),
                generateStoryQuest("Parallel Puzzles", "Solve riddles from alternate realities", QuestDifficulty.MEDIUM, "entertainment", 7),
                generateStoryQuest("Cosmic Conundrums", "Explore the mysteries of the cosmos", QuestDifficulty.HARD, "science", 9)
            ))
        }

        // Chapter 3: The Mastery (levels 11-15)
        if (playerLevel >= 11) {
            quests.addAll(listOf(
                generateStoryQuest("Quantum Quandary", "Master the complex nature of quantum trivia", QuestDifficulty.HARD, "science", 11),
                generateStoryQuest("Temporal Trials", "Face challenges across time periods", QuestDifficulty.ADEPT, "history", 13),
                generateStoryQuest("Interdimensional Inquiry", "Delve into questions from across dimensions", QuestDifficulty.ADEPT, "entertainment", 15)
            ))
        }

        // Chapter 4: The Ascension (levels 16-20)
        if (playerLevel >= 16) {
            quests.addAll(listOf(
                generateStoryQuest("Reality Riddles", "Unravel the fabric of reality through knowledge", QuestDifficulty.EXPERT, "science", 16),
                generateStoryQuest("Chronos Challenge", "Master the most complex time-based questions", QuestDifficulty.EXPERT, "history", 18),
                generateStoryQuest("Multiverse Mastery", "Prove your mastery of all knowledge domains", QuestDifficulty.LEGENDARY, "any", 20)
            ))
        }

        // Final chapter: The Multiverse Breaker (level 25+)
        if (playerLevel >= 25) {
            quests.add(generateStoryQuest(
                "The Ultimate Convergence",
                "Face the ultimate challenge as realities converge",
                QuestDifficulty.MULTIVERSE_BREAKER,
                "any",
                25,
                QuestType.BOSS_BATTLE
            ))
        }

        return quests
    }

    // Helper methods to generate different types of quests

    private fun generateDailyQuest(difficulty: QuestDifficulty, requiredLevel: Int = 1): Quest {
        val id = "daily_${difficulty.name.lowercase()}_${UUID.randomUUID().toString().substring(0, 8)}"
        val questionCount = when (difficulty) {
            QuestDifficulty.EASY -> 5
            QuestDifficulty.MEDIUM -> 7
            QuestDifficulty.HARD -> 10
            QuestDifficulty.ADEPT -> 12
            QuestDifficulty.EXPERT -> 15
            QuestDifficulty.LEGENDARY -> 20
            QuestDifficulty.MULTIVERSE_BREAKER -> 25
        }

        val categories = listOf("science", "history", "entertainment", "any")
        val category = categories.random()

        val title = "Daily ${difficulty.name.capitalize()} ${category.capitalize()} Challenge"
        val description = "Complete a daily challenge of $questionCount $category questions at ${difficulty.name.lowercase()} difficulty."

        val baseXp = baseXpRewards[QuestType.STANDARD] ?: 100
        val xpReward = (baseXp * xpMultipliers[difficulty]!!).toInt()

        val timeLimit = when (difficulty) {
            QuestDifficulty.EASY -> 120
            QuestDifficulty.MEDIUM -> 150
            QuestDifficulty.HARD -> 180
            QuestDifficulty.ADEPT -> 210
            QuestDifficulty.EXPERT -> 240
            QuestDifficulty.LEGENDARY -> 300
            QuestDifficulty.MULTIVERSE_BREAKER -> 360
        }

        // Set completion threshold based on difficulty (% of questions needed to complete)
        val completionThreshold = when (difficulty) {
            QuestDifficulty.EASY -> (questionCount * 0.6).toInt()
            QuestDifficulty.MEDIUM -> (questionCount * 0.65).toInt()
            QuestDifficulty.HARD -> (questionCount * 0.7).toInt()
            QuestDifficulty.ADEPT -> (questionCount * 0.7).toInt()
            QuestDifficulty.EXPERT -> (questionCount * 0.75).toInt()
            QuestDifficulty.LEGENDARY -> (questionCount * 0.8).toInt()
            QuestDifficulty.MULTIVERSE_BREAKER -> (questionCount * 0.85).toInt()
        }

        // Generate rewards
        val rewards = generateRewards(difficulty, xpReward)

        // Create the quest
        return Quest(
            id = id,
            title = title,
            description = description,
            difficulty = difficulty,
            type = QuestType.STANDARD,
            category = category,
            questionCount = questionCount,
            completionThreshold = completionThreshold,
            timeLimit = timeLimit,
            rewards = rewards,
            requiredLevel = requiredLevel,
            isTimeLimited = true,
            expirationDate = getDailyExpirationTime()
        )
    }

    private fun generateWeeklyQuest(difficulty: QuestDifficulty, requiredLevel: Int): Quest {
        val id = "weekly_${difficulty.name.lowercase()}_${UUID.randomUUID().toString().substring(0, 8)}"
        val questionCount = when (difficulty) {
            QuestDifficulty.EASY -> 10
            QuestDifficulty.MEDIUM -> 15
            QuestDifficulty.HARD -> 20
            QuestDifficulty.ADEPT -> 25
            QuestDifficulty.EXPERT -> 30
            QuestDifficulty.LEGENDARY -> 40
            QuestDifficulty.MULTIVERSE_BREAKER -> 50
        }

        val questTypes = listOf(QuestType.STANDARD, QuestType.TIME_ATTACK, QuestType.CHALLENGE_MODE)
        val questType = questTypes.random()

        val categories = listOf("science", "history", "entertainment", "any")
        val category = categories.random()

        val titlePrefix = when (questType) {
            QuestType.STANDARD -> "Weekly Challenge:"
            QuestType.TIME_ATTACK -> "Speed Trial:"
            QuestType.CHALLENGE_MODE -> "Expert Challenge:"
            else -> "Special Event:"
        }

        val title = "$titlePrefix ${category.capitalize()} Mastery"
        val description = "Complete a weekly challenge of $questionCount $category questions at ${difficulty.name.lowercase()} difficulty."

        val baseXp = baseXpRewards[questType] ?: 100
        val xpReward = (baseXp * questionCount / 5 * xpMultipliers[difficulty]!!).toInt()

        val timeLimit = when (questType) {
            QuestType.TIME_ATTACK -> questionCount * 10 // 10 seconds per question for time attack
            else -> questionCount * 15 // 15 seconds per question for other types
        }

        // Set completion threshold based on difficulty (% of questions needed to complete)
        val completionThreshold = when (difficulty) {
            QuestDifficulty.EASY -> (questionCount * 0.6).toInt()
            QuestDifficulty.MEDIUM -> (questionCount * 0.65).toInt()
            QuestDifficulty.HARD -> (questionCount * 0.7).toInt()
            QuestDifficulty.ADEPT -> (questionCount * 0.75).toInt()
            QuestDifficulty.EXPERT -> (questionCount * 0.8).toInt()
            QuestDifficulty.LEGENDARY -> (questionCount * 0.85).toInt()
            QuestDifficulty.MULTIVERSE_BREAKER -> (questionCount * 0.9).toInt()
        }

        // Generate rewards with better rewards for weekly quests
        val rewards = generateRewards(difficulty, xpReward, isWeekly = true)

        // Create the quest
        return Quest(
            id = id,
            title = title,
            description = description,
            difficulty = difficulty,
            type = questType,
            category = category,
            questionCount = questionCount,
            completionThreshold = completionThreshold,
            timeLimit = timeLimit,
            rewards = rewards,
            requiredLevel = requiredLevel,
            isTimeLimited = true,
            expirationDate = getWeeklyExpirationTime()
        )
    }

    private fun generateHolidayQuest(title: String, category: String): Quest {
        val id = "holiday_${UUID.randomUUID().toString().substring(0, 8)}"
        val difficulty = QuestDifficulty.MEDIUM
        val questionCount = 15

        val description = "Celebrate the season with a special holiday trivia challenge!"

        val baseXp = 150
        val xpReward = (baseXp * xpMultipliers[difficulty]!!).toInt()

        val timeLimit = questionCount * 15

        // Generate special holiday-themed rewards
        val rewards = listOf(
            Reward(
                id = "holiday_reward_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Festive Token Bundle",
                description = "A special bundle of multiverse tokens to celebrate the holiday season",
                tier = RewardTier.RARE,
                type = RewardType.MULTIVERSE_FRAGMENT,
                rarity = 0.3f,
                xpValue = xpReward,
                multiverseTokenValue = 25
            ),
            Reward(
                id = "holiday_cosmetic_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Seasonal Cosmetic",
                description = "A limited-time cosmetic item to showcase your holiday spirit",
                tier = RewardTier.EPIC,
                type = RewardType.COSMETIC_ITEM,
                rarity = 0.1f,
                xpValue = 50,
                multiverseTokenValue = 0
            )
        )

        return Quest(
            id = id,
            title = title,
            description = description,
            difficulty = difficulty,
            type = QuestType.STANDARD,
            category = category,
            questionCount = questionCount,
            completionThreshold = (questionCount * 0.7).toInt(),
            timeLimit = timeLimit,
            rewards = rewards,
            requiredLevel = 1,
            isTimeLimited = true,
            expirationDate = getHolidayExpirationTime()
        )
    }

    private fun generateMonthlyChallenge(): Quest {
        val id = "monthly_challenge_${UUID.randomUUID().toString().substring(0, 8)}"
        val difficulty = QuestDifficulty.LEGENDARY
        val questionCount = 30

        val calendar = Calendar.getInstance()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

        val title = "$month's Legendary Challenge"
        val description = "Take on this month's ultimate trivia challenge for exclusive rewards!"

        val baseXp = 300
        val xpReward = (baseXp * xpMultipliers[difficulty]!!).toInt()

        val timeLimit = questionCount * 20

        // Generate legendary rewards for monthly challenge
        val rewards = listOf(
            Reward(
                id = "monthly_reward_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Legendary Token Cache",
                description = "A significant bundle of multiverse tokens for completing the monthly challenge",
                tier = RewardTier.LEGENDARY,
                type = RewardType.MULTIVERSE_FRAGMENT,
                rarity = 0.2f,
                xpValue = xpReward,
                multiverseTokenValue = 100
            ),
            Reward(
                id = "monthly_unique_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Monthly Champion Title",
                description = "An exclusive title that marks you as a champion for this month",
                tier = RewardTier.MYTHICAL,
                type = RewardType.UNIQUE_CHARACTER,
                rarity = 0.05f,
                xpValue = 200,
                multiverseTokenValue = 0
            )
        )

        return Quest(
            id = id,
            title = title,
            description = description,
            difficulty = difficulty,
            type = QuestType.BOSS_BATTLE,
            category = "any",
            questionCount = questionCount,
            completionThreshold = (questionCount * 0.8).toInt(),
            timeLimit = timeLimit,
            rewards = rewards,
            requiredLevel = 15,
            isTimeLimited = true,
            expirationDate = getMonthlyExpirationTime()
        )
    }

    private fun generateStoryQuest(
        title: String,
        description: String,
        difficulty: QuestDifficulty,
        category: String,
        requiredLevel: Int,
        type: QuestType = QuestType.STANDARD
    ): Quest {
        val id = "story_${difficulty.name.lowercase()}_${UUID.randomUUID().toString().substring(0, 8)}"
        val questionCount = when (difficulty) {
            QuestDifficulty.EASY -> 8
            QuestDifficulty.MEDIUM -> 12
            QuestDifficulty.HARD -> 15
            QuestDifficulty.ADEPT -> 18
            QuestDifficulty.EXPERT -> 20
            QuestDifficulty.LEGENDARY -> 25
            QuestDifficulty.MULTIVERSE_BREAKER -> 30
        }

        val baseXp = baseXpRewards[type] ?: 100
        val xpReward = (baseXp * questionCount / 5 * xpMultipliers[difficulty]!!).toInt()

        val timeLimit = when (type) {
            QuestType.TIME_ATTACK -> questionCount * 10
            QuestType.BOSS_BATTLE -> questionCount * 20
            else -> questionCount * 15
        }

        // Set completion threshold based on difficulty
        val completionThreshold = when (difficulty) {
            QuestDifficulty.EASY -> (questionCount * 0.6).toInt()
            QuestDifficulty.MEDIUM -> (questionCount * 0.65).toInt()
            QuestDifficulty.HARD -> (questionCount * 0.7).toInt()
            QuestDifficulty.ADEPT -> (questionCount * 0.75).toInt()
            QuestDifficulty.EXPERT -> (questionCount * 0.8).toInt()
            QuestDifficulty.LEGENDARY -> (questionCount * 0.85).toInt()
            QuestDifficulty.MULTIVERSE_BREAKER -> (questionCount * 0.9).toInt()
        }

        // Generate story-specific rewards
        val rewards = generateStoryRewards(difficulty, xpReward, title)

        return Quest(
            id = id,
            title = title,
            description = description,
            difficulty = difficulty,
            type = type,
            category = category,
            questionCount = questionCount,
            completionThreshold = completionThreshold,
            timeLimit = timeLimit,
            rewards = rewards,
            requiredLevel = requiredLevel,
            isTimeLimited = false
        )
    }

    // Helper methods for generating rewards

    private fun generateRewards(difficulty: QuestDifficulty, xpValue: Int, isWeekly: Boolean = false): List<Reward> {
        val rewards = mutableListOf<Reward>()

        // Always add XP reward
        rewards.add(
            Reward(
                id = "xp_reward_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "XP Boost",
                description = "Experience points to help level up your character",
                tier = when (difficulty) {
                    QuestDifficulty.EASY -> RewardTier.COMMON
                    QuestDifficulty.MEDIUM -> RewardTier.COMMON
                    QuestDifficulty.HARD -> RewardTier.RARE
                    QuestDifficulty.ADEPT -> RewardTier.RARE
                    QuestDifficulty.EXPERT -> RewardTier.EPIC
                    QuestDifficulty.LEGENDARY -> RewardTier.EPIC
                    QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.LEGENDARY
                },
                type = RewardType.RARE_ACHIEVEMENT,
                rarity = 1.0f,  // Always awarded
                xpValue = xpValue
            )
        )

        // Add token reward based on difficulty
        val tokenAmount = when (difficulty) {
            QuestDifficulty.EASY -> if (isWeekly) 5 else 2
            QuestDifficulty.MEDIUM -> if (isWeekly) 10 else 5
            QuestDifficulty.HARD -> if (isWeekly) 15 else 8
            QuestDifficulty.ADEPT -> if (isWeekly) 20 else 12
            QuestDifficulty.EXPERT -> if (isWeekly) 30 else 18
            QuestDifficulty.LEGENDARY -> if (isWeekly) 50 else 25
            QuestDifficulty.MULTIVERSE_BREAKER -> if (isWeekly) 100 else 50
        }

        rewards.add(
            Reward(
                id = "token_reward_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Multiverse Tokens",
                description = "Currency that can be used to purchase power-ups and cosmetics",
                tier = when (difficulty) {
                    QuestDifficulty.EASY, QuestDifficulty.MEDIUM -> RewardTier.COMMON
                    QuestDifficulty.HARD, QuestDifficulty.ADEPT -> RewardTier.RARE
                    QuestDifficulty.EXPERT -> RewardTier.EPIC
                    QuestDifficulty.LEGENDARY -> RewardTier.LEGENDARY
                    QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.MYTHICAL
                },
                type = RewardType.MULTIVERSE_FRAGMENT,
                rarity = 1.0f,  // Always awarded
                xpValue = 0,
                multiverseTokenValue = tokenAmount
            )
        )

        // Add special reward for weekly quests or harder difficulties
        if (isWeekly || difficulty.ordinal >= QuestDifficulty.HARD.ordinal) {
            val rewardTypes = listOf(
                RewardType.UNIQUE_CHARACTER,
                RewardType.COSMETIC_ITEM,
                RewardType.EXCLUSIVE_BACKGROUND
            )

            val rewardTier = when (difficulty) {
                QuestDifficulty.HARD -> RewardTier.RARE
                QuestDifficulty.ADEPT -> RewardTier.EPIC
                QuestDifficulty.EXPERT -> RewardTier.EPIC
                QuestDifficulty.LEGENDARY -> RewardTier.LEGENDARY
                QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.MYTHICAL
                else -> RewardTier.COMMON
            }

            val rewardType = rewardTypes.random()
            val rewardName = when (rewardType) {
                RewardType.UNIQUE_CHARACTER -> getRandomCharacterName()
                RewardType.COSMETIC_ITEM -> getRandomCosmeticName()
                RewardType.EXCLUSIVE_BACKGROUND -> getRandomBackgroundName()
                else -> "Special Reward"
            }

            rewards.add(
                Reward(
                    id = "special_reward_${UUID.randomUUID().toString().substring(0, 8)}",
                    name = rewardName,
                    description = getRewardDescription(rewardType, rewardTier),
                    tier = rewardTier,
                    type = rewardType,
                    rarity = when (rewardTier) {
                        RewardTier.COMMON -> 0.5f
                        RewardTier.RARE -> 0.3f
                        RewardTier.EPIC -> 0.15f
                        RewardTier.LEGENDARY -> 0.07f
                        RewardTier.MYTHICAL -> 0.03f
                    },
                    xpValue = xpValue / 5,
                    multiverseTokenValue = 0
                )
            )
        }

        return rewards
    }

    private fun generateStoryRewards(difficulty: QuestDifficulty, xpValue: Int, questTitle: String): List<Reward> {
        val rewards = mutableListOf<Reward>()

        // XP reward
        rewards.add(
            Reward(
                id = "story_xp_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Story XP Boost",
                description = "A significant XP boost for advancing the story",
                tier = when (difficulty) {
                    QuestDifficulty.EASY -> RewardTier.COMMON
                    QuestDifficulty.MEDIUM -> RewardTier.RARE
                    QuestDifficulty.HARD -> RewardTier.RARE
                    QuestDifficulty.ADEPT -> RewardTier.EPIC
                    QuestDifficulty.EXPERT -> RewardTier.EPIC
                    QuestDifficulty.LEGENDARY -> RewardTier.LEGENDARY
                    QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.MYTHICAL
                },
                type = RewardType.RARE_ACHIEVEMENT,
                rarity = 1.0f,
                xpValue = (xpValue * 1.5).toInt()  // Story quests give 50% more XP
            )
        )

        // Token reward
        val tokenAmount = when (difficulty) {
            QuestDifficulty.EASY -> 5
            QuestDifficulty.MEDIUM -> 10
            QuestDifficulty.HARD -> 20
            QuestDifficulty.ADEPT -> 35
            QuestDifficulty.EXPERT -> 50
            QuestDifficulty.LEGENDARY -> 80
            QuestDifficulty.MULTIVERSE_BREAKER -> 150
        }

        rewards.add(
            Reward(
                id = "story_token_${UUID.randomUUID().toString().substring(0, 8)}",
                name = "Story Token Cache",
                description = "A collection of multiverse tokens for progressing the story",
                tier = when (difficulty) {
                    QuestDifficulty.EASY, QuestDifficulty.MEDIUM -> RewardTier.COMMON
                    QuestDifficulty.HARD, QuestDifficulty.ADEPT -> RewardTier.RARE
                    QuestDifficulty.EXPERT -> RewardTier.EPIC
                    QuestDifficulty.LEGENDARY -> RewardTier.LEGENDARY
                    QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.MYTHICAL
                },
                type = RewardType.MULTIVERSE_FRAGMENT,
                rarity = 1.0f,
                xpValue = 0,
                multiverseTokenValue = tokenAmount
            )
        )

        // Story-specific reward
        if (difficulty.ordinal >= QuestDifficulty.MEDIUM.ordinal) {
            val rewardTier = when (difficulty) {
                QuestDifficulty.MEDIUM -> RewardTier.RARE
                QuestDifficulty.HARD -> RewardTier.RARE
                QuestDifficulty.ADEPT -> RewardTier.EPIC
                QuestDifficulty.EXPERT -> RewardTier.EPIC
                QuestDifficulty.LEGENDARY -> RewardTier.LEGENDARY
                QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.MYTHICAL
                else -> RewardTier.COMMON
            }

            rewards.add(
                Reward(
                    id = "story_unique_${UUID.randomUUID().toString().substring(0, 8)}",
                    name = "Chapter: $questTitle",
                    description = "A special story item commemorating your completion of this chapter",
                    tier = rewardTier,
                    type = RewardType.UNIQUE_CHARACTER,
                    rarity = when (rewardTier) {
                        RewardTier.COMMON -> 0.5f
                        RewardTier.RARE -> 0.3f
                        RewardTier.EPIC -> 0.15f
                        RewardTier.LEGENDARY -> 0.07f
                        RewardTier.MYTHICAL -> 0.03f
                    },
                    xpValue = xpValue / 4,
                    multiverseTokenValue = 0
                )
            )
        }

        // Special cosmetic for high difficulty story quests
        if (difficulty.ordinal >= QuestDifficulty.EXPERT.ordinal) {
            rewards.add(
                Reward(
                    id = "story_cosmetic_${UUID.randomUUID().toString().substring(0, 8)}",
                    name = getStoryThemeCosmetic(questTitle),
                    description = "An exclusive cosmetic item inspired by your journey through this chapter",
                    tier = when (difficulty) {
                        QuestDifficulty.EXPERT -> RewardTier.EPIC
                        QuestDifficulty.LEGENDARY -> RewardTier.LEGENDARY
                        QuestDifficulty.MULTIVERSE_BREAKER -> RewardTier.MYTHICAL
                        else -> RewardTier.RARE
                    },
                    type = RewardType.COSMETIC_ITEM,
                    rarity = 0.1f,
                    xpValue = xpValue / 3,
                    multiverseTokenValue = 0
                )
            )
        }

        return rewards
    }

    // Utility methods

    private fun getDailyExpirationTime(): Long {
        val calendar = Calendar.getInstance()
        // Set to next day at midnight
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    private fun getWeeklyExpirationTime(): Long {
        val calendar = Calendar.getInstance()
        // Set to next week, same day
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    private fun getHolidayExpirationTime(): Long {
        val calendar = Calendar.getInstance()
        // Set expiration to 3 days later
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        return calendar.timeInMillis
    }

    private fun getMonthlyExpirationTime(): Long {
        val calendar = Calendar.getInstance()
        // Set to first day of next month
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    // Random name generators for rewards

    private fun getRandomCharacterName(): String {
        val prefixes = listOf("Quantum", "Cosmic", "Astral", "Ethereal", "Dimensional", "Temporal", "Galactic", "Void")
        val titles = listOf("Explorer", "Guardian", "Sentinel", "Traveler", "Warden", "Keeper", "Sage", "Master")
        return "${prefixes.random()} ${titles.random()}"
    }

    private fun getRandomCosmeticName(): String {
        val types = listOf("Aura", "Emote", "Badge", "Emblem", "Glove", "Halo", "Crown", "Robe")
        val attributes = listOf("Glowing", "Shimmering", "Pulsing", "Radiant", "Vibrant", "Mystical", "Arcane", "Legendary")
        return "${attributes.random()} ${types.random()}"
    }

    private fun getRandomBackgroundName(): String {
        val themes = listOf("Nebula", "Quantum Realm", "Timestream", "Dimensional Nexus", "Cosmic Void", "Reality Fracture", "Parallel Universe", "Starfield")
        return "$themes"
    }

    private fun getStoryThemeCosmetic(questTitle: String): String {
        val parts = questTitle.split(" ")
        val adjective = if (parts.size > 1) parts[0] else "Mysterious"
        val cosmetics = listOf("Emblem", "Aura", "Robe", "Staff", "Mask", "Cloak", "Pendant", "Bracer")

        return "$adjective ${cosmetics.random()}"
    }

    private fun getRewardDescription(type: RewardType, tier: RewardTier): String {
        return when (type) {
            RewardType.UNIQUE_CHARACTER ->
                "A ${tier.name.lowercase()} character skin to showcase your trivia prowess."
            RewardType.COSMETIC_ITEM ->
                "A ${tier.name.lowercase()} cosmetic item to customize your multiverse traveler."
            RewardType.MULTIVERSE_FRAGMENT ->
                "A fragment of multiverse energy that can be used for special crafting."
            RewardType.RARE_ACHIEVEMENT ->
                "A ${tier.name.lowercase()} achievement marking your trivia mastery."
            RewardType.EXCLUSIVE_BACKGROUND ->
                "An exclusive ${tier.name.lowercase()} background for your profile."
        }
    }
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}