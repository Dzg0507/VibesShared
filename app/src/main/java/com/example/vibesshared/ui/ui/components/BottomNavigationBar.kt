package com.example.vibesshared.ui.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    authState: AuthState?,  // Pass authState directly
    userId: String
) {
    val navItems = Screen.bottomNavItems()

    // Check authState directly here
    if (authState is AuthState.Authenticated) {
        NavigationBar(
            containerColor = Color.Black
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route
            navItems.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon!!,
                            contentDescription = screen.title,
                            tint = Color.White
                        )
                    },
                    label = { Text(screen.title!!, fontSize = 9.sp, color = Color.White) },
                    selected = currentDestination == screen.route, // Simplified selection check
                    onClick = {
                        // Force navigation to the specific route, ignoring the back stack
                        navController.navigate(screen.route) {
                            // Clear the back stack and start fresh with this destination
                            popUpTo(screen.route) {
                                inclusive = true // Remove all instances of this route from the stack
                            }
                            launchSingleTop = true // Avoid adding duplicate destinations
                        }
                    }
                )
            }
        }
    }
}