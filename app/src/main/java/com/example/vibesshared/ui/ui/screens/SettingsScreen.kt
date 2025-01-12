package com.example.vibesshared.ui.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    name: String,
    settingsName: String,
    aboutName: String
) {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var privateAccount by remember { mutableStateOf(false) }
    var showProfilePicture by remember { mutableStateOf(true) }
    var allowDirectMessages by remember { mutableStateOf(true) }
    val authState: AuthState by authViewModel.authState.collectAsState() // Collect authState

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, authState, "settings") } // Pass authState
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
            Spacer(modifier = Modifier.height(48.dp))

            SettingItem(
                title = "Notifications",
                checked = notifications,
                onCheckedChange = { notifications = it }
            )
            Spacer(modifier = Modifier.height(48.dp))

            SettingItem(
                title = "Sound",
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it }
            )
            Spacer(modifier = Modifier.height(48.dp))

            SettingItem(
                title = "Private Account",
                checked = privateAccount,
                onCheckedChange = { privateAccount = it }
            )
            Spacer(modifier = Modifier.height(48.dp))

            SettingItem(
                title = "Show Profile Picture",
                checked = showProfilePicture,
                onCheckedChange = { showProfilePicture = it }
            )
            Spacer(modifier = Modifier.height(48.dp))

            SettingItem(
                title = "Allow Direct Messages",
                checked = allowDirectMessages,
                onCheckedChange = { allowDirectMessages = it }
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Logout button
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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