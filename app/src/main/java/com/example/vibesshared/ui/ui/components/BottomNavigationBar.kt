package com.example.vibesshared.ui.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.vibesshared.ui.ui.screens.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    authState: AuthState?,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    if (authState !is AuthState.Authenticated) return

    val items = Screen.bottomNavItems()

    NavigationBar(
        containerColor = Color.White,
        modifier = modifier
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { item.icon?.let { Icon(it, contentDescription = item.title) } },
                label = { Text(item.title ?: "") },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}