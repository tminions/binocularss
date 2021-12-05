package monster.minions.binocularss.activities.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColorScheme(
    primary = evilMinionPurple,
    secondary = evilMinionPurpleAlt,
    tertiary = minionYellow,
    // secondaryVariant = minionYellowAlt,
    background = charcoal,
    surface = charcoal,
    error = errorRed,
    onPrimary = black,
    onSecondary = black,
    onBackground = white,
    onSurface = white,
    onError = black,
)

private val LightColorPalette = lightColorScheme(
    primary = minionYellow,
    // primaryVariant = minionYellowAlt,
    secondary = evilMinionPurple,
    // secondaryVariant = evilMinionPurpleAlt,
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
    val colorScheme = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}