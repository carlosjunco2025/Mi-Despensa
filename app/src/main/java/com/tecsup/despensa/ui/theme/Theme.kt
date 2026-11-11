package com.tecsup.despensa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = VerdeMedio,
    onPrimary = Color.White,
    primaryContainer = Menta,
    onPrimaryContainer = VerdeBosque,
    secondary = NaranjaDespensa,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8D2),
    onSecondaryContainer = Color(0xFF7A3E00),
    tertiary = VerdeBrillante,
    onTertiary = Color.White,
    tertiaryContainer = VerdeSuave,
    onTertiaryContainer = VerdeBosque,
    background = Crema,
    onBackground = TextoPrincipal,
    surface = BlancoCalido,
    onSurface = TextoPrincipal,
    surfaceVariant = CremaOscura,
    onSurfaceVariant = TextoSecundario,
    outline = Color(0xFFB9C4BC),
    outlineVariant = Color(0xFFDDE5DF),
    error = RojoSuave,
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = MentaOscura,
    onPrimary = VerdeBosque,
    primaryContainer = VerdeMedio,
    onPrimaryContainer = Menta,
    secondary = Ambar,
    onSecondary = Color(0xFF3A1D00),
    secondaryContainer = Color(0xFF5C3312),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = VerdeSuave,
    onTertiary = VerdeBosque,
    background = NocheFondo,
    onBackground = Color(0xFFE3EFE7),
    surface = NocheSuperficie,
    onSurface = Color(0xFFE3EFE7),
    surfaceVariant = NocheSuperficieAlta,
    onSurfaceVariant = Color(0xFFB6C7BC),
    outline = Color(0xFF4A5D51),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

/**
 * Tema de la app. Se desactiva el color dinamico para que la identidad verde
 * de Despensa se vea igual en todos los dispositivos.
 */
@Composable
fun DespensaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> if (darkTheme) EsquemaOscuro else EsquemaClaro
        darkTheme -> EsquemaOscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}
