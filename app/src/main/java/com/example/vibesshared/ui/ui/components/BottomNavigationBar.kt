package com.example.vibesshared.ui.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.navigation.Screen

sealed class BottomNavItem(val title: Int, val icon: Int, val screen: Screen) {
    object Home : BottomNavItem(R.string.home, R.drawable.ic_home, Screen.Home)
    object Friends : BottomNavItem(R.string.friends, R.drawable.ic_friend, Screen.Friends) // Changed to ic_friend
    object Chats : BottomNavItem(R.string.chats, R.drawable.ic_chat, Screen.Chats)
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Friends,
        BottomNavItem.Chats
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = item.icon),
                        contentDescription = stringResource(id = item.title)
                    )
                },
                label = { Text(stringResource(id = item.title)) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}