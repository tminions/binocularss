package monster.minions.binocularss

import android.os.AsyncTask.execute
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }

        val feed: Feed = Feed(link = "https://rss.cbc.ca/lineup/topstories.xml")
        val task = PullFeedTask(this)
        task.execute(feed)
        // PullFeedTask.execute(feed)
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