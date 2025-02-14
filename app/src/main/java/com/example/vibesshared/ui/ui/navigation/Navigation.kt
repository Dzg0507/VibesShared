// navigation/Screen.kt
package com.example.vibesshared.ui.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
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
import androidx.media3.common.util.Log
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

// **Screen sealed class - ALL SCREENS DEFINED HERE**
sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object CreateAccount : Screen("create_account_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object Home : Screen("home_screen", "Home", Icons.Filled.Home)
    object Friends : Screen("friends_screen", "Friends", Icons.Filled.People)
    object Chats : Screen("chats_screen", "Chats", Icons.Filled.ChatBubble)
    object CreatePost : Screen("create_post_screen")
    object Settings : Screen("settings_screen", "Settings", Icons.Filled.Settings)
    object AboutUs : Screen("about_us_screen", "AboutUs", Icons.Filled.Settings)
    object ArrowScreen : Screen("arrow_screen", "Arrow", Icons.Filled.Settings)
    object MyProfile : Screen("my_profile_screen", "My Profile", Icons.Filled.AccountBox)
    object TriviaGame : Screen("trivia_game_screen") // ADD TRIVIA GAME SCREEN

    // Profile now is an object and has a function to create the route
    object Profile : Screen("profile_screen") {
        const val USER_ID_KEY = "userId"
        fun createRoute(userId: String) = "profile_screen/$userId"
    }

    // Messaging now is an object and has a function to create the route
    object Messaging : Screen("messaging_screen") {
        const val CHAT_ID_KEY = "chatId"
        fun createRoute(chatId: String) = "messaging_screen/$chatId"
    }
    object Comments : Screen("comments_screen/{postId}") {
        // ADD COMMENTS SCREEN HERE
        fun createRoute(postId: String) = "comments_screen/$postId" // Route with postId argument
    }

    companion object {
        fun bottomNavItems(): List<Screen> = listOf(Home, Friends, Chats, MyProfile)
    }
}

@UnstableApi
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String,
    paddingValues: PaddingValues
) {
    val authViewModel: AuthViewModel = hiltViewModel() // Hoist the ViewMode
    var currentGreetingPreference = remember {
        mutableStateOf(authViewModel.getGreetingPreference() ?: GreetingPreference.FIRST_NAME)
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Screen.CreateAccount.route) {
            CreateAccountScreen(navController = navController)
        }
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(route = Screen.Home.route) {
            HomeScreen(
                navController = navController,
                greetingPreference = currentGreetingPreference.value  // Access the value here
            )
        }

        // In SetupNavGraph
        composable(route = Screen.Friends.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState() //Correct way

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
                    LaunchedEffect(key1 = true) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
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
                    Log.d("SettingsScreen", "Preference changed to: $newPreference")
                    authViewModel.updateGreetingPreference(newPreference)
                    currentGreetingPreference.value = newPreference
                    if (true) {
                        currentGreetingPreference.value = newPreference
                    } else {
                        Log.e("SettingsScreen", "New preference is null, not updating.")
                    }
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
        composable(Screen.Comments.route) { backStackEntry -> // ADD COMMENTS SCREEN COMPOSABLE HERE
            val postId = backStackEntry.arguments?.getString("postId")
            CommentsScreen(navController = navController, postId = postId.toString()) // Pass postId
        }
        // Profile screen with argument
        composable(
            route = Screen.Profile.route + "/{${Screen.Profile.USER_ID_KEY}}",
            arguments = listOf(navArgument(Screen.Profile.USER_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screen.Profile.USER_ID_KEY)
            if (userId != null) {
                ProfileScreen(navController = navController, userId = userId)
            } else {
                Text("Error loading profile")
            }
        }
        // Messaging screen with argument
        composable(
            route = Screen.Messaging.route + "/{${Screen.Messaging.CHAT_ID_KEY}}",
            arguments = listOf(navArgument(Screen.Messaging.CHAT_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString(Screen.Messaging.CHAT_ID_KEY)
            if (chatId != null) {
                MessagingScreen(navController = navController, chatId = chatId)
            } else {
                Text("Error loading chat")
            }
        }
        composable(route = Screen.TriviaGame.route) { // ADD TRIVIA GAME COMPOSABLE
            TriviaGameScreen()
        }
    }
}