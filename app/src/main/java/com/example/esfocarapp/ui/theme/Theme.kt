package com.example.esfocarapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresClaros = lightColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF2E7D5B)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFF215724),
    secondary = Color(0xFF005B52)
)

@Composable
fun EsfocarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) ColoresOscuros else ColoresClaros,
        content = content
    )
}
