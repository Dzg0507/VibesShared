package com.example.vibesshared.ui.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState() // Get loading state from ViewModel

    // LaunchedEffect to handle navigation on successful authentication
    LaunchedEffect(authState) {
        Log.d("LoginScreen", "AuthState changed: $authState") // Log AuthState changes
        when (authState) {
            is AuthState.Authenticated -> {
                Log.d("LoginScreen", "Navigation to Home triggered by AuthState.Authenticated")
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                // Error message is handled directly in the UI
                Log.d("LoginScreen", "AuthState is Error: ${(authState as AuthState.Error).message}")
            }
            AuthState.Loading -> {
                Log.d("LoginScreen", "AuthState is Loading")
                // No need to set isLoading here, it's handled by the ViewModel
            }
            AuthState.Unauthenticated -> {
                Log.d("LoginScreen", "AuthState is Unauthenticated")
                // No need to set isLoading here
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo1), // Make sure this resource exists
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next // Add imeAction
            ),
            isError = authState is AuthState.Error && email.isBlank(), // Simplified error check
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done // Add imeAction
            ),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            isError = authState is AuthState.Error && password.isBlank(), // Simplified error check
            singleLine = true
        )

        AnimatedVisibility(
            visible = authState is AuthState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message, // Display the error message
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                authViewModel.login(email, password) // Directly call login
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() // Use isLoading from ViewModel
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    color = Color.White // Or your desired color
                )
            }
            Text("Login")
        }

        // Navigate to Create Account Screen
        TextButton(
            onClick = {
                Log.d("LoginScreen", "Create New Account button clicked")
                navController.navigate(Screen.CreateAccount.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Account")
        }

        // Navigate to Forgot Password Screen
        TextButton(
            onClick = {
                Log.d("LoginScreen", "Forgot Password button clicked")
                navController.navigate(Screen.ForgotPassword.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot Password?")
        }
    }
}