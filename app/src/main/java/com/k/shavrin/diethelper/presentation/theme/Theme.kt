package com.k.shavrin.diethelper.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Lime40
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCEC1FF),
    onPrimary = Color(0xFF302454),
    primaryContainer = Color(0xFF423460),
    secondary = Color(0xFFB8F3D4),
    secondaryContainer = Color(0xFF2F493D),
    tertiary = Color(0xFFE9CC92),
    background = Color(0xFF0F0D13),
    surface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFF2B2930),
    onSurface = Color(0xFFE6E0E9),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

@Composable
fun DietHelperTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DietHelperTypography,
        content = content
    )
}
