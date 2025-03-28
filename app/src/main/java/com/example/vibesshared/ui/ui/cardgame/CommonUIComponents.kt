package com.example.vibesshared.ui.ui.cardgame

// File: CommonUIComponents.kt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared, basic UI elements used across multiple screens.
 */

/**
 * Primary button with customizable text and icon.
 *
 * @param text String button text
 * @param onClick () -> Unit callback when clicked
 * @param modifier Modifier additional styling
 * @param icon ImageVector? optional icon to display
 * @param enabled Boolean whether the button is enabled
 * @param color Color primary button color
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = Color(0xFF4CAF50) // Default green
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Secondary button with outline.
 *
 * @param text String button text
 * @param onClick () -> Unit callback when clicked
 * @param modifier Modifier additional styling
 * @param icon ImageVector? optional icon to display
 * @param enabled Boolean whether the button is enabled
 * @param color Color button outline and text color
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = Color(0xFF4CAF50) // Default green
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,  // Changed from backgroundColor
            contentColor = color
        ),
        border = BorderStroke(2.dp, if (enabled) color else color.copy(alpha = 0.3f))
    ) {
        // Button content here
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) color else color.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = if (enabled) color else color.copy(alpha = 0.3f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Resource counter displaying a labeled value (for tokens, energy, etc.).
 *
 * @param value Int current value of the resource
 * @param label String label for the resource
 * @param icon ImageVector icon representing the resource
 * @param color Color color theme for the resource
 * @param modifier Modifier additional styling
 */
@Composable
fun ResourceCounter(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Loading indicator with optional text.
 *
 * @param text String? optional text to display
 * @param modifier Modifier additional styling
 */
@Composable
fun LoadingIndicator(
    text: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp)
        )

        if (text != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Screen header with title and optional back button.
 *
 * @param title String screen title
 * @param showBackButton Boolean whether to show back button
 * @param onBackClick () -> Unit callback when back button is clicked
 * @param actions @Composable RowScope.() -> Unit optional actions for the header
 * @param modifier Modifier additional styling
 */
@Composable
fun ScreenHeader(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        actions()
    }
}

/**
 * Themed background with gradient and optional pattern overlay.
 *
 * @param modifier Modifier additional styling
 * @param themeColors List<Color> colors to use in the gradient
 * @param addPattern Boolean whether to add a pattern overlay
 */
@Composable
fun ThemedBackground(
    modifier: Modifier = Modifier,
    themeColors: List<Color> = listOf(Color(0xFF1A237E), Color(0xFF303F9F)), // Default blue theme
    addPattern: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = themeColors
                )
            )
    ) {
        if (addPattern) {
            // Here you would add a pattern overlay, like dots or lines
            // For this example, we'll just leave it with the gradient
        }
    }
}

/**
 * Badge indicator for notifications or counters.
 *
 * @param count Int the count to display
 * @param maxVisible Int maximum number to show before displaying "+"
 * @param color Color badge background color
 * @param modifier Modifier additional styling
 */
@Composable
fun Badge(
    count: Int,
    maxVisible: Int = 99,
    color: Color = Color(0xFFE53935), // Default red
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count <= maxVisible) count.toString() else "$maxVisible+",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Card stat display with icon and label.
 *
 * @param value Int stat value
 * @param label String stat label
 * @param icon ImageVector icon for the stat
 * @param color Color color theme for the stat
 * @param modifier Modifier additional styling
 */
@Composable
fun StatDisplay(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF2D2D2D), RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}