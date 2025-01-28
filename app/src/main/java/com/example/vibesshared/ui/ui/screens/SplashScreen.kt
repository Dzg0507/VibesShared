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
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.components.VideoPlayer

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
fun SplashScreen(onTimeout: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(activity) {
        activity?.window?.let { window ->
            setSystemBars(window, true)
        }
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.window?.let { window ->
                setSystemBars(window, false)
            }
        }
    }

    VideoPlayer(
        modifier = Modifier.fillMaxSize(),
        videoResId = R.raw.char_splash,
        onVideoEnd = onTimeout
    )
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}