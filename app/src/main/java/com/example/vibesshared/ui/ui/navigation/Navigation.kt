package com.example.vibesshared.ui.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vibesshared.ui.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Friends : Screen("friends")
    object Chats : Screen("chats")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AboutUs : Screen("about_us")
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object CreateAccount : Screen("create_account")
}

@Composable
fun Navigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route, // Start at the LoginScreen
        modifier = modifier
    ) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Friends.route) { FriendsScreen(navController) }
        composable(Screen.Chats.route) { ChatsScreen(navController) }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, "My Profile")
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController, "My Settings")
        }
        composable(Screen.AboutUs.route) {
            AboutUsScreen(navController, "About VibesShared")
        }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.CreateAccount.route) { CreateAccountScreen(navController) }
    }
}