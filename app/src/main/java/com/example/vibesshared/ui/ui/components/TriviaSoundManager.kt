package com.example.vibesshared.ui.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages sound effects for the trivia game
 */
class TriviaSoundManager(private val context: Context) {
    private val soundPool: SoundPool
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Media players for longer sounds/background music
    private val mediaPlayers = ConcurrentHashMap<String, MediaPlayer>()

    // Sound IDs for short effects
    private val soundIds = ConcurrentHashMap<String, Int>()

    // Default settings
    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var soundVolume = 1.0f
    private var musicVolume = 0.7f

    // Sound resource mapping
    private val soundResourceMap = mapOf(
        "correct_answer" to R.raw.correct_answer,
        "incorrect_answer" to R.raw.incorrect_answer,
        "button_press" to R.raw.button_press,
        "power_up" to R.raw.power_up,
        "level_up" to R.raw.level_up,
        "quest_complete" to R.raw.quest_complete,
        "forge_start" to R.raw.forge_start,
        "forge_complete" to R.raw.forge_complete,
        "reward_open" to R.raw.reward_open
    )

    // Music resource mapping
    private val musicResourceMap = mapOf(
        "menu_music" to R.raw.menu_music,
        "game_music" to R.raw.game_music,
        "shop_music" to R.raw.shop_music
    )

    init {
        // Initialize SoundPool for short sound effects
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Pre-load all sound effects
        coroutineScope.launch {
            loadSounds()
        }
    }

    private fun loadSounds() {
        // For demonstration, we're using placeholder resource IDs
        // In a real app, you would replace R.raw.X with actual resource IDs
        soundResourceMap.forEach { (soundName, resourceId) ->
            try {
                // This is a placeholder implementation since we don't have actual resource IDs
                // In a real implementation, you would use:
                // val soundId = soundPool.load(context, resourceId, 1)
               // val soundId = soundPool.load(context, android.R.raw.notification_alert_private, 1)
               // soundIds[soundName] = soundId
            } catch (e: Exception) {
                println("Failed to load sound: $soundName")
            }
        }
    }

    /**
     * Play a short sound effect
     */
    fun playSound(soundName: String) {
        if (!isSoundEnabled) return

        val soundId = soundIds[soundName] ?: return
        soundPool.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f)
    }

    /**
     * Play a sound with a specific resource ID
     */
    fun playSoundResource(resourceId: Int) {
        if (!isSoundEnabled) return

        try {
            // Load and play the sound
            val soundId = soundPool.load(context, resourceId, 1)
            soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
                if (status == 0) { // 0 indicates success
                    soundPool.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f)
                }
            }
        } catch (e: Exception) {
            println("Failed to play sound resource: $resourceId")
        }
    }

    /**
     * Start background music
     */
    fun startMusic(musicName: String, loop: Boolean = true) {
        if (!isMusicEnabled) return

        // Stop any currently playing music
        stopAllMusic()

        try {
            // This is a placeholder implementation since we don't have actual resource IDs
            // In a real implementation, you would use actual resource IDs from musicResourceMap
         //   val resourceId = android.R.raw.notification_alert_private

         //   val mediaPlayer = MediaPlayer.create(context, resourceId)
         //   mediaPlayer.isLooping = loop
         //   mediaPlayer.setVolume(musicVolume, musicVolume)
         //   mediaPlayer.start()

        //    mediaPlayers[musicName] = mediaPlayer
        } catch (e: Exception) {
            println("Failed to start music: $musicName")
        }
    }

    /**
     * Stop specific background music
     */
    fun stopMusic(musicName: String) {
        val mediaPlayer = mediaPlayers[musicName] ?: return

        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
            mediaPlayers.remove(musicName)
        } catch (e: Exception) {
            println("Failed to stop music: $musicName")
        }
    }

    /**
     * Stop all background music
     */
    fun stopAllMusic() {
        mediaPlayers.forEach { (_, mediaPlayer) ->
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                // Ignore errors when stopping
            }
        }
        mediaPlayers.clear()
    }

    /**
     * Enable or disable sound effects
     */
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    /**
     * Enable or disable background music
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled

        if (!enabled) {
            stopAllMusic()
        }
    }

    /**
     * Set sound effects volume
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Set background music volume
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)

        // Update volume of any currently playing music
        mediaPlayers.forEach { (_, mediaPlayer) ->
            try {
                mediaPlayer.setVolume(musicVolume, musicVolume)
            } catch (e: Exception) {
                // Ignore errors when adjusting volume
            }
        }
    }

    /**
     * Clean up resources
     */
    fun release() {
        stopAllMusic()
        soundPool.release()
    }

    // Mock R class for demonstration purposes
    private object R {
        object raw {
            const val correct_answer = 1
            const val incorrect_answer = 2
            const val button_press = 3
            const val power_up = 4
            const val level_up = 5
            const val quest_complete = 6
            const val forge_start = 7
            const val forge_complete = 8
            const val reward_open = 9
            const val menu_music = 10
            const val game_music = 11
            const val shop_music = 12
        }
    }
}

/**
 * Composable that manages the lifecycle of the sound manager
 */
@Composable
fun rememberSoundManager(): TriviaSoundManager {
    val context = LocalContext.current
    val soundManager = remember { TriviaSoundManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    return soundManager
}

/**
 * Sound effects for specific game actions
 */
class TriviaGameSoundEffects(private val soundManager: TriviaSoundManager) {
    fun playButtonClick() {
        soundManager.playSound("button_press")
    }

    fun playCorrectAnswer() {
        soundManager.playSound("correct_answer")
    }

    fun playIncorrectAnswer() {
        soundManager.playSound("incorrect_answer")
    }

    fun playPowerUpActivation() {
        soundManager.playSound("power_up")
    }

    fun playLevelUp() {
        soundManager.playSound("level_up")
    }

    fun playQuestComplete() {
        soundManager.playSound("quest_complete")
    }

    fun playForgeStart() {
        soundManager.playSound("forge_start")
    }

    fun playForgeComplete() {
        soundManager.playSound("forge_complete")
    }

    fun playRewardOpen() {
        soundManager.playSound("reward_open")
    }

    fun startMenuMusic() {
        soundManager.startMusic("menu_music")
    }

    fun startGameMusic() {
        soundManager.startMusic("game_music")
    }

    fun startShopMusic() {
        soundManager.startMusic("shop_music")
    }

    fun stopAllMusic() {
        soundManager.stopAllMusic()
    }
}