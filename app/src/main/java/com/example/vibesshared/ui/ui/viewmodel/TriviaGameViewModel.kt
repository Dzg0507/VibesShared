package com.example.vibesshared.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibesshared.ui.ui.data.*
import com.example.vibesshared.ui.ui.repository.PowerUpRepository
import com.example.vibesshared.ui.ui.repository.QuestRepository
import com.example.vibesshared.ui.ui.repository.TriviaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TriviaGameViewModel @Inject constructor(
    private val triviaRepository: TriviaRepository,
    private val questRepository: QuestRepository,
    private val powerUpRepository: PowerUpRepository
) : ViewModel() {

    private val _levelUpState = MutableStateFlow(LevelUpState())
    val levelUpState: StateFlow<LevelUpState> = _levelUpState.asStateFlow()

    private val _uiState = MutableStateFlow(TriviaGameUiState())
    val uiState: StateFlow<TriviaGameUiState> = _uiState.asStateFlow()

    private val _questState = MutableStateFlow(QuestUiState())
    val questState: StateFlow<QuestUiState> = _questState.asStateFlow()

    private val _multivergeForgeState = MutableStateFlow(MultivergeForgeUiState())
    val multivergeForgeState: StateFlow<MultivergeForgeUiState> =
        _multivergeForgeState.asStateFlow()

    private val _powerUpState = MutableStateFlow(PowerUpUiState())
    val powerUpState: StateFlow<PowerUpUiState> = _powerUpState.asStateFlow()

    private var timerJob: Job? = null
    private var powerUpEffectJobs: MutableMap<String, Job> = mutableMapOf()

    // For haptic and sound effects
    private var _gameAudioEnabled = MutableStateFlow(true)
    val gameAudioEnabled: StateFlow<Boolean> = _gameAudioEnabled.asStateFlow()

    // Track consecutive correct answers for streaks
    private var consecutiveCorrectAnswers = 0
    private val _streakState = MutableStateFlow(StreakState())
    val streakState: StateFlow<StreakState> = _streakState.asStateFlow()

    init {
        // Observe player progress
        viewModelScope.launch {
            questRepository.playerProgress.collect { progress ->
                _questState.update { currentState ->
                    currentState.copy(
                        playerProgress = progress,
                        availableQuests = questRepository.getAvailableQuests()
                    )
                }
            }
        }

        // Initialize available power-ups
        viewModelScope.launch {
            powerUpRepository.getAvailablePowerUps().collect { powerUps ->
                _powerUpState.update { currentState ->
                    currentState.copy(
                        availablePowerUps = powerUps,
                        playerPowerUps = powerUpRepository.getPlayerPowerUps()
                    )
                }
            }
        }
    }
    fun dismissLevelUpAnimation() {
        _levelUpState.update { state ->
            state.copy(showLevelUpAnimation = false)
        }
    }

    fun getRewardById(rewardId: String): Reward? {
        return questRepository.getRewardById(rewardId)
    }

    fun startGame(difficulty: String, category: String, questionCount: Int) {
        viewModelScope.launch {
            try {
                println("Starting game with:")
                println("Difficulty: $difficulty")
                println("Category: $category")
                println("Question Count: $questionCount")

                val questions = triviaRepository.getQuestions(difficulty, category, questionCount)

                println("Total Questions Fetched: ${questions.size}")

                if (questions.isEmpty()) {
                    // Handle no questions scenario
                    _uiState.update {
                        it.copy(
                            isGameOver = true,
                            showSettings = true
                        )
                    }
                    return@launch
                }

                // Reset streak
                consecutiveCorrectAnswers = 0
                _streakState.update { StreakState() }

                _uiState.update { currentState ->
                    currentState.copy(
                        questions = questions,
                        currentQuestion = questions.firstOrNull(),
                        currentQuestionIndex = 0,
                        currentAnswers = shuffleAnswers(questions.first()),
                        showSettings = false,
                        timeLeft = 60,
                        isGameOver = false,
                        score = 0,
                        answerFeedback = null,
                        selectedAnswer = null,
                        activePowerUps = emptyList(),
                        isTimeFrozen = false,
                        doublePointsActive = false
                    )
                }

                // Reset power-up states for new game
                _powerUpState.update { currentState ->
                    currentState.copy(
                        activePowerUps = emptyList(),
                        powerUpCooldowns = emptyMap()
                    )
                }

                startTimer()
            } catch (e: Exception) {
                println("Unhandled error fetching questions: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isGameOver = true,
                        showSettings = true
                    )
                }
            }
        }
    }

    fun startQuest(questId: String) {
        val quest = _questState.value.availableQuests.find { it.id == questId }
        quest?.let {
            // Update quest state
            _questState.update { currentState ->
                currentState.copy(
                    activeQuest = quest
                )
            }

            // Start the quest
            questRepository.startQuest(questId)

            // Start the game with quest parameters
            startGame(
                difficulty = it.difficulty.name.lowercase(),
                category = it.category,
                questionCount = it.questionCount
            )

            // Update timer if quest has a time limit
            _uiState.update { currentState ->
                currentState.copy(
                    timeLeft = it.timeLimit,
                    showSettings = false  // Directly hide settings for quests
                )
            }
        }
    }

    private fun calculateStreakBonus(streakCount: Int): Pair<Int, String> {
        // Define streak milestones and corresponding bonuses
        return when {
            streakCount >= 10 -> Pair(100, "10+ Streak Super Bonus")
            streakCount >= 7 -> Pair(50, "7+ Streak Mega Bonus")
            streakCount >= 5 -> Pair(30, "5+ Streak Ultra Bonus")
            streakCount >= 3 -> Pair(15, "3+ Streak Bonus")
            else -> Pair(0, "")
        }
    }

    // Update the checkAnswer method to award XP for streaks
    fun checkAnswer(selectedAnswer: String) {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion
        val isCorrect = selectedAnswer == currentQuestion?.correctAnswer

        // Pause the timer immediately when an answer is selected
        _uiState.update { state ->
            state.copy(isTimeFrozen = true)
        }

        // Calculate score, applying double points if active
        val pointsEarned = if (isCorrect) {
            if (currentState.doublePointsActive) 2 else 1
        } else 0

        // Update streak tracking
        if (isCorrect) {
            consecutiveCorrectAnswers++

            // Check if streak milestone reached
            if (consecutiveCorrectAnswers >= 3) {
                _streakState.update {
                    StreakState(
                        hasStreak = true,
                        streakCount = consecutiveCorrectAnswers
                    )
                }

                // Calculate streak bonus
                val (streakBonus, bonusDescription) = calculateStreakBonus(consecutiveCorrectAnswers)

                // Log the bonus
                println("TriviaGame: Player hit streak of $consecutiveCorrectAnswers. Awarding $streakBonus XP bonus ($bonusDescription)")

                // Add XP to player progress for the streak bonus
                if (streakBonus > 0) {
                    questRepository.addStreakBonusXp(streakBonus)

                    // Show streak notification
                    viewModelScope.launch {
                        delay(800) // Delay to show streak notification
                        _streakState.update { it.copy(
                            showStreakAnimation = true,
                            bonusXp = streakBonus,
                            bonusDescription = bonusDescription
                        ) }
                        delay(2000) // Show animation
                        _streakState.update { it.copy(showStreakAnimation = false) }
                    }
                }
            }
        } else {
            // Reset streak on wrong answer
            consecutiveCorrectAnswers = 0
            _streakState.update { StreakState() }
        }

        // Calculate final score with any streak bonuses
        val streakBonus = if (isCorrect && consecutiveCorrectAnswers >= 3) {
            consecutiveCorrectAnswers / 3
        } else 0

        val newScore = currentState.score + pointsEarned + streakBonus

        _uiState.update { currentState ->
            currentState.copy(
                score = newScore,
                answerFeedback = if (isCorrect) AnswerFeedback.CORRECT else AnswerFeedback.INCORRECT,
                selectedAnswer = selectedAnswer
            )
        }

        // No auto-advance to next question - let user control the pace
    }

    // Show the correct answer (called when user sees the incorrect feedback)
    fun showCorrectAnswer() {
        val currentQuestion = _uiState.value.currentQuestion

        if (currentQuestion != null) {
            // Update UI state to highlight the correct answer
            _uiState.update { state ->
                state.copy(
                    showingCorrectAnswer = true,  // Set the flag to true
                    isTimeFrozen = true          // Keep timer frozen
                )
            }
        }
    }

    fun showNextQuestion() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < currentState.questions.size) {
            _uiState.update { currentState ->
                currentState.copy(
                    currentQuestionIndex = nextIndex,
                    currentQuestion = currentState.questions[nextIndex],
                    currentAnswers = shuffleAnswers(currentState.questions[nextIndex]),
                    answerFeedback = null,
                    selectedAnswer = null,
                    showingCorrectAnswer = false,
                    isTimeFrozen = false  // Resume the timer when showing next question
                )
            }
        } else {
            // All questions answered, end game
            endGame()
        }
    }

    // Update the endGame method in TriviaGameViewModel.kt to ensure rewards are properly processed

    fun endGame() {
        val currentState = _uiState.value
        val activeQuest = _questState.value.activeQuest

        // Stop the timer
        timerJob?.cancel()

        // Cancel all active power-up effects
        powerUpEffectJobs.values.forEach { it.cancel() }
        powerUpEffectJobs.clear()

        _uiState.update { it.copy(isGameOver = true) }

        // Update quest progress if applicable
        activeQuest?.let {
            if (currentState.score >= it.completionThreshold) {
                // Create additional special forgeable reward for easy quests
                val rewardsToAward = ArrayList<Reward>()

                // Add all standard quest rewards
                rewardsToAward.addAll(it.rewards)

                // Add a special forgeable item for the first quest (easy difficulty quests)
                if (it.difficulty == QuestDifficulty.EASY || it.id.contains("easy") || it.id.contains(
                        "tutorial"
                    )
                ) {
                    val forgeableItem = Reward(
                        id = "forgeable_crystal_${System.currentTimeMillis()}",
                        name = "Dimensional Crystal",
                        description = "A rare crystal from another dimension that can be used in the Multiverse Forge",
                        tier = RewardTier.RARE,
                        type = RewardType.MULTIVERSE_FRAGMENT,
                        rarity = 0.3f,
                        xpValue = 50,
                        multiverseTokenValue = 10
                    )
                    rewardsToAward.add(forgeableItem)
                }

                // Debug logging
                println("TriviaGameViewModel: Quest completed - awarding ${rewardsToAward.size} rewards")

                // Complete the quest in the repository - this tracks it as completed
                questRepository.completeQuest(it.id, currentState.score)
                println("TriviaGameViewModel: Quest ${it.id} marked as completed")

                // Update UI state with the rewards we're awarding
                _uiState.update { state ->
                    state.copy(
                        awardedRewards = rewardsToAward,
                        showQuestCompletionAnimation = true,
                        questCompletionBannerVisible = true,
                        questCompleted = it.title
                    )
                }

                // Process each reward manually to ensure they're properly added to the forge inventory
                rewardsToAward.forEach { reward ->
                    println("TriviaGameViewModel: Processing reward for forge: ${reward.name} (${reward.id}, type: ${reward.type})")

                    // First, ensure it's in the rewards cache
                    // This step makes sure the reward is available for the Multiverse Forge
                    try {
                        // Try to directly add the reward using the repository's method
                        addRewardToForgeInventory(reward)
                    } catch (e: Exception) {
                        println("TriviaGameViewModel: Error adding reward to forge: ${e.message}")
                        e.printStackTrace()
                    }

                    // Also claim the reward normally to get XP and tokens
                    claimReward(reward.id)
                }
            } else {
                // Reset active quest if not completed successfully
                _questState.update { state ->
                    state.copy(activeQuest = null)
                }
            }
        }

        // Award power-ups based on performance
        awardPowerUpsBasedOnPerformance(currentState.score, currentState.questions.size)
    }

    // Add this helper method to ensure rewards are added to the forge inventory
    private fun addRewardToForgeInventory(reward: Reward) {
        println("TriviaGameViewModel: Adding reward to forge inventory: ${reward.name} (${reward.id})")

        // First, make sure the reward is in the QuestRepository's rewards cache
        val existingReward = questRepository.getRewardById(reward.id)
        if (existingReward == null) {
            println("TriviaGameViewModel: Reward not in cache, adding it now")

            // We need to ensure this reward gets added to both the cache and the player's unlocked rewards
            // Use the reflection technique to access the private method in QuestRepository
            try {
                val addRewardToInventoryMethod = questRepository.javaClass.getDeclaredMethod(
                    "addRewardToInventory",
                    Reward::class.java
                )
                addRewardToInventoryMethod.isAccessible = true
                addRewardToInventoryMethod.invoke(questRepository, reward)
                println("TriviaGameViewModel: Successfully added reward to forge inventory via reflection")
            } catch (e: Exception) {
                println("TriviaGameViewModel: Failed to add reward via reflection: ${e.message}")

                // Fallback - manually update the player progress
                val currentProgress = questRepository.getPlayerProgressSnapshot()
                val unlockedRewards = currentProgress.unlockedRewards.toMutableList()
                if (!unlockedRewards.contains(reward.id)) {
                    unlockedRewards.add(reward.id)
                    println("TriviaGameViewModel: Added reward to unlocked rewards list manually")

                    // Now need to update player progress - we'll use the claim method which should handle this
                    claimReward(reward.id)
                }
            }
        } else {
            println("TriviaGameViewModel: Reward already in cache, ensuring it's in unlocked rewards")

            // Ensure it's in the player's unlocked rewards
            val currentProgress = questRepository.getPlayerProgressSnapshot()
            if (!currentProgress.unlockedRewards.contains(reward.id)) {
                // Use the claim method which should add it to unlocked rewards
                claimReward(reward.id)
            }
        }
    }

    private fun startTimer() {
        // Cancel any existing timer job
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver) {
                delay(1000)

                // Only decrease timer if time isn't frozen
                if (!_uiState.value.isTimeFrozen) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            timeLeft = currentState.timeLeft - 1
                        )
                    }
                }

                // Time's up
                if (_uiState.value.timeLeft <= 0) {
                    endGame()
                }
            }
        }
    }

    private fun shuffleAnswers(question: Question?): List<String> {
        return question?.let {
            (listOf(it.correctAnswer) + it.incorrectAnswers).shuffled()
        } ?: emptyList()
    }

    // Settings methods
    fun toggleAudio() {
        _gameAudioEnabled.update { !it }
    }

    // Power-Up Methods

    /**
     * Use a power-up in the current game
     */
    fun usePowerUp(powerUpId: String) {
        val powerUp = _powerUpState.value.availablePowerUps.find { it.id == powerUpId }
        val playerPowerUps = _powerUpState.value.playerPowerUps

        // Check if the player has this power-up and it's not on cooldown
        if (powerUp != null && playerPowerUps.hasPowerUp(powerUpId) && !isPowerUpOnCooldown(
                powerUpId
            )
        ) {
            // Apply power-up effect based on type
            when (powerUp.type) {
                PowerUpType.TIME_FREEZE -> applyTimeFreeze(powerUp)
                PowerUpType.TIME_BOOST -> applyTimeBoost(powerUp)
                PowerUpType.FIFTY_FIFTY -> applyFiftyFifty()
                PowerUpType.CORRECT_ANSWER -> applyCorrectAnswer()
                PowerUpType.HINT -> applyHint()
                PowerUpType.SKIP_QUESTION -> applySkipQuestion()
                PowerUpType.DOUBLE_POINTS -> applyDoublePoints(powerUp)
            }

            // Consume one power-up and set cooldown
            powerUpRepository.consumePowerUp(powerUpId)
            setPowerUpCooldown(powerUpId, powerUp.cooldownSeconds)

            // Update UI state to reflect power-up usage
            _powerUpState.update { currentState ->
                currentState.copy(
                    playerPowerUps = powerUpRepository.getPlayerPowerUps(),
                    activePowerUps = currentState.activePowerUps + powerUpId
                )
            }
        }
    }

    private fun isPowerUpOnCooldown(powerUpId: String): Boolean {
        return _powerUpState.value.powerUpCooldowns[powerUpId]?.let {
            it > System.currentTimeMillis()
        } == true
    }

    private fun setPowerUpCooldown(powerUpId: String, durationSeconds: Int) {
        val expirationTime = System.currentTimeMillis() + (durationSeconds * 1000)

        _powerUpState.update { currentState ->
            val updatedCooldowns = currentState.powerUpCooldowns.toMutableMap().apply {
                put(powerUpId, expirationTime)
            }
            currentState.copy(powerUpCooldowns = updatedCooldowns)
        }

        // Start a job to clear cooldown when it expires
        viewModelScope.launch {
            delay(durationSeconds * 1000L)
            _powerUpState.update { currentState ->
                val updatedCooldowns = currentState.powerUpCooldowns.toMutableMap().apply {
                    remove(powerUpId)
                }
                currentState.copy(powerUpCooldowns = updatedCooldowns)
            }
        }
    }

    private fun applyTimeFreeze(powerUp: PowerUp) {
        _uiState.update { currentState ->
            currentState.copy(isTimeFrozen = true)
        }

        // Create a job to unfreeze the timer after duration
        val job = viewModelScope.launch {
            delay(powerUp.durationSeconds * 1000L)
            _uiState.update { currentState ->
                currentState.copy(isTimeFrozen = false)
            }
        }

        powerUpEffectJobs[powerUp.id] = job
    }

    private fun applyTimeBoost(powerUp: PowerUp) {
        _uiState.update { currentState ->
            currentState.copy(timeLeft = currentState.timeLeft + powerUp.durationSeconds)
        }
    }

    private fun applyFiftyFifty() {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion
        val correctAnswer = currentQuestion?.correctAnswer

        if (correctAnswer != null && currentState.currentAnswers.size >= 3) {
            // Get two random incorrect answers to remove
            val incorrectAnswers = currentState.currentAnswers.filter { it != correctAnswer }
            val answersToRemove = incorrectAnswers.shuffled().take(2)

            // Keep only the correct answer and one incorrect answer
            val filteredAnswers = currentState.currentAnswers.filter {
                it == correctAnswer || it !in answersToRemove
            }

            _uiState.update { currentState ->
                currentState.copy(currentAnswers = filteredAnswers)
            }
        }
    }

    private fun applyCorrectAnswer() {
        val currentState = _uiState.value
        val correctAnswer = currentState.currentQuestion?.correctAnswer

        if (correctAnswer != null) {
            checkAnswer(correctAnswer)
        }
    }

    private fun applyHint() {
        // For now, just highlight the correct answer briefly
        val correctAnswer = _uiState.value.currentQuestion?.correctAnswer

        if (correctAnswer != null) {
            _uiState.update { currentState ->
                currentState.copy(hintedAnswer = correctAnswer)
            }

            // Remove hint after 1.5 seconds
            viewModelScope.launch {
                delay(1500)
                _uiState.update { currentState ->
                    currentState.copy(hintedAnswer = null)
                }
            }
        }
    }

    private fun applySkipQuestion() {
        showNextQuestion()
    }

    private fun applyDoublePoints(powerUp: PowerUp) {
        _uiState.update { currentState ->
            currentState.copy(doublePointsActive = true)
        }

        // Create a job to disable double points after the next question
        val job = viewModelScope.launch {
            // Wait for next question to be answered
            while (_uiState.value.answerFeedback == null) {
                delay(100)
            }

            // Disable double points
            _uiState.update { currentState ->
                currentState.copy(doublePointsActive = false)
            }
        }

        powerUpEffectJobs[powerUp.id] = job
    }

    private fun awardPowerUpsBasedOnPerformance(score: Int, totalQuestions: Int) {
        val performance = score.toFloat() / totalQuestions.toFloat()

        when {
            performance >= 0.9f -> {
                // Award a rare power-up for excellent performance
                val rareOptions = listOf(
                    PowerUpFactory.createCorrectAnswerPowerUp(),
                    PowerUpFactory.createDoublePointsPowerUp()
                )
                powerUpRepository.awardPowerUp(rareOptions.random().id)
            }

            performance >= 0.7f -> {
                // Award a good power-up for good performance
                val goodOptions = listOf(
                    PowerUpFactory.createFiftyFiftyPowerUp(),
                    PowerUpFactory.createSkipQuestionPowerUp(),
                    PowerUpFactory.createTimeFreezePowerUp()
                )
                powerUpRepository.awardPowerUp(goodOptions.random().id)
            }

            performance >= 0.5f -> {
                // Award a basic power-up for decent performance
                val basicOptions = listOf(
                    PowerUpFactory.createTimeBoostPowerUp(),
                    PowerUpFactory.createHintPowerUp()
                )
                powerUpRepository.awardPowerUp(basicOptions.random().id)
            }
        }
    }

    // Multiverse Forge Methods
    fun selectRewardForForge(rewardId: String) {
        _multivergeForgeState.update { currentState ->
            val updatedSelectedRewards = if (currentState.selectedRewardIds.contains(rewardId)) {
                currentState.selectedRewardIds.filter { it != rewardId }
            } else {
                // Limit to 2 rewards for forging
                if (currentState.selectedRewardIds.size < 2) {
                    currentState.selectedRewardIds + rewardId
                } else {
                    currentState.selectedRewardIds
                }
            }

            currentState.copy(selectedRewardIds = updatedSelectedRewards)
        }
    }

    fun forgeMultiverseItem() {
        val currentState = _multivergeForgeState.value

        // Store the player's current level to check for level ups later
        val previousLevel = questState.value.playerProgress.level

        if (currentState.selectedRewardIds.size == 2) {
            println("TriviaGameViewModel: Forging items with IDs: ${currentState.selectedRewardIds}")

            // Get the names of the items being forged for logging
            val sourceItems = currentState.selectedRewardIds.mapNotNull { id ->
                getRewardById(id)?.name
            }
            println("TriviaGameViewModel: Forging items: ${sourceItems.joinToString(" and ")}")

            // Call repository to forge the items
            val forgedReward = questRepository.forgeMultiverseItem(currentState.selectedRewardIds)

            // Handle the result
            forgedReward?.let {
                println("TriviaGameViewModel: Successfully forged new item: ${it.name} (${it.id})")

                // Update the forge state to show the new item and clear selection
                _multivergeForgeState.update { state ->
                    state.copy(
                        forgedReward = it,
                        selectedRewardIds = emptyList()  // Clear the selection
                    )
                }

                // Get fresh player progress to check for level up
                val updatedPlayerProgress = questRepository.getPlayerProgressSnapshot()

                // Check if player leveled up
                val newLevel = updatedPlayerProgress.level
                if (newLevel > previousLevel) {
                    println("TriviaGameViewModel: Player leveled up from $previousLevel to $newLevel")

                    // Update level up state to trigger animation
                    _levelUpState.update { state ->
                        state.copy(
                            showLevelUpAnimation = true,
                            previousLevel = previousLevel,
                            newLevel = newLevel
                        )
                    }

                    // Auto-reset the animation after display
                    viewModelScope.launch {
                        delay(5000) // Give enough time for animation to be seen
                        _levelUpState.update { state ->
                            state.copy(showLevelUpAnimation = false)
                        }
                    }
                }

                // Ensure we refresh any UIs that are displaying the player's inventory
                _questState.update { currentState ->
                    currentState.copy(
                        playerProgress = updatedPlayerProgress
                    )
                }

                println("TriviaGameViewModel: Updated state with forged reward and cleared selection")
                println("TriviaGameViewModel: Player progress - Level: ${updatedPlayerProgress.level}, XP: ${updatedPlayerProgress.xp}")
            } ?: run {
                println("TriviaGameViewModel: Failed to forge item")
            }
        } else {
            println("TriviaGameViewModel: Cannot forge - need exactly 2 items, have ${currentState.selectedRewardIds.size}")
        }
    }

    // For resetting the game when returning to menu
    fun resetGame() {
        timerJob?.cancel()

        // Cancel all power-up effects
        powerUpEffectJobs.values.forEach { it.cancel() }
        powerUpEffectJobs.clear()

        _uiState.update {
            TriviaGameUiState(showSettings = true)
        }
    }

    fun claimReward(rewardId: String) {
        println("TriviaGameViewModel: Claiming reward: $rewardId")
        questRepository.claimReward(rewardId)

        // Check if reward is a power-up type
        val reward = questRepository.getRewardById(rewardId)
        if (reward?.type == RewardType.MULTIVERSE_FRAGMENT) {
            // Award a random power-up
            val randomPowerUp = PowerUpFactory.getAllPowerUps().random()
            println("TriviaGameViewModel: Awarding random power-up: ${randomPowerUp.name}")
            powerUpRepository.awardPowerUp(randomPowerUp.id)
        }
    }

    fun purchaseMultiverseTokens(amount: Int) {
        // Implementation would connect to payment provider
        // For now, just add tokens directly for testing
        questRepository.addMultiverseTokens(amount)
    }

    fun purchasePowerUp(powerUpId: String) {
        val powerUp = _powerUpState.value.availablePowerUps.find { it.id == powerUpId }

        if (powerUp != null && questRepository.getPlayerTokens() >= powerUp.tokenCost) {
            questRepository.spendMultiverseTokens(powerUp.tokenCost)
            powerUpRepository.awardPowerUp(powerUpId)

            // Update UI
            _powerUpState.update { currentState ->
                currentState.copy(
                    playerPowerUps = powerUpRepository.getPlayerPowerUps()
                )
            }
        }
    }

    // Optional: Add error logging method
    private fun logError(message: String, throwable: Throwable? = null) {
        println("TriviaGameViewModel Error: $message")
        throwable?.printStackTrace()
    }

    // Called when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        powerUpEffectJobs.values.forEach { it.cancel() }
    }
}


// Data classes to support the ViewModel
data class TriviaGameUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val currentAnswers: List<String> = emptyList(),
    val score: Int = 0,
    val timeLeft: Int = 60,
    val isGameOver: Boolean = false,
    var showSettings: Boolean = true,
    val answerFeedback: AnswerFeedback? = null,
    val selectedAnswer: String? = null,
    val isTimeFrozen: Boolean = false,
    val doublePointsActive: Boolean = false,
    val activePowerUps: List<String> = emptyList(),
    val hintedAnswer: String? = null,
    val showingCorrectAnswer: Boolean = false,
    val showQuestCompletionAnimation: Boolean = false,
    val questCompletionBannerVisible: Boolean = false,
    val questCompleted: String = "",
    val awardedRewards: List<Reward> = emptyList()
)

data class QuestUiState(
    val playerProgress: PlayerProgress = PlayerProgress(),
    val availableQuests: List<Quest> = emptyList(),
    val activeQuest: Quest? = null
)

data class MultivergeForgeUiState(
    val selectedRewardIds: List<String> = emptyList(),
    val forgedReward: Reward? = null
)

data class PowerUpUiState(
    val availablePowerUps: List<PowerUp> = emptyList(),
    val playerPowerUps: PlayerProgressWithPowerUps = PlayerProgressWithPowerUps(),
    val activePowerUps: List<String> = emptyList(),
    val powerUpCooldowns: Map<String, Long> = emptyMap() // PowerUp ID to expiration time (milliseconds)
)

data class StreakState(
    val hasStreak: Boolean = false,
    val streakCount: Int = 0,
    val showStreakAnimation: Boolean = false,
    val bonusXp: Int = 0,
    val bonusDescription: String = ""
)

data class LevelUpState(
    val showLevelUpAnimation: Boolean = false,
    val previousLevel: Int = 0,
    val newLevel: Int = 0
)