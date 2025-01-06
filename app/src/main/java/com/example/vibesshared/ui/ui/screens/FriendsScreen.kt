package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController

@Composable
fun FriendsScreen(navController: NavController) {
    val context = LocalContext.current
    val videoUri = remember {
        Uri.parse("android.resource://" + context.packageName + "/" + com.example.vibesshared.R.raw.fire_smoke)
    }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(context) {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ONE // Loop the video
            playWhenReady = true
            prepare()
        }

        onDispose {
            exoPlayer?.release()
        }
    }

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = false // Hide the player controls
                // You can add other customizations here, like resize modes:
                // resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}