package com.example.vibesshared.ui.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vibesshared.ui.ui.gaming.AnimationType
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AchievementAnimationOverlay(
    animationType: AnimationType,
    title: String,
    description: String,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showOverlay by remember { mutableStateOf(true) }
    var showContent by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (showOverlay) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
        delay(3000)
        showOverlay = false
        delay(500)
        onAnimationComplete()
    }
    
    if (showOverlay) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .zIndex(10f)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(scale)
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lottie Animation
                when (animationType) {
                    AnimationType.TROPHY_CELEBRATION -> {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.Asset("lottie/trophy_celebration.json")
                        )
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = 1
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = progress,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    AnimationType.STAR_BURST -> {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.Asset("lottie/star_burst.json")
                        )
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = 1
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = progress,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    AnimationType.COIN_COLLECT -> {
                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.Asset("lottie/coin_collect.json")
                        )
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = 1
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = progress,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    AnimationType.NONE -> {
                        // Fallback to icon animation
                        val infiniteTransition = rememberInfiniteTransition(label = "infinite")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 80.sp,
                                modifier = Modifier.graphicsLayer { rotationZ = rotation }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Achievement Text
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Achievement Unlocked!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricPurple
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = description,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelUpOverlay(
    newLevel: Int,
    levelTitle: String,
    rewards: List<com.example.vibesshared.ui.ui.gaming.LevelReward>,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOverlay by remember { mutableStateOf(true) }
    var showContent by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (showOverlay) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
        delay(4000)
        showOverlay = false
        delay(500)
        onAnimationComplete()
    }
    
    if (showOverlay) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .zIndex(10f)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(scale)
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Level Up Animation
                val infiniteTransition = rememberInfiniteTransition(label = "infinite")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                val pulse by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )
                
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .scale(pulse),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 80.sp,
                        modifier = Modifier.rotate(rotation)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Level Up Text
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LEVEL UP!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LimeGreen
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Level $newLevel",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricPurple
                        )
                        
                        Text(
                            text = levelTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (rewards.isNotEmpty()) {
                            Text(
                                text = "Rewards Earned:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            rewards.forEach { reward ->
                                Text(
                                    text = "• ${reward.description}",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticleEffect(
    modifier: Modifier = Modifier,
    duration: Int = 3000
) {
    val particles = remember { (1..20).map { 
        Particle(
            x = (0..400).random().toFloat(),
            y = (0..400).random().toFloat(),
            size = (2..8).random().toFloat(),
            color = listOf(
                Color(0xFFFFD700), // Gold
                Color(0xFFFFA500), // Orange
                Color(0xFFFF6347), // Tomato
                Color(0xFFFF1493), // Deep Pink
                Color(0xFF00CED1)  // Dark Turquoise
            ).random()
        )
    }}
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    Box(modifier = modifier) {
        particles.forEachIndexed { index, particle ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = particle.y,
                targetValue = particle.y - 200f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particle_$index"
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha_$index"
            )
            
            Box(
                modifier = Modifier
                    .offset(x = particle.x.dp, y = offsetY.dp)
                    .size(particle.size.dp)
                    .background(
                        particle.color.copy(alpha = alpha),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color
)