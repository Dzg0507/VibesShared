package com.example.vibesshared.ui.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    name: String,
    settingsName: String,
    aboutName: String
) {
    val context = LocalContext.current
    var isFirstNameEditing by remember { mutableStateOf(false) }
    var isLastNameEditing by remember { mutableStateOf(false) }
    var isEmailEditing by remember { mutableStateOf(false) }
    var isBioEditing by remember { mutableStateOf(false) }
    var isProfilePictureHovered by remember { mutableStateOf(false) }

    var editedFirstName by remember { mutableStateOf("") }
    var editedLastName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedBio by remember { mutableStateOf("") }
    var profilePictureUri by remember {
        mutableStateOf<Uri?>(loadProfilePictureUriFromSharedPrefs(context))
    }

    val coroutineScope = rememberCoroutineScope()

    val userProfileState = profileViewModel.userProfile.collectAsState(initial = null)

    val profilePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profilePictureUri = uri // Assign the URI to the correct state variable
        uri?.let {
            saveProfilePictureUriToSharedPrefs(context, it)
        }
    }

    userProfileState.value?.let { userProfile ->
        editedFirstName = userProfile.firstName ?: ""
        editedLastName = userProfile.lastName ?: ""
        editedEmail = userProfile.email ?: ""
        editedBio = userProfile.bio ?: ""
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val profilePictureScale by animateFloatAsState(
        targetValue = if (isProfilePictureHovered) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val firstNameScale by animateFloatAsState(
        targetValue = if (isFirstNameEditing) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val lastNameScale by animateFloatAsState(
        targetValue = if (isLastNameEditing) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val emailScale by animateFloatAsState(
        targetValue = if (isEmailEditing) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val bioScale by animateFloatAsState(
        targetValue = if (isBioEditing) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(name) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                AsyncImage(
                    model = profilePictureUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(150.dp)
                        .scale(profilePictureScale)
                        .clickable {
                            isProfilePictureHovered = !isProfilePictureHovered
                            profilePictureLauncher.launch("image/*")
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // First Name
                OutlinedTextField(
                    value = editedFirstName,
                    onValueChange = { editedFirstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleY = firstNameScale
                        },
                    trailingIcon = {
                        IconButton(onClick = { isFirstNameEditing = !isFirstNameEditing }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit First Name"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Last Name
                OutlinedTextField(
                    value = editedLastName,
                    onValueChange = { editedLastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleY = lastNameScale
                        },
                    trailingIcon = {
                        IconButton(onClick = { isLastNameEditing = !isLastNameEditing }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Last Name"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Email
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleY = emailScale
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    trailingIcon = {
                        IconButton(onClick = { isEmailEditing = !isEmailEditing }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Email"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Bio
                OutlinedTextField(
                    value = editedBio,
                    onValueChange = { editedBio = it },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleY = bioScale
                        },
                    trailingIcon = {
                        IconButton(onClick = { isBioEditing = !isBioEditing }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Bio"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            profileViewModel.saveUserProfile(
                                firstName = editedFirstName,
                                lastName = editedLastName,
                                email = editedEmail,
                                bio = editedBio,
                                profilePictureUri = profilePictureUri.toString() // Pass the URI here
                            )
                            saveBioToSharedPrefs(context, editedBio)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

private fun saveProfilePictureUriToSharedPrefs(context: Context, uri: Uri) {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("profile_picture_uri", uri.toString()).apply()
}

private fun loadProfilePictureUriFromSharedPrefs(context: Context): Uri? {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val uriString = prefs.getString("profile_picture_uri", null)
    return uriString?.let { Uri.parse(it) }
}

private fun saveBioToSharedPrefs(context: Context, bio: String) {
    val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("bio", bio).apply()
}