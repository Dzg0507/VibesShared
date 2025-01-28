package com.example.vibesshared.ui.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@UnstableApi
@Composable
fun VideoPlayer(modifier: Modifier = Modifier, videoResId: Int, onVideoEnd: () -> Unit, viewModel: VideoPlayerViewModel = hiltViewModel()) {
    val exoPlayer = viewModel.exoPlayer

    DisposableEffect(Unit) { // Use a fixed key like Unit
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    onVideoEnd()
                }
            }
        }
        exoPlayer.addListener(listener)

        exoPlayer.apply {
            val mediaItem = MediaItem.fromUri("android.resource://${viewModel.context.packageName}/$videoResId")
            setMediaItem(mediaItem)
            prepare()
        }

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release() // Release the player when the effect is disposed
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        },
        modifier = modifier
    )
}

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {
    @UnstableApi
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        playWhenReady = true
        repeatMode = ExoPlayer.REPEAT_MODE_OFF
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}