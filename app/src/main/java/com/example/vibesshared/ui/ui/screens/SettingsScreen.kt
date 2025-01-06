package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HomeButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, settingsName: String) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()), // Make the content scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = settingsName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider()

            // Settings items
            SettingsItem(
                title = "Notifications",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            SettingsItem(
                title = "Dark Mode",
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
            SettingsItem(
                title = "Sound",
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it }
            )

            // Add more settings items as needed
            Spacer(modifier = Modifier.height(16.dp))
            HomeButton(navController)
        }
    }
}

@Composable
fun SettingsItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}