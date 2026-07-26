package com.veroflow.verostay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VeroPrimary = Color(0xFFE85D4E)
private val VeroSecondary = Color(0xFF1F6F78)

private val LightColors = lightColorScheme(
    primary = VeroPrimary,
    secondary = VeroSecondary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE)
)

private val DarkColors = darkColorScheme(
    primary = VeroPrimary,
    secondary = VeroSecondary,
    background = Color(0xFF141218),
    surface = Color(0xFF1D1B20)
)

@Composable
fun VeroStayTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
