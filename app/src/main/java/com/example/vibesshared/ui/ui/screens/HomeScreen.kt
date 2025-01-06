package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.vibesshared.R
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.widget.VideoView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val videoUri = "android.resource://${context.packageName}/${R.raw.video1}"

        AndroidView(
            modifier = Modifier.size(700.dp),
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse(videoUri))
                    setOnPreparedListener { mp ->
                        mp.isLooping = true // Make the video loop
                        mp.start()
                    }

                    // Add lifecycle observer to start/stop video playback
                    lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> start()
                                Lifecycle.Event.ON_PAUSE -> pause()
                                else -> {}
                            }
                        }
                    })
                }
            }
        )

        Text("Video Player")
    }
}