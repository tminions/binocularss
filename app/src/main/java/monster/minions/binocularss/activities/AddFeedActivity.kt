package monster.minions.binocularss.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao

// TODO make sure to check that this is an RSS feed (probably in pull feed or something of
//  the sort and send a toast to the user if it is not. Try and check this when initially
//  adding maybe? Basically deeper checking than just is this a URL so we don't encounter
//  errors later. Or we could leave it and have PullFeed silently remove a feed if it is
//  invalid like it currently does.
class AddFeedActivity : ComponentActivity() {

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    private var text = mutableStateOf("")

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
            BinoculaRSSTheme {
                UI()
            }
        }

        // Set private variables. This is done here as we cannot initialize objects that require context
        //  before we have context (generated during onCreate)
        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()
        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(60L * 60L * 100L) // Set the cache to expire in one hour
            // Different options for cacheExpiration
            // .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
            // .cacheExpirationMillis(0)
            .build()

        // Handle a link being shared to the application
        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            setTextView(intent)
        }
    }

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
        Log.d("AddFeedActivity", "onStop called")
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
        Log.d("AddFeedActivity", "onResume called")
        feedGroup.feeds = feedDao.getAll()
    }

    /**
     * Set the value of the text view from an extra stored in the intent
     *
     * @param intent The intent passed to the activity upon launch.
     */
    private fun setTextView(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            text.value = it
        }
    }

    /**
     * Function to append https:// if the string does not have the prefix http:// or https://.
     *
     * @param url String to have https:// appended to it.
     * @return url with https:// possibly appended to it.
     */
    private fun addHttps(url: String): String {
        return when {
            url.startsWith("https://") || url.startsWith("http://") -> url
            else -> "https://$url"
        }
    }

    /**
     * Add a feed with the given source url to the feedGroup if the source url is valid.
     */
    private fun submit() {
        // Add https:// or http:// to the front of the url if not present
        val url = addHttps(text.value)

        // If the url is valid ...
        if (Patterns.WEB_URL.matcher(url).matches()) {
            // Check if the feed is already in the feedGroup
            val feedToAdd = Feed(source = url)
            var inFeedGroup = false
            for (feed in feedGroup.feeds) {
                if (feedToAdd == feed) {
                    inFeedGroup = true
                    Toast.makeText(
                        this@AddFeedActivity,
                        "You've already added that RSS feed",
                        Toast.LENGTH_SHORT
                    ).show()
                    continue
                }
            }

            // Add feed and update feeds if the feed is not in the feedGroup
            if (!inFeedGroup) {
                feedGroup.feeds.add(Feed(source = url))
                val viewModel = PullFeed(this, feedGroup)
                viewModel.updateRss(parser)
                finish()
            }
        } else {
            Toast.makeText(this@AddFeedActivity, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun FeedTextField() {
        val textState = remember { mutableStateOf(TextFieldValue()) }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = textState.value,
            placeholder = { Text("Enter Feed URL") },
            onValueChange = {
                textState.value = it
                text = mutableStateOf(textState.value.text)
            },
            singleLine = true,
            maxLines = 1,
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            )
        )
    }

    @Composable
    fun UI() {
        Surface(color = MaterialTheme.colors.background) {
            val padding = 16.dp

            Column(
                modifier = Modifier.padding(padding),
                horizontalAlignment = Alignment.Start,
            ) {
                FeedTextField()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        BinoculaRSSTheme {
            UI()
        }
    }
}