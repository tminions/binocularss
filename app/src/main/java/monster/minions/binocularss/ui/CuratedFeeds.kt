package monster.minions.binocularss.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import monster.minions.binocularss.activities.ui.theme.*
import monster.minions.binocularss.dataclasses.Feed
import java.util.*


val feeds = listOf(
    Feed(source = "https://rss.cbc.ca/lineup/topstories.xml", title = "CBC Top Stories"),
    Feed(source = "https://rss.cbc.ca/lineup/world.xml", title = "CBC World"),
    Feed(source = "https://rss.cbc.ca/lineup/canada.xml", title = "CBC Canada"),
    Feed(source = "https://rss.cbc.ca/lineup/politics.xml", title = "CBC Politics"),
    Feed(source = "https://androidauthority.com/feed", title = "Android Authority"),
    Feed(source = "https://www.ctvnews.ca/rss/ctvnews-ca-top-stories-public-rss-1.822009", title = "CTV Top Stories"),
)

/**
 * Last list of predefined curated feeds
 *
 * @param addFeedToGroup: callback to run when row is clicked
 */
@Composable
fun CuratedFeeds(addFeedToGroup: (url: String, feedExistsCallback: () -> Unit) -> Unit, existingFeeds: MutableList<Feed>) {
    Surface(modifier = Modifier.padding(top = paddingLargeMedium)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = feeds) { feed ->
                if(existingFeeds.all {existingFeed -> existingFeed.source != feed.source}) {
                    AddableFeed(feed = feed, addFeedToGroup = addFeedToGroup)
                }
            }
        }
    }
}

@Composable
fun AddableFeed(feed: Feed, addFeedToGroup: (url: String, feedExistsCallback: () -> Unit) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(42.dp)
        .padding(8.dp)
        .clickable {
            addFeedToGroup(feed.source!!) {}
        }, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(feed.title!!)
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add this feed"
        )
    }
}