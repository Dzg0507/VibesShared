package com.example.vibesshared.ui.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.Teal200
import com.example.vibesshared.ui.ui.theme.VividBlue
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
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
            .background(Brush.verticalGradient(listOf(color, Teal200)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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

            // Show placeholder if no image is selected
            if (imageUri == null) {
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

            val userProfileState = profileViewModel.userProfile.collectAsState()
            val userProfile = userProfileState.value

            userProfile?.let {
                Text("Username: ${it.firstName}", color = Color.White, fontSize = 18.sp)
                Text("Full Name: ${it.firstName} ${it.lastName}", color = Color.White, fontSize = 18.sp)
                Text("Bio: ${it.bio}", color = Color.White, fontSize = 18.sp)

                // Conditionally display the profile picture
                if (it.profilePictureUri != null) {
                    AsyncImage(
                        model = it.profilePictureUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop)



                } else
                    // This part will now only show if there's NO profile picture URI from Firebase
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
            }

            // Profile information with bounce animation
            val animatedModifier = Modifier.animateContentSize()
            Column(modifier = animatedModifier) {
                // ... your existing code ...
            }
        }
    }
