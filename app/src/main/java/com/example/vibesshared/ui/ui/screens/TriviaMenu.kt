package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.vibesshared.ui.ui.components.LightningBolt
import com.example.vibesshared.ui.ui.components.LightningBoltAnimation
import com.example.vibesshared.ui.ui.components.createLightningPath
import com.example.vibesshared.ui.ui.viewmodel.TriviaGameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


@Composable
fun TriviaMenuScreen(viewModel: TriviaGameViewModel) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowPulse = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val bolts = remember { mutableStateListOf<LightningBolt>() }
    val coroutineScope = rememberCoroutineScope()

    // Game settings
    var difficultyIndex by remember { mutableIntStateOf(1) }
    var categoryIndex by remember { mutableIntStateOf(0) }
    var countIndex by remember { mutableIntStateOf(0) }

    val difficulties = listOf("easy", "medium", "hard")
    val categories = listOf("any", "science", "history")
    val counts = listOf("5", "10", "20")

    // Create a scroll state
    val scrollState = rememberScrollState()

    // Create background stars
    val stars = remember {
        List(150) {
            Triple(
                Random.nextFloat() * screenSize.width.coerceAtLeast(1),
                Random.nextFloat() * screenSize.height.coerceAtLeast(1),
                Random.nextFloat() * 0.8f + 0.2f
            )
        }
    }

    // Background star twinkle effect
    val starTwinkle = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Create occasional auto-generated lightning
    LaunchedEffect(Unit) {
        scheduleAutomaticLightning(screenSize, bolts, coroutineScope)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050014), // Darker deep space color
                        Color(0xFF0D0026),
                        Color(0xFF26004D)
                    )
                )
            )
            .drawBehind {
                // Draw stars
                stars.forEach { (x, y, brightness) ->
                    val starSize = brightness * 4f
                    val glow = (brightness * 0.5f + starTwinkle.value * 0.5f) * 255

                    drawCircle(
                        color = Color.White.copy(alpha = glow / 255f),
                        radius = starSize,
                        center = Offset(x, y)
                    )

                    // Occasional lens flare for brighter stars
                    if (brightness > 0.8f) {
                        drawLine(
                            color = Color.White.copy(alpha = (glow / 255f) * 0.3f),
                            start = Offset(x - starSize * 2, y),
                            end = Offset(x + starSize * 2, y),
                            strokeWidth = starSize * 0.5f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = (glow / 255f) * 0.3f),
                            start = Offset(x, y - starSize * 2),
                            end = Offset(x, y + starSize * 2),
                            strokeWidth = starSize * 0.5f
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val startEdge = Random.nextInt(4)
                    val start = when (startEdge) {
                        0 -> Offset(0f, Random.nextFloat() * screenSize.height)
                        1 -> Offset(screenSize.width.toFloat(), Random.nextFloat() * screenSize.height)
                        2 -> Offset(Random.nextFloat() * screenSize.width, 0f)
                        else -> Offset(Random.nextFloat() * screenSize.width, screenSize.height.toFloat())
                    }

                    val color = when (Random.nextInt(3)) {
                        0 -> Color(0xFFFF1744)
                        1 -> Color(0xFF00E676)
                        else -> Color(0xFF2979FF)
                    }

                    val path = createLightningPath(start, offset)
                    bolts.add(LightningBolt(Random.nextInt(), start, offset, color, path))

                    // Add secondary smaller lightning after a short delay
                    coroutineScope.launch {
                        delay(100)
                        val secondaryStart = Offset(
                            start.x + Random.nextFloat() * 50 - 25,
                            start.y + Random.nextFloat() * 50 - 25
                        )
                        val secondaryPath = createLightningPath(secondaryStart, offset)
                        bolts.add(LightningBolt(Random.nextInt(), secondaryStart, offset, color.copy(alpha = 0.7f), secondaryPath))
                    }
                }
            }
            .onSizeChanged { screenSize = it }
    ) {

        // Draw lightning bolts
        bolts.forEach { bolt ->
            key(bolt.id) {
                LightningBoltAnimation(bolt) { bolts.remove(bolt) }
            }
        }

        // Nebula effect - subtle colorful clouds
        NebulaEffect()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .border(
                    width = (1 + (glowPulse.value * 2)).dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00FFFF).copy(alpha = glowPulse.value * 0.7f),
                            Color(0xFFFF00FF).copy(alpha = glowPulse.value * 0.5f),
                            Color(0xFF00FFFF).copy(alpha = glowPulse.value * 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState), // Add vertical scrolling
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Add some top padding to ensure good spacing when scrolling
            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced game title
            EnhancedGameTitle()

            Spacer(modifier = Modifier.height(32.dp))

            CarouselSelector(
                label = "DIFFICULTY",
                options = difficulties,
                currentIndex = difficultyIndex,
                onIndexChanged = { difficultyIndex = it },
                gradientColors = listOf(Color(0xFFFF00FF), Color(0xFFCC00CC))
            )

            Spacer(modifier = Modifier.height(32.dp))

            CarouselSelector(
                label = "CATEGORY",
                options = categories,
                currentIndex = categoryIndex,
                onIndexChanged = { categoryIndex = it },
                gradientColors = listOf(Color(0xFF00FFFF), Color(0xFF00CCCC))
            )

            Spacer(modifier = Modifier.height(32.dp))

            CarouselSelector(
                label = "QUESTIONS",
                options = counts,
                currentIndex = countIndex,
                onIndexChanged = { countIndex = it },
                gradientColors = listOf(Color(0xFFFFE600), Color(0xFFFFB300))
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Enhanced start button with animation effects
            EnhancedStartButton(
                onClick = {
                    viewModel.startGame(
                        difficulties[difficultyIndex],
                        categories[categoryIndex],
                        counts[countIndex].toInt()
                    )
                }
            )

            // Add bottom padding to ensure the button is visible when scrolled
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
private fun scheduleAutomaticLightning(
    screenSize: IntSize,
    bolts: MutableList<LightningBolt>,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        while(true) {
            // Decreased delay - random delay between 1-4 seconds
            delay(Random.nextLong(1000, 4000))

            // 80% chance of lightning (increased from previous)
            if (Random.nextFloat() > 0.2f) {
                val startEdge = Random.nextInt(4)
                val start = when (startEdge) {
                    0 -> Offset(0f, Random.nextFloat() * screenSize.height)
                    1 -> Offset(screenSize.width.toFloat(), Random.nextFloat() * screenSize.height)
                    2 -> Offset(Random.nextFloat() * screenSize.width, 0f)
                    else -> Offset(Random.nextFloat() * screenSize.width, screenSize.height.toFloat())
                }

                // Slightly guide the lightning toward the center area for better visibility
                val centerOffsetX = Random.nextFloat() * screenSize.width * 0.6f + screenSize.width * 0.2f
                val centerOffsetY = Random.nextFloat() * screenSize.height * 0.6f + screenSize.height * 0.2f

                val end = Offset(
                    centerOffsetX,
                    centerOffsetY
                )

                val color = when (Random.nextInt(5)) {
                    0 -> Color(0xFFFF1744) // Red
                    1 -> Color(0xFF00E676) // Green
                    2 -> Color(0xFF2979FF) // Blue
                    3 -> Color(0xFFFFEB3B) // Yellow
                    else -> Color(0xFFE040FB) // Purple
                }

                val path = createLightningPath(start, end)
                bolts.add(LightningBolt(Random.nextInt(), start, end, color, path))

                // 50% chance of a second bolt following shortly after
                if (Random.nextFloat() > 0.5f) {
                    delay(100)
                    val secondaryStart = Offset(
                        start.x + Random.nextFloat() * 50 - 25,
                        start.y + Random.nextFloat() * 50 - 25
                    )
                    val secondaryPath = createLightningPath(secondaryStart, end)
                    bolts.add(LightningBolt(Random.nextInt(), secondaryStart, end, color.copy(alpha = 0.8f), secondaryPath))
                }

                // 25% chance of a third bolt for a proper storm effect
                if (Random.nextFloat() > 0.75f) {
                    delay(200)
                    val tertiaryStart = when (startEdge) {
                        0 -> Offset(0f, Random.nextFloat() * screenSize.height)
                        1 -> Offset(screenSize.width.toFloat(), Random.nextFloat() * screenSize.height)
                        2 -> Offset(Random.nextFloat() * screenSize.width, 0f)
                        else -> Offset(Random.nextFloat() * screenSize.width, screenSize.height.toFloat())
                    }
                    val tertiaryPath = createLightningPath(tertiaryStart, Offset(
                        centerOffsetX + Random.nextFloat() * 100f - 50f,
                        centerOffsetY + Random.nextFloat() * 100f - 50f
                    ))

                    val tertiaryColor = when (Random.nextInt(5)) {
                        0 -> Color(0xFFFF1744) // Red
                        1 -> Color(0xFF00E676) // Green
                        2 -> Color(0xFF2979FF) // Blue
                        3 -> Color(0xFFFFEB3B) // Yellow
                        else -> Color(0xFFE040FB) // Purple
                    }

                    bolts.add(LightningBolt(Random.nextInt(), tertiaryStart, end, tertiaryColor, tertiaryPath))
                }
            }
        }
    }
}

@Composable
fun EnhancedStartButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()

    // Main button glow and pulse
    val buttonGlow = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Halo effect animation
    val haloScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val haloAlpha = infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Energy flow animation around button
    val energyOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer halo effect
        Box(
            modifier = Modifier
                .size(280.dp, 100.dp)
                .scale(haloScale.value)
                .alpha(haloAlpha.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF00FF),
                            Color(0xFF00FFFF).copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(48.dp)
                )
        )

        // Energy particles flowing around the button
        for (i in 0 until 8) {
            val offsetRatio = (energyOffset.value + i / 8f) % 1f
            val angle = offsetRatio * 2 * Math.PI
            val radius = 80f

            val x = (kotlin.math.cos(angle) * radius).toFloat()
            val y = (kotlin.math.sin(angle) * radius).toFloat()

            val particleSize = 5.dp + (Random.nextFloat() * 5).dp
            val particleColor = if (i % 2 == 0) Color(0xFFFF00FF) else Color(0xFF00FFFF)

            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(particleSize)
                    .alpha(0.7f)
                    .background(
                        color = particleColor,
                        shape = CircleShape
                    )
                    .blur(2.dp)
            )
        }

        // Main button
        Button(
            onClick = onClick,
            modifier = Modifier
                .width(200.dp)
                .height(70.dp)
                .scale(buttonGlow.value)
                .shadow(16.dp, RoundedCornerShape(35.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF00FF),
                            Color(0xFF9000FF),
                            Color(0xFF00FFFF)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(200f, 70f)
                    ),
                    shape = RoundedCornerShape(35.dp)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Button icon
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(8.dp, CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Button text
                Text(
                    text = "IGNITE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.shadow(4.dp, CircleShape)
                )
            }
        }
    }
}

@Composable
fun NebulaEffect() {
    val infiniteTransition = rememberInfiniteTransition()

    val nebulaAlpha = infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val nebulaRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(120000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(nebulaAlpha.value)
            .rotate(nebulaRotation.value)
    ) {
        // Top left nebula
        Box(
            modifier = Modifier
                .offset((-150).dp, (-150).dp)
                .size(500.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9C00FF),
                            Color(0xFF9C00FF).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )

        // Bottom right nebula
        Box(
            modifier = Modifier
                .offset(250.dp, 350.dp)
                .size(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FFCC),
                            Color(0xFF00FFCC).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )

        // Center nebula
        Box(
            modifier = Modifier
                .offset(100.dp, 100.dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF5500).copy(alpha = 0.7f),
                            Color(0xFFFF5500).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .blur(40.dp)
        )
    }
}

@Composable
fun EnhancedGameTitle() {
    // For 3D-like perspective and animation effects
    val infiniteTransition = rememberInfiniteTransition()

    // Main scale pulsing animation
    val titleScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Rotation for 3D effect - FIXED: changed declaration from var to val and removed explicit type
    val rotationY = infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Color animation for the text
    val titleColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF00FFFF),  // Cyan
        targetValue = Color(0xFFFF00FF),   // Magenta
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glow effect animation
    val glowRadius = infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Particle effect animations
    val particleRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .size(350.dp, 180.dp)
                .graphicsLayer {
                    shadowElevation = glowRadius.value
                    shape = RoundedCornerShape(24.dp)
                    clip = true
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            titleColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(0.5f, 0.5f),
                        radius = 1.5f
                    )
                )
        )

        // Rotating particle effects
        Box(
            modifier = Modifier
                .size(320.dp)
                .rotate(particleRotation.value)
        ) {
            // Create orbital particles
            repeat(12) { index ->
                val angle = (index / 12f) * 360f
                val radius = 150f
                val x = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius
                val y = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius

                // Individual particle animation
                val particlePulse = remember {
                    Animatable(0f)
                }.apply {
                    LaunchedEffect(key1 = Unit) {
                        animateTo(
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 1500 + (index * 100),
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    }
                }

                val particleColor = when (index % 4) {
                    0 -> Color(0xFF00FFFF)  // Cyan
                    1 -> Color(0xFFFF00FF)  // Magenta
                    2 -> Color(0xFFFFFF00)  // Yellow
                    else -> Color(0xFF00FF00)  // Green
                }

                Box(
                    modifier = Modifier
                        .offset(x = x.dp, y = y.dp)
                        .size((5 + (index % 8)).dp * particlePulse.value)
                        .alpha(particlePulse.value * 0.7f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    particleColor,
                                    particleColor.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }

        // Main title with 3D effect
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(titleScale.value)
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    cameraDistance = 12f * density
                }
        ) {
            // MULTIVERSE text
            Text(
                text = "MULTIVERSE",
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            titleColor,
                            Color.White,
                            titleColor
                        )
                    ),
                    shadow = Shadow(
                        color = titleColor.copy(alpha = 0.7f),
                        offset = Offset(0f, 0f),
                        blurRadius = glowRadius.value
                    )
                )
            )

            // TRIVIA text
            Text(
                text = "TRIVIA",
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),  // Gold
                            Color.White,
                            Color(0xFFFFD700)   // Gold
                        )
                    ),
                    shadow = Shadow(
                        color = Color(0xFFFFD700).copy(alpha = 0.8f),
                        offset = Offset(0f, 0f),
                        blurRadius = glowRadius.value
                    )
                )
            )
        }

        // Subtle star particles
        repeat(20) { index ->
            val delay = index * 100
            val particleScale = remember { Animatable(0f) }
            val particleAlpha = remember { Animatable(0f) }
            val particleX = remember { Animatable((Random.nextFloat() * 300f) - 150f) }
            val particleY = remember { Animatable((Random.nextFloat() * 200f) - 100f) }

            LaunchedEffect(Unit) {
                delay(delay.toLong())
                launch {
                    while(true) {
                        particleScale.animateTo(
                            targetValue = Random.nextFloat() * 0.5f + 0.5f,
                            animationSpec = tween(1000 + (Random.nextInt(1000)), easing = LinearEasing)
                        )
                        particleScale.animateTo(
                            targetValue = Random.nextFloat() * 0.5f,
                            animationSpec = tween(1000 + (Random.nextInt(1000)), easing = LinearEasing)
                        )
                    }
                }

                launch {
                    while(true) {
                        particleAlpha.animateTo(
                            targetValue = Random.nextFloat() * 0.5f + 0.5f,
                            animationSpec = tween(700 + (Random.nextInt(1000)))
                        )
                        particleAlpha.animateTo(
                            targetValue = Random.nextFloat() * 0.3f,
                            animationSpec = tween(700 + (Random.nextInt(1000)))
                        )
                    }
                }

                launch {
                    while(true) {
                        val newX = (Random.nextFloat() * 300f) - 150f
                        particleX.animateTo(
                            targetValue = newX,
                            animationSpec = tween(5000 + (Random.nextInt(5000)))
                        )
                    }
                }

                launch {
                    while(true) {
                        val newY = (Random.nextFloat() * 200f) - 100f
                        particleY.animateTo(
                            targetValue = newY,
                            animationSpec = tween(5000 + (Random.nextInt(5000)))
                        )
                    }
                }
            }

            val particleColor = when (index % 3) {
                0 -> Color(0xFFFFFFFF)  // White
                1 -> Color(0xFFFFD700)  // Gold
                else -> Color(0xFF00FFFF)  // Cyan
            }

            Box(
                modifier = Modifier
                    .offset(x = particleX.value.dp, y = particleY.value.dp)
                    .size(3.dp * particleScale.value)
                    .alpha(particleAlpha.value)
                    .background(
                        color = particleColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CarouselSelector(
    label: String,
    options: List<String>,
    currentIndex: Int,
    onIndexChanged: (Int) -> Unit,
    gradientColors: List<Color>
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glow = infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Enhanced label animations
    val labelPulse = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Enhanced label
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .scale(labelPulse.value)
                .shadow(6.dp, CircleShape)
                .padding(bottom = 8.dp)
                .drawBehind {
                    // Draw a subtle horizontal line under the label
                    drawLine(
                        color = gradientColors.first().copy(alpha = 0.7f),
                        start = Offset(size.width * 0.2f, size.height),
                        end = Offset(size.width * 0.8f, size.height),
                        strokeWidth = 2f
                    )
                }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Arrow buttons with enhanced visuals
            IconButton(
                onClick = { if (currentIndex > 0) onIndexChanged(currentIndex - 1) },
                enabled = currentIndex > 0,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(if (currentIndex > 0) glow.value.dp else 0.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = if (currentIndex > 0) {
                            Brush.radialGradient(
                                colors = listOf(
                                    gradientColors.first().copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = "Previous",
                    tint = Color(0xFFFFE600).copy(alpha = if (currentIndex > 0) 1f else 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }

            // Enhanced animated content for selector options
            AnimatedContent(
                targetState = options[currentIndex],
                transitionSpec = {
                    if (targetState > initialState)
                        (slideInHorizontally { width -> width } + fadeIn()).with(
                            slideOutHorizontally { width -> -width } + fadeOut()) else
                        (slideInHorizontally { width -> -width } + fadeIn()) with
                                (slideOutHorizontally { width -> width } + fadeOut())
                },
                modifier = Modifier.weight(1f)
            ) { currentOption ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            shadowElevation = glow.value
                            shape = RoundedCornerShape(16.dp)
                            clip = true
                        }
                        .background(
                            brush = Brush.linearGradient(
                                colors = gradientColors,
                                start = Offset(0f, 0f),
                                end = Offset(100f, 100f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // For depth effect, add a subtle shadow behind text
                    Text(
                        text = currentOption.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                offset = Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        )
                    )
                }
            }

            // Right arrow with enhanced visuals
            IconButton(
                onClick = { if (currentIndex < options.size - 1) onIndexChanged(currentIndex + 1) },
                enabled = currentIndex < options.size - 1,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(if (currentIndex < options.size - 1) glow.value.dp else 0.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = if (currentIndex < options.size - 1) {
                            Brush.radialGradient(
                                colors = listOf(
                                    gradientColors.first().copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "Next",
                    tint = Color(0xFFFFE600).copy(alpha = if (currentIndex < options.size - 1) 1f else 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}