package com.example.vibesshared.ui.ui.components

import android.content.Context
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.light.intensity
import io.github.sceneview.math.Position
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * A 3D scene renderer for the trivia game, using SceneView and Filament
 * Simplified version with stub implementations for compatibility
 */
class TriviaSceneRenderer(
    private val context: Context,
    private val modelsManager: TriviaModelsManager
) {
    private var sceneView: SceneView? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val animationJobs = mutableMapOf<String, Job>()

    // Camera settings
    private var defaultCameraPosition = Position(z = 4.0f)

    /**
     * Initialize the SceneView
     */
    fun initSceneView(sceneView: SceneView) {
        this.sceneView = sceneView

        // Let the models manager know about the SceneView
        modelsManager.setSceneView(sceneView)

        // Configure camera (minimal setup)
        try {
            sceneView.cameraNode.position = defaultCameraPosition

            // Add some ambient lighting if available
            sceneView.mainLight?.apply {
                intensity = 5000f
            }

            // Log initialization
            println("Initializing 3D scene with enhanced features")

        } catch (e: Exception) {
            println("Could not set camera position: ${e.message}")
        }
    }

    /**
     * Rotate the camera slightly for dynamic scene feel
     */
    fun rotateCameraSlightly(rotX: Float, rotY: Float) {
        sceneView?.cameraNode?.let { camera ->
            camera.rotation = io.github.sceneview.math.Rotation(rotX, rotY, 0f)
        }
    }

    /**
     * Create a 3D model for a power-up
     * Stub implementation that logs actions instead of creating actual nodes
     */
    fun createPowerUpModel(powerUpId: String, position: Position) {
        println("Creating power-up model for: $powerUpId at position: $position")

        // In a real implementation, we would create and add nodes to the scene
        coroutineScope.launch {
            // Simulate power-up effect timing
            delay(5000)
            println("Power-up effect for $powerUpId completed")
        }
    }

    /**
     * Display correct answer animation
     * Stub implementation
     */
    fun displayCorrectAnswerAnimation() {
        println("Displaying correct answer animation")

        coroutineScope.launch {
            // Camera shake effect for correct answer
            launch {
                shakeCameraGently(duration = 500, intensity = 0.05f)
            }
        }
    }

    /**
     * Display incorrect answer animation
     * Stub implementation
     */
    fun displayIncorrectAnswerAnimation() {
        println("Displaying incorrect answer animation")

        coroutineScope.launch {
            // Camera shake effect for incorrect answer - more intense
            launch {
                shakeCameraGently(duration = 700, intensity = 0.15f)
            }
        }
    }

    /**
     * Create quest completed animation
     * Stub implementation
     */
    fun createQuestCompletedAnimation() {
        println("Creating quest completion animation")

        coroutineScope.launch {
            // Camera celebration movement
            launch {
                celebrateCameraMovement()
            }
        }
    }

    /**
     * Trigger a lightning effect in the 3D scene based on screen coordinates
     * Stub implementation
     */
    fun triggerLightningEffect(screenX: Float, screenY: Float) {
        println("Triggering lightning effect at screen coordinates: $screenX, $screenY")

        coroutineScope.launch {
            // Brief camera animation
            flashCamera()
        }
    }

    /**
     * Shake camera gently for effects
     */
    private fun shakeCameraGently(duration: Long, intensity: Float) {
        coroutineScope.launch {
            try {
                val camera = sceneView?.cameraNode ?: return@launch
                val originalPosition = camera.position
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < duration) {
                    // Random offset
                    val xOffset = (Random.nextFloat() * 2f - 1f) * intensity
                    val yOffset = (Random.nextFloat() * 2f - 1f) * intensity

                    // Apply offset
                    camera.position = Position(
                        originalPosition.x + xOffset,
                        originalPosition.y + yOffset,
                        originalPosition.z
                    )

                    delay(16) // ~60fps
                }

                // Reset position
                camera.position = originalPosition

            } catch (e: Exception) {
                println("Camera shake interrupted: ${e.message}")
            }
        }
    }

    /**
     * Camera celebration movement (circular)
     */
    private fun celebrateCameraMovement() {
        coroutineScope.launch {
            try {
                val camera = sceneView?.cameraNode ?: return@launch
                val originalPosition = camera.position
                val originalRotation = camera.rotation

                // Circular movement around center
                for (i in 0..120) {
                    val progress = i / 120f
                    val angle = progress * kotlin.math.PI.toFloat() * 4f // Two full circles

                    // Circular path
                    val radius = 0.3f
                    val x = originalPosition.x + kotlin.math.cos(angle) * radius
                    val y = originalPosition.y + kotlin.math.sin(angle) * radius

                    // Apply new position
                    camera.position = Position(x, y, originalPosition.z)

                    // Look at center
                    camera.rotation = io.github.sceneview.math.Rotation(
                        -kotlin.math.sin(angle) * 10f,
                        kotlin.math.cos(angle) * 10f,
                        0f
                    )

                    delay(16) // ~60fps
                }

                // Reset to original
                camera.position = originalPosition
                camera.rotation = originalRotation

            } catch (e: Exception) {
                println("Camera celebration interrupted: ${e.message}")
            }
        }
    }

    /**
     * Quick camera flash for lightning effect
     */
    private fun flashCamera() {
        coroutineScope.launch {
            try {
                // Would normally adjust scene exposure or add a full-screen flash
                // Simplified version just logs the effect
                println("Flash camera effect")

                // Simulate light adjustment if available
                val light = sceneView?.mainLight

                if (light != null) {
                    val originalIntensity = light.intensity

                    // Bright flash
                    light.intensity = 100000f

                    delay(50)

                    // Reset
                    light.intensity = originalIntensity
                }

            } catch (e: Exception) {
                println("Camera flash effect interrupted: ${e.message}")
            }
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        // Cancel all animation jobs
        animationJobs.values.forEach { it.cancel() }
        animationJobs.clear()

        // Cancel the coroutine scope
        coroutineScope.cancel("Scene cleanup")

        // Clear scene view reference
        sceneView = null

        println("Scene renderer cleaned up")
    }
}

/**
 * A Composable wrapper for the TriviaSceneRenderer with enhanced interaction
 */
@Composable
fun TriviaScene(
    modifier: Modifier = Modifier,
    modelsManager: TriviaModelsManager,
    onSceneReady: (TriviaSceneRenderer) -> Unit = {},
    onInteraction: (Float, Float) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val renderer = remember { TriviaSceneRenderer(context, modelsManager) }

    // State for tracking drag gestures
    val dragX = remember { mutableFloatStateOf(0f) }
    val dragY = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(key1 = renderer) {
        onSceneReady(renderer)
    }

    DisposableEffect(Unit) {
        onDispose {
            renderer.cleanup()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragX.value += dragAmount.x * 0.1f
                    dragY.value += dragAmount.y * 0.1f

                    // Limit the rotation range
                    dragX.floatValue = dragX.floatValue.coerceIn(-10f, 10f)
                    dragY.floatValue = dragY.floatValue.coerceIn(-10f, 10f)

                    // Pass the interaction to parent
                    onInteraction(dragY.floatValue, dragX.floatValue)
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                SceneView(ctx).apply {
                    renderer.initSceneView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}