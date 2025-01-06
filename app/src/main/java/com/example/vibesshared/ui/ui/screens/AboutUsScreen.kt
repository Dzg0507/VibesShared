package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HomeButton

@Composable
fun AboutUsScreen(navController: NavController, aboutName: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = aboutName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display About Us information
        Text("App Name: VibesShared")
        Text("Version: 1.0")
        Text("Developers: The VibesShared Team")
        Text("Contact Email: support@vibesshared.com")

        Spacer(modifier = Modifier.weight(1f)) // Push the HomeButton to the bottom

        HomeButton(navController)
        Spacer(modifier = Modifier.height(16.dp))
    }
}