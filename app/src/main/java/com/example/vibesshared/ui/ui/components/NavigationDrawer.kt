package com.example.vibesshared.ui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.navigation.Screen

sealed class DrawerNavItem(val title: Int, val icon: ImageVector, val route: Screen) {
    data object Home : DrawerNavItem(R.string.home, Icons.Filled.Home, Screen.Home)
    data object Profile : DrawerNavItem(R.string.profile, Icons.Filled.Person, Screen.Profile)
    data object Settings : DrawerNavItem(R.string.settings, Icons.Filled.Settings, Screen.Settings)
    data object AboutUs : DrawerNavItem(R.string.about_us, Icons.Filled.Info, Screen.AboutUs)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(navController: NavController, onDrawerItemClick: (Screen) -> Unit) {
    val items = listOf(
        DrawerNavItem.Home,
        DrawerNavItem.Profile,
        DrawerNavItem.Settings,
        DrawerNavItem.AboutUs
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        items.forEach { item ->
            DrawerItem(
                item = item,
                onDrawerItemClick = { onDrawerItemClick(item.route) },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun DrawerItem(item: DrawerNavItem, onDrawerItemClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDrawerItemClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(id = item.title)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = item.title),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}