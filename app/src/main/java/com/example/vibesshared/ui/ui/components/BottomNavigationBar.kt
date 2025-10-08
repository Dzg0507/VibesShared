package com.example.vibesshared.ui.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.vibesshared.ui.ui.screens.Screen
import com.example.vibesshared.ui.ui.theme.*
import com.example.vibesshared.ui.ui.viewmodel.AuthState

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    authState: AuthState?,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    if (authState !is AuthState.Authenticated) return

    val items = Screen.bottomNavItems()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.9f else 1f,
                animationSpec = spring(dampingRatio = 0.6f)
            )
            
            NavigationBarItem(
                icon = { 
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { isPressed = true },
                                    onRelease = { isPressed = false }
                                )
                            }
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(VibrantPurple, ElectricBlue)
                                        )
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                item.icon?.let { 
                                    Icon(
                                        it, 
                                        contentDescription = item.title,
                                        tint = Color.White,
                                        modifier = Modifier.scale(1.1f)
                                    ) 
                                }
                            }
                        } else {
                            item.icon?.let { 
                                Icon(
                                    it, 
                                    contentDescription = item.title,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ) 
                            }
                        }
                    }
                },
                label = { 
                    Text(
                        text = item.title ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) VibrantPurple else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    ) 
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = VibrantPurple,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = VibrantPurple.copy(alpha = 0.1f)
                )
            )
        }
    }
}