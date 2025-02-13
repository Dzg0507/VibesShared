package com.example.vibesshared.ui.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState


@Composable
fun AuthNavigationHandler(
    navController: NavHostController, // Use NavHostController
    authState: AuthState?, // authState can be null
    currentRoute: String?
) {
    LaunchedEffect(authState, currentRoute) {
        // Wait for a valid route AND for the auth state to be determined
        if (currentRoute != null && authState != null) {
            when (authState) {
                is AuthState.Authenticated -> {
                    // Only navigate to Home IF we're NOT already on a valid content screen
                    if (currentRoute in listOf(
                            Screen.Login.route,
                            Screen.CreateAccount.route,
                            Screen.ForgotPassword.route,
                            Screen.Splash.route
                        )) {
                        Log.d("AuthNavigation", "Navigating to Home from Auth screen")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    }
                }

                is AuthState.Unauthenticated -> {
                    if (currentRoute !in listOf(
                            Screen.Login.route,
                            Screen.CreateAccount.route,
                            Screen.ForgotPassword.route,
                            Screen.Splash.route
                        )) {
                        Log.d("AuthNavigation", "Navigating to Login from $currentRoute")
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    }
                }

                is AuthState.Error -> {
                    // We *don't* navigate away on error.  The Login/CreateAccount
                    // screens are responsible for *displaying* the error message
                    // to the user (which you're already doing).  We just stay put.
                    Log.d("AuthNavigation", "Auth error: ${authState.message}")
                }
                else -> {} //Do nothing
            }
        }
    }
}