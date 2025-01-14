package com.example.vibesshared.ui.ui.components

import android.app.Activity
import android.view.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.example.vibesshared.R
import com.example.vibesshared.ui.ui.screens.setSystemBars

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

fun setSystemBars(window: Window, hide: Boolean) {
    setDecorFitsSystemWindows(window, !hide)
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    insetsController.systemBarsBehavior =
        if (hide) BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE else WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
}