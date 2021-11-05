package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme

class MainActivity : ComponentActivity() {

    // Variables that can be accessed from other classes by calling `MainActivity.<variable>`.
    companion object {
        // Global FeedGroup object
        var feedGroup: FeedGroup = FeedGroup()

        // Parser variable using lateinit because we want to get the context
        lateinit var parser: Parser

        // Setup parser
        private fun setParser(context: Context) {
            parser = Parser.Builder()
                .context(context)
                // .charset(Charset.forName("ISO-8859-7")) // Default is UTF-8
//                .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
                // .cacheExpirationMillis(0)
                .build()


        }

        // Setup Room database
        private lateinit var db: RoomDatabase
        private lateinit var feedDao: FeedDao
        private fun setDb(context: Context) {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "feed-db"
            ).allowMainThreadQueries().build()
            feedDao = (db as AppDatabase).feedDao()
        }

        // Local variables
        var feedGroupText = MutableStateFlow("Empty\n")
        // Key for accessing feed group in onSaveInstanceState
        // const val FEED_GROUP_KEY = "feedGroup"
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

        // TODO this code goes with onSaveInstanceState and onRestoreInstanceState
        // if (savedInstanceState != null) {
        // feedGroup = savedInstanceState.getParcelable<FeedGroup>("feedGroup")!!
        //    Toast.makeText(this@MainActivity, feedGroup.feeds[0].title, Toast.LENGTH_SHORT).show()
        // }

        setContent {
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    UI()
                }
            }
        }

        // Set feed-db in companion object
        setDb(applicationContext)
        // Set parser in companion object
        setParser(this@MainActivity)
    }

    /**
     * Call the required functions to update the Rss feed.
     *
     * @param parser A parser with preconfigured settings.
     */
    @DelicateCoroutinesApi
    private fun updateRss(parser: Parser) {
        val viewModel = PullFeed()

        GlobalScope.launch(Dispatchers.Main) {
            feedGroup = viewModel.pullRss(feedGroup, parser)
            // Debug toast to confirm that the data has come back to the MainActivity variable.
            // for (feed in MainActivity.feedGroup.feeds) {
            //     Toast.makeText(context, "Pulled: ${feed.title}", Toast.LENGTH_SHORT).show()
            // }
            updateText()
        }
    }

    private fun updateText() {
        var text = ""

        for (feed in feedGroup.feeds) {
            text += feed.title
            text += "\n"
        }

        feedGroupText.value = text
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
     * This function is called before `onStop` and `onDestroy` or any time a "stop" happens. This
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
//        if (feedGroup.feeds.isNullOrEmpty()) {
//            // Add some feeds to the feedGroup
//            feedGroup.feeds.add(Feed(link = "https://rss.cbc.ca/lineup/topstories.xml"))
//            feedGroup.feeds.add(Feed(link = "https://androidauthority.com/feed"))
//            // TODO This feed is malformed according to the exception that the xml parser throws.
//            //  We can use this to develop a bad formatting indication to the user
//            //  feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
//            feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))
//
//            // Tell the user that this change happened
//            Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
//                .show()
//        }
        ///////////////////////////////////////////////////////////////////////////////////////////

        updateText()
    }

    @Composable
    fun FeedTitles() {
        val text by feedGroupText.collectAsState()
        Text(text = text)
    }

    @Composable
    fun UpdateFeedButton() {
        Button(
            onClick = { updateRss(parser) }
        ) {
            Text("Update RSS Feeds")
        }
    }

    @Composable
    fun AddFeedButton() {
        Button(
            onClick = {
                val intent = Intent(this, AddFeedActivity::class.java).apply {}
                startActivity(intent)
                Log.d("AddFeed", "this")
                feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
            }
        ) {
            Text("Add Feed")
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun UI() {
        val padding = 16.dp
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FeedTitles()
            UpdateFeedButton()
            Spacer(Modifier.size(padding))
            AddFeedButton()
        }
    }
}

