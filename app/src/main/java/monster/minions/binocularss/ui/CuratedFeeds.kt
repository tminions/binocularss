package monster.minions.binocularss.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import monster.minions.binocularss.activities.ui.theme.paddingLargeMedium
import monster.minions.binocularss.dataclasses.Feed


/**
 * Last list of predefined curated feeds
 *
 * @param addFeedToGroup: callback to run when row is clicked
 */
@Composable
fun CuratedFeeds(
    addFeedToGroup: (url: String, feedExistsCallback: () -> Unit) -> Unit,
    feeds: List<Feed>,
    existingFeeds: MutableList<Feed> = mutableListOf()
) {
    Column(modifier = Modifier.padding(top = paddingLargeMedium)) {
        Text(style = MaterialTheme.typography.caption, text = "Suggested feeds")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = feeds) { feed ->
                // Check to not show any existing feed
                if (existingFeeds.all { existingFeed -> existingFeed.source != feed.source }) {
                    AddableFeed(feed = feed, addFeedToGroup = addFeedToGroup)
                }
            }
        }
    }
}

@Composable
fun AddableFeed(feed: Feed, addFeedToGroup: (url: String, feedExistsCallback: () -> Unit) -> Unit) {
    Row(
        modifier = Modifier
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