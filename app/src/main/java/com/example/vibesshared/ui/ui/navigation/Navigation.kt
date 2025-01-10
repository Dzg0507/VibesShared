package com.example.vibesshared.ui.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.screens.AboutUsScreen
import com.example.vibesshared.ui.ui.screens.ChatsScreen
import com.example.vibesshared.ui.ui.screens.CreateAccountScreen
import com.example.vibesshared.ui.ui.screens.ForgotPasswordScreen
import com.example.vibesshared.ui.ui.screens.FriendsScreen
import com.example.vibesshared.ui.ui.screens.HomeScreen
import com.example.vibesshared.ui.ui.screens.LoginScreen
import com.example.vibesshared.ui.ui.screens.ProfileScreen
import com.example.vibesshared.ui.ui.screens.SettingsScreen
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

sealed class Screen(
    val route: String,
    val icon: ImageVector? = null,
    val title: String? = null,
    val name: String = "",
    val settingsName: String = "Settings",
    val aboutName: String = "About Us",


) {
    // Bottom Navigation Items
    data object Home : Screen("home", Icons.Default.Home, "Home")
    data object Friends : Screen("friends", Icons.Default.People, "Friends")
    data object Chats : Screen("chats", Icons.AutoMirrored.Filled.Chat, "Chats")

    // Drawer Navigation Items
    data object Profile : Screen(
        route = "profile",
        icon = Icons.Default.Person,
        title = "Profile",
        name = "Profile",

    )

    data object Settings : Screen(
        route = "settings",
        icon = Icons.Default.Settings,
        title = "Settings",
        name = "Settings"
    )

    data object AboutUs : Screen(
        route = "about_us",
        icon = Icons.Default.Info,
        title = "About Us",
        name = "About Us"
    )

    // Auth Screens
    data object Login : Screen("login")
    data object ForgotPassword : Screen("forgot_password")
    data object CreateAccount : Screen("create_account")

    companion object {
        fun bottomNavItems() = listOf(Home, Friends, Chats)
        fun drawerNavItems() = listOf(Profile, Settings, AboutUs)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, authState, currentRoute)
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = if (authState is AuthState.Authenticated) {
                Screen.Home.route
            } else {
                Screen.Login.route
            },
            modifier = modifier
        ) {
            // Auth screens
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.CreateAccount.route) {
                CreateAccountScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            // Bottom Nav Screens
            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                )
            }

            composable(Screen.Friends.route) {
                FriendsScreen(navController)
            }

            composable(Screen.Chats.route) {
                ChatsScreen(navController)
            }

            // Drawer Screens
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    name = Screen.Profile.title ?: "",
                    settingsName = Screen.Settings.title ?: "",
                    aboutName = Screen.AboutUs.title ?: ""
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    name = Screen.Settings.title ?: "",
                    settingsName = Screen.Settings.title ?: "",
                    aboutName = Screen.AboutUs.title ?: ""
                )
            }

            composable(Screen.AboutUs.route) {
                AboutUsScreen(
                    name = Screen.AboutUs.title ?: "",
                    settingsName = Screen.Settings.title ?: "",
                    aboutName = Screen.AboutUs.title ?: ""
                )
            }
        }
    }
}