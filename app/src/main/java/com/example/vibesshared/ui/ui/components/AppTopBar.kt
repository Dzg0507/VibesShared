package com.example.vibesshared.ui.ui.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(onMenuIconClick: () -> Unit, showMenuIcon: Boolean = true) {
    CenterAlignedTopAppBar(
        title = { /* ... */ },
        navigationIcon = {
            if (showMenuIcon) {
                IconButton(onClick = onMenuIconClick) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        }
    )
}