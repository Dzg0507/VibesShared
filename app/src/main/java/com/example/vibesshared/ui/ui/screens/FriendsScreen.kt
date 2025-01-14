package com.example.vibesshared.ui.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.vibesshared.ui.ui.theme.ElectricPurple
import com.example.vibesshared.ui.ui.theme.LimeGreen
import com.example.vibesshared.ui.ui.theme.NeonPink
import com.example.vibesshared.ui.ui.theme.VividBlue
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

data class Friend(val name: String, val avatarUrl: String, val post: String, val isOnline: Boolean = Random.nextBoolean())

val friends = listOf(
    Friend("Alice", "https://picsum.photos/200/300", "Feeling good today! 😎"),
    Friend("Bob", "https://picsum.photos/200/300", "Just finished a great workout! 💪"),
    Friend("Charlie", "https://picsum.photos/200/300", "Excited for the weekend! 🎉"),
    Friend("David", "https://picsum.photos/200/300", "Enjoying a delicious coffee ☕"),
    Friend("Alice1", "https://picsum.photos/200/300", "Feeling good today! 😎"),
    Friend("Bob1", "https://picsum.photos/200/300", "Just finished a great workout! 💪"),
    Friend("Charlie1", "https://picsum.photos/200/300", "Excited for the weekend! 🎉"),
    Friend("David1", "https://picsum.photos/200/300", "Enjoying a delicious coffee ☕")
)

val cardColors = listOf(
    Color(0xFFDC8686),
    Color(0xFF8572CB),
    Color(0xFFF4EAE0),
    Color(0xFF6D5D6E),
    Color(0xFF393646)
)

@Composable
fun FriendsScreen(navController: NavHostController, paddingValues: PaddingValues) {
    var expandedFriend by remember { mutableStateOf<Friend?>(null) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue)
    val infiniteTransition = rememberInfiniteTransition()
    val currentOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = with(LocalDensity.current) { screenWidthDp.toPx() } * gradientColors.size,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = currentOffset - with(LocalDensity.current) { screenWidthDp.toPx() },
                    endX = currentOffset
                )
            )
            .padding(paddingValues) // Apply Scaffold padding here
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(top = 70.dp, bottom = 60.dp) // You might need to adjust these values
        ) {
            items(friends) { friend ->
                FriendCard(
                    friend = friend,
                    isExpanded = expandedFriend == friend,
                    onExpandClick = {
                        expandedFriend = if (expandedFriend == friend) null else friend
                    },
                    navController = navController,
                    cardColors = cardColors,
                    friendId = friend.name
                )
            }
        }
    }
}

@Composable
fun FriendCard(
    friend: Friend,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    navController: NavHostController,
    cardColors: List<Color>,
    friendId: String
) {
    var cardColor by remember { mutableStateOf(LimeGreen) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            cardColor = cardColors.random()
        }
    }

    val selectedColors = if (isExpanded) CardDefaults.cardColors(containerColor = cardColor) else CardDefaults.cardColors(containerColor = LimeGreen)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize()
            .clickable(onClick = onExpandClick)
            .zIndex(if (isExpanded) 1f else 0f),
        shape = RoundedCornerShape(25.dp),
        colors = selectedColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        AsyncImage(
                            model = friend.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(if (isExpanded) 80.dp else 60.dp)
                                .clip(CircleShape)
                        )
                        if (friend.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(if (isExpanded) 15.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Green)
                                    .align(Alignment.BottomEnd)
                                    .offset(
                                        x = if (isExpanded) (-2).dp else (-1).dp,
                                        y = if (isExpanded) (-2).dp else (-1).dp
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = friend.name,
                            style = typography.headlineSmall.copy(
                                fontSize = if (isExpanded) 22.sp else 18.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (isExpanded) {
                            Text(
                                text = "Tap to close",
                                style = typography.bodySmall.copy(color = Color.Black)
                            )
                        }
                    }
                }

                if (!isExpanded) {
                    IconButton(onClick = { /* TODO: Implement Add Friend logic */ }) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Add Friend",
                            tint = Color.Black
                        )
                    }
                }
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds()
                        ) {
                            val words = friend.post.split(" ")
                            var animatedText by remember { mutableStateOf("") }

                            LaunchedEffect(key1 = friend.post) {
                                words.forEachIndexed { index, word ->
                                    animatedText = words.subList(0, index + 1).joinToString(" ")
                                    delay(200.milliseconds)
                                }
                            }

                            Text(
                                text = animatedText,
                                style = typography.bodyLarge.copy(
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = {
                            navController.navigate("messaging/$friendId")
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC8686))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = "Message",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Message",
                            style = typography.bodyLarge.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}