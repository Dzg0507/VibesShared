package com.example.vibesshared.ui.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null, val selectedIcon: ImageVector? = null) {
    object Login : Screen("login_screen")
    object CreateAccount : Screen("create_account_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Home : Screen("home_screen", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Settings : Screen("settings_screen", "Settings")
    object Friends : Screen("friends_screen", "Friends", Icons.Outlined.Person, Icons.Filled.Person)
    object Chats : Screen("chats_screen", "Chats", Icons.Outlined.Settings, Icons.Filled.Settings)
    object AboutUs : Screen("about_us_screen", "About Us")
    object ArrowScreen : Screen("arrow_screen")
    object Splash : Screen("splash_screen")
    object CreatePost : Screen("create_post_screen")
    object Messaging : Screen("messaging_screen/{${Screen.CHAT_ID_KEY}}") {
        const val CHAT_ID_KEY = "chatId"
        fun createRoute(chatId: String) = "messaging_screen/$chatId"
    }
    data object Profile : Screen("profile_screen?userId={userId}", "Profile") {
        const val USER_ID_KEY = "userId"
        fun createRoute(userId: String? = null)=
            "profile_screen${userId?.let {"?userId=$it"} ?:""}"
    }

    companion object {
        const val CHAT_ID_KEY = "chatId"
        const val USER_ID_KEY = "userId"

        fun bottomNavItems(): List<Screen> {
            return listOf(Home, Friends, Chats)
        }
    }
}
