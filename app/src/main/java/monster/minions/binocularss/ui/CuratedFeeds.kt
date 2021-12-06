package monster.minions.binocularss.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import monster.minions.binocularss.activities.ui.theme.*
import monster.minions.binocularss.dataclasses.Feed
import java.util.*


/**
 * Displays a single article in a card view format
 *
 * @param article The article to be displayed
 */

val feeds = listOf(
    Feed(source = "https://www.cbc.ca/rss/", ),
    Feed(source = "https://androidauthority.com/feed"),
    Feed(source = "https://www.ctvnews.ca/rss/ctvnews-ca-top-stories-public-rss-1.822009"),
    Feed(source = "https://www.cbc.ca/cmlink/rss-Indigenous")
)

@Composable
fun CuratedFeeds(addFeedToGroup: (url: String, feedExistsCallback: () -> Unit) -> Unit) {
    Surface(modifier = Modifier.padding(top = paddingLargeMedium)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = feeds) { feed ->
                AddableFeed(feed = feed, addFeedToGroup = addFeedToGroup)
            }
        }
    }
}

@Composable
fun AddableFeed(feed: Feed, addFeedToGroup: (url: String, feedExistsCallback: () -> Unit) -> Unit) {
    Row(modifier = Modifier.clickable {
        addFeedToGroup(feed.source!!) {}
    }) {
        Text(feed.source!!)
    }
}