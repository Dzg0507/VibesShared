package com.example.vibesshared.ui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.navigation.SetupNavGraph
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @UnstableApi
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VibesSharedTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.authState.collectAsState()
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Determine start destination based on auth state
                val startDestination = if (authState is AuthState.Authenticated) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }
                Log.d("MainActivity", "AuthState in MainActivity: $authState")

                // Removed unused currentDestinationState

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            NavigationDrawer(navController = navController, drawerState = drawerState, scope = scope)
                        }
                    },
                    content = {
                        Scaffold(
                            bottomBar = {
                                BottomNavigationBar(
                                    navController = navController,
                                    authState = authState,
                                    userId = Firebase.auth.currentUser?.uid ?: "",
                                )
                            }
                        ) { innerPadding ->
                            SetupNavGraph(
                                navController = navController,
                                startDestination = startDestination,
                                paddingValues = innerPadding
                            )
                        }
                    }
                )
            }
        }
    }
}