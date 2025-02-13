package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HolographicProfile
import com.example.vibesshared.ui.ui.theme.AppColors
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String, // Get userId from navigation arguments
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load the profile when the screen is created or userId changes
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit){ animateIn = true}

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is ProfileViewModel.ProfileUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ProfileViewModel.ProfileUiState.Success -> {
                val profile = (uiState as ProfileViewModel.ProfileUiState.Success).profile
                ProfileContent(profile, animateIn) // Show the profile content
            }
            is ProfileViewModel.ProfileUiState.Error -> {
                val errorMessage = (uiState as ProfileViewModel.ProfileUiState.Error).message
                Text(
                    text = "Error: $errorMessage",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
@Composable
fun ProfileContent(profile: com.example.vibesshared.ui.ui.viewmodel.UserProfile, animateIn: Boolean){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedText(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            animateIn = animateIn,
            delayMillis = 900,
            fromTop = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedProfilePicture(
            imageUrl = profile.profilePictureUrl,
            animateIn = animateIn,
            delayMillis = 2222,
            fromTop = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedOutlinedTextField(
            value = profile.userName ?: "N/A",
            onValueChange = { },
            label = "Username",
            isEditMode = false,
            animateIn = animateIn,
            delayMillis = 1620,
            fromTop = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedOutlinedTextField(
            value = profile.firstName ?: "N/A",
            onValueChange = { },
            label = "First Name",
            isEditMode = false,
            animateIn = animateIn,
            delayMillis = 740,
            fromTop = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedOutlinedTextField(
            value = profile.lastName ?: "N/A",
            onValueChange = {  },
            label = "Last Name",
            isEditMode = false,
            animateIn = animateIn,
            delayMillis = 1300,
            fromTop = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedText(
            text = "Last Post: Coming Soon!",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.LimeGreen.color,
            animateIn = animateIn,
            delayMillis = 1463,
            fromTop = false
        )
    }

}

@Composable
private fun AnimatedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean = true,
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-100).dp else 1800.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis), label = "text"
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + slideInVertically(), // Use slideInVertically
        exit = fadeOut() + slideOutVertically() // Use slideOutVertically for consistency
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            modifier = Modifier.graphicsLayer {
                translationY = with(density) { animatedOffsetY.toPx() }
            }
        )
    }
}

@Composable
private fun AnimatedProfilePicture(
    imageUrl: String?,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean = true
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-400).dp else 100.dp  // Keep consistent with other animations
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 1200, delayMillis = delayMillis), label = "ppic"
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + slideInVertically(), // Use slideInVertically
        exit = fadeOut() + slideOutVertically()  // Use slideOutVertically
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    translationY = with(density) { animatedOffsetY.toPx() }
                },
            contentAlignment = Alignment.BottomEnd  // Keep content at BottomEnd
        ) {
            HolographicProfile( // Assuming HolographicProfile is defined elsewhere
                imageUrl = imageUrl.toString(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun AnimatedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isEditMode: Boolean,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean
) {
    val density = LocalDensity.current
    val offsetX = if (fromTop) (-700).dp else 100.dp // Keep consistent with other animations
    val animatedOffsetX by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetX,
        animationSpec = tween(durationMillis = 750, delayMillis = delayMillis), label = "textF"
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + slideInHorizontally(), // Use slideInHorizontally
        exit = fadeOut() + slideOutHorizontally()  // Use slideOutHorizontally
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Gray) }, // Gray color for label
            enabled = isEditMode,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = with(density) { animatedOffsetX.toPx() }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.LimeGreen.color,
                unfocusedTextColor = AppColors.VividBlue.color,
                focusedBorderColor = AppColors.SunsetOrange.color,
                unfocusedBorderColor = AppColors.NeonPink.color,
                cursorColor = AppColors.LimeGreen.color, // Cursor color
            ),
            shape = RoundedCornerShape(8.dp),
            readOnly = true
        )
    }
}