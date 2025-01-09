package com.example.vibesshared.ui.ui.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    navController: NavController,
    authState: AuthState,
    currentRoute: String?,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Profile is now first in the drawer
                Screen.drawerNavItems().forEach { screen ->
                    NavigationDrawerItem(
                        icon = {
                            screen.icon?.let { Icon(it, contentDescription = screen.title) }
                        },
                        label = { Text(screen.title ?: "") },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(screen.route)
                            }
                        }
                    )
                }
            }
        }
    ) {
        content()
    }
}