package com.example.vibesshared.ui.ui.cardgame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibesshared.R

/**
 * Helper class for rendering card UI components in the app
 */


    /**
     * Returns the border color gradient for a card based on its tier
     */
    fun getCardTierGradient(tier: CardTier): Brush {
        return when (tier) {
            CardTier.COMMON -> Brush.linearGradient(
                colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE)) // Grey
            )
            CardTier.UNCOMMON -> Brush.linearGradient(
                colors = listOf(Color(0xFF66BB6A), Color(0xFF388E3C)) // Green
            )
            CardTier.RARE -> Brush.linearGradient(
                colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2)) // Blue
            )
            CardTier.EPIC -> Brush.linearGradient(
                colors = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2)) // Purple
            )
            CardTier.LEGENDARY -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF6F00)) // Gold to Orange
            )

            CardTier.MYTHIC -> Brush.linearGradient(
                colors = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2))) // Purple
        }
    }

    /**
     * Returns the background color for a card back based on its tier
     */
    fun getCardBackColor(tier: CardTier): Color {
        return when (tier) {
            CardTier.COMMON -> Color(0xFF78909C) // Blue grey
            CardTier.UNCOMMON -> Color(0xFF4CAF50) // Green
            CardTier.RARE -> Color(0xFF2196F3) // Blue
            CardTier.EPIC -> Color(0xFF9C27B0) // Purple
            CardTier.LEGENDARY -> Color(0xFFFFC107) // Amber
            CardTier.MYTHIC -> Color(0xFF85042F)
        }
    }


/**
 * Composable that renders a card in the collection or during gameplay.
 *
 * @param card The card to render
 * @param modifier Modifier for customizing the card's appearance
 * @param isSelected Whether the card is currently selected
 * @param showDetails Whether to show detailed card information
 * @param onClick Action to perform when the card is clicked
 */
@Composable
fun MultiverseCardView(
    card: MultiverseCard,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showDetails: Boolean = true,
    onClick: () -> Unit = {}
) {
    val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
    val cardTierGradient = CardRenderingUtils.getCardTierGradient(card.tier)

    Card(
        modifier = modifier
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                brush = cardTierGradient,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            cardTypeColor.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(8.dp)
        ) {
            // Card Header with Name and Energy Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Energy Cost
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            Color(0xFF1A237E), // Dark blue for energy
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.energy.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Card Name
                Text(
                    text = card.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Card Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardTypeColor.copy(alpha = 0.3f))
            ) {
                // Load card image if available
                // Load card image
                if (true) {
                    // Use the resource ID if available
                    Image(
                        painter = painterResource(id = R.drawable.charizard),
                        contentDescription = card.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Fallback to a placeholder if no image is available
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardTypeColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display first letter of card name as placeholder
                        Text(
                            text = card.name.first().toString(),
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Type and Tier badges
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(cardTypeColor)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.type.name.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Tier badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CardRenderingUtils.getCardBackColor(card.tier))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.tier.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Card stats (Power/Defense)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Power
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFFD32F2F), // Red for power
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.power.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Defense
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF1976D2), // Blue for defense
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.defense.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Spacer to push keywords to the right

                Spacer(modifier = Modifier.weight(1f))
                val keywords = card.name// Keywords (if any and there's enough space)
                // If your card model has keywords property
                if (card.name.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.name.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
// If you don't have keywords, you can replace this with card type or another property
                else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.type.name.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Card description and effects (if showing details)
                if (showDetails) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Description (flavor text)
                    Text(
                        text = card.description,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    // Effects
                    if (card.effects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp)
                        ) {
                            Column {
                                card.effects.forEach { effect ->
                                    Text(
                                        text = "• ${effect.description}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Composable that renders a card back (used when the card is face down)
     *
     * @param tier The tier of the card, which determines the back design
     * @param modifier Modifier for customizing the card's appearance
     */
    @Composable
    fun CardBackView(
        tier: CardTier,
        modifier: Modifier = Modifier
    ) {
        val backColor = CardRenderingUtils.getCardBackColor(tier)
        val borderGradient = CardRenderingUtils.getCardTierGradient(tier)

        Card(
            modifier = modifier
                .border(
                    width = 2.dp,
                    brush = borderGradient,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                backColor,
                                backColor.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card back emblem/logo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .border(2.dp, borderGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MB",  // Multiverse Battle initials
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Card tier on the back
                    Text(
                        text = tier.name.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    /**
     * Composable that renders a mini version of the card (for deck building, etc.)
     *
     * @param card The card to render
     * @param modifier Modifier for customizing the card's appearance
     * @param onClick Action to perform when the card is clicked
     */
    @Composable
    fun MiniCardView(
        card: MultiverseCard,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ) {
        val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
        val cardTierGradient = CardRenderingUtils.getCardTierGradient(card.tier)

        Card(
            modifier = modifier
                .border(
                    width = 1.dp,
                    brush = cardTierGradient,
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                cardTypeColor.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Energy cost
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF1A237E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.energy.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Card name and type
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = card.type.name.toString(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Power/Defense
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.power.toString(),
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "/",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Text(
                        text = card.defense.toString(),
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}