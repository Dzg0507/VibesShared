package com.example.vibesshared.ui.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Neon Colors (matching HomeScreen and CreatePostScreen aesthetic)
val NeonGreen = Color(0xFF39FF14)
val NeonPink = Color(0xFFFF00FF)
val NeonBlue = Color(0xFF00FFFF)
val NeonYellow = Color(0xFFFFFF00)
val DarkBackground = Color(0xFF0A0A0A)

// App-Specific Colors (from AppColors sealed class, with corrected values and typo)
object AppColors {
    val ElectricPurple = Color(0xFF8E44AD)
    val NeonPink = Color(0xFFFF69B4) // Adjusted to match your neon theme, but distinct from global NeonPink
    val SunsetOrange = Color(0xFFFFA500)
    val LimeGreen = Color(0xFF32CD32)
    val Teal200 = Color(0xFF008080)
    val VividBlue = Color(0xFF0000FF)
    val DarkBackground = Color(0xFF000000) // Corrected typo (DarkBackgroud -> DarkBackground)
    val GoldenYellow = Color(0xFFDAA520) // Renamed to camelCase for consistency
}

// Additional Theme Colors (Material Design-like or custom for your app)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF9997A1)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Basic Colors (for general use in your app)
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF212121)
val Error = Color(0xFFB00020)
val OnError = Color(0xFFFFFFFF)
val Grey200 = Color(0xFFEEEEEE)
val Grey500 = Color(0xFF9E9E9E)
val Grey700 = Color(0xFF616161)

// Alternative Neon and Cyber Colors (consolidated to avoid conflicts)
val NeonGreenAlt = Color(0xFF00FF41)
val NeonRed = Color(0xFFFF003C)
val NightBlue = Color(0xFF0B1A2F)
val NeonPurple = Color(0xFF9D00FF)
val NeonBlueAlt = Color(0xFF00F6FF)
val NeonYellowAlt = Color(0xFFFFF700)
val CyberDark = Color(0xFF1A1A2E) // Consolidated cyberDark2 and cyberDark3
val GoldenYellowAlt = Color(0xFFFFD700)