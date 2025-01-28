package com.example.vibesshared.ui.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavController, name: String?, aboutName: String?) {


    val context = LocalContext.current


    val videoUri = remember {
        try {
            Uri.parse("android.resource://${context.packageName}/${R.raw.char_splash}")
        } catch (e: Exception) {
            println("Error parsing video URI: ${e.message}")
            Uri.EMPTY
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Background for the video
    ) {
        if (videoUri != Uri.EMPTY) {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(videoUri)
                        start()
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
                    .scale(scaleX = 1.5f, scaleY = 2.7f)
            )
        } else {
            Text("Video Not Found", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(aboutName.toString(), color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vibes Shared App",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Blue,

                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Version 2.3.1",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Green,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "As someone who loves connecting with people and sharing experiences, I was inspired to create this chat app to provide a space for others to do the same. I believe in the power of technology to foster meaningful interactions and build stronger communities. While I may not be a coder myself, I've dedicated myself to learning the ins and outs of app development to bring this vision to life. I hope this app helps you stay connected with the people who matter most to you.                                           -------------------------------------------------------------------------------    Solo Creator/Developer                   Devin Z Griffin",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(30.dp))

                AsyncImage(
                    model = "https://picsum.photos/200/300",
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
            }
        }
    }
}