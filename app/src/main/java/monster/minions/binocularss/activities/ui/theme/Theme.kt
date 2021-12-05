package monster.minions.binocularss.activities.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = evilMinionPurple,
    primaryVariant = evilMinionPurpleAlt,
    secondary = minionYellow,
    secondaryVariant = minionYellowAlt,
    background = charcoal,
    surface = charcoal,
    error = errorRed,
    onPrimary = black,
    onSecondary = black,
    onBackground = white,
    onSurface = white,
    onError = black,
)

private val LightColorPalette = lightColors(
    primary = minionYellow,
    primaryVariant = minionYellowAlt,
    secondary = evilMinionPurple,
    secondaryVariant = evilMinionPurpleAlt,
    background = white,
    surface = white,
    error = errorRed,
    onPrimary = black,
    onSecondary = black,
    onBackground = black,
    onSurface = black,
    onError = black,
)

@Composable
fun BinoculaRSSTheme(
    theme: String = "System Default",
    content: @Composable () -> Unit,
) {
    val darkTheme = when(theme) {
        "Light Theme" -> false
        "Dark Theme" -> true
        else -> isSystemInDarkTheme()
    }
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = RoundedCorner,
        content = content
    )
}