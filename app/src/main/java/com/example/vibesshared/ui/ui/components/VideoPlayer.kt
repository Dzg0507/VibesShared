package com.example.vibesshared.ui.ui.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoResId: Int,
    onVideoEnd: () -> Unit
) {
    val context = LocalContext.current  // Get context outside remember

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${videoResId}")
                setMediaItem(mediaItem)
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF // Use Player.REPEAT_MODE_OFF
                prepare()
            }
    }
    DisposableEffect(exoPlayer) { // Use exoPlayer as the key
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoEnd()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release() // Release player resources
        }
    }


    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // Hide controls for splash screen
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL // Fill the screen
            }
        },
        modifier = modifier
    )
}