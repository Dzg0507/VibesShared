package com.example.vibesshared.ui.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.vibesshared.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    title: String,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        if (drawerState.isOpen) drawerState.close() else drawerState.open()
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo1), // Replace with your logo image
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp)
                )
            }
        },
        modifier = modifier // Add this line to apply the modifier
    )
}