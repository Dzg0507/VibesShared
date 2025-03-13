package com.example.vibesshared.ui.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the loading and caching of 3D models for the trivia game
 */
class TriviaModelsManager(private val context: Context) {
    // Coroutine scope for loading operations
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Caches for loaded models
    private val modelCache = ConcurrentHashMap<String, Any>()

    // SceneView instance (to be set by the component using this manager)
    private var sceneView: SceneView? = null

    // Default models for different game objects
    private val defaultModels = mapOf(
        "question_platform" to "models/platform.glb",
        "power_up_time_freeze" to "models/power_up_time.glb",
        "power_up_time_boost" to "models/power_up_time_boost.glb",
        "power_up_fifty_fifty" to "models/power_up_fifty.glb",
        "power_up_correct_answer" to "models/power_up_answer.glb",
        "power_up_hint" to "models/power_up_hint.glb",
        "power_up_skip_question" to "models/power_up_skip.glb",
        "power_up_double_points" to "models/power_up_double.glb",
        "correct_animation" to "models/correct.glb",
        "incorrect_animation" to "models/incorrect.glb",
        "quest_complete" to "models/quest_complete.glb",
        "forge" to "models/forge.glb",
        "reward_common" to "models/reward_common.glb",
        "reward_rare" to "models/reward_rare.glb",
        "reward_epic" to "models/reward_epic.glb",
        "reward_legendary" to "models/reward_legendary.glb",
        "reward_mythical" to "models/reward_mythical.glb"
    )

    /**
     * Set the SceneView to use for loading models
     */
    fun setSceneView(sceneView: SceneView) {
        this.sceneView = sceneView

        // Preload commonly used models
        coroutineScope.launch {
            preloadCommonModels()
        }
    }

    /**
     * Preload commonly used models
     */
    private suspend fun preloadCommonModels() {
        try {
            // For now, we don't need to preload anything since we're using placeholder nodes
        } catch (e: Exception) {
            println("Error preloading models: ${e.message}")
        }
    }

    /**
     * Create a model node for the given model key
     */
    suspend fun createModelNode(
        modelKey: String,
        position: Position = Position(x = 0f, y = 0f, z = 0f),
        rotation: Rotation = Rotation(x = 0f, y = 0f, z = 0f),
        scale: Scale = Scale(x = 1f, y = 1f, z = 1f)
    ): ModelNode {
        // Get a reference to the SceneView
        val sceneView = sceneView ?: throw IllegalStateException("SceneView not initialized")

        // Create a model node using the SceneView's engine
        return ModelNode(sceneView.engine).apply {
            this.position = position
            this.rotation = rotation
            this.scale = scale

            // In a real implementation, we would load the model here
            // For now, we'll let the node be empty
        }
    }

    /**
     * Helper method to map power-up ID to model key
     */
    fun getPowerUpModelKey(powerUpId: String): String {
        return when (powerUpId) {
            "time_freeze" -> "power_up_time_freeze"
            "time_boost" -> "power_up_time_boost"
            "fifty_fifty" -> "power_up_fifty_fifty"
            "correct_answer" -> "power_up_correct_answer"
            "hint" -> "power_up_hint"
            "skip_question" -> "power_up_skip_question"
            "double_points" -> "power_up_double_points"
            else -> "power_up_hint" // Default
        }
    }

    /**
     * Helper method to map reward tier to model key
     */
    fun getRewardModelKey(tier: String): String {
        return when (tier.lowercase()) {
            "common" -> "reward_common"
            "rare" -> "reward_rare"
            "epic" -> "reward_epic"
            "legendary" -> "reward_legendary"
            "mythical" -> "reward_mythical"
            else -> "reward_common" // Default
        }
    }

    /**
     * Clean up resources
     */
    fun release() {
        modelCache.clear()
        sceneView = null
    }
}

/**
 * Composable that manages the lifecycle of the models manager
 */
@Composable
fun rememberTriviaModelsManager(): TriviaModelsManager {
    val context = LocalContext.current
    val modelsManager = remember { TriviaModelsManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            modelsManager.release()
        }
    }

    return modelsManager
}