package com.k.shavrin.diethelper.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Lime40
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Lime80
)

@Composable
fun DietHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DietHelperTypography,
        content = content
    )
}
