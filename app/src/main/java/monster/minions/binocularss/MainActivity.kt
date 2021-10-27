package monster.minions.binocularss

import android.content.Context
import android.os.AsyncTask.execute
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.*
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity() {

    companion object {
        var feedGroup: FeedGroup = FeedGroup()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Minions")
                }
            }
        }

        // Add some feeds to the feedGroup
        feedGroup.feeds.add(Feed(link = "https://rss.cbc.ca/lineup/topstories.xml"))
        feedGroup.feeds.add(Feed(link = "https://androidauthority.com/feed"))
        feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
        feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))

        // Setup parser
        val parser = Parser.Builder()
            .context(this)
            .charset(Charset.forName("ISO-8859-7"))
            .cacheExpirationMillis(24L * 60L * 60L * 100L) // one day
            .build()

        // Update Rss
        updateRss(feedGroup, parser, this@MainActivity)
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
            for (feed in MainActivity.feedGroup.feeds) {
                Toast.makeText(context, "Pulled: ${feed.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BinoculaRSSTheme {
        Greeting("Android")
    }
}