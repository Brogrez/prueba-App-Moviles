package cl.duoc.vitairosm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF1F6F6B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBDEBE6),
    onPrimaryContainer = Color(0xFF00201E),
    secondary = Color(0xFF4A6362),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E6),
    onSecondaryContainer = Color(0xFF051F1F),
    tertiary = Color(0xFF4D5F7D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF071B36),
    background = Color(0xFFF6FAF9),
    onBackground = Color(0xFF161D1C),
    surface = Color(0xFFF6FAF9),
    onSurface = Color(0xFF161D1C)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF82D5CE),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF00504B),
    onPrimaryContainer = Color(0xFFBDEBE6),
    secondary = Color(0xFFB0CCCB),
    onSecondary = Color(0xFF1B3534),
    secondaryContainer = Color(0xFF324B4A),
    onSecondaryContainer = Color(0xFFCCE8E6),
    tertiary = Color(0xFFB4C7E8),
    onTertiary = Color(0xFF1E314D),
    tertiaryContainer = Color(0xFF354764),
    onTertiaryContainer = Color(0xFFD5E3FF),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFDDE4E3),
    surface = Color(0xFF0E1514),
    onSurface = Color(0xFFDDE4E3)
)

@Composable
fun VitairosTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography(),
        content = content
    )
}
