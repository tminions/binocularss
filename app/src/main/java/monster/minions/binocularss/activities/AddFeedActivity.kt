package monster.minions.binocularss.activities

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prof.rssparser.Parser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.operations.PullFeed

class AddFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                UI()
            }
        }
    }

    // TODO write about clean architecture problem of having duplicate code
    /**
     * Call the required functions to update the Rss feed.
     *
     * @param parser A parser with preconfigured settings.
     */
    @DelicateCoroutinesApi
    private fun updateRss(parser: Parser) {
        val viewModel = PullFeed()

        GlobalScope.launch(Dispatchers.Main) {
            MainActivity.feedGroup = viewModel.pullRss(MainActivity.feedGroup, parser)
            // Debug toast to confirm that the data has come back to the MainActivity variable.
            // for (feed in MainActivity.feedGroup.feeds) {
            //     Toast.makeText(context, "Pulled: ${feed.title}", Toast.LENGTH_SHORT).show()
            // }

            var text = ""
            println(MainActivity.feedGroup.feeds.size)
            for (feed in MainActivity.feedGroup.feeds) {
                text += feed.title
                text += "\n"
            }
            MainActivity.feedGroupText.value = text

            Log.d("AddFeed: updateRss", (MainActivity.feedGroup.feeds[0].articles[0].description).toString())
            // TODO try and save to DB here or when returning back to main
            //  or figure out what is causing the other feeds to be wiped out
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

    var text = mutableStateOf("")

    @Composable
    fun FeedTextField() {
        val textState = remember { mutableStateOf(TextFieldValue()) }
        OutlinedTextField(
            value = textState.value,
            onValueChange = {
                textState.value = it; text = mutableStateOf(textState.value.text)
            },
            singleLine = true,
            maxLines = 1
        )
    }

    @Composable
    fun SubmitButton() {
        Button(
            modifier = Modifier.requiredWidth(70.dp),
            onClick = {
                val url = addHttps(text.value)

                println(url)

                if (Patterns.WEB_URL.matcher(url).matches()) {
                    // TODO this should be adding one feed to a list of feeds but it seems like
                    //  it's adding the one feed to an empty list? Maybe when MainActivity is stopped
                    //  the companion variables are deleted?
                    MainActivity.feedGroup.feeds.add(Feed(link = url))

                    // Update the RSS feed
                    updateRss(MainActivity.parser)
                    finish()
                } else {
                    Toast.makeText(this@AddFeedActivity, "Invalid URL", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Add")
        }
    }

    // TODO fix scale of button and text box to be maybe 80/20 or 90/10 and figure out how
    //  to do fractional scaling with this
    @Preview(showBackground = true)
    @Composable
    fun UI() {
        Surface(color = MaterialTheme.colors.background) {
            val padding = 16.dp
            Row(
                modifier = Modifier.padding(padding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeedTextField()
                Spacer(Modifier.size(padding))
                SubmitButton()
            }
        }
    }
}