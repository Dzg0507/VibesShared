package com.example.vibesshared.ui.ui.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define DataStore at the top level
private val Context.loginDataStore: DataStore<Preferences> by preferencesDataStore(name = "login_tracker")

object LoginTracker {
    private val LOGIN_COUNT_KEY = intPreferencesKey("login_count")

    // Get the current login count as a Flow
    fun getLoginCount(context: Context): Flow<Int> {
        return context.loginDataStore.data.map { preferences ->
            preferences[LOGIN_COUNT_KEY] ?: 0
        }
    }

    // Increment the login count
    suspend fun incrementLoginCount(context: Context) {
        context.loginDataStore.edit { preferences ->
            val currentCount = preferences[LOGIN_COUNT_KEY] ?: 0
            preferences[LOGIN_COUNT_KEY] = currentCount + 1
        }
    }
}