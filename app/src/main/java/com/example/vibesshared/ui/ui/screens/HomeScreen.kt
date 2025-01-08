// HomeScreen.kt
package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.vibesshared.R
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.example.vibesshared.ui.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
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

    // Animated background gradient
    var currentColor by remember { mutableIntStateOf(0) }
    val colors = listOf(
        ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen
    )
    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = colors[currentColor],
        targetValue = colors[(currentColor + 1) % colors.size],
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Update currentColor for the next iteration
    LaunchedEffect(key1 = color) {
        if (color == colors[(currentColor + 1) % colors.size]) {
            currentColor = (currentColor + 1) % colors.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color, Teal200))) // Gradient background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Set text color to white
                ),
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated profile image
            AnimatedVisibility(
                visible = imageUri != null,
                enter = slideInVertically(
                    initialOffsetY = { -40 }
                ) + fadeIn(initialAlpha = 0.3f),
                exit = slideOutVertically() + fadeOut()
            ) {
                imageUri?.let {
                    Image(
                        bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
                            .asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                launcher.launch("image/*")
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (imageUri == null) { // Show placeholder if no image is selected
                Image(
                    painter = painterResource(id = R.drawable.my_profile_icon),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile information with bounce animation
            val animatedModifier = Modifier.animateContentSize()
            Column(modifier = animatedModifier) {
                Text("Username: user123", color = Color.White, fontSize = 18.sp)
                Text("Full Name: John Doe", color = Color.White, fontSize = 18.sp)
                Text("Bio: This is my bio", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            HomeButton(navController)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}