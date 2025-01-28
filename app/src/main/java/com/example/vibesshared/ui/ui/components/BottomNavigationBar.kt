
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    authState: AuthState?,
    modifier: Modifier = Modifier,
) {
    if (authState !is AuthState.Authenticated) return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = remember {
        listOf(
            Screen.Home,
            Screen.Friends,
            Screen.Chats,
            Screen.Profile
        )
    }

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Color.DarkGray,
        modifier = modifier
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
                    if (screen == Screen.Profile) {
                        navController.navigate(Screen.Profile.createRoute())
                    } else {
                        navigateToScreen(navController, screen)
                    }
                }
            )
        }
    }
}
private fun navigateToScreen(navController: NavHostController, screen: Screen) {
    navController.navigate(screen.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}