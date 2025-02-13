package com.example.vibesshared.ui.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.components.VideoPlayer
import com.example.vibesshared.ui.ui.navigation.Screen //  <--- IMPORTANT: Import Screen

fun setSystemBars(window: Window, hide: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(window, !hide)
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    if (hide) {
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}

@UnstableApi
@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(activity) {
        activity?.window?.let { window ->
            setSystemBars(window, true)
        }
    }

    DisposableEffect(Unit) { // Use Unit as the key
        onDispose {
            activity?.window?.let { window ->
                setSystemBars(window, false)
            }
        }
    }

    VideoPlayer(
        modifier = Modifier.fillMaxSize(),
        videoResId = R.raw.char_splash,
        onVideoEnd = {
            // *** CORRECT NAVIGATION WITH POPUP ***
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true } // Remove splash from back stack
            }
        }
    )
}

// Helper function to find the Activity from a Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}