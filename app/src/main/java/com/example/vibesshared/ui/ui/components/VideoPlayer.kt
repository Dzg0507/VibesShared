package com.example.vibesshared.ui.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vibesshared.ui.ui.theme.NeonBlue
import com.example.vibesshared.ui.ui.theme.NeonGreen
import com.example.vibesshared.ui.ui.theme.NeonYellow

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoUrl: String, // Changed from videoResId to videoUrl for Firebase Storage
    onVideoEnd: () -> Unit = {} // Optional callback for video end
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }

    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                playWhenReady = false // Start paused, user controls playback
                repeatMode = Player.REPEAT_MODE_OFF
                prepare()
            }
    }

    // Handle playback state changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoEnd()
                    isPlaying = false
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release() // Release player resources
        }
    }

    // Show thumbnail if not playing, with play icon overlay
    // Show thumbnail if not playing, with play icon overlay
    Box(
        modifier = modifier
            .clickable {
                isPlaying = !isPlaying
                exoPlayer.playWhenReady = isPlaying
                showControls = true
            }
    ) {
        if (!isPlaying) {
            // Display video thumbnail using the first frame or a placeholder
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoUrl) // Use video URL to fetch a thumbnail (Coil may not natively support video thumbnails, so we’ll assume a generic image or placeholder)
                    .crossfade(true)
                    .build(),
                contentDescription = "Video Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    // Fallback to a placeholder if thumbnail fails
                    @Composable
                    fun PlaceholderContent() {
                        Box(
                            modifier = Modifier
                                .size(48.dp) // Match the size of the Icon for consistency
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            NeonGreen,
                                            NeonBlue
                                        )
                                    ), // Neon gradient background
                                    shape = CircleShape
                                )
                                .border(2.dp, NeonYellow, CircleShape) // Neon border for emphasis
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = NeonYellow,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp) // Slightly smaller icon to fit within the background
                            )
                        }
                        PlaceholderContent()
                    }
                     // Call the composable function directly
                }
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Video",
                tint = NeonYellow,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        } else {
            // Display ExoPlayer view when playing
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true // Show controls for user interaction
                        resizeMode =
                            AspectRatioFrameLayout.RESIZE_MODE_FIT // Fit the video to maintain aspect ratio
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) // Show buffering spinner
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
