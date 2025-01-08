// MainActivity.kt
package com.example.vibesshared.ui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.navigation.Navigation
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import com.example.vibesshared.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibesSharedTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    Navigation(navController = navController)
}