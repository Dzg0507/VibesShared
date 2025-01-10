package com.example.vibesshared.ui.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.components.TopBar
import com.example.vibesshared.ui.ui.navigation.Navigation
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import com.example.vibesshared.ui.ui.viewmodel.AuthViewModel
import com.example.vibesshared.ui.ui.viewmodel.ProfileViewModel


class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels { // Specify the ViewModel type
        ProfileViewModel.Companion.provideFactory(this) // Access provideFactory through Companion
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibesSharedTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val authState by authViewModel.authState.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Log.d("AuthNavigation", "Calling AuthNavigationHandler")

                NavigationDrawer(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            topBar = {
                                TopBar(
                                    scope = scope,
                                    drawerState = drawerState,
                                    title = "Vibes Are Shared"
                                )
                            }
                        ) {
                            Navigation(
                                navController = navController,
                                authViewModel = authViewModel,
                                profileViewModel = profileViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}