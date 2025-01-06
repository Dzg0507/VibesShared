package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.components.HomeButton
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.res.painterResource
import com.example.vibesshared.R

@Composable
fun ProfileScreen(navController: NavController, name: String) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf(BitmapFactory.decodeResource(context.resources, R.drawable.my_profile_icon)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.my_profile_icon), // Use a placeholder image initially
            contentDescription = "Profile Picture",
            modifier = Modifier.clickable {
                launcher.launch("image/*")
            }
        )

        // Profile information
        Text("Username: user123")
        Text("Full Name: John Doe")
        Text("Bio: This is my bio")

        Spacer(modifier = Modifier.weight(1f)) // Push the HomeButton to the bottom

        HomeButton(navController)
        Spacer(modifier = Modifier.height(16.dp))
    }
}