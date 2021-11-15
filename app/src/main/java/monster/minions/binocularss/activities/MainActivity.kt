package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao

class  MainActivity : ComponentActivity() {

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    // User Preferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var isFirstRun = true
    private var cacheExpiration = 0L

    // Companion object as this variable needs to be updated from other asynchronous classes.
    companion object {
        var feedGroupText = MutableStateFlow("Empty\n")
    }

    /**
     * The function that is run when the activity is created. This is on app launch in this case.
     * It is also called when the activity is destroyed then recreated. It initializes the main
     * functionality of the application (UI, companion variables, etc.)
     *
     * @param savedInstanceState A bundle of parcelable information that was previously saved.
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

        sharedPref = getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        cacheExpiration = sharedPref.getLong(SettingsActivity.PreferenceKeys.CACHE_EXPIRATION, 0L)

        // Set private variables. This is done here as we cannot initialize objects that require context
        //  before we have context (generated during onCreate)
        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()
        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(cacheExpirationMillis = cacheExpiration) // Set the cache to expire in one hour
            // Different options for cacheExpiration
            // .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
            // .cacheExpirationMillis(0)
            .build()
    }

    // /**
    //  * Saves instance state when activity is destroyed
    //  *
    //  * TODO: We do not need onSaveInstanceState (goes with onRestoreInstanceState) for the current
    //  *  use case. The room database handles
    //  *  it all. I am leaving it here so that whoever uses it has a base for the code
    //  *
    //  * @param outState A bundle of instance state information stored using key-value pairs
    //  */
    // override fun onSaveInstanceState(outState: Bundle) {
    //     Log.d("MainActivity", "onSaveInstanceState called")
    //     super.onSaveInstanceState(outState)
    //     outState.putParcelable(FEED_GROUP_KEY, feedGroup)
    // }

    // /**
    //  * Restores instance state when activity is recreated
    //  *
    //  * TODO: We do not need onRestoreInstanceState (goes with onSaveInstanceState) for the current
    //  *  use case. The room database handles it all. I am leaving it here so that whoever uses it
    //  *  has a base for the code
    //  *
    //  * @param savedInstanceState A bundle of instance state information stored using key-value pairs
    //  */
    // override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    //     Log.d("MainActivity", "onRestoreInstanceState called")
    //     super.onRestoreInstanceState(savedInstanceState)
    //     feedGroup = savedInstanceState.getParcelable<FeedGroup>(FEED_GROUP_KEY)!!

    //     var text = ""
    //     for (feed in feedGroup.feeds) {
    //         text += feed.title
    //         text += "\n"
    //     }
    //     feedGroupText.value = text
    // }

    /**
     * Save the list of user feeds to the Room database (feed-db) for data persistence.
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called before `onDestroy` or any time a "stop" happens. This
     * includes when an app is exited but not closed.
     */
    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
        feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
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

        ///////////////////////////////////////////////////////////////////////////////////////////
        // NOT PERMANENT: If the user does not have any feeds added, add some.
        if (feedGroup.feeds.isNullOrEmpty()) {
            // Add some feeds to the feedGroup
            feedGroup.feeds.add(Feed(source = "https://rss.cbc.ca/lineup/topstories.xml"))
            feedGroup.feeds.add(Feed(source = "https://androidauthority.com/feed"))
            feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
            feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))

            // Inform the user of this
            Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
                .show()
        }
        ///////////////////////////////////////////////////////////////////////////////////////////

        theme = sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        if (!isFirstRun) {
            themeState.value = theme
        }
        isFirstRun = false
        updateText()
    }

    /**
     * Update the text for UI elements
     *
     * TODO this function can be modified to update other UI elements after the
     *  feeds have been fetched as well at which point it should be named updateUI
     *  or something along those lines.
     */
    private fun updateText() {
        var text = ""
        for (feed in feedGroup.feeds) {
            text += feed.title
            text += "\n"
        }
        feedGroupText.value = text
    }

    @Composable
    fun FeedTitles() {
        val text by feedGroupText.collectAsState()
        Text(text = text)
    }

    @Composable
    fun UpdateFeedButton() {
        Button(
            onClick = {
                val viewModel = PullFeed(this, feedGroup)
                viewModel.updateRss(parser)
            }
        ) {
            Text("Update RSS Feeds")
        }
    }

    @Composable
    fun AddFeedButton() {
        Button(
            onClick = {
                val intent = Intent(this, AddFeedActivity::class.java).apply {}
                feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                startActivity(intent)
            }
        ) {
            Text("Add Feed")
        }
    }

    @Composable
    fun ClearFeeds() {
        Button(
            onClick = {
                for (feed in feedGroup.feeds) {
                    feedDao.deleteBySource(feed.source)
                }
                feedGroup.feeds = mutableListOf()
                updateText()
            }
        ) {
            Text("Clear DB")
        }
    }

    @Composable
    fun BookmarksButton() {
        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent(context, BookmarksActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Go to Bookmarks")
        }
    }


    @Composable
    fun SettingsButton() {
        Button(
            onClick = {
                val intent = Intent(this, SettingsActivity::class.java).apply {}
                startActivity(intent)
            }
        ) {
            Text("Settings")
        }
    }

    @Composable
    fun UI() {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(color = MaterialTheme.colors.background) {
            val padding = 16.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeedTitles()
                UpdateFeedButton()
                Spacer(Modifier.size(padding))
                AddFeedButton()
                Spacer(modifier = Modifier.size(padding))
                ClearFeeds()
                Spacer(modifier = Modifier.size(padding))
                BookmarksButton()
                Spacer(modifier = Modifier.size(padding))
                SettingsButton()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        UI()
    }
}

