package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                if ((authState as AuthState.Error).message.contains("reset email sent", ignoreCase = true)) {
                    resetEmailSent = true
                    isLoading = false
                    // Auto navigate back after showing success message
                    delay(3000)
                    navController.navigateUp()
                } else {
                    isLoading = false
                }
            }
            else -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.my_profile_icon),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 24.dp)
            )

            // Instructions text
            Text(
                text = "Enter your email address and we'll send you instructions to reset your password.",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, "Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = authState is AuthState.Error && !(authState as? AuthState.Error)?.message?.contains("reset email sent", ignoreCase = true)!!,
                singleLine = true,
                enabled = !isLoading && !resetEmailSent
            )

            // Error or Success message
            AnimatedVisibility(
                visible = authState is AuthState.Error,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (authState is AuthState.Error) {
                    val errorMessage = (authState as AuthState.Error).message
                    Text(
                        text = errorMessage,
                        color = if (errorMessage.contains("reset email sent", ignoreCase = true))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            isLoading = true
                            // Using Firebase Auth directly for password reset
                            auth.sendPasswordResetEmail(email).await()
                            resetEmailSent = true
                            SnackbarHostState().showSnackbar(
                                "Password reset email sent to $email"
                            )
                        } catch (e: Exception) {
                            SnackbarHostState().showSnackbar(
                                e.message ?: "Failed to send reset email"
                            )
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading &&
                        !resetEmailSent &&
                        email.isNotBlank() &&
                        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Send Reset Link")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to login button
            TextButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Login")
            }

            // Additional help text
            if (!isLoading && !resetEmailSent) {
                Text(
                    text = "If you don't receive an email within a few minutes, please check your spam folder.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }
        }
    }
}