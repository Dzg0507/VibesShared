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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    name: String,
    settingsName: String,
    aboutName: String,
    onDrawerWidthChange: (Float) -> Unit,
    onBottomBarWidthChange: (Float) -> Unit,
    initialDrawerWidthFraction: Float,
    initialBottomBarWidthFraction: Float
) {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var privateAccount by remember { mutableStateOf(false) }
    var showProfilePicture by remember { mutableStateOf(true) }
    var allowDirectMessages by remember { mutableStateOf(true) }
    var drawerWidthFraction by remember { mutableFloatStateOf(initialDrawerWidthFraction) }
    var bottomBarWidthFraction by remember { mutableFloatStateOf(initialBottomBarWidthFraction) }

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

        // Slider for Navigation Drawer Width
        Text(text = "Navigation Drawer Width")
        Slider(
            value = drawerWidthFraction,
            onValueChange = {
                drawerWidthFraction = it
                onDrawerWidthChange(it)
            },
            valueRange = 0.5f..0.9f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "${(drawerWidthFraction * 100).toInt()}%")
        Spacer(modifier = Modifier.height(16.dp))

        // Slider for Bottom Navigation Bar Width
        Text(text = "Bottom Navigation Bar Width")
        Slider(
            value = bottomBarWidthFraction,
            onValueChange = {
                bottomBarWidthFraction = it
                onBottomBarWidthChange(it)
            },
            valueRange = 0.5f..1f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "${(bottomBarWidthFraction * 100).toInt()}%")
        Spacer(modifier = Modifier.height(16.dp))

        // Logout button
        Button(
            onClick = {
                authViewModel.logout(profileViewModel)
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
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

