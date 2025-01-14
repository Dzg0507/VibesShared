package com.example.vibesshared.ui.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.screens.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel
) {
    val showSplash = remember { mutableStateOf(true) }
    val authState by authViewModel.authState.collectAsState()

    val startDestination = if (showSplash.value) {
        Screen.Splash.route
    } else if (authState is AuthState.Authenticated) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    Scaffold(
        bottomBar = {
            if (!showSplash.value) {
                BottomNavigationBar(
                    navController = navController,
                    authState = authState,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(paddingValues) // Apply Scaffold padding here
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(showSplash)
            }
            composable(Screen.Login.route) {
                // Your LoginScreen composable
            }
            composable(Screen.Home.route) {
                // Your HomeScreen composable
            }
            // ... other composables
        }
    }
}

@Composable
fun SplashScreen(showSplash: MutableState<Boolean>) {
    if (showSplash.value) {
        LaunchedEffect(Unit) {
            delay(2000) // Simulate a delay
            showSplash.value = false
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Splash Screen")
        }
    }
}