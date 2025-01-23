package com.example.vibesshared.ui.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.screens.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedLottieButton(onClick: () -> Unit) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.compass)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LottieAnimation(
        composition,
        progress = { progress },
        modifier = Modifier
            .size(100.dp)
            .clickable { onClick() }
    )
}

@Composable
fun ColorSplashEffect(
    modifier: Modifier = Modifier,
    startAnimation: Boolean,
    onAnimationEnd: () -> Unit
) {
    val colors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan
    )
    val density = LocalDensity.current
    val maxSize = with(density) { 300.dp.toPx() }
    val animatableSize = remember { Animatable(0f) }

    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            animatableSize.animateTo(
                targetValue = maxSize,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            onAnimationEnd()
        }
    }

    if (startAnimation) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val currentSize = animatableSize.value

            for (i in colors.indices) {
                val angle = (i.toFloat() / colors.size) * 360f
                val color = colors[i]

                drawCircle(
                    color = color,
                    radius = currentSize / 2,
                    center = center + Offset(
                        x = currentSize / 4 * cos(Math.toRadians(angle.toDouble())).toFloat(),
                        y = currentSize / 4 * sin(Math.toRadians(angle.toDouble())).toFloat()
                    ),
                    alpha = 1f - (currentSize / maxSize)
                )
            }
        }
    }
}

@Composable
fun NavigationDrawer(
    navController: NavHostController,
    drawerState: DrawerState,
    gesturesEnabled: Boolean = true,
    drawerWidthFraction: Float = 0.8f,
    content: @Composable () -> Unit

) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = screenWidth * drawerWidthFraction

    val items = listOf(
        Screen.Profile,
        Screen.Settings,
        Screen.AboutUs
        // Remove ArrowScreen if you don't want it in the drawer
    )

    var isLogoInteractive by remember { mutableStateOf(true) }
    var isLogoBouncing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val logoAnimatableX = remember { Animatable(0f) }
    val logoAnimatableY = remember { Animatable(0f) }
    val logoSize = 100.dp
    val collisionSize = 250.dp
    val screenHeight = configuration.screenHeightDp.dp

    var showColorSplash by remember { mutableStateOf(false) }
    var startColorSplash by remember { mutableStateOf(false) }
    var showSplashButton by remember { mutableStateOf(true) }

    fun resetLogoState() {
        scope.launch {
            isLogoInteractive = false
            isLogoBouncing = false
            showSplashButton = false
            logoAnimatableX.snapTo(0f)
            logoAnimatableY.snapTo(0f)
        }
    }

    LaunchedEffect(isLogoBouncing, drawerState.currentValue) {
        if (isLogoBouncing && drawerState.isOpen) {
            val maxX = with(density) { (drawerWidth - collisionSize).toPx() }
            val maxY = with(density) { (screenHeight - collisionSize - 56.dp).toPx() }

            var velocityX = with(density) { 3.dp.toPx() }
            var velocityY = with(density) { 3.dp.toPx() }

            logoAnimatableX.snapTo(maxX / 2)
            logoAnimatableY.snapTo(maxY / 2)
            velocityX *= if (Random.nextBoolean()) 1f else -1f
            velocityY *= if (Random.nextBoolean()) 1f else -1f

            launch {
                while (isLogoBouncing && drawerState.isOpen) {
                    val newX = logoAnimatableX.value + velocityX
                    val newY = logoAnimatableY.value + velocityY

                    when {
                        newX > maxX -> {
                            logoAnimatableX.snapTo(maxX)
                            velocityX = -velocityX * 0.9f
                        }
                        newX < 0f -> {
                            logoAnimatableX.snapTo(0f)
                            velocityX = -velocityX * 0.9f
                        }
                        else -> {
                            logoAnimatableX.snapTo(newX)
                        }
                    }

                    when {
                        newY > maxY -> {
                            logoAnimatableY.snapTo(maxY)
                            velocityY = -velocityY * 0.9f
                        }
                        newY < 0f -> {
                            logoAnimatableY.snapTo(0f)
                            velocityY = -velocityY * 0.9f
                        }
                        else -> {
                            logoAnimatableY.snapTo(newY)
                        }
                    }

                    delay(5)
                }
            }
        } else {
            logoAnimatableX.stop()
            logoAnimatableY.stop()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(drawerWidth)
                    .clickable(
                        enabled = isLogoInteractive,
                        onClick = {
                            if (isLogoInteractive) {
                                isLogoBouncing = !isLogoBouncing
                            }
                        }
                    ),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ColorSplashEffect(
                        modifier = Modifier.fillMaxSize(),
                        startAnimation = startColorSplash,
                        onAnimationEnd = {
                            startColorSplash = false
                            showColorSplash = false
                            resetLogoState()
                        }
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.height(12.dp))
                        items.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(item.title ?: "") },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors()
                            )
                        }
                        AnimatedLottieButton(onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(Screen.ArrowScreen.route)
                            }
                        })

                        Spacer(modifier = Modifier.weight(1f))

                        if (showSplashButton) {
                            Button(
                                onClick = {
                                    showColorSplash = true
                                    startColorSplash = true
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 16.dp)
                            ) {
                                Text("Color Splash")
                            }
                        }

                        if (!showColorSplash) {
                            Image(
                                painter = painterResource(id = R.drawable.my_profile_icon),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(logoSize)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        rotationZ = rotationAngle
                                    }
                                    .offset {
                                        IntOffset(
                                            x = logoAnimatableX.value.toInt(),
                                            y = logoAnimatableY.value.toInt()
                                        )
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        },
        content = content
    )
}