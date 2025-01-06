package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HomeButton
import com.example.vibesshared.ui.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { /* Handle forgot password logic */ }) {
                Text("Reset Password")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate(Screen.Login.route) }) { // Navigate back to LoginScreen
                Text("Back to Login")
            }

            Spacer(modifier = Modifier.weight(1f))

            HomeButton(navController)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}