package com.example.vibesshared.ui.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    name: String,
    settingsName: String,
    aboutName: String
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val bitmap = remember {
        mutableStateOf(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.my_profile_icon
            )
        )
    }

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
            painter = painterResource(id = R.drawable.my_profile_icon),
            contentDescription = "Profile Picture",
            modifier = Modifier.clickable {
                launcher.launch("image/*")
            }
        )

        // Profile information
        Text("Username: user123")
        Text("Full Name: John Doe")
        Text("Bio: This is my bio")

        // You can use settingsName and aboutName in navigation or UI if needed
        // For example:
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Go to $settingsName",
            modifier = Modifier.clickable {
                navController.navigate("settings")
            }
        )
        Text(
            text = "Go to $aboutName",
            modifier = Modifier.clickable {
                navController.navigate("about_us")
            }
        )
    }
}