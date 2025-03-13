package com.example.vibesshared.ui.ui.navigation

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vibesshared.ui.ui.enums.GreetingPreference
import com.example.vibesshared.ui.ui.screens.*
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel

// Screen sealed class - ALL SCREENS DEFINED HERE
sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login_screen")
    object CreateAccount : Screen("create_account_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Home : Screen("home_screen", "Home", Icons.Filled.Home) {
        const val POST_ID_KEY = "postId"
        fun createRoute(postId: String? = null) = if (postId != null) "home_screen?$POST_ID_KEY=$postId" else "home_screen"
    }
    object Friends : Screen("friends_screen", "Friends", Icons.Filled.People)
    object Chats : Screen("chats_screen", "Chats", Icons.Filled.ChatBubble)
    object CreatePost : Screen("create_post_screen")
    object Settings : Screen("settings_screen", "Settings", Icons.Filled.Settings)
    object AboutUs : Screen("about_us_screen", "AboutUs", Icons.Filled.Settings)
    object ArrowScreen : Screen("arrow_screen", "Arrow", Icons.Filled.Settings)
    object MyProfile : Screen("my_profile_screen", "My Profile", Icons.Filled.AccountBox)

    // Trivia Game Screens
    object TriviaGame : Screen("trivia_game_screen", "Trivia Game", Icons.Filled.SportsEsports)
    // Add to your Screen sealed class
    object QuestSelection : Screen("quest_selection_screen", "Multiverse Quests", Icons.Filled.Explore)
    object MultiverseForge : Screen("multiverse_forge_screen", "Multiverse Forge", Icons.Filled.Build)
    // Add to the sealed class Screen
    object PowerUpShop : Screen("power_up_shop_screen", "Power-Up Shop", Icons.Filled.ShoppingCart)

    object Profile : Screen("profile_screen") {
        const val USER_ID_KEY = "userId"
        fun createRoute(userId: String) = "profile_screen/$userId"
    }

    object Messaging : Screen("messaging_screen") {
        const val CHAT_ID_KEY = "chatId"
        fun createRoute(chatId: String) = "messaging_screen/$chatId"
    }

    object Comments : Screen("comments_screen/{postId}") {
        fun createRoute(postId: String) = "comments_screen/$postId"
    }

    object VideoPlayback : Screen("video_playback_screen") {
        const val VIDEO_URL_KEY = "videoUrl"
        fun createRoute(videoUrl: String) = "video_playback_screen/$videoUrl"
    }

    companion object {
        fun bottomNavItems(): List<Screen> = listOf(Home, Friends, Chats, MyProfile)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String,
    paddingValues: PaddingValues,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentGreetingPreference = remember {
        mutableStateOf(authViewModel.getGreetingPreference() ?: GreetingPreference.FIRST_NAME)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(route = Screen.CreateAccount.route) {
            CreateAccountScreen(navController = navController)
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(
            route = Screen.Home.route + "?${Screen.Home.POST_ID_KEY}={${Screen.Home.POST_ID_KEY}}",
            arguments = listOf(navArgument(Screen.Home.POST_ID_KEY) {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString(Screen.Home.POST_ID_KEY)
            HomeScreen(
                navController = navController,
                greetingPreference = currentGreetingPreference.value,
            )
        }

        composable(route = Screen.Friends.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AuthState.Authenticated -> {
                    val userId = (authState as AuthState.Authenticated).user!!.uid
                    FriendsScreen(navController = navController, currentUserId = userId)
                }

                is AuthState.Unauthenticated -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }

                is AuthState.Error -> {
                    Text("Authentication error: ${(authState as AuthState.Error).message}")
                }
            }
        }

        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(navController = navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                greetingPreference = currentGreetingPreference.value,
                onGreetingPreferenceChange = { newPreference ->
                    authViewModel.updateGreetingPreference(newPreference)
                    currentGreetingPreference.value = newPreference
                }
            )
        }

        composable(route = Screen.AboutUs.route) {
            AboutUsScreen(navController = navController)
        }

        composable(route = Screen.ArrowScreen.route) {
            ArrowScreen(navController = navController)
        }

        composable(route = Screen.Chats.route) {
            ChatsScreen(navController = navController)
        }

        composable(route = Screen.MyProfile.route) {
            MyProfileScreen(navController = navController)
        }

        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                CommentsScreen(navController = navController, postId = postId)
            } else {
                Text("Error loading comments")
            }
        }

        composable(
            route = Screen.Profile.route + "/{${Screen.Profile.USER_ID_KEY}}",
            arguments = listOf(navArgument(Screen.Profile.USER_ID_KEY) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screen.Profile.USER_ID_KEY)
            if (userId != null) {
                ProfileScreen(navController = navController, userId = userId)
            } else {
                Text("Error loading profile")
            }
        }

        composable(
            route = Screen.Messaging.route + "/{${Screen.Messaging.CHAT_ID_KEY}}",
            arguments = listOf(navArgument(Screen.Messaging.CHAT_ID_KEY) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString(Screen.Messaging.CHAT_ID_KEY)
            if (chatId != null) {
                MessagingScreen(navController = navController, chatId = chatId)
            } else {
                Text("Error loading chat")
            }
        }

        // Trivia Game Screens
        composable(route = Screen.TriviaGame.route) {
            val triviaViewModel: TriviaGameViewModel = hiltViewModel()
            TriviaGameScreen(
                navController = navController,
                viewModel = triviaViewModel
            )
        }

        composable(route = Screen.QuestSelection.route) {
            val triviaViewModel: TriviaGameViewModel = hiltViewModel()
            QuestSelectionScreenDetailed(
                viewModel = triviaViewModel,
                onStartQuest = {
                    // Change this to navigate to TriviaGame when a quest starts
                    navController.navigate(Screen.TriviaGame.route)
                },
                onBackToMenu = { navController.navigate(Screen.TriviaGame.route) }
            )
        }

        composable(route = Screen.MultiverseForge.route) {
            val triviaViewModel: TriviaGameViewModel = hiltViewModel()
            MultiverseForgeScreenDetailed(
                viewModel = triviaViewModel,
                onBackToMenu = { navController.navigate(Screen.TriviaGame.route) }
            )
        }
        // Add this composable block after the MultiverseForge composable
        composable(route = Screen.PowerUpShop.route) {
            val triviaViewModel: TriviaGameViewModel = hiltViewModel()
            PowerUpShopScreen(
                viewModel = triviaViewModel,
                powerUpState = triviaViewModel.powerUpState.collectAsState().value,
                questState = triviaViewModel.questState.collectAsState().value,
                onBackToMenu = { navController.navigate(Screen.TriviaGame.route) }
            )
        }

        composable(
            route = Screen.VideoPlayback.route + "/{${Screen.VideoPlayback.VIDEO_URL_KEY}}",
            arguments = listOf(navArgument(Screen.VideoPlayback.VIDEO_URL_KEY) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString(Screen.VideoPlayback.VIDEO_URL_KEY)
            if (videoUrl != null) {
                VideoPlaybackScreen(navController = navController, videoUrl = videoUrl)
            } else {
                Text("Error loading video")
            }
        }
    }
}