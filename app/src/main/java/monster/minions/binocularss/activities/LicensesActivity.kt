package monster.minions.binocularss.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.activities.ui.theme.paddingSmall

class LicensesActivity : ComponentActivity() {

    // SharedPreferences variables.
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var cacheExpiration = 0L

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            BinoculaRSSTheme(theme = themeState.value) {
                UI()
            }
        }

        // Initialize lateinit variables.
        sharedPref = this.getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        cacheExpiration = sharedPref.getLong(SettingsActivity.PreferenceKeys.CACHE_EXPIRATION, 0L)
    }

    /**
     * Top navigation bar
     */
    @Composable
    fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button icon that goes back one activity.
            IconButton(onClick = { finish() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(paddingSmall))
            // Title of current page.
            Text("Open-Source Licenses", style = MaterialTheme.typography.headlineMedium)
        }
    }

    @ExperimentalMaterial3Api
    @Composable
    fun UI() {
        // Set status bar and nav bar colours.
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = when (theme) {
            "Dark Theme" -> false
            "Light Theme" -> true
            else -> !isSystemInDarkTheme()
        }
        val color = MaterialTheme.colorScheme.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(topBar = { TopBar() }) {
                // Call the library to generate the list of libraries
                LibrariesContainer(
                    Modifier.fillMaxSize()
                )
            }
        }
    }

    @ExperimentalMaterial3Api
    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        UI()
    }
}