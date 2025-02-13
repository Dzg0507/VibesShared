package com.example.vibesshared.ui.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.ui.ui.navigation.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier // Add modifier parameter
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteState by remember { derivedStateOf { navBackStackEntry?.destination?.route } }
    val currentRoute = currentRouteState
    val auth = Firebase.auth

    val drawerScreens = remember {
        listOf(
            Screen.Home,
            Screen.Settings,
            Screen.AboutUs,
            Screen.ArrowScreen,
            Screen.MyProfile
        )
    }

    ModalDrawerSheet(
        modifier = modifier
            .width(250.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        drawerScreens.forEach { screen ->
            NavigationDrawerItem(
                icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
                label = { Text(screen.title ?: "Unnamed Screen") },
                selected = currentRoute == screen.route,
                onClick = {
                    Log.d("NavigationDebug", "Drawer Item Clicked: ${screen.route}, Current Route: $currentRoute")
                    scope.launch { drawerState.close() }

                    if (screen is Screen.Home) {
                        navigateToHomeScreenDrawer(navController, screen.route)
                        Log.d("NavigationDebug", "Navigation Drawer Navigating to Home: ${screen.route}")
                    } else {
                        navigateToScreenDrawer(navController, screen.route)
                        Log.d("NavigationDebug", "Navigation Drawer Navigating to: ${screen.route}")
                    }
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .background(Color.Green.copy(alpha = 0.1f))
            )
        }
    }
}


private fun navigateToScreenDrawer(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.currentBackStackEntry?.destination?.parent?.id?: navController.graph.findStartDestination().id) {
            inclusive = false
        }
        launchSingleTop = true
        restoreState = true
    }
}
private fun navigateToHomeScreenDrawer(navController: NavController, route: String) { // For HomeScreen
    navController.navigate(route) {
        popUpTo(Screen.Home.route) {
            inclusive = true
        }
        launchSingleTop = true
        restoreState = true
    }
}