package monster.minions.binocularss.activities

import android.content.Intent
import android.os.Bundle
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
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.operations.PullFeed

// TODO make sure to check that this is an RSS feed (probably in pull feed or something of
//  the sort and send a toast to the user if it is not. Try and check this when initially
//  adding maybe? Basically deeper checking than just is this a URL so we don't encounter
//  errors later. Or we could leave it and have PullFeed silently remove a feed if it is
//  invalid like it currently does.
class AddFeedActivity : ComponentActivity() {
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

        // Handle a link being shared to the application
        if (intent?.action == Intent.ACTION_SEND && "text/uri-list" == intent.type) {
            setTextView(intent)
        }
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
            for (feed in MainActivity.feedGroup.feeds) {
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
                MainActivity.feedGroup.feeds.add(Feed(source = url))
                val viewModel = PullFeed()
                viewModel.updateRss(MainActivity.parser)
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