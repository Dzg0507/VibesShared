package com.example.vibesshared.ui.ui.screens

import android.app.Activity
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
import com.example.vibesshared.R

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

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val activity = LocalContext.current as Activity // Cast to Activity

    LaunchedEffect(Unit) {
        activity.window?.also { window -> setSystemBars(window, true) }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity.window?.also { window -> setSystemBars(window, false) }
        }
    }

    VideoPlayer(
        modifier = Modifier.fillMaxSize(),
        videoResId = R.raw.paint_anim,
        onVideoEnd = onSplashFinished
    )
}

@Composable
fun VideoPlayer(modifier: Modifier, videoResId: Int, onVideoEnd: () -> Unit) {
    LocalContext.current}
