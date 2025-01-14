package com.example.vibesshared.ui.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.vibesshared.ui.ui.screens.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun AuthNavigationHandler(
    navController: NavHostController,
    authState: AuthState,
    currentRoute: String?
) {
    var navigated by remember { mutableStateOf(false) }
    val isAuthScreen = currentRoute in listOf(
        Screen.Login.route,
        Screen.CreateAccount.route,
        Screen.ForgotPassword.route
    )

    LaunchedEffect(authState, currentRoute) {
        if (!navigated) {
            when (authState) {
                is AuthState.Authenticated -> {
                    if (isAuthScreen) {
                        Log.d("AuthNavigation", "Navigating to Home from Auth screen")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        navigated = true
                    }
                }
                is AuthState.Unauthenticated -> {
                    if (!isAuthScreen && currentRoute != null && currentRoute != Screen.Splash.route) {
                        Log.d("AuthNavigation", "Navigating to Login from Content screen: $currentRoute")
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                        navigated = true
                    }
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        navigated = false
    }
}