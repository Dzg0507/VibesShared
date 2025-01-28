package com.example.vibesshared.ui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.navigation.SetupNavGraph
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibesSharedTheme {
                VibeAppScreen(auth)
            }
        }
    }
}

@Composable
fun VibeAppScreen(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavigationDrawer(
        navController = navController,
        drawerState = drawerState,
        gesturesEnabled = true,
        auth = auth,
        content = {
            SetupNavGraph(navController = navController, auth = auth)
        }
    )
}