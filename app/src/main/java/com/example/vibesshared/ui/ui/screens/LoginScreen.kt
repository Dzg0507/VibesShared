package com.example.vibesshared.ui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HomeButton
import com.example.vibesshared.ui.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (email == "admin" && password == "password") {
                    navController.navigate(Screen.Home.route)
                } else {
                    // Handle incorrect login
                }
            }) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate(Screen.ForgotPassword.route) }) {
                Text("Forgot Password?")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate(Screen.CreateAccount.route) }) {
                Text("Create Account")
            }

            Spacer(modifier = Modifier.weight(1f))

            HomeButton(navController)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}