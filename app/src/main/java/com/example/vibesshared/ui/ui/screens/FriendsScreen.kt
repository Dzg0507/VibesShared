// FriendsScreen.kt
package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.SunsetOrange
import com.example.vibesshared.ui.ui.theme.VividBlue

data class Friend(val name: String, val avatarUrl: String, val post: String)

val friends = listOf(
    Friend("Alice", "https://example.com/avatar1.jpg", "Feeling good today! 😎"),
    Friend("Bob", "https://example.com/avatar2.jpg", "Just finished a great workout! 💪"),
    Friend("Charlie", "https://example.com/avatar3.jpg", "Excited for the weekend! 🎉"),
    Friend("David", "https://example.com/avatar4.jpg", "Enjoying a delicious coffee ☕"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    var expandedFriend by remember { mutableStateOf<Friend?>(null) }

    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue)
    val transition = rememberInfiniteTransition()
    val currentOffset = transition.animateFloat(
        initialValue = 0f,
        targetValue = screenWidthDp.value * gradientColors.size,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = currentOffset.value - screenWidthDp.value,
                    endX = currentOffset.value
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(friends) { friend ->
                FriendCard(
                    friend = friend,
                    isExpanded = expandedFriend == friend,
                    onExpandClick = { expandedFriend = if (expandedFriend == friend) null else friend },
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FriendCard(
    friend: Friend,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    navController: NavController
) {
    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Smooth scrolling animation
    val scrollOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 15000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize()
            .clickable(onClick = onExpandClick)
            .zIndex(if (isExpanded) 1f else 0f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) SunsetOrange else LimeGreen
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isExpanded) 80.dp else 50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        val baseText = friend.post + "" // Added fixed spacing

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds()
                        ) {
                            // First copy
                            Text(
                                text = baseText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier
                                    .offset(x = with(LocalDensity.current) {
                                        (scrollOffset * 1000+1000).dp
                                    })
                                    .scale(scale),
                                maxLines = 1,
                                softWrap = false
                            )

                            // Second copy
                            Text(
                                text = baseText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier
                                    .offset(x = with(LocalDensity.current) {
                                        (scrollOffset * 1000 + 1000).dp
                                    })
                                    .scale(scale),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Text(
                        text = "More details about ${friend.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}