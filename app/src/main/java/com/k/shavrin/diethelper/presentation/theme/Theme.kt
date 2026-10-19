package com.k.shavrin.diethelper.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF34664D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCEECD8),
    onPrimaryContainer = Ink,
    secondary = Color(0xFF526556),
    secondaryContainer = Color(0xFFDCE9DC),
    tertiary = Color(0xFF806536),
    background = Color(0xFFF5F6F0),
    surface = Color(0xFFF5F6F0),
    surfaceVariant = Color(0xFFE6EBE2),
    onSurface = Ink,
    onSurfaceVariant = Color(0xFF526056),
    outline = Color(0xFF738277),
    outlineVariant = Color(0xFFD2DCD1)
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Ink,
    primaryContainer = Color(0xFF304D3D),
    onPrimaryContainer = Mint,
    secondary = Mint,
    onSecondary = Ink,
    secondaryContainer = ForestContainer,
    onSecondaryContainer = Mint,
    tertiary = FatSand,
    onTertiary = Ink,
    background = Graphite,
    surface = ForestSurface,
    surfaceVariant = ForestContainer,
    surfaceContainer = ForestSurface,
    surfaceContainerLow = ForestSurface,
    surfaceContainerHigh = ForestContainer,
    surfaceContainerHighest = Color(0xFF2D3A32),
    onSurface = SoftWhite,
    onSurfaceVariant = MutedSage,
    outline = Color(0xFF74877A),
    outlineVariant = Color(0xFF34463A),
    error = Color(0xFFFFB4AB)
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
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
