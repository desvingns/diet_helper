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
    primary = Color(0xFF7044A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECDDFC),
    onPrimaryContainer = Ink,
    secondary = Color(0xFF4D695C),
    secondaryContainer = Color(0xFFD2ECDE),
    tertiary = Color(0xFF806536),
    background = Color(0xFFF8F5FC),
    surface = Color(0xFFF8F5FC),
    surfaceVariant = Color(0xFFEDE6F2),
    onSurface = Ink,
    onSurfaceVariant = Color(0xFF655C70),
    outline = Color(0xFF80738F),
    outlineVariant = Color(0xFFDCD2E5)
)

private val DarkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = Ink,
    primaryContainer = Color(0xFF3B2A53),
    onPrimaryContainer = Color(0xFFEAD9FF),
    secondary = Mint,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF233B32),
    onSecondaryContainer = Mint,
    tertiary = FatSand,
    onTertiary = Ink,
    background = Graphite,
    surface = PlumSurface,
    surfaceVariant = PlumContainer,
    surfaceContainer = PlumSurface,
    surfaceContainerLow = PlumSurface,
    surfaceContainerHigh = PlumContainer,
    surfaceContainerHighest = PlumHigh,
    onSurface = SoftWhite,
    onSurfaceVariant = MutedLilac,
    outline = Color(0xFF8A7C9C),
    outlineVariant = PlumOutline,
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
