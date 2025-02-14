package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HolographicProfile
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.theme.AppColors
import com.example.vibesshared.ui.ui.viewmodel.MyProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(navController: NavController,
    viewModel: MyProfileViewModel = hiltViewModel()

) {
    Button(onClick = {
        navController.navigate(Screen.TriviaGame.route) // Navigate to the trivia game
    }) {
        Text("Play Trivia")
    }
    val userId = remember { Firebase.auth.currentUser?.uid }
    var userName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrlFromFirebase by remember { mutableStateOf("") }

    var isEditMode by remember { mutableStateOf(false) }
    val loadingStatus by viewModel.loadingStatus.collectAsState(initial = null)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadUserProfile(it) }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let { user ->
            userName = user.userName!!
            firstName = user.firstName!!
            lastName = user.lastName!!
            profilePictureUrlFromFirebase = user.profilePictureUrl ?: ""
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profilePictureUri = uri
    }

    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedText(
            text = "My Profile",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            animateIn = animateIn,
            delayMillis = 900,
            fromTop = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedProfilePicture(
            imageUrl = profilePictureUri ?: profilePictureUrlFromFirebase,
            isEditMode = isEditMode,
            animateIn = animateIn,
            delayMillis = 2222,
            onEditClick = { imagePickerLauncher.launch("image/*") },
            fromTop = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedOutlinedTextField(
            value = userName,
            onValueChange = { if (isEditMode) userName = it },
            label = "Username",
            isEditMode = isEditMode,
            animateIn = animateIn,
            delayMillis = 1620,
            fromTop = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedOutlinedTextField(
            value = firstName,
            onValueChange = { if (isEditMode) firstName = it },
            label = "First Name",
            isEditMode = isEditMode,
            animateIn = animateIn,
            delayMillis = 740,
            fromTop = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedOutlinedTextField(
            value = lastName,
            onValueChange = { if (isEditMode) lastName = it },
            label = "Last Name",
            isEditMode = isEditMode,
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

        AnimatedText(
            text = "Total Vibes Shared: 0",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.ClectricPurple.color,
            animateIn = animateIn,
            delayMillis = 850,
            fromTop = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedEditModeRow(
            isEditMode = isEditMode,
            onCheckedChange = { isEditMode = it },
            animateIn = animateIn,
            delayMillis = 800,
            fromTop = false
        )

        AnimatedSaveButton(
            isEditMode = isEditMode,
            viewModel = viewModel,
            userId = userId,
            onSaveComplete = { isEditMode = false },
            userName = userName,
            firstName = firstName,
            lastName = lastName,
            profilePictureUri = profilePictureUri
        )

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Red)
        }

        if (loadingStatus == MyProfileViewModel.LoadingStatus.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { }
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.SunsetOrange.color)
            }
        }
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
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
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
    imageUrl: Any?,
    isEditMode: Boolean,
    animateIn: Boolean,
    delayMillis: Int,
    onEditClick: () -> Unit,
    fromTop: Boolean = true
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-400).dp else 100.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 1200, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    translationY = with(density) { animatedOffsetY.toPx() }
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            HolographicProfile(
                imageUrl = imageUrl.toString(),
                modifier = Modifier.fillMaxSize()
            )

            if (isEditMode) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White,
                    )
                }
            }
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
    val offsetX = if (fromTop) (-700).dp else 100.dp
    val animatedOffsetX by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetX,
        animationSpec = tween(durationMillis = 750, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Gray) },
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
                cursorColor = AppColors.LimeGreen.color,
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun AnimatedEditModeRow(
    isEditMode: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    animateIn: Boolean,
    delayMillis: Int,
    fromTop: Boolean
) {
    val density = LocalDensity.current
    val offsetY = if (fromTop) (-400).dp else 100.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else offsetY,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMillis)
    )

    AnimatedVisibility(
        visible = animateIn,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = with(density) { animatedOffsetY.toPx() }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Edit Mode", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isEditMode,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun AnimatedSaveButton(
    isEditMode: Boolean,
    viewModel: MyProfileViewModel,
    userId: String?,
    onSaveComplete: () -> Unit,
    userName: String,
    firstName: String,
    lastName: String,
    profilePictureUri: Uri?
) {
    val offsetY by animateDpAsState(
        targetValue = if (isEditMode) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 300)
    )

    if (isEditMode) {
        Button(
            onClick = {
                userId?.let {
                    viewModel.saveUserProfile(
                        userId = it,
                        userName = userName,
                        firstName = firstName,
                        lastName = lastName,
                        profilePictureUri = profilePictureUri
                    )
                    onSaveComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .shadow(8.dp, RoundedCornerShape(8.dp))
        ) {
            Text(text = "Save Profile")
        }
    }
}