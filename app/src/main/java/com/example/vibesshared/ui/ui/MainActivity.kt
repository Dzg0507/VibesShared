package com.example.vibesshared.ui.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.navigation.Navigation
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.NavigationEvent
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VibesSharedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VibeSharedApp(authViewModel = authViewModel)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VibeSharedApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(
    )
) {
    val authState by authViewModel.authState.collectAsState()
    val navigationEvent by authViewModel.navigationEvent.collectAsState(initial = null)

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateToRoute -> {
                navController.navigate((navigationEvent as NavigationEvent.NavigateToRoute).route) {
                    (navigationEvent as NavigationEvent.NavigateToRoute).popUpToRoute?.let { popUpToRoute ->
                        popUpTo(popUpToRoute) {
                            inclusive =
                                (navigationEvent as NavigationEvent.NavigateToRoute).inclusive
                        }
                    }
                }
            }

            else -> {}
        }
    }

    Scaffold {
        val innerPadding = null
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") innerPadding
        AuthNavigationHandler(
            navController = navController,
            authState = authState,
            currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        )

        Navigation(
            navController = navController,
            authViewModel = authViewModel,
            profileViewModel = profileViewModel,
            authState = authState,
        )

        }
    }


