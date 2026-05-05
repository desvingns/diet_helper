package com.k.shavrin.diethelper.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

internal val MacroColorBrightGreen = Color(0xFF2E7D32)
internal val MacroColorLightGreen = Color(0xFF81C784)
internal val MacroColorBrightRed = Color(0xFFB71C1C)

internal fun macroProgressColor(actual: Float, min: Float, max: Float): Color {
    val lowerStart = min * 0.5f
    val upperEnd = max * 1.25f
    return when {
        actual < lowerStart -> MacroColorBrightRed
        actual < min -> lerp(MacroColorBrightRed, MacroColorBrightGreen, (actual - lowerStart) / (min - lowerStart))
        actual <= max -> MacroColorBrightGreen
        actual <= upperEnd -> lerp(MacroColorBrightGreen, MacroColorBrightRed, (actual - max) / (upperEnd - max))
        else -> MacroColorBrightRed
    }
}

internal fun caloriesProgressColor(actual: Float, target: Float): Color {
    val warningStart = target * 0.8f
    val dangerEnd = target * 1.2f
    return when {
        actual <= warningStart -> MacroColorBrightGreen
        actual <= target -> lerp(MacroColorBrightGreen, MacroColorLightGreen, (actual - warningStart) / (target - warningStart))
        actual <= dangerEnd -> lerp(MacroColorLightGreen, MacroColorBrightRed, (actual - target) / (dangerEnd - target))
        else -> MacroColorBrightRed
    }
}
