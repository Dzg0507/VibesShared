package com.example.vibesshared.ui.ui.components // Make sure this package name is correct

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.screens.Screen // Import Screen from the correct package

@Composable
fun HomeButton(navController: NavController) {
    Button(onClick = { navController.navigate(Screen.Home.route) }) {
        Text("Home")
    }
}