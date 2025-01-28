package com.example.vibesshared.ui.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.screens.*
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    authState: AuthState?,
    modifier: Modifier = Modifier
) {
    if (authState !is AuthState.Authenticated) return

    val items = listOf(
        Screen.Home,
        Screen.Friends,
        Screen.Chats
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.DarkGray,
        modifier = modifier
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    screen.icon?.let { iconRes ->
                        val imageVector = when (currentDestination?.route) {
                            screen.route -> screen.selectedIcon ?: iconRes
                            else -> iconRes
                        }
                        Icon(imageVector = imageVector, contentDescription = screen.title)
                    }
                },
                label = { Text(screen.title ?: "") },
                selected = currentDestination?.route == screen.route,
                onClick = { navigateToScreen(navController, screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(id = R.color.night_blue),
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.Gray,
                    selectedTextColor = colorResource(id = R.color.neon_green),
                    indicatorColor = Color.DarkGray),
            )
        }
    }
}


private fun navigateToScreen(navController: NavHostController, screen: Screen) {
    navController.navigate(screen.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(UnstableApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupNavGraph(
    navController: NavHostController = rememberNavController(),
    auth: FirebaseAuth
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val navigationEvent by authViewModel.navigationEvent.collectAsState(initial = NavigationEvent.None)

    val showSplash = rememberSaveable { mutableStateOf(true) }
    val bottomBarWidthFraction = remember { mutableFloatStateOf(1f) }
    val drawerWidthFraction = remember { mutableFloatStateOf(0.8f) }

    val startDestination = when {
        showSplash.value -> Screen.Splash.route
        authState is AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Login.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route



    BackHandler(enabled = currentDestination != Screen.Home.route) {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) {
                inclusive = true
            }
        }
    }

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateToRoute -> {
                navController.navigate((navigationEvent as NavigationEvent.NavigateToRoute).route) {
                    (navigationEvent as NavigationEvent.NavigateToRoute).popUpToRoute?.let { popUpToRoute ->
                        popUpTo(popUpToRoute) {
                            inclusive = (navigationEvent as NavigationEvent.NavigateToRoute).inclusive
                        }
                    }
                }
            }
            else -> { /* Do nothing */ }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (!showSplash.value && authState is AuthState.Authenticated) {
                BottomNavigationBar(
                    navController = navController,
                    authState = authState,
                    modifier = Modifier.fillMaxWidth(bottomBarWidthFraction.value)
                )
            }
        }
    ) { scaffoldPadding ->
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        NavigationDrawer(
            navController = navController,
            drawerState = drawerState,
            auth = auth
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(navController = navController, authViewModel = authViewModel)
                }

                splashComposable(showSplash, authState, navController)
                authComposables(navController, authViewModel)
                homeComposable(navController)
                friendsComposable(navController)
                chatsScreenComposable(navController)
                messagingComposable(navController)
                profileComposable(navController, auth)
                settingsComposable(navController)
                aboutUsComposable(navController)
                arrowScreenComposable(navController)
                createPostComposable(navController)

            }
        }
    }
}

private fun NavGraphBuilder.homeComposable(navController: NavHostController) {
    composable(Screen.Home.route) {
        HomeScreen(navController)
    }
}

@UnstableApi
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
        CreateAccountScreen(navController = navController, authViewModel = authViewModel)
    }
    composable(Screen.ForgotPassword.route) {
        ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
    }
}

private fun NavGraphBuilder.friendsComposable(navController: NavHostController) {
    composable(Screen.Friends.route) {
        FriendsScreen(navController)
    }
}

private fun NavGraphBuilder.chatsScreenComposable(navController: NavHostController) {
    composable(Screen.Chats.route) {
        ChatsScreen(navController)
    }
}

private fun NavGraphBuilder.messagingComposable(navController: NavHostController) {
    composable(
        route = Screen.Messaging.route,
        arguments = listOf(navArgument(Screen.Messaging.CHAT_ID_KEY) { type = NavType.StringType })
    ) { backStackEntry ->
        val chatId = backStackEntry.arguments?.getString(Screen.Messaging.CHAT_ID_KEY) ?: return@composable
        MessagingScreen(navController, chatId)
    }
}

private fun NavGraphBuilder.profileComposable(navController: NavHostController, auth: FirebaseAuth) {
    composable(
        route = "profile/{userId}",
        arguments = listOf(
            navArgument("userId") {
                type = NavType.StringType
                defaultValue = auth.currentUser?.uid ?: ""
            }
        )
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId")
        val isCurrentUser = userId == auth.currentUser?.uid
        ProfileScreen(
            navController = navController,
            userId = userId ?: "",
            isCurrentUser = isCurrentUser
        )
    }
}

private fun NavGraphBuilder.settingsComposable(navController: NavHostController) {
    composable(Screen.Settings.route) {
        SettingsScreen(
            navController = navController,
            onDrawerWidthChange = { /* Will implement later */ },
            onBottomBarWidthChange = { /* Will implement later */ },
            initialDrawerWidthFraction = 0.8f,
            initialBottomBarWidthFraction = 1f
        )
    }
}


private fun NavGraphBuilder.aboutUsComposable(navController: NavHostController) {
    composable(Screen.AboutUs.route) {
        AboutUsScreen(
            navController = navController,
            name = "About",
            aboutName = "About Us"
        )
    }
}


private fun NavGraphBuilder.arrowScreenComposable(navController: NavHostController) {
    composable(Screen.ArrowScreen.route) {
        ArrowScreen(navController)
    }
}

private fun NavGraphBuilder.createPostComposable(navController: NavHostController) {
    composable(Screen.CreatePost.route) {
        CreatePostScreen(navController)
    }
}

