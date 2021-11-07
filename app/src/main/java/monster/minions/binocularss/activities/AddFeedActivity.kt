package monster.minions.binocularss.activities

import android.os.Bundle
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
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.operations.PullFeed

class AddFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinoculaRSSTheme {
                UI()
            }
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

    private var text = mutableStateOf("")

    @Composable
    fun FeedTextField() {
        val textState = remember { mutableStateOf(TextFieldValue()) }
        OutlinedTextField(
            value = textState.value,
            placeholder = { Text("Enter Feed URL") },
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
                // Add https:// or http:// to the front of the url if not present
                val url = addHttps(text.value)

                // If the url is valid ...
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    // Check if the feed is already in the feedGroup
                    val feedToAdd = Feed(source = url)
                    var inFeedGroup = false
                    for (feed in MainActivity.feedGroup.feeds) {
                        if (feedToAdd == feed) {
                            inFeedGroup = true
                            Toast.makeText(this@AddFeedActivity,
                                "You've already added that RSS feed",
                                Toast.LENGTH_SHORT).show()
                            continue
                        }
                    }

                    // Add feed and update feeds if the feed is not in the feedGroup
                    if (!inFeedGroup) {
                        MainActivity.feedGroup.feeds.add(Feed(source = url))
                        val viewModel = PullFeed()
                        viewModel.updateRss(MainActivity.parser)
                        finish()
                    }

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

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        BinoculaRSSTheme {
            UI()
        }
    }
}