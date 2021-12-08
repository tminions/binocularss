package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.CACHE_EXPIRATION
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.MATERIAL_YOU
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.SETTINGS
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.THEME
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.activities.ui.theme.paddingLarge
import monster.minions.binocularss.activities.ui.theme.paddingSmall
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.ClearData
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.*
import monster.minions.binocularss.operations.ExportData
import monster.minions.binocularss.operations.ImportData
import monster.minions.binocularss.operations.PullFeed
import kotlin.properties.Delegates
import monster.minions.binocularss.ui.PreferenceTitle as PreferenceTitle

/**
 * Settings Activity responsible for the settings UI and saving changes to settings.
 */
class SettingsActivity : ComponentActivity() {

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Room database variables
    private lateinit var dataGateway: DatabaseGateway

    // SharedPreferences variables.
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var materialYou by Delegates.notNull<Boolean>()
    private lateinit var materialYouState: MutableState<Boolean>
    private var cacheExpiration = 0L

    // Global preference keys to retrieve settings from shared preferences.
    object PreferenceKeys {
        const val SETTINGS = "settings"
        const val THEME = "theme"
        const val MATERIAL_YOU = "materialYou"
        const val CACHE_EXPIRATION = "cacheExpiration"
    }

    /**
     * Create method that sets the UI and initializes lateinit variables.
     *
     * @param savedInstanceState Bundle to retrieve saved information from.
     */
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            materialYouState = remember { mutableStateOf(materialYou) }
            BinoculaRSSTheme(theme = themeState.value, materialYou = materialYouState.value) {
                UI()
            }
        }

        // Initialize lateinit variables.
        sharedPref = this.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref.getString(THEME, "System Default").toString()
        materialYou = sharedPref.getBoolean(MATERIAL_YOU, false)
        cacheExpiration = sharedPref.getLong(CACHE_EXPIRATION, 0L)

        dataGateway = DatabaseGateway(context = this)

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

        val feeds: MutableList<Feed> = dataGateway.read()

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
            IconButton(onClick = { finish() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(paddingSmall))
            // Title of current page.
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
        }
    }

    /**
     * Compilation of UI elements in the correct order.
     */
    @ExperimentalMaterial3Api
    @Preview(showBackground = true)
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

        // Surface as a background.
        Surface(color = MaterialTheme.colorScheme.background) {
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

            Surface(color = MaterialTheme.colorScheme.background) {
                Scaffold(topBar = { TopBar() }) {
                    // Column of all the preference items.
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(vertical = paddingLarge)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PreferenceTitle(title = "Appearance")
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
                        // Material You toggle. Only show on android version >= 12.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ToggleItem(
                                title = "Material You Theme",
                                checked = materialYouState.value,
                            ) {
                                materialYouState.value = it
                                // Update the shared preferences.
                                sharedPrefEditor.putBoolean(MATERIAL_YOU, it)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                            }
                        }

                        PreferenceTitle(title = "Preferences")
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

                        var disableClearDatabase by remember {
                            mutableStateOf(feedGroup.feeds.isNullOrEmpty())
                        }

                        ActionItem(
                            title = "Clear database",
                            disabled = disableClearDatabase
                        ) {
                            // Delete each feed in the database
                            for (feed in feedGroup.feeds) {
                                dataGateway.removeFeedBySource(feed.source)
                            }

                            // Set feedGroup.feeds to empty
                            feedGroup.feeds = mutableListOf()
                            disableClearDatabase = true

                            // Update MainActivity UI
                            MainActivity.articleList.value = mutableListOf()
                            MainActivity.bookmarkedArticleList.value = mutableListOf()
                            MainActivity.currentFeedArticles.value = mutableListOf()
                            MainActivity.readArticleList.value = mutableListOf()

                            Toast.makeText(
                                this@SettingsActivity,
                                "Feeds cleared",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        PreferenceTitle(title = "Data")
                        // Item to import a JSON of feed URLs.
                        ActionItem (
                            title = "Import Data"
                        ) {
                            val dataImporter = ImportData()
                            val jsonData = dataImporter.importFile(this@SettingsActivity)
                            val feedUrls = dataImporter.toUrlList(jsonData)
                            // Adds imported feeds, if there are any, to the database
                            if (feedUrls.isNotEmpty()) {
                                feedGroup = FeedGroup()
                                val parser = Parser.Builder()
                                    .context(this@SettingsActivity)
                                    .cacheExpirationMillis(cacheExpirationMillis = cacheExpiration)
                                    .build()
                                for(feedUrl in feedUrls){
                                    feedGroup.feeds.add(Feed(source = feedUrl))
                                }
                                val viewModel = PullFeed(this@SettingsActivity, feedGroup)
                                viewModel.updateRss(parser)
                                dataGateway.addFeeds(feedGroup.feeds)
                                Toast.makeText(
                                    this@SettingsActivity,
                                    "Data Import Successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@SettingsActivity,
                                    "No Saved Feeds",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        // Item to export URLs of subscribed feeds.
                        ActionItem (
                            title = "Export Data"
                        ) {
                            val dataExporter = ExportData()
                            val jsonData = dataExporter.toJson(feedGroup.feeds)
                            // Checks if there are any feeds to export
                            if (jsonData != "[]") {
                                if (dataExporter.exportFile(this@SettingsActivity, jsonData)) {
                                    Toast.makeText(
                                        this@SettingsActivity,
                                        "Data Export Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@SettingsActivity,
                                        "Data Export Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@SettingsActivity,
                                    "No Saved Feeds",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        //Item to clear the JSON of feeds
                        ActionItem (
                            title = "Clear Data"
                        ) {
                            val dataClearer = ClearData()
                            dataClearer.clearData(this@SettingsActivity)
                            Toast.makeText(
                                this@SettingsActivity,
                                "Data Cleared",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        PreferenceTitle(title = "Support")
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

                        PreferenceTitle(title = "About")
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
                        // Item that displays information on all the open source libraries used.
                        ActionItem(title = "Open-Source Licenses") {
                            val intent = Intent(
                                this@SettingsActivity,
                                LicensesActivity::class.java
                            ).apply {}
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}
