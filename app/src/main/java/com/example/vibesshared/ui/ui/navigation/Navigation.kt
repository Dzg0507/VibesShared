// Navigation.kt
package com.example.vibesshared.ui.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vibesshared.ui.ui.components.AppTopBar
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.components.NavigationDrawer
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
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Authentication screens (no bottom bar or drawer)
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.CreateAccount.route) { CreateAccountScreen(navController) }

        // Main app screens (with bottom bar and drawer)
        composable(Screen.Home.route) {
            MainLayout(navController) {
                HomeScreen(navController)
            }
        }
        composable(Screen.Friends.route) {
            MainLayout(navController) {
                FriendsScreen(navController)
            }
        }
        composable(Screen.Chats.route) {
            MainLayout(navController) {
                ChatsScreen(navController)
            }
        }
        composable(Screen.Profile.route) {
            MainLayout(navController) {
                ProfileScreen(navController, "My Profile")
            }
        }
        composable(Screen.Settings.route) {
            MainLayout(navController) {
                SettingsScreen(navController, "My Settings")
            }
        }
        composable(Screen.AboutUs.route) {
            MainLayout(navController) {
                AboutUsScreen(navController, "About VibesShared")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(navController: NavController, content: @Composable () -> Unit) {
    var showDrawer by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(key1 = showDrawer) {
        if (showDrawer) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(onMenuIconClick = { showDrawer = !showDrawer })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    NavigationDrawer(navController = navController) { screen ->
                        navController.navigate(screen.route)
                        showDrawer = false
                    }
                },
                gesturesEnabled = showDrawer
            ) {
                content()
            }
        }
    }
}