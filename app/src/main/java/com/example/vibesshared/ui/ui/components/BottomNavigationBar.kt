package com.example.vibesshared.ui.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    authState: AuthState?,
    userId: String, // userId parameter is accepted but not directly used in navigation here
    modifier: Modifier = Modifier,
) {
    if (authState !is AuthState.Authenticated) return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination by remember { derivedStateOf { navBackStackEntry?.destination } }

    val items = remember {
        listOf(
            Screen.Home,
            Screen.Friends,
            Screen.Chats,
            Screen.MyProfile // **Corrected: Using Screen.MyProfile for MyProfileScreen**
        )
    }

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Color.DarkGray,
        modifier = modifier.height(56.dp)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    screen.icon?.let { iconRes ->
                        Icon(
                            imageVector = iconRes,
                            contentDescription = screen.title ?: ""
                        )
                    }
                },
                label = {
                    Text(text = screen.title ?: "")
                },
                selected = currentDestination?.route == screen.route,
                onClick = {
                    Log.d("NavigationDebug", "BottomNav Item Clicked: ${screen.route}, Current Destination BEFORE NAV: ${currentDestination?.route}")
                    Log.d("NavigationDebug", "BottomNav - Navigating to: ${screen.route}")
                    navigateToScreen(navController, screen.route)
                    Log.d("NavigationDebug", "BottomNav Item Clicked: ${screen.route}, Current Destination AFTER NAV: ${currentDestination?.route}") // Log after navigation
                },
                modifier = Modifier.background(Color.Red.copy(alpha = 0.1f))
            )

            LaunchedEffect(currentDestination?.route) {
                Log.d("NavigationDebug", "BottomNav - Current Destination (LaunchedEffect): ${currentDestination?.route}")
            }
        }
    }
}

private fun navigateToScreen(navController: NavHostController, route: String) {
    Log.d("NavigationDebug", "navigateToScreen - Attempting to navigate to: $route (Minimal Nav)")
    navController.navigate(route)
    Log.d("NavigationDebug", "navigateToScreen - Navigation Completed to: $route (Minimal Nav)")
}