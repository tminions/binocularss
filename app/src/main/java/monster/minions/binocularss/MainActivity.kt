package monster.minions.binocularss

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.*
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme

class MainActivity : ComponentActivity() {

    companion object {
        // Global FeedGroup object
        var feedGroup: FeedGroup = FeedGroup()
        // Parser variable using lateinit because we want to get the context
        private lateinit var parser: Parser
        // Setup parser
        private fun setParser(context: Context) {
            parser = Parser.Builder()
                .context(context)
                // .charset(Charset.forName("ISO-8859-7")) // Default is UTF-8
                .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
                .build()
        }

        const val FEED_GROUP_KEY = "feedGroup"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            feedGroup = savedInstanceState.getParcelable<FeedGroup>("feedGroup")!!
            Toast.makeText(this@MainActivity, feedGroup.feeds[0].title, Toast.LENGTH_SHORT).show()
        }

        setContent {
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        FeedTitles(feedGroup)
                        UpdateFeedButton()
                    }
                }
            }
        }

        if (feedGroup.feeds.isNullOrEmpty()) {
            // Add some feeds to the feedGroup
            feedGroup.feeds.add(Feed(link = "https://rss.cbc.ca/lineup/topstories.xml"))
            feedGroup.feeds.add(Feed(link = "https://androidauthority.com/feed"))
            feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
            feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))

            Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
                .show()
        }

        // Set parser in companion object
        setParser(this@MainActivity)
    }

    /**
     * Call the required functions to update the Rss feed.
     *
     * @param feedGroup A group of feeds to be updated.
     * @param parser A parser with preconfigured settings.
     * @param context The application context (current view).
     */
    @DelicateCoroutinesApi
    private fun updateRss(feedGroup: FeedGroup, parser: Parser, context: Context) {
        val viewModel = PullFeed(context)

        GlobalScope.launch(Dispatchers.Main) {
            MainActivity.feedGroup = viewModel.pullRss(MainActivity.feedGroup, parser)
            // Debug toast to confirm that the data has come back to the MainActivity variable.
            // for (feed in MainActivity.feedGroup.feeds) {
            //     Toast.makeText(context, "Pulled: ${feed.title}", Toast.LENGTH_SHORT).show()
            // }
            var text = ""
            for (feed in MainActivity.feedGroup.feeds) {
                text += feed.title
                text += "\n"
            }
            feedGroupText.value = text
        }
    }

    /**
     * Saves instance state when activity is destroyed
     *
     * @param outState A bundle of instance state information stored using key-value pairs
     */
    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("MainActivity", "onSaveInstanceState called")
        super.onSaveInstanceState(outState)
        outState.putParcelable(FEED_GROUP_KEY, feedGroup)
    }

    /**
     * Restores instance state when activity is recreated
     *
     * @param savedInstanceState A bundle of instance state information stored using key-value pairs
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("MainActivity", "onRestoreInstanceState called")
        super.onRestoreInstanceState(savedInstanceState)
        feedGroup = savedInstanceState.getParcelable<FeedGroup>(FEED_GROUP_KEY)!!

        var text = ""
        for (feed in feedGroup.feeds) {
            text += feed.title
            text += "\n"
        }
        feedGroupText.value = text
    }

    /**
     * TODO implement saving to some sort of DB
     */
    override fun onPause() {
        super.onPause()
    }

    private val feedGroupText = MutableStateFlow("Empty\n")

    @Composable
    fun FeedTitles(feedGroup: FeedGroup) {
        val text by feedGroupText.collectAsState()
        Text(text = text)
    }

    @Composable
    fun UpdateFeedButton() {
        Button(
            onClick = { updateRss(feedGroup, parser, this@MainActivity) }
        ) {
            Text("Update RSS Feeds")
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        BinoculaRSSTheme {
            Column {
                FeedTitles(MainActivity.feedGroup)
                UpdateFeedButton()
            }
        }
    }
}

