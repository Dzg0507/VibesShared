package com.example.vibesshared.ui.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Manages theming and visual styles for the trivia game
 */
class TriviaThemeManager {
    companion object {
        // Theme options
        const val THEME_DEFAULT = "default"
        const val THEME_CYBERPUNK = "cyberpunk"
        const val THEME_SPACE = "space"
        const val THEME_FANTASY = "fantasy"

        // Font scales
        const val FONT_SCALE_SMALL = 0.85f
        const val FONT_SCALE_NORMAL = 1.0f
        const val FONT_SCALE_LARGE = 1.15f
        const val FONT_SCALE_EXTRA_LARGE = 1.3f
    }

    // Colors for the default theme
    object DefaultColors {
        val primary = Color(0xFF3F51B5)
        val onPrimary = Color.White
        val primaryContainer = Color(0xFF5C6BC0)
        val onPrimaryContainer = Color.White

        val secondary = Color(0xFFE91E63)
        val onSecondary = Color.White
        val secondaryContainer = Color(0xFFF06292)
        val onSecondaryContainer = Color.White

        val tertiary = Color(0xFF00BCD4)
        val onTertiary = Color.Black
        val tertiaryContainer = Color(0xFF4DD0E1)
        val onTertiaryContainer = Color.Black

        val background = Color(0xFF121212)
        val onBackground = Color.White
        val surface = Color(0xFF1E1E1E)
        val onSurface = Color.White

        val error = Color(0xFFCF6679)
        val onError = Color.Black
        val errorContainer = Color(0xFFF5CAD0)
        val onErrorContainer = Color.Black
    }

    // Colors for the cyberpunk theme
    object CyberpunkColors {
        val primary = Color(0xFF00FFFF)
        val onPrimary = Color.Black
        val primaryContainer = Color(0xFF0AFFFF)
        val onPrimaryContainer = Color.Black

        val secondary = Color(0xFFFF00FF)
        val onSecondary = Color.Black
        val secondaryContainer = Color(0xFFFF80FF)
        val onSecondaryContainer = Color.Black

        val tertiary = Color(0xFFFFFF00)
        val onTertiary = Color.Black
        val tertiaryContainer = Color(0xFFFFFF80)
        val onTertiaryContainer = Color.Black

        val background = Color(0xFF090422)
        val onBackground = Color(0xFFFFFFFF)
        val surface = Color(0xFF1E0F3D)
        val onSurface = Color(0xFFFFFFFF)

        val error = Color(0xFFFF5252)
        val onError = Color.Black
        val errorContainer = Color(0xFFFFCDCD)
        val onErrorContainer = Color.Black
    }

    // Colors for the space theme
    object SpaceColors {
        val primary = Color(0xFF4E78C5)
        val onPrimary = Color.White
        val primaryContainer = Color(0xFF7595D2)
        val onPrimaryContainer = Color.White

        val secondary = Color(0xFFAB47BC)
        val onSecondary = Color.White
        val secondaryContainer = Color(0xFFCE93D8)
        val onSecondaryContainer = Color.Black

        val tertiary = Color(0xFF26A69A)
        val onTertiary = Color.White
        val tertiaryContainer = Color(0xFF80CBC4)
        val onTertiaryContainer = Color.Black

        val background = Color(0xFF000A17)
        val onBackground = Color.White
        val surface = Color(0xFF001529)
        val onSurface = Color.White

        val error = Color(0xFFFF5252)
        val onError = Color.White
        val errorContainer = Color(0xFFE57373)
        val onErrorContainer = Color.Black
    }

    // Colors for the fantasy theme
    object FantasyColors {
        val primary = Color(0xFF8D6E63)
        val onPrimary = Color.White
        val primaryContainer = Color(0xFFBCAAA4)
        val onPrimaryContainer = Color.Black

        val secondary = Color(0xFF795548)
        val onSecondary = Color.White
        val secondaryContainer = Color(0xFFA1887F)
        val onSecondaryContainer = Color.Black

        val tertiary = Color(0xFF6D4C41)
        val onTertiary = Color.White
        val tertiaryContainer = Color(0xFF8D6E63)
        val onTertiaryContainer = Color.White

        val background = Color(0xFF251605)
        val onBackground = Color(0xFFECDBC5)
        val surface = Color(0xFF3E2411)
        val onSurface = Color(0xFFECDBC5)

        val error = Color(0xFFC62828)
        val onError = Color.White
        val errorContainer = Color(0xFFEF9A9A)
        val onErrorContainer = Color.Black
    }

    /**
     * Get color scheme based on the selected theme
     */
    @Composable
    fun getColorScheme(theme: String = THEME_DEFAULT, isDark: Boolean = isSystemInDarkTheme()): ColorScheme {
        return when (theme) {
            THEME_CYBERPUNK -> darkColorScheme(
                primary = CyberpunkColors.primary,
                onPrimary = CyberpunkColors.onPrimary,
                primaryContainer = CyberpunkColors.primaryContainer,
                onPrimaryContainer = CyberpunkColors.onPrimaryContainer,
                secondary = CyberpunkColors.secondary,
                onSecondary = CyberpunkColors.onSecondary,
                secondaryContainer = CyberpunkColors.secondaryContainer,
                onSecondaryContainer = CyberpunkColors.onSecondaryContainer,
                tertiary = CyberpunkColors.tertiary,
                onTertiary = CyberpunkColors.onTertiary,
                tertiaryContainer = CyberpunkColors.tertiaryContainer,
                onTertiaryContainer = CyberpunkColors.onTertiaryContainer,
                background = CyberpunkColors.background,
                onBackground = CyberpunkColors.onBackground,
                surface = CyberpunkColors.surface,
                onSurface = CyberpunkColors.onSurface,
                error = CyberpunkColors.error,
                onError = CyberpunkColors.onError,
                errorContainer = CyberpunkColors.errorContainer,
                onErrorContainer = CyberpunkColors.onErrorContainer
            )
            THEME_SPACE -> darkColorScheme(
                primary = SpaceColors.primary,
                onPrimary = SpaceColors.onPrimary,
                primaryContainer = SpaceColors.primaryContainer,
                onPrimaryContainer = SpaceColors.onPrimaryContainer,
                secondary = SpaceColors.secondary,
                onSecondary = SpaceColors.onSecondary,
                secondaryContainer = SpaceColors.secondaryContainer,
                onSecondaryContainer = SpaceColors.onSecondaryContainer,
                tertiary = SpaceColors.tertiary,
                onTertiary = SpaceColors.onTertiary,
                tertiaryContainer = SpaceColors.tertiaryContainer,
                onTertiaryContainer = SpaceColors.onTertiaryContainer,
                background = SpaceColors.background,
                onBackground = SpaceColors.onBackground,
                surface = SpaceColors.surface,
                onSurface = SpaceColors.onSurface,
                error = SpaceColors.error,
                onError = SpaceColors.onError,
                errorContainer = SpaceColors.errorContainer,
                onErrorContainer = SpaceColors.onErrorContainer
            )
            THEME_FANTASY -> darkColorScheme(
                primary = FantasyColors.primary,
                onPrimary = FantasyColors.onPrimary,
                primaryContainer = FantasyColors.primaryContainer,
                onPrimaryContainer = FantasyColors.onPrimaryContainer,
                secondary = FantasyColors.secondary,
                onSecondary = FantasyColors.onSecondary,
                secondaryContainer = FantasyColors.secondaryContainer,
                onSecondaryContainer = FantasyColors.onSecondaryContainer,
                tertiary = FantasyColors.tertiary,
                onTertiary = FantasyColors.onTertiary,
                tertiaryContainer = FantasyColors.tertiaryContainer,
                onTertiaryContainer = FantasyColors.onTertiaryContainer,
                background = FantasyColors.background,
                onBackground = FantasyColors.onBackground,
                surface = FantasyColors.surface,
                onSurface = FantasyColors.onSurface,
                error = FantasyColors.error,
                onError = FantasyColors.onError,
                errorContainer = FantasyColors.errorContainer,
                onErrorContainer = FantasyColors.onErrorContainer
            )
            else -> darkColorScheme(
                primary = DefaultColors.primary,
                onPrimary = DefaultColors.onPrimary,
                primaryContainer = DefaultColors.primaryContainer,
                onPrimaryContainer = DefaultColors.onPrimaryContainer,
                secondary = DefaultColors.secondary,
                onSecondary = DefaultColors.onSecondary,
                secondaryContainer = DefaultColors.secondaryContainer,
                onSecondaryContainer = DefaultColors.onSecondaryContainer,
                tertiary = DefaultColors.tertiary,
                onTertiary = DefaultColors.onTertiary,
                tertiaryContainer = DefaultColors.tertiaryContainer,
                onTertiaryContainer = DefaultColors.onTertiaryContainer,
                background = DefaultColors.background,
                onBackground = DefaultColors.onBackground,
                surface = DefaultColors.surface,
                onSurface = DefaultColors.onSurface,
                error = DefaultColors.error,
                onError = DefaultColors.onError,
                errorContainer = DefaultColors.errorContainer,
                onErrorContainer = DefaultColors.onErrorContainer
            )
        }
    }

    /**
     * Get typography based on the selected theme and font scale
     */
    @Composable
    fun getTypography(theme: String = THEME_DEFAULT, fontScale: Float = FONT_SCALE_NORMAL): Typography {
        val baseTypography = Typography()

        // Get font family based on theme
        val fontFamily = when (theme) {
            THEME_CYBERPUNK -> FontFamily.Monospace
            THEME_SPACE -> FontFamily.SansSerif
            THEME_FANTASY -> FontFamily.Serif
            else -> FontFamily.Default
        }

        // Apply font scale to all text styles
        return Typography(
            displayLarge = baseTypography.displayLarge.scaled(fontScale, fontFamily),
            displayMedium = baseTypography.displayMedium.scaled(fontScale, fontFamily),
            displaySmall = baseTypography.displaySmall.scaled(fontScale, fontFamily),
            headlineLarge = baseTypography.headlineLarge.scaled(fontScale, fontFamily),
            headlineMedium = baseTypography.headlineMedium.scaled(fontScale, fontFamily),
            headlineSmall = baseTypography.headlineSmall.scaled(fontScale, fontFamily),
            titleLarge = baseTypography.titleLarge.scaled(fontScale, fontFamily),
            titleMedium = baseTypography.titleMedium.scaled(fontScale, fontFamily),
            titleSmall = baseTypography.titleSmall.scaled(fontScale, fontFamily),
            bodyLarge = baseTypography.bodyLarge.scaled(fontScale, fontFamily),
            bodyMedium = baseTypography.bodyMedium.scaled(fontScale, fontFamily),
            bodySmall = baseTypography.bodySmall.scaled(fontScale, fontFamily),
            labelLarge = baseTypography.labelLarge.scaled(fontScale, fontFamily),
            labelMedium = baseTypography.labelMedium.scaled(fontScale, fontFamily),
            labelSmall = baseTypography.labelSmall.scaled(fontScale, fontFamily)
        )
    }

    /**
     * Helper extension function to scale a TextStyle by a factor and apply a font family
     */
    private fun TextStyle.scaled(factor: Float, fontFamily: FontFamily? = null): TextStyle {
        return if (fontFamily != null) {
            this.copy(
                fontSize = this.fontSize * factor,
                fontFamily = fontFamily
            )
        } else {
            this.copy(
                fontSize = this.fontSize * factor
            )
        }
    }

    /**
     * Get shapes based on the selected theme
     */
    fun getShapes(theme: String = THEME_DEFAULT): Shapes {
        return when (theme) {
            THEME_CYBERPUNK -> Shapes(
                small = RoundedCornerShape(0.dp), // Sharp corners
                medium = RoundedCornerShape(0.dp),
                large = RoundedCornerShape(0.dp)
            )
            THEME_SPACE -> Shapes(
                small = RoundedCornerShape(16.dp), // Very rounded
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(24.dp)
            )
            THEME_FANTASY -> Shapes(
                small = RoundedCornerShape(8.dp, 0.dp, 8.dp, 0.dp), // Scrolls
                medium = RoundedCornerShape(12.dp, 0.dp, 12.dp, 0.dp),
                large = RoundedCornerShape(16.dp, 0.dp, 16.dp, 0.dp)
            )
            else -> Shapes(
                small = RoundedCornerShape(4.dp),
                medium = RoundedCornerShape(8.dp),
                large = RoundedCornerShape(12.dp)
            )
        }
    }

    /**
     * Difficulty colors for different themes
     */
    fun getDifficultyColor(difficulty: String, theme: String = THEME_DEFAULT): Color {
        return when (theme) {
            THEME_CYBERPUNK -> when (difficulty.lowercase()) {
                "easy" -> Color(0xFF00FF00)
                "medium" -> Color(0xFF00FFFF)
                "hard" -> Color(0xFFFF00FF)
                "adept" -> Color(0xFFFF8000)
                "expert" -> Color(0xFFFF0080)
                "legendary" -> Color(0xFFFF0000)
                "multiverse_breaker" -> Color(0xFFFFFFFF)
                else -> Color(0xFF00FF00)
            }
            THEME_SPACE -> when (difficulty.lowercase()) {
                "easy" -> Color(0xFF64B5F6)
                "medium" -> Color(0xFF42A5F5)
                "hard" -> Color(0xFF2196F3)
                "adept" -> Color(0xFF1E88E5)
                "expert" -> Color(0xFF1976D2)
                "legendary" -> Color(0xFF1565C0)
                "multiverse_breaker" -> Color(0xFF0D47A1)
                else -> Color(0xFF64B5F6)
            }
            THEME_FANTASY -> when (difficulty.lowercase()) {
                "easy" -> Color(0xFFD7CCC8)
                "medium" -> Color(0xFFBCAAA4)
                "hard" -> Color(0xFFA1887F)
                "adept" -> Color(0xFF8D6E63)
                "expert" -> Color(0xFF795548)
                "legendary" -> Color(0xFF6D4C41)
                "multiverse_breaker" -> Color(0xFF5D4037)
                else -> Color(0xFFBCAAA4)
            }
            else -> when (difficulty.lowercase()) {
                "easy" -> Color(0xFF4CAF50)
                "medium" -> Color(0xFF2196F3)
                "hard" -> Color(0xFFFF9800)
                "adept" -> Color(0xFF9575CD)
                "expert" -> Color(0xFFEF5350)
                "legendary" -> Color(0xFFE91E63)
                "multiverse_breaker" -> Color(0xFF9C27B0)
                else -> Color(0xFF4CAF50)
            }
        }
    }

    /**
     * Power-up colors for different themes
     */
    fun getPowerUpColor(powerUpType: String, theme: String = THEME_DEFAULT): Color {
        return when (theme) {
            THEME_CYBERPUNK -> when (powerUpType.lowercase()) {
                "time_freeze" -> Color(0xFF00FFFF)
                "time_boost" -> Color(0xFF00FF00)
                "fifty_fifty" -> Color(0xFFFFFF00)
                "correct_answer" -> Color(0xFFFF0080)
                "hint" -> Color(0xFFFF8000)
                "skip_question" -> Color(0xFF8000FF)
                "double_points" -> Color(0xFFFF00FF)
                else -> Color(0xFF00FFFF)
            }
            THEME_SPACE -> when (powerUpType.lowercase()) {
                "time_freeze" -> Color(0xFF64B5F6)
                "time_boost" -> Color(0xFF81C784)
                "fifty_fifty" -> Color(0xFFFFD54F)
                "correct_answer" -> Color(0xFFF06292)
                "hint" -> Color(0xFFFFB74D)
                "skip_question" -> Color(0xFFAB47BC)
                "double_points" -> Color(0xFFFF7043)
                else -> Color(0xFF64B5F6)
            }
            THEME_FANTASY -> when (powerUpType.lowercase()) {
                "time_freeze" -> Color(0xFFBCAAA4)
                "time_boost" -> Color(0xFFA5D6A7)
                "fifty_fifty" -> Color(0xFFFFE082)
                "correct_answer" -> Color(0xFFF48FB1)
                "hint" -> Color(0xFFFFCC80)
                "skip_question" -> Color(0xFFCE93D8)
                "double_points" -> Color(0xFFFFAB91)
                else -> Color(0xFFBCAAA4)
            }
            else -> when (powerUpType.lowercase()) {
                "time_freeze" -> Color(0xFF29B6F6)
                "time_boost" -> Color(0xFF00E676)
                "fifty_fifty" -> Color(0xFFFFEB3B)
                "correct_answer" -> Color(0xFFFF4081)
                "hint" -> Color(0xFFFFD54F)
                "skip_question" -> Color(0xFF7C4DFF)
                "double_points" -> Color(0xFFFF5722)
                else -> Color(0xFF29B6F6)
            }
        }
    }

    /**
     * Background gradients for different themes
     */
    fun getBackgroundGradient(theme: String = THEME_DEFAULT): List<Color> {
        return when (theme) {
            THEME_CYBERPUNK -> listOf(
                Color(0xFF000000),
                Color(0xFF090422),
                Color(0xFF130844)
            )
            THEME_SPACE -> listOf(
                Color(0xFF000000),
                Color(0xFF000A17),
                Color(0xFF001529)
            )
            THEME_FANTASY -> listOf(
                Color(0xFF1A0D00),
                Color(0xFF251605),
                Color(0xFF3E2411)
            )
            else -> listOf(
                Color(0xFF000000),
                Color(0xFF0D0026),
                Color(0xFF26004D)
            )
        }
    }
}

/**
 * Composable that provides the TriviaTheme with proper theming
 */
@Composable
fun TriviaTheme(
    theme: String = TriviaThemeManager.THEME_DEFAULT,
    fontScale: Float = TriviaThemeManager.FONT_SCALE_NORMAL,
    content: @Composable () -> Unit
) {
    val themeManager = remember { TriviaThemeManager() }
    val colorScheme = themeManager.getColorScheme(theme)
    val typography = themeManager.getTypography(theme, fontScale)
    val shapes = themeManager.getShapes(theme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}