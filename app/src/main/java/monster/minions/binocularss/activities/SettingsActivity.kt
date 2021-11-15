package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.CACHE_EXPIRATION
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.SETTINGS
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.THEME
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.*
import monster.minions.binocularss.ui.PreferenceTitle as PreferenceTitle1

/**
 * Settings Activity responsible for the settings UI and saving changes to settings.
 */
class SettingsActivity : ComponentActivity() {

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Room database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    // SharedPreferences variables.
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var cacheExpiration = 0L

    // Global preference keys to retrieve settings from shared preferences.
    object PreferenceKeys {
        const val SETTINGS = "settings"
        const val THEME = "theme"
        const val CACHE_EXPIRATION = "cacheExpiration"
    }

    /**
     * Create method that sets the UI and initializes lateinit variables.
     *
     * @param savedInstanceState Bundle to retrieve saved information from.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            BinoculaRSSTheme(
                theme = themeState.value
            ) {
                UI()
            }
        }

        // Initialize lateinit variables.
        sharedPref = this.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref.getString(THEME, "System Default").toString()
        cacheExpiration = sharedPref.getLong(CACHE_EXPIRATION, 0L)

        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()
    }

    /**
     * Gets the list of user feeds from the Room database (feed-db).
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called after `onCreate` or any time a "resume" happens. This includes
     * the app being opened after the app is exited but not closed.
     */
    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")

        val feeds: MutableList<Feed> = feedDao.getAll()

        feedGroup.feeds = feeds
    }

    /**
     * Open link with in the user's default browser.
     */
    private fun openLink(link: String) {
        if (link != "") {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(link)
            startActivity(intent)
        }
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
            IconButton(onClick = {
                finish()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(4.dp))
            // Title of current page.
            Text("Settings", style = MaterialTheme.typography.h5)
        }
    }


    /**
     * Compilation of UI elements in the correct order.
     */
    @Preview(showBackground = true)
    @Composable
    fun UI() {
        // Set status bar and nav bar colours.
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        // Surface as a background.
        Surface(color = MaterialTheme.colors.background) {
            val padding = 16.dp

            var themeSubtitle by remember { mutableStateOf(theme) }
            var cacheExpirationString = ""
            when (cacheExpiration) {
                24L * 60L * 60L * 1000L -> cacheExpirationString = "24 Hours"
                12L * 60L * 60L * 1000L -> cacheExpirationString = "12 Hours"
                6L * 60L * 60L * 1000L -> cacheExpirationString = "6 Hours"
                60L * 60L * 1000L -> cacheExpirationString = "1 Hour"
                30L * 60L * 1000L -> cacheExpirationString = "30 Minutes"
                15L * 60L * 1000L -> cacheExpirationString = "15 Minutes"
                0L -> cacheExpirationString = "Off"
            }
            var cacheSubtitle by remember { mutableStateOf(cacheExpirationString) }

            Surface(color = MaterialTheme.colors.background) {
                Scaffold(topBar = { TopBar() }) {
                    // Column of all the preference items.
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(vertical = padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PreferenceTitle1(title = "Appearance")
                        // Theme selector.
                        MultipleOptionItem(
                            title = "Theme",
                            subtitle = themeSubtitle,
                            radioOptions = listOf("Light Theme", "Dark Theme", "System Default"),
                            initialItem = themeSubtitle,
                            onSelect = {
                                // Update the subtitle and theme of the current activity.
                                themeSubtitle = it
                                themeState.value = it
                                // Update the shared preferences.
                                sharedPrefEditor.putString(THEME, it)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                            }
                        )
                        // Material You toggle.
                        ToggleItem(
                            title = "Material You Theme",
                            checked = false, // TODO get this value from shared preferences
                            onToggle = { println(it)/* TODO set shared preferences here */ }
                        )
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle1(title = "Preferences")
                        // Cache expiration time selector.
                        MultipleOptionItem(
                            title = "Cache Expiration Time",
                            subtitle = cacheSubtitle,
                            radioOptions = listOf(
                                "24 Hours",
                                "12 Hours",
                                "6 Hours",
                                "1 Hour",
                                "30 Minutes",
                                "15 Minutes",
                                "Off"
                            ),
                            initialItem = cacheSubtitle,
                            onSelect = {
                                var cacheExpiration = 0L
                                when (it) {
                                    "24 Hours" -> cacheExpiration = 24L * 60L * 60L * 1000L
                                    "12 Hours" -> cacheExpiration = 12L * 60L * 60L * 1000L
                                    "6 Hours" -> cacheExpiration = 6L * 60L * 60L * 1000L
                                    "1 Hour" -> cacheExpiration = 60L * 60L * 1000L
                                    "30 Minutes" -> cacheExpiration = 30L * 60L * 1000L
                                    "15 Minutes" -> cacheExpiration = 15L * 60L * 1000L
                                    "Off" -> cacheExpiration = 0L
                                }
                                // Update the subtitle.
                                cacheSubtitle = it
                                // Update the shared preferences.
                                sharedPrefEditor.putLong(CACHE_EXPIRATION, cacheExpiration)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                            }
                        )

                        val disableClearDatabase by remember {
                            mutableStateOf(feedGroup.feeds.isNullOrEmpty())
                        }

                        ActionItem(
                            title = "Clear database",
                            disabled = disableClearDatabase
                        ) {
                            // Delete each feed in the database
                            for (feed in feedGroup.feeds) {
                                feedDao.deleteBySource(feed.source)
                            }

                            // Set feedGroup.feeds to empty
                            feedGroup.feeds = mutableListOf()

                            // Update MainActivity UI
                            MainActivity.articleList.value = mutableListOf()
                            MainActivity.bookmarkedArticleList.value = mutableListOf()

                            Toast.makeText(
                                this@SettingsActivity,
                                "Feeds cleared",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle1(title = "Support")
                        // Email item
                        EmailItem(
                            context = this@SettingsActivity,
                            title = "Contact",
                            email = "hisbaan@gmail.com"
                        )
                        // Item that links to feedback form.
                        LinkItem(
                            title = "Feedback",
                            link = "google form or something"
                        ) {
                            openLink(it)
                        }
                        // Item that links to github issues page.
                        LinkItem(
                            title = "Bug Report",
                            link = "https://github.com/tminions/binocularss/issues/new"
                        ) {
                            openLink(it)
                        }
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle1(title = "About")
                        // Item that links to github source code page.
                        LinkItem(
                            title = "GitHub",
                            subtitle = "github.com/tminions/binocularss",
                            link = "https://github.com/tminions/binocularss"
                        ) {
                            openLink(it)
                        }
                        // Item that links to github releases.
                        LinkItem(
                            title = "Version",
                            subtitle = "1.0",
                            link = "https://github.com/tminions/binocularss/releases"
                        ) {
                            openLink(it)
                        }
                        // Popup information on all the open source libraries used.
                        InformationPopupItem(title = "Open Source Libraries") {
                            Text(
                                "RSS-Parser",
                                Modifier
                                    .padding(top = 4.dp)
                                    .clickable { openLink("https://github.com/prof18/RSS-Parser") })
                            Text(
                                "Coil",
                                Modifier
                                    .padding(top = 4.dp)
                                    .clickable { openLink("https://github.com/coil-kt/coil") })
                            Text(
                                "Room",
                                Modifier
                                    .padding(top = 4.dp)
                                    .clickable { openLink("https://developer.android.com/training/data-storage/room") })
                            Text(
                                "Material.io Theming Information",
                                Modifier
                                    .padding(top = 4.dp)
                                    .clickable { openLink("https://material.io/design/color/the-color-system.html#color-theme-creation") })
                            // TODO finish adding libraries then size the popup accordinly
                        }
                    }
                }
            }
        }
    }
}
