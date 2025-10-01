package com.example.vibesshared.ui.ui.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.screens.*
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    authState: AuthState
) {
    val showSplash = rememberSaveable { mutableStateOf(true) }
    val bottomBarWidthFraction = remember { mutableFloatStateOf(1f) }
    val drawerWidthFraction = remember { mutableFloatStateOf(0.8f) }

    val startDestination = when {
        showSplash.value -> Screen.Splash.route
        authState is AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Login.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = rememberSaveable { mutableStateOf(startDestination) }

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let { route ->
            currentRoute.value = route
        }
    }

    Scaffold(
        bottomBar = {
            if (!showSplash.value && authState is AuthState.Authenticated) {
                BottomNavigationBar(
                    navController,
                    authState,
                    currentRoute.value,
                    modifier = Modifier.fillMaxWidth(fraction = bottomBarWidthFraction.floatValue)
                )
            }
        }
    ) { scaffoldPadding ->
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        NavigationDrawer(
            navController = navController,
            drawerState = drawerState
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                composable(Screen.Login.route) {
                    Log.d("Navigation", "LoginScreen entered")
                    LoginScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                splashComposable(showSplash, authState, navController)
                authComposables(navController, authViewModel)
                homeComposable(navController, authViewModel, profileViewModel)
                friendsComposable(navController)
                chatsScreenComposable(navController)
                messagingComposable(navController)
                profileComposable(navController, authViewModel, profileViewModel)
                settingsComposable(
                    navController,
                    authViewModel,
                    profileViewModel,
                    drawerWidthFraction,
                    bottomBarWidthFraction
                )
                aboutUsComposable(navController)
                arrowScreenComposable(navController)
                
                // Epic Features
                aiAssistantComposable(navController)
                arCameraComposable(navController)
                liveStreamingComposable(navController)
                gamingComposable(navController)
                musicComposable(navController)
                locationComposable(navController)
                marketplaceComposable(navController)
                videoComposable(navController)
                eventManagementComposable(navController)
            }
        }
    }
}

private fun NavGraphBuilder.splashComposable(
    showSplash: MutableState<Boolean>,
    authState: AuthState?,
    navController: NavHostController
) {
    composable(Screen.Splash.route) {
        SplashScreen(
            onTimeout = {
                showSplash.value = false
                navController.navigate(
                    if (authState is AuthState.Authenticated) Screen.Home.route else Screen.Login.route
                ) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.authComposables(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    composable(Screen.CreateAccount.route) {
        Log.d("Navigation", "CreateAccountScreen entered")
        CreateAccountScreen(navController = navController, authViewModel = authViewModel)
    }
    composable(Screen.ForgotPassword.route) {
        Log.d("Navigation", "ForgotPasswordScreen entered")
        ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
    }
}

private fun NavGraphBuilder.homeComposable(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    composable(Screen.Home.route) {
        HomeScreen(
            navController = navController,
            authViewModel = authViewModel,
            profileViewModel = profileViewModel
        )
    }
}

private fun NavGraphBuilder.friendsComposable(navController: NavHostController) {
    composable(Screen.Friends.route) {
        FriendsScreen(navController = navController)
    }
}

private fun NavGraphBuilder.chatsScreenComposable(navController: NavHostController) {
    composable(Screen.Chats.route) {
        ChatsScreen(navController = navController)
    }
}

private fun NavGraphBuilder.messagingComposable(navController: NavHostController) {
    composable(
        route = Screen.Messaging.route,
        arguments = listOf(navArgument(Screen.Messaging.CHAT_ID_KEY) { type = NavType.StringType })
    ) { backStackEntry ->
        val chatId = backStackEntry.arguments?.getString(Screen.Messaging.CHAT_ID_KEY) ?: ""
        MessagingScreen(navController = navController, chatId = chatId)
    }
}

private fun NavGraphBuilder.profileComposable(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    composable(Screen.Profile.route) {
        ProfileScreen(
            navController = navController,
            authViewModel = authViewModel,
            profileViewModel = profileViewModel,
            name = Screen.Profile.title ?: ""
        )
    }
}

private fun NavGraphBuilder.settingsComposable(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    drawerWidthFraction: MutableState<Float>,
    bottomBarWidthFraction: MutableState<Float>
) {
    composable(Screen.Settings.route) {
        SettingsScreen(
            navController = navController,
            authViewModel = authViewModel,
            profileViewModel = profileViewModel,
            name = Screen.Settings.title ?: "",
            settingsName = Screen.Settings.title ?: "",
            aboutName = Screen.AboutUs.title ?: "",
            onDrawerWidthChange = { fraction -> drawerWidthFraction.value = fraction },
            onBottomBarWidthChange = { fraction -> bottomBarWidthFraction.value = fraction },
            initialDrawerWidthFraction = drawerWidthFraction.value,
            initialBottomBarWidthFraction = bottomBarWidthFraction.value
        )
    }
}

private fun NavGraphBuilder.aboutUsComposable(navController: NavHostController) {
    composable(Screen.AboutUs.route) {
        AboutUsScreen(
            navController = navController,
            name = Screen.AboutUs.title ?: "",
            aboutName = Screen.AboutUs.title ?: ""
        )
    }
}

private fun NavGraphBuilder.arrowScreenComposable(navController: NavHostController) {
    composable(Screen.ArrowScreen.route) {
        ArrowScreen(navController = navController)
    }
}

// Epic Features Composables
private fun NavGraphBuilder.aiAssistantComposable(navController: NavHostController) {
    composable(Screen.AIAssistant.route) {
        AIAssistantScreen(navController = navController)
    }
}

private fun NavGraphBuilder.arCameraComposable(navController: NavHostController) {
    composable(Screen.ARCamera.route) {
        ARCameraScreen(navController = navController)
    }
}

private fun NavGraphBuilder.liveStreamingComposable(navController: NavHostController) {
    composable(Screen.LiveStreaming.route) {
        LiveStreamingScreen(navController = navController)
    }
}

private fun NavGraphBuilder.gamingComposable(navController: NavHostController) {
    composable(Screen.Gaming.route) {
        GamingScreen(navController = navController)
    }
}

private fun NavGraphBuilder.musicComposable(navController: NavHostController) {
    composable(Screen.Music.route) {
        MusicScreen(navController = navController)
    }
}

private fun NavGraphBuilder.locationComposable(navController: NavHostController) {
    composable(Screen.Location.route) {
        LocationScreen(navController = navController)
    }
}

private fun NavGraphBuilder.marketplaceComposable(navController: NavHostController) {
    composable(Screen.Marketplace.route) {
        MarketplaceScreen(navController = navController)
    }
}

private fun NavGraphBuilder.videoComposable(navController: NavHostController) {
    composable(Screen.Video.route) {
        VideoScreen(navController = navController)
    }
}

private fun NavGraphBuilder.eventManagementComposable(navController: NavHostController) {
    composable(Screen.EventManagement.route) {
        EventManagementScreen(navController = navController)
    }
}