package com.example.centinela.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MidnightBlue,
    secondary = SteelGray,
    tertiary = CrimsonRed,
    background = DeepOcean,
    surface = DeepOcean,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BoneWhite,
    onSurface = BoneWhite
)

private val LightColorScheme = lightColorScheme(
    primary = MidnightBlue,
    secondary = SteelGray,
    tertiary = CrimsonRed,
    background = BoneWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DeepOcean,
    onSurface = DeepOcean
)

@Composable
fun CentinelaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
