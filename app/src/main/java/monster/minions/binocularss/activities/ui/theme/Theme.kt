package monster.minions.binocularss.activities.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorPalette = darkColorScheme(
//    primary = evilMinionPurple,
//    secondary = evilMinionPurpleAlt,
//    tertiary = minionYellow,
//    // secondaryVariant = minionYellowAlt,
//    background = charcoal,
//    surface = charcoal,
//    error = errorRed,
//    onPrimary = black,
//    onSecondary = black,
//    onBackground = white,
//    onSurface = white,
//    onError = black,
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
    materialYou: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when(theme) {
        "Light Theme" -> false
        "Dark Theme" -> true
        else -> isSystemInDarkTheme()
    }

    val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when(materialYou && dynamic) {
        true -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        false -> { if (darkTheme) DarkColorPalette else LightColorPalette }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}