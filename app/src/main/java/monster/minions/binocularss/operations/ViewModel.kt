package monster.minions.binocularss.operations

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.activities.MainActivity
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.room.FeedDao

/**
 * Asynchronous execution class that runs XML parser code off of the main thread to not interrupt UI.
 *
 * @param context The application context it is being called from.
 * @param feedGroup The feed group provided to merge with the new list of feeds.
 */
class ViewModel(context: Context, feedGroup: FeedGroup) : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    var localContext = context
    var isRefreshing = MutableStateFlow(false)

    // FeedGroup object
    private var localFeedGroup: FeedGroup = feedGroup

    // Room database variables
    private lateinit var databaseGateway: DatabaseGateway

    /**
     * Call the required functions to update the Rss feed.
     *
     * @param parser A parser with preconfigured settings.
     */
    fun updateRss(parser: Parser) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            isRefreshing.value = true
            // Update feedGroup variable.
            val fetchFeed = FetchFeed()
            localFeedGroup = fetchFeed.pullRss(localFeedGroup, parser)

            // Update DB with updated feeds.
            databaseGateway = DatabaseGateway(context = localContext)
            databaseGateway.addFeeds(localFeedGroup.feeds)

            // Update list states in MainActivity.
            val sortArticlesByDate = SortArticles(SortArticlesByDateStrategy())
            MainActivity.articleList.value = sortArticlesByDate.sort(getAllArticles(localFeedGroup))
            MainActivity.bookmarkedArticleList.value =
                sortArticlesByDate.sort(getBookmarkedArticles(localFeedGroup))
            MainActivity.currentFeedArticles.value =
                sortArticlesByDate.sort(getArticlesFromFeed(MainActivity.currentFeed))
            MainActivity.readArticleList.value =
                SortArticles(SortArticlesByReadDateStrategy()).sort(getReadArticles(localFeedGroup))
            MainActivity.feedList.value =
                SortFeeds(SortFeedsByTitleStrategy()).sort(localFeedGroup.feeds)

            isRefreshing.value = false

            MainActivity.updateFeedGroup(databaseGateway.read())
        }
    }
}