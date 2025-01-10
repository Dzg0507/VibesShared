package com.example.vibesshared.ui.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vibesshared.ui.ui.components.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

object DataStoreKeys {
    val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
    val PROFILE_DATA = stringPreferencesKey("profile_data")
    val PROFILE_PICTURE = stringPreferencesKey("profile_picture")
}

suspend fun Context.saveProfileData(userProfile: UserProfile) {
    dataStore.edit { preferences ->
        val profileJson = Json.encodeToString(userProfile)
        preferences[DataStoreKeys.PROFILE_DATA] = profileJson
    }
}

fun Context.getProfileData(): Flow<UserProfile?> {
    return dataStore.data.map { preferences ->
        val profileJson = preferences[DataStoreKeys.PROFILE_DATA] ?: return@map null
        Json.decodeFromString<UserProfile>(profileJson)
    }
}
suspend fun Context.saveProfilePicture(profilePictureUrl: String) {
    dataStore.edit { preferences ->
        preferences[DataStoreKeys.PROFILE_PICTURE] = profilePictureUrl
    }
}
fun Context.getProfilePicture(): Flow<String?> {
    return dataStore.data.map { preferences ->
        preferences[DataStoreKeys.PROFILE_PICTURE]
    }
}