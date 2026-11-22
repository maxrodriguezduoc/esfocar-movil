package com.example.esfocarapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esfocarapp.util.Tema

// Identidad visual: verde institucional con acentos dorados (Material Design 3)

val VerdeInstitucional = Color(0xFF1F5130)
val VerdeProfundo = Color(0xFF0E3B1E)
val Dorado = Color(0xFFC9A227)

private val Claros = lightColorScheme(
    primary = VerdeInstitucional,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7E5BE),
    onPrimaryContainer = Color(0xFF00210D),
    secondary = Color(0xFF7D6200),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE08A),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF2B5C8A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD2E4FF),
    onTertiaryContainer = Color(0xFF001C37),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF6F8F3),
    onBackground = Color(0xFF181D18),
    surface = Color(0xFFF6F8F3),
    onSurface = Color(0xFF181D18),
    surfaceVariant = Color(0xFFDCE5DA),
    onSurfaceVariant = Color(0xFF414941),
    outline = Color(0xFF717970),
    outlineVariant = Color(0xFFC0C9BE),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F3ED),
    surfaceContainer = Color(0xFFEAEFE7),
    surfaceContainerHigh = Color(0xFFE4E9E1),
    surfaceContainerHighest = Color(0xFFDFE4DC)
)

private val Oscuros = darkColorScheme(
    primary = Color(0xFF9CD4A4),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF1F5130),
    onPrimaryContainer = Color(0xFFB7E5BE),
    secondary = Color(0xFFE9C349),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4500),
    onSecondaryContainer = Color(0xFFFFE08A),
    tertiary = Color(0xFFA0CAFD),
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF0F4470),
    onTertiaryContainer = Color(0xFFD2E4FF),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF0F1510),
    onBackground = Color(0xFFDFE4DC),
    surface = Color(0xFF0F1510),
    onSurface = Color(0xFFDFE4DC),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC0C9BE),
    outline = Color(0xFF8A9389),
    outlineVariant = Color(0xFF414941),
    surfaceContainerLowest = Color(0xFF0A0F0B),
    surfaceContainerLow = Color(0xFF181D18),
    surfaceContainer = Color(0xFF1C211C),
    surfaceContainerHigh = Color(0xFF262B26),
    surfaceContainerHighest = Color(0xFF313631)
)

private val base = Typography()

private val Tipografia = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(lineHeight = 26.sp),
    bodyMedium = base.bodyMedium.copy(lineHeight = 22.sp),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Tema elegido por el usuario (Sistema / Claro / Oscuro). Al cambiarlo, toda la app se redibuja.
object PreferenciaTema {
    var tema by mutableStateOf(Tema.SISTEMA)
}

// Colores de estado (acierto / error / advertencia) que se adaptan a claro y oscuro
object ColoresEstado {
    val exito: Color @Composable get() = if (esOscuro()) Color(0xFF8BD99A) else Color(0xFF1E7B34)
    val exitoContenedor: Color @Composable get() = if (esOscuro()) Color(0xFF1B3D22) else Color(0xFFD5F2DA)
    val advertencia: Color @Composable get() = if (esOscuro()) Color(0xFFFFC66B) else Color(0xFF8A5300)
    val advertenciaContenedor: Color @Composable get() = if (esOscuro()) Color(0xFF4A3300) else Color(0xFFFFE2B6)
}

@Composable
fun esOscuro(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun EsfocarTheme(content: @Composable () -> Unit) {
    val oscuro = when (PreferenciaTema.tema) {
        Tema.CLARO -> false
        Tema.OSCURO -> true
        Tema.SISTEMA -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (oscuro) Oscuros else Claros,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
