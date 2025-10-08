package com.example.vibesshared.ui.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.theme.*
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // Removed navigation logic from here
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = (authState as AuthState.Error).message
            }
            AuthState.Loading -> {
                isLoading = true
            }
            AuthState.Unauthenticated -> {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VibrantPurple.copy(alpha = 0.1f),
                        ElectricBlue.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(dampingRatio = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Section
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(animationSpec = spring(0.7f, delayMillis = 200)) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(VibrantPurple, ElectricBlue)
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo1),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Welcome Text
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) + slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = spring(0.8f, delayMillis = 400)
                    )
                ) {
                    Text(
                        text = "Welcome to Vibes",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 500)) + slideInHorizontally(
                        initialOffsetX = { it / 3 },
                        animationSpec = spring(0.8f, delayMillis = 500)
                    )
                ) {
                    Text(
                        text = "Connect, Share, and Discover",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Login Form Card
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(800, delayMillis = 600)) + scaleIn(animationSpec = spring(0.8f, delayMillis = 600))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = VibrantPurple.copy(alpha = 0.3f)
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Email, 
                                        "Email",
                                        tint = VibrantPurple
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                isError = authState is AuthState.Error && (authState as? AuthState.Error)?.message?.contains("email", ignoreCase = true) == true,
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VibrantPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = VibrantPurple
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            if (showPassword) "Hide password" else "Show password",
                                            tint = VibrantPurple
                                        )
                                    }
                                },
                                isError = authState is AuthState.Error && (authState as? AuthState.Error)?.message?.contains("password", ignoreCase = true) == true,
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VibrantPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedLabelColor = VibrantPurple
                                )
                            )

                            AnimatedVisibility(
                                visible = authState is AuthState.Error,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                if (authState is AuthState.Error) {
                                    Text(
                                        text = (authState as AuthState.Error).message,
                                        color = Rose,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        authViewModel.login(email, password)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantPurple,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(end = 8.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                                Text(
                                    text = "Login",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Navigate to Create Account Screen
                            TextButton(
                                onClick = {
                                    Log.d("LoginScreen", "Create New Account button clicked")
                                    navController.navigate(Screen.CreateAccount.route)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Create New Account",
                                    color = VibrantPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Navigate to Forgot Password Screen
                            TextButton(
                                onClick = {
                                    Log.d("LoginScreen", "Forgot Password button clicked")
                                    navController.navigate(Screen.ForgotPassword.route)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}