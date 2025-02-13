package com.example.vibesshared.ui.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun triggerHapticFeedback() {
    val haptic = LocalHapticFeedback.current
    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
}