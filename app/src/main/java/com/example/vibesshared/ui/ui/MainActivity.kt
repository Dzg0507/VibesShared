package com.example.vibesshared.ui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.components.AppTopBar
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.navigation.Navigation
import com.example.vibesshared.ui.ui.navigation.Screen
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import com.example.vibesshared.ui.ui.viewmodel.AuthState
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            VibesSharedTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val authState by authViewModel.authState.collectAsState()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Check if current screen is an auth screen
                val isAuthScreen = currentRoute in listOf(
                    Screen.Login.route,
                    Screen.CreateAccount.route,
                    Screen.ForgotPassword.route
                )

                // Handle navigation based on auth state
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Authenticated -> {
                            if (isAuthScreen) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            if (!isAuthScreen) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        else -> {} // Handle other states if needed
                    }
                }

                NavigationDrawer(
                    navController = navController,
                    authState = authState,
                    currentRoute = currentRoute
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            topBar = {
                                AppTopBar(
                                    currentRoute = currentRoute,
                                    drawerState = drawerState,
                                    authState = authState,
                                    isAuthScreen = isAuthScreen
                                )
                            },
                            bottomBar = {
                                BottomNavigationBar(
                                    navController = navController,
                                    authState = authState,
                                    currentRoute = currentRoute
                                )
                            }
                        ) { paddingValues ->
                            Navigation(
                                navController = navController,
                                authViewModel = authViewModel,
                                modifier = Modifier.padding(paddingValues)
                            ) { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}