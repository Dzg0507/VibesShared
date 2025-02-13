package com.example.vibesshared.ui.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.enums.GreetingPreference
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel

@OptIn(UnstableApi::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    greetingPreference: GreetingPreference,
    onGreetingPreferenceChange: (GreetingPreference) -> Unit,
    // Add this parameter
) {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var privateAccount by remember { mutableStateOf(false) }
    var showProfilePicture by remember { mutableStateOf(true) }
    var allowDirectMessages by remember { mutableStateOf(true) }

    val authViewModel: AuthViewModel = hiltViewModel()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Settings options with Moderate Spacing
        SettingItem(
            title = "Dark Mode",
            checked = darkMode,
            onCheckedChange = { darkMode = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            title = "Notifications",
            checked = notifications,
            onCheckedChange = { notifications = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            title = "Sound",
            checked = soundEnabled,
            onCheckedChange = { soundEnabled = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            title = "Private Account",
            checked = privateAccount,
            onCheckedChange = { privateAccount = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            title = "Show Profile Picture",
            checked = showProfilePicture,
            onCheckedChange = { showProfilePicture = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingItem(
            title = "Allow Direct Messages",
            checked = allowDirectMessages,
            onCheckedChange = { allowDirectMessages = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Greeting preference setting
        SettingItem(
            title = "Greeting Preference",
            options = listOf(
                GreetingPreference.FIRST_NAME.displayName,
                GreetingPreference.LAST_NAME.displayName,
                GreetingPreference.USER_NAME.displayName,
                "${GreetingPreference.FIRST_NAME.displayName} & ${GreetingPreference.LAST_NAME.displayName}" // Combined option
            ),
            selectedOption = greetingPreference.displayName,
            onSelectionChange = { selected ->
                val foundPreference = when (selected) {
                    GreetingPreference.FIRST_NAME.displayName -> GreetingPreference.FIRST_NAME
                    GreetingPreference.LAST_NAME.displayName -> GreetingPreference.LAST_NAME
                    GreetingPreference.USER_NAME.displayName -> GreetingPreference.USER_NAME
                    "${GreetingPreference.FIRST_NAME.displayName} & ${GreetingPreference.LAST_NAME.displayName}" -> GreetingPreference.FULL_NAME // Handle the combined name
                    else -> {
                        Log.e("SettingsScreen", "No matching GreetingPreference found for display name: $selected")
                        GreetingPreference.FIRST_NAME
                    }
                }
                onGreetingPreferenceChange(foundPreference)
            }

        )
        Spacer(modifier = Modifier.height(16.dp))

        // Logout button
        Button(
            onClick = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout", color = Color.White)
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = selectedOption,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = true }
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}