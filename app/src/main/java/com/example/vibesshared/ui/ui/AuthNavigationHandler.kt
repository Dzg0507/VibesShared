package com.example.vibesshared.ui.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun AuthNavigationHandler(
    navController: NavHostController,
    authState: AuthState,
    currentRoute: String?
) {
    LaunchedEffect(authState, currentRoute) {
        when (authState) {
            is AuthState.Authenticated -> {
                // If authenticated and on an auth screen, navigate to Home
                if (currentRoute in listOf(
                        Screen.Login.route,
                        Screen.CreateAccount.route,
                        Screen.ForgotPassword.route
                    )) {
                    Log.d("AuthNavigation", "Navigating to Home from Auth screen")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                // Only navigate to Login if NOT on an auth screen and NOT on Splash
                if (currentRoute != null && currentRoute != Screen.Splash.route &&
                    currentRoute !in listOf(
                        Screen.Login.route,
                        Screen.CreateAccount.route,
                        Screen.ForgotPassword.route
                    )
                ) {
                    Log.d("AuthNavigation", "Navigating to Login from Content screen: $currentRoute")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {null}
        }
    }
}