package com.example.vibesshared.ui.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
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
    
    // New Epic Features
    data object AIAssistant : Screen("ai_assistant", Icons.Filled.SmartToy, "AI Assistant")
    data object ARCamera : Screen("ar_camera", Icons.Filled.CameraAlt, "AR Camera")
    data object LiveStreaming : Screen("live_streaming", Icons.Filled.LiveTv, "Live Streaming")
    data object Gaming : Screen("gaming", Icons.Filled.SportsEsports, "Gaming")
    data object Music : Screen("music", Icons.Filled.MusicNote, "Music")
    data object Location : Screen("location", Icons.Filled.LocationOn, "Location")
    data object Marketplace : Screen("marketplace", Icons.Filled.Store, "Marketplace")
    data object Video : Screen("video", Icons.Filled.Videocam, "Video")

    companion object {
        fun bottomNavItems() = listOf(Home, Friends, Chats)
        fun drawerNavItems() = listOf(Profile, Settings, AboutUs, ArrowScreen)
        fun epicFeatures() = listOf(
            AIAssistant, ARCamera, LiveStreaming, Gaming, 
            Music, Location, Marketplace, Video
        )
    }
}