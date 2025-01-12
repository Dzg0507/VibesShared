package com.example.vibesshared.ui.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.components.UserProfile
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun generateInitialsBitmap(firstName: String?, lastName: String?): ImageBitmap {
    val initials = when {
        firstName != null && lastName != null -> "${firstName.firstOrNull()} ${lastName.firstOrNull()}"
        firstName != null -> firstName.firstOrNull().toString()
        lastName != null -> lastName.firstOrNull().toString()
        else -> ""
    }

    val size = 500
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 200f
    }

    val x = size / 2f
    val y = size / 2f - (paint.descent() + paint.ascent()) / 2f

    canvas.drawColor(android.graphics.Color.DKGRAY)
    canvas.drawText(initials, x, y, paint)

    return bitmap.asImageBitmap()
}

@Composable
fun AnimatedSpaceBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current

    // Twinkling Stars
    val numStars = 50
    val stars = remember {
        List(numStars) {
            val size = with(density) { Random.nextInt(2, 6).dp.toPx() }
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = size,
                alpha = Animatable(Random.nextFloat())
            )
        }
    }

    // Comet
    val cometAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing), // Slower comet
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    LaunchedEffect(Unit) {
        stars.forEach { star ->
            launch {
                while (true) {
                    star.alpha.animateTo(
                        targetValue = if (star.alpha.value == 0f) 1f else 0f,
                        animationSpec = tween(durationMillis = Random.nextInt(500, 2000))
                    )
                    delay(Random.nextLong(500, 2000))
                }
            }
        }
    }

    // Gradient Colors
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF18122B),
        targetValue = Color(0xFF393646),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF393646),
        targetValue = Color(0xFF6D5D6E),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF6D5D6E),
        targetValue = Color(0xFF18122B),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val brush = Brush.verticalGradient(
        colors = listOf(color1, color2, color3)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Stars
            stars.forEach { star ->
                drawCircle(
                    color = Color.White,
                    radius = star.size / 2,
                    center = Offset(star.x * size.width, star.y * size.height),
                    alpha = star.alpha.value
                )
            }

            // Draw Comet
            drawComet(cometAnimation.value)
        }
    }
}

private fun DrawScope.drawComet(animationProgress: Float) {
    val screenWidth = size.width
    val screenHeight = size.height

    // Comet Path
    val startX = -screenWidth * 0.2f
    val endX = screenWidth * 1.2f
    val startY = screenHeight * 0.1f
    val endY = screenHeight * 0.9f

    val cometX = startX + (endX - startX) * animationProgress
    val cometY = startY + (endY - startY) * animationProgress

    // Comet Head Size: Make it smaller
    val headSize = 3.dp.toPx()

    // Comet Tail: Elongated and more particles
    val tailLength = screenWidth * 0.2f  // Increased tail length
    val numTailParticles = 60 // More particles
    val cometAngle = Math.atan2((endY - startY).toDouble(), (endX - startX).toDouble()).toFloat()

    for (i in 0 until numTailParticles) {
        val particleProgress = i.toFloat() / numTailParticles

        // Calculate particle position with a curve
        val curveFactor = particleProgress * particleProgress // Adjust curve with exponent
        val particleX = cometX - tailLength * cos(cometAngle) * curveFactor
        val particleY = cometY - tailLength * sin(cometAngle) * curveFactor

        // Particle size and alpha
        val particleSize = headSize * (1 - particleProgress) * 0.4f  // Taper more gradually
        val particleAlpha = (1f - particleProgress) * 0.8f // Adjust alpha for visibility

        drawCircle(
            color = Color.White.copy(alpha = particleAlpha),
            radius = particleSize,
            center = Offset(particleX, particleY)
        )
    }

    // Draw Comet Head
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = headSize,
        center = Offset(cometX, cometY)
    )
}

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Animatable<Float, AnimationVector1D>
)

@Composable
fun OrbitingPlanets(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val planetSize1 = with(density) { 15.dp.toPx() }
    val planetSize2 = with(density) { 8.dp.toPx() }
    val planetSize3 = with(density) { 20.dp.toPx() }
    val orbitRadius1 = with(density) { 70.dp.toPx() }
    val orbitRadius2 = with(density) { 100.dp.toPx() }
    val orbitRadius3 = with(density) { 140.dp.toPx() }

    val infiniteTransition = rememberInfiniteTransition()

    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val angle3 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)

        val planet1Offset = center + Offset(
            x = orbitRadius1 * cos(Math.toRadians(angle1.toDouble())).toFloat(),
            y = orbitRadius1 * sin(Math.toRadians(angle1.toDouble())).toFloat()
        )
        drawCircle(color = Color(0xFF8572CB), radius = planetSize1 / 2, center = planet1Offset)

        val planet2Offset = center + Offset(
            x = orbitRadius2 * cos(Math.toRadians(angle2.toDouble())).toFloat(),
            y = orbitRadius2 * sin(Math.toRadians(angle2.toDouble())).toFloat()
        )
        drawCircle(color = Color(0xFFDC8686), radius = planetSize2 / 2, center = planet2Offset)

        val planet3Offset = center + Offset(
            x = orbitRadius3 * cos(Math.toRadians(angle3.toDouble())).toFloat(),
            y = orbitRadius3 * sin(Math.toRadians(angle3.toDouble())).toFloat()
        )
        drawCircle(color = Color(0xFFF4EAE0), radius = planetSize3 / 2, center = planet3Offset)
    }
}

@Composable
fun ProfilePicture(firstName: String?, lastName: String?, profilePictureUri: Uri?, modifier: Modifier = Modifier) {
    if (profilePictureUri != null) {
        AsyncImage(
            model = profilePictureUri,
            contentDescription = "Profile Picture",
            modifier = modifier
        )
    } else {
        val initialsBitmap = generateInitialsBitmap(firstName, lastName)
        Image(
            bitmap = initialsBitmap,
            contentDescription = "Profile Picture Placeholder",
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(context = LocalContext.current)
    ),
    name: String = "Profile",
) {
    var isFirstNameEditing by remember { mutableStateOf(false) }
    var isLastNameEditing by remember { mutableStateOf(false) }
    var isEmailEditing by remember { mutableStateOf(false) }
    var isBioEditing by remember { mutableStateOf(false) }

    var editedFirstName by remember { mutableStateOf("") }
    var editedLastName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedBio by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val userProfileState = profileViewModel.userProfile.collectAsState(initial = null)
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var focusedField by remember { mutableStateOf<TextFieldType?>(null) }

    val profilePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profilePictureUri = uri
    }

    LaunchedEffect(key1 = userProfileState.value) {
        userProfile = userProfileState.value
        userProfile?.let {
            editedFirstName = it.firstName ?: ""
            editedLastName = it.lastName ?: ""
            editedEmail = it.email ?: ""
            editedBio = it.bio ?: ""
            profilePictureUri = it.profilePictureUri?.let { Uri.parse(it) }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val firstNameBackgroundColor by animateColorAsState(
        targetValue = if (focusedField == TextFieldType.FIRST_NAME) Color.LightGray else Color.Transparent, label = ""
    )
    val lastNameBackgroundColor by animateColorAsState(
        targetValue = if (focusedField == TextFieldType.LAST_NAME) Color.LightGray else Color.Transparent, label = ""
    )
    val emailBackgroundColor by animateColorAsState(
        targetValue = if (focusedField == TextFieldType.EMAIL) Color.LightGray else Color.Transparent, label = ""
    )
    val bioBackgroundColor by animateColorAsState(
        targetValue = if (focusedField == TextFieldType.BIO) Color.LightGray else Color.Transparent, label = ""
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF4EAE0)
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedSpaceBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                ) {
                    OrbitingPlanets(
                        modifier = Modifier.size(600.dp)
                    )
                    ProfilePicture(
                        firstName = editedFirstName,
                        lastName = editedLastName,
                        profilePictureUri = profilePictureUri,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .align(Alignment.Center)
                            .clickable { profilePictureLauncher.launch("image/*") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editedFirstName,
                    onValueChange = {
                        editedFirstName = it
                        isFirstNameEditing = it.isNotBlank()
                    },
                    label = {
                        Text(
                            "First Name",
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFF4EAE0)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(firstNameBackgroundColor, RoundedCornerShape(4.dp)),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF4EAE0)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color(0xFFF4EAE0)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedLastName,
                    onValueChange = {
                        editedLastName = it
                        isLastNameEditing = it.isNotBlank()
                    },
                    label = {
                        Text(
                            "Last Name",fontWeight = FontWeight.Normal,
                            color = Color(0xFFF4EAE0)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lastNameBackgroundColor, RoundedCornerShape(4.dp)),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF4EAE0)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color(0xFFF4EAE0)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = {
                        editedEmail = it
                        isEmailEditing = it.isNotBlank()
                    },
                    label = {
                        Text(
                            "Email",
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFF4EAE0)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(emailBackgroundColor, RoundedCornerShape(4.dp)),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF4EAE0)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color(0xFFF4EAE0)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedBio,
                    onValueChange = {
                        editedBio = it
                        isBioEditing = it.isNotBlank()
                    },
                    label = {
                        Text(
                            "Bio",
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFF4EAE0)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bioBackgroundColor, RoundedCornerShape(4.dp)),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF4EAE0)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color(0xFFF4EAE0)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable(enabled = false) {}
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                profileViewModel.saveUserProfile(
                                    firstName = editedFirstName,
                                    lastName = editedLastName,
                                    email = editedEmail,
                                    bio = editedBio,
                                    profilePictureUri = profilePictureUri?.toString()
                                )
                                showSaveConfirmation = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8572CB)
                        )
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFF4EAE0)
                        )
                    }
                }

                if (showSaveConfirmation) {
                    Text(
                        "Profile Saved!",
                        color = Color.Green,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    LaunchedEffect(key1 = showSaveConfirmation) {
        if (showSaveConfirmation) {
            delay(2000)
            showSaveConfirmation = false
        }
    }
}

private enum class TextFieldType {
    FIRST_NAME,
    LAST_NAME,
    EMAIL,
    BIO
}




