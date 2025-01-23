package com.example.vibesshared.ui.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val icon: ImageVector? = null,
    val title: String? = null,
    var name: String = "",
    var settingsName: String = "Settings",
    var aboutName: String = "About Us",
) {
    data object Home : Screen("home", Icons.Default.Home, "Home")
    data object Friends : Screen("friends", Icons.Default.People, "Friends")
    data object Chats : Screen("chats", Icons.AutoMirrored.Filled.Chat, "Chats")
    data object Profile : Screen("profile", Icons.Default.Person, "Profile")
    data object Settings : Screen("settings", Icons.Default.Settings, "Settings")
    data object AboutUs : Screen("about_us", Icons.Default.Info, "About Us")
    data object ArrowScreen : Screen("arrow_screen", Icons.AutoMirrored.Filled.ArrowForward, "Arrow Screen")
    data object Login : Screen("login")
    data object ForgotPassword : Screen("forgot_password")
    data object CreateAccount : Screen("create_account")
    data object Splash : Screen("splash")
    data object Messaging : Screen("messaging/{chatId}", Icons.Default.People, "Messaging") {
        const val CHAT_ID_KEY = "chatId"
        fun createRoute(chatId: String) = "messaging/$chatId"
    }

    companion object {
        fun bottomNavItems() = listOf(Home, Friends, Chats)
        fun drawerNavItems() = listOf(Profile, Settings, AboutUs, ArrowScreen)
    }
}