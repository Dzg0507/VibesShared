package com.example.vibesshared.ui.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vibesshared.ui.ui.components.UserProfile
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.viewmodel.ProfileState
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userId: String,
    isCurrentUser: Boolean,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0B0B),
                        Color(0xFF0B1A2F)
                    )
                )
            )
    ) {
        when (profileState) {
            is ProfileState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00FF41)
                )
            }

            is ProfileState.Error -> {
                Text(
                    text = (profileState as ProfileState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ProfileState.Success -> {
                val profile = (profileState as ProfileState.Success).profile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileCard(
                        userProfile = profile,
                        isCurrentUser = isCurrentUser,
                        onEdit = { isEditing = true },
                        selectedImageUri = selectedImageUri,
                        launcher = launcher
                    )

                    if (isCurrentUser && isEditing) {
                        EditProfileSection(
                            editedFirstName = profile.firstName,
                            editedLastName = profile.lastName,
                            editedBio = profile.bio,
                            editedExperience = profile.experience ?: "",
                            editedFavLanguage = profile.favoriteLanguage ?: "",
                            editedSpecialty = profile.specialty ?: "",
                            editedCurrentProject = profile.currentProject ?: "",
                            editedLearning = profile.learning ?: "",
                            onSave = {
                                viewModel.updateProfile(
                                    firstName = profile.firstName,
                                    lastName = profile.lastName,
                                    bio = profile.bio,
                                    profilePicture = selectedImageUri,
                                    experience = profile.experience ?: "",
                                    favoriteLanguage = profile.favoriteLanguage ?: "",
                                    specialty = profile.specialty ?: "",
                                    currentProject = profile.currentProject ?: "",
                                    learning = profile.learning ?: "",
                                    onSuccess = { isEditing = false },
                                    onError = { /* Handle error */ }
                                )
                            },
                            onCancel = { isEditing = false }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileCard(
    userProfile: UserProfile?,
    isCurrentUser: Boolean,
    onEdit: () -> Unit,
    selectedImageUri: Uri?,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { if (isCurrentUser) onEdit() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = userProfile?.firstName ?: "First Name", style = MaterialTheme.typography.headlineSmall)
            Text(text = userProfile?.lastName ?: "Last Name", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userProfile?.bio ?: "Bio", textAlign = TextAlign.Center)

            if (isCurrentUser) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    launcher.launch(intent)
                }) {
                    Icon(Icons.Default.Build, contentDescription = "Edit Profile Picture")
                }
            }
        }
    }
}

@Composable
fun EditProfileSection(
    editedFirstName: String,
    editedLastName: String,
    editedBio: String,
    editedExperience: String,
    editedFavLanguage: String,
    editedSpecialty: String,
    editedCurrentProject: String,
    editedLearning: String,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var firstName by remember { mutableStateOf(editedFirstName) }
    var lastName by remember { mutableStateOf(editedLastName) }
    var bio by remember { mutableStateOf(editedBio) }
    var experience by remember { mutableStateOf(editedExperience) }
    var favLanguage by remember { mutableStateOf(editedFavLanguage) }
    var specialty by remember { mutableStateOf(editedSpecialty) }
    var currentProject by remember { mutableStateOf(editedCurrentProject) }
    var learning by remember { mutableStateOf(editedLearning) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") }
        )
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") }
        )
        TextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") }
        )
        TextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience") }
        )
        TextField(
            value = favLanguage,
            onValueChange = { favLanguage = it },
            label = { Text("Favorite Language") }
        )
        TextField(
            value = specialty,
            onValueChange = { specialty = it },
            label = { Text("Specialty") }
        )
        TextField(
            value = currentProject,
            onValueChange = { currentProject = it },
            label = { Text("Current Project") }
        )
        TextField(
            value = learning,
            onValueChange = { learning = it },
            label = { Text("Learning") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                onSave()
            }) {
                Text("Save")
            }
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedTextColor = color,
            focusedTextColor = color,
            cursorColor = color
        ),
        textStyle = TextStyle(
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = singleLine,
        maxLines = maxLines
    )
}

@Composable
private fun InfoField(
    isEditing: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    displayValue: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        if (isEditing) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = color,
                    focusedTextColor = color,
                    cursorColor = color
                ),
                textStyle = TextStyle(fontSize = 16.sp),
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                }
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayValue,
                    color = color,
                    fontSize = 16.sp
                )
            }
        }
    }
}


