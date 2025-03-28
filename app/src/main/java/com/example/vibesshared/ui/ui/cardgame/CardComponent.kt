package com.example.vibesshared.ui.ui.cardgame

// --- Imports ---
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.vibesshared.R

// --- Assume necessary imports exist ---
// CardInPlay, MultiverseCard, CardDisplayMode, CardType, CardTier,
// CardRenderingUtils, StatusEffectIcon, FloatingHealthBar, EffectType

// --- ScreenHeader (Unchanged from original file) ---
@Composable
fun ScreenHeader(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        // actions composable is invoked here, rendering any trailing icons/buttons
        actions()
    }
}


// --- CardComponent (Unchanged routing logic) ---
@Composable
fun CardComponent(
    modifier: Modifier = Modifier,
    card: MultiverseCard,
    displayMode: CardDisplayMode = CardDisplayMode.GRID_ITEM,
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
    cardInPlay: CardInPlay? = null // Keep this nullable parameter
) {
    // Assuming CardRenderingUtils is available and provides these functions
    val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
    val cardTierGradient = CardRenderingUtils.getCardTierGradient(card.tier)

    val clickableModifier = modifier.clickable(onClick = onClick)

    Box(
        modifier = clickableModifier
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                brush = cardTierGradient,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        cardTypeColor.copy(alpha = 0.2f),
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        // Route to the appropriate internal composable based on displayMode
        when (displayMode) {
            CardDisplayMode.GRID_ITEM -> GridItemCard(card)
            CardDisplayMode.FULL -> FullSizeCard(card, cardInPlay)
            CardDisplayMode.DECK_LIST -> DeckListCard(card)
            CardDisplayMode.HAND -> HandCard(card)
            CardDisplayMode.BOARD -> BoardCard(card, cardInPlay) // Calls MODIFIED BoardCard
        }
    }
}

// --- GridItemCard, FullSizeCard, DeckListCard, HandCard (Unchanged from original file) ---
@Composable
private fun GridItemCard(card: MultiverseCard) {
    // Assuming CardRenderingUtils is available
    val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cardTypeColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(card.imageResId ?: R.drawable.charizard) // Use placeholder if null
                    .error(R.drawable.logo1) // Fallback placeholder
                    .crossfade(true)
                    .build(),
                contentDescription = card.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(card.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${card.type} • ${card.tier}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatBadge(value = card.energy, label = "E", color = Color(0xFF03A9F4))
            StatBadge(value = card.power, label = "P", color = Color(0xFFD32F2F))
            StatBadge(value = card.defense, label = "D", color = Color(0xFF1976D2))
        }
    }
}

@Composable
private fun FullSizeCard(card: MultiverseCard, cardInPlay: CardInPlay? = null) {
    // Assuming CardRenderingUtils is available
    val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
    val displayHealth = cardInPlay?.currentHealth ?: card.health
    // val maxHealth = card.health // Already available via card.health

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.size(32.dp).background(Color(0xFF1A237E), CircleShape), contentAlignment = Alignment.Center) {
                Text(text = card.energy.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = card.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).background(cardTypeColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(card.imageResId ?: R.drawable.charizard) // Use placeholder
                    .error(R.drawable.logo1) // Fallback placeholder
                    .crossfade(true)
                    .build(),
                contentDescription = card.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = card.type.name, color = cardTypeColor, fontSize = 16.sp, fontStyle = FontStyle.Italic)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = card.tier.name, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            LargeStatBadge(value = card.power, label = "Power", color = Color(0xFFD32F2F))
            LargeStatBadge(value = card.defense, label = "Defense", color = Color(0xFF1976D2))
            LargeStatBadge(
                value = displayHealth, // Use displayHealth
                label = "Health",
                color = Color(0xFF4CAF50)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = card.description, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, lineHeight = 20.sp)

        if (card.abilities.isNotEmpty()) { // Check abilities, not effects
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Abilities", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            // Displaying ability names; details would require lookup
            card.abilities.forEach { abilityName ->
                Text("• $abilityName", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        // Display active status effects from cardInPlay if needed
        cardInPlay?.activeEffects?.let { effects ->
            if (effects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Active Effects", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                effects.forEach { (type, data) ->
                    Text("• ${type.name} (${data.first} turns, Mag: ${data.second})", color = Color(0xFFFFEB3B), fontSize = 12.sp) // Example color
                }
            }
        }
    }
}


@Composable
private fun DeckListCard(card: MultiverseCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(28.dp).background(Color(0xFF1A237E), CircleShape), contentAlignment = Alignment.Center) {
            Text(text = card.energy.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = card.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${card.type} • ${card.tier}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text("${card.power}/${card.defense}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HandCard(card: MultiverseCard) {
    // Assuming CardRenderingUtils is available
    val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Box(Modifier.size(18.dp).background(Color(0xFF1A237E), CircleShape).padding(2.dp), contentAlignment = Alignment.Center){
                Text(card.energy.toString(), color=Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(4.dp))
            Text(card.name, color=Color.White, fontSize=10.sp, fontWeight=FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(cardTypeColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(card.imageResId ?: R.drawable.logo1) // Use placeholder
                    .error(R.drawable.logo1) // Fallback placeholder
                    .crossfade(true)
                    .build(),
                contentDescription = card.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatBadge(value = card.power, label = "", color = Color(0xFFD32F2F))
            StatBadge(value = card.defense, label = "", color = Color(0xFF1976D2))
        }
    }
}

// --- BoardCard --- (MODIFIED to reserve space for status icons)
@Composable
private fun BoardCard(card: MultiverseCard, cardInPlay: CardInPlay? = null) {
    // Assuming CardRenderingUtils is available
    val cardTypeColor = CardRenderingUtils.getCardTypeColor(card.type)
    val displayHealth = cardInPlay?.currentHealth ?: card.health
    val maxHealth = card.health
    val statusEffectIconSize = 18.dp // Define size for consistency
    val statusEffectRowHeight = statusEffectIconSize + 4.dp // Icon size + padding

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box( // Container for Image + Status Icons
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Image takes most space
                .padding(vertical = 0.dp) // Reduced padding
                .clip(RoundedCornerShape(6.dp))
                .background(cardTypeColor.copy(alpha = 0.3f))
        ) {
            AsyncImage( // Image takes full space of the Box
                model = ImageRequest.Builder(LocalContext.current)
                    .data(card.imageResId ?: R.drawable.charizard)
                    .error(R.drawable.logo1)
                    .crossfade(true)
                    .build(),
                contentDescription = card.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // --- MODIFIED Status Effect Row ---
            // Always reserve space for the status icon row using a fixed height Box
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart) // Position at top start of the image Box
                    .fillMaxWidth() // Take full width to align content within
                    .height(statusEffectRowHeight) // Reserve fixed height
                    .padding(2.dp) // Padding inside the reserved space
            ) {
                Row { // Row to hold the actual icons
                    cardInPlay?.activeEffects?.keys?.take(3)?.forEach { effectType ->
                        StatusEffectIcon( // Assumed defined/imported
                            effectType = effectType,
                            modifier = Modifier.size(statusEffectIconSize) // Use consistent size
                        )
                        Spacer(Modifier.width(2.dp))
                    }
                    // If no effects, this Row is empty but the parent Box still takes up space
                }
            }
            // --- End MODIFIED Status Effect Row ---
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = card.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatBadge(value = cardInPlay?.getCurrentPower() ?: card.power, label = "", color = Color(0xFFD32F2F))
            StatBadge(value = cardInPlay?.getCurrentDefense() ?: card.defense, label = "", color = Color(0xFF1976D2))
        }
        Spacer(modifier = Modifier.height(4.dp))
        FloatingHealthBar( // Assumed defined/imported
            currentHealth = displayHealth,
            maxHealth = maxHealth,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(2.dp))
    }
}

// --- StatBadge, LargeStatBadge (Unchanged from original file) ---
@Composable
private fun StatBadge(
    value: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(20.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
        if(label.isNotEmpty()){
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun LargeStatBadge(
    value: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}


// --- CardBackView (Unchanged from original file) ---
@Composable
fun CardBackView(
    modifier: Modifier = Modifier,
    tier: CardTier = CardTier.COMMON,
) {
    // Assuming CardRenderingUtils is available
    val backColor = CardRenderingUtils.getCardBackColor(tier)
    val borderGradient = CardRenderingUtils.getCardTierGradient(tier)

    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                brush = borderGradient,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            backColor,
                            backColor.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Simple placeholder for card back
            Text(
                text = "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp // Adjust size as needed
            )
        }
    }
}

// --- Floating Health Bar Composable (Unchanged from original file) ---
@Composable
fun FloatingHealthBar(
    modifier: Modifier = Modifier,
    currentHealth: Int,
    maxHealth: Int,
    barHeight: androidx.compose.ui.unit.Dp = 8.dp,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.5f),
    lowHealthColor: Color = Color(0xFFE53935),
    midHealthColor: Color = Color(0xFFFFC107),
    highHealthColor: Color = Color(0xFF4CAF50)
) {
    val healthPercentage = (currentHealth.toFloat() / maxHealth.toFloat()).coerceIn(0f, 1f)

    val animatedHealthPercentage by animateFloatAsState(
        targetValue = healthPercentage,
        animationSpec = tween(durationMillis = 300),
        label = "HealthBarAnimation"
    )

    val healthColor = when {
        animatedHealthPercentage < 0.3f -> lowHealthColor
        animatedHealthPercentage < 0.7f -> midHealthColor
        else -> highHealthColor
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(animatedHealthPercentage)
            .background(healthColor)
        )
    }
}


// --- CardRenderingUtils Object (Unchanged from original file) ---
object CardRenderingUtils {
    fun getCardTypeColor(type: CardType): Color {
        return when (type) {
            CardType.COSMIC -> Color(0xFF6A00FF)
            CardType.ELEMENTAL -> Color(0xFF00B8D4)
            CardType.QUANTUM -> Color(0xFF00BFA5)
            CardType.TEMPORAL -> Color(0xFF1565C0)
            CardType.PSYCHIC -> Color(0xFF8E24AA)
            CardType.MYTHIC -> Color(0xFFB71C1C)
            CardType.TECH -> Color(0xFF607D8B)
            CardType.VOID -> Color(0xFF4A148C)
            // Add other types if they exist in your enum
        }
    }

    fun getCardTierGradient(tier: CardTier): Brush {
        return when (tier) {
            CardTier.COMMON -> Brush.linearGradient(colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE)))
            CardTier.UNCOMMON -> Brush.linearGradient(colors = listOf(Color(0xFF66BB6A), Color(0xFF388E3C)))
            CardTier.RARE -> Brush.linearGradient(colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2)))
            CardTier.EPIC -> Brush.linearGradient(colors = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2)))
            CardTier.LEGENDARY -> Brush.linearGradient(colors = listOf(Color(0xFFFFD54F), Color(0xFFFF6F00)))
            CardTier.MYTHIC -> Brush.linearGradient(colors = listOf(Color(0xFFF44336), Color(0xFFD32F2F), Color(0xFFB71C1C)))
        }
    }

    fun getCardBackColor(tier: CardTier): Color {
        return when (tier) {
            CardTier.COMMON -> Color(0xFF78909C)
            CardTier.UNCOMMON -> Color(0xFF4CAF50)
            CardTier.RARE -> Color(0xFF2196F3)
            CardTier.EPIC -> Color(0xFF9C27B0)
            CardTier.LEGENDARY -> Color(0xFFFFC107)
            CardTier.MYTHIC -> Color(0xFF85042F)
        }
    }
}