package com.example.vibesshared.ui.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    authState: AuthState,
    currentRoute: String?
) {
    if (authState is AuthState.Authenticated && !isAuthScreen(currentRoute)) {
        NavigationBar {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route

            Screen.bottomNavItems().forEach { screen ->
                NavigationBarItem(
                    icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                    label = { Text(screen.title!!) },
                    selected = currentDestination == screen.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the Home screen to reset the navigation stack
                            popUpTo(Screen.Home.route) {
                                saveState = true
                                inclusive = true // This might also be necessary
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

private fun isAuthScreen(route: String?): Boolean {
    return route in listOf(
        Screen.Login.route,
        Screen.CreateAccount.route,
        Screen.ForgotPassword.route
    )
}