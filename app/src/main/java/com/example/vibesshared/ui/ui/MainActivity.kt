package com.example.vibesshared.ui.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.compose.rememberNavController
import com.example.vibesshared.ui.ui.components.AppTopBar
import com.example.vibesshared.ui.ui.components.BottomNavigationBar
import com.example.vibesshared.ui.ui.components.NavigationDrawer
import com.example.vibesshared.ui.ui.navigation.Navigation
import com.example.vibesshared.ui.ui.theme.VibesSharedTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibesSharedTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Conditionally show the login UI or the app content
    if (!isLoggedIn) {
        // Login UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(onClick = {
                if (username == "admin" && password == "password") {
                    isLoggedIn = true
                }
            }) {
                Text("Login")
            }
        }
    } else {
        // App content with navigation drawer
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavigationDrawer(
                    navController = navController,
                    onDrawerItemClick = { screen ->
                        scope.launch {
                            drawerState.close()
                            navController.navigate(screen.route)
                        }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    AppTopBar(
                        onMenuIconClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavigationBar(navController)
                }
            ) { innerPadding ->
                Navigation(navController, Modifier.padding(innerPadding))
            }
        }
    }
}