package com.example.vibesshared.ui.ui.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vibesshared.ui.ui.data.PlayerProgressWithPowerUps
import com.example.vibesshared.ui.ui.data.PowerUp
import com.example.vibesshared.ui.ui.data.PowerUpFactory
import com.example.vibesshared.ui.ui.data.PowerUpInventory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.powerUpDataStore: DataStore<Preferences> by preferencesDataStore(name = "power_ups")

@Singleton
class PowerUpRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questRepository: QuestRepository
) {
    // Track the player's power-up inventory
    private val _playerPowerUps = MutableStateFlow(
        PowerUpInventory(
            powerUps = mapOf(
                "time_boost" to 3,
                "hint" to 5,
                "fifty_fifty" to 2,
                "time_freeze" to 1
            )
        )
    )

    /**
     * Get all available power-ups in the game
     */
    fun getAvailablePowerUps(): Flow<List<PowerUp>> {
        return questRepository.playerProgress.map { playerProgress ->
            PowerUpFactory.getAllPowerUps().filter { powerUp ->
                playerProgress.level >= powerUp.unlockLevel
            }
        }
    }

    /**
     * Get all power-ups the player currently owns
     */
    fun getPlayerPowerUps(): PlayerProgressWithPowerUps {
        return PlayerProgressWithPowerUps(
            baseProgress = questRepository.getPlayerProgressSnapshot(),
            powerUpInventory = _playerPowerUps.value
        )
    }

    /**
     * Award a power-up to the player
     */
    fun awardPowerUp(powerUpId: String) {
        // Update the power-up inventory
        val currentInventory = _playerPowerUps.value
        val currentCount = currentInventory.powerUps[powerUpId] ?: 0

        val updatedPowerUps = currentInventory.powerUps.toMutableMap()
        updatedPowerUps[powerUpId] = currentCount + 1

        _playerPowerUps.value = PowerUpInventory(
            powerUps = updatedPowerUps,
            activeEffects = currentInventory.activeEffects
        )

        println("PowerUp awarded to player: $powerUpId, new count: ${currentCount + 1}")
    }

    /**
     * Consume a power-up (reduce count by 1)
     */
    fun consumePowerUp(powerUpId: String) {
        // Update the power-up inventory
        val currentInventory = _playerPowerUps.value
        val currentCount = currentInventory.powerUps[powerUpId] ?: 0

        if (currentCount > 0) {
            val updatedPowerUps = currentInventory.powerUps.toMutableMap()
            updatedPowerUps[powerUpId] = currentCount - 1

            _playerPowerUps.value = PowerUpInventory(
                powerUps = updatedPowerUps,
                activeEffects = currentInventory.activeEffects
            )

            println("PowerUp consumed: $powerUpId, remaining: ${currentCount - 1}")
        }
    }

    /**
     * Get details for a specific power-up
     */
    fun getPowerUpById(powerUpId: String): PowerUp? {
        return PowerUpFactory.getAllPowerUps().find { it.id == powerUpId }
    }

    // The following methods would typically be implemented with DataStore
    // For a full implementation, these would actually update persistent storage

    suspend fun savePowerUpInventory(inventory: PowerUpInventory) {
        context.powerUpDataStore.edit { preferences ->
            // Store power-up counts
            inventory.powerUps.forEach { (powerUpId, count) ->
                preferences[intPreferencesKey("powerup_${powerUpId}_count")] = count
            }

            // Store active effects
            preferences[intPreferencesKey("active_effects_count")] = inventory.activeEffects.size
            inventory.activeEffects.forEachIndexed { index, effect ->
                preferences[stringPreferencesKey("active_effect_${index}_id")] = effect.powerUpId
                preferences[longPreferencesKey("active_effect_${index}_activation")] = effect.activationTime
                preferences[longPreferencesKey("active_effect_${index}_expiration")] = effect.expirationTime
            }
        }
    }

    fun getPowerUpInventory(): Flow<PowerUpInventory> {
        return context.powerUpDataStore.data.map { _ ->
            // In a real implementation, this would reconstruct the inventory from preferences
            _playerPowerUps.value
        }
    }
}