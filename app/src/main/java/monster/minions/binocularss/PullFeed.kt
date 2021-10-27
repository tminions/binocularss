package monster.minions.binocularss

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.*

/**
 * Asynchronous execution class that runs XML parser code off of the main thread to not interrupt UI
 */
class PullFeed(context: Context) : ViewModel() {

//    @SuppressLint("StaticFieldLeak")
//    private val context: Context = context

    /**
     * Get the RSS feeds in feedGroup from the internet or cache.
     *
     * @param feedGroup A group of feeds to be updated.
     * @param parser A parser with preconfigured settings.
     * @return An updated FeedGroup object.
     */
    suspend fun pullRss(feedGroup: FeedGroup, parser: Parser): FeedGroup {
        val feedList: MutableList<Feed> = mutableListOf()

        // Loop through all the feeds
        for (feed in feedGroup.feeds) {
            // Launch the async task
            withContext(viewModelScope.coroutineContext) {
                Log.d("Pull Feed", "Pulling: " + feed.link)
                try {
                    // Get channel from RSS parser and convert it to feed
                    val pulledFeed = mergeFeeds(feed, channelToFeed(parser.getChannel(feed.link)))
                    // Add the updated feed to a aggregator list
                    feedList.add(pulledFeed)
                } catch (e: Exception) {
                    // TODO tell user that url is invalid. This is the most common exception cause.
                    //  Another one may be intrnet access. Figure out which exception is which and
                    //  inform the user accordingly.
                    e.printStackTrace()
                }
            }
        }

        feedGroup.feeds = feedList

        // Debug message
        for (feed in feedGroup.feeds) {
            Log.d("PullFeed", "Pulled Feed: " + feed.title)
        }

        return feedGroup
    }

    /**
     * Convert com.prof.rssparser.Channel to monster.minions.binocularss.Feed.
     *
     * @param channel Channel to be converted.
     * @return Converted Feed.
     */
    private fun channelToFeed(channel: Channel): Feed {
        val feed: Feed
        val articles = mutableListOf<Article>()

        val title = channel.title.toString()
        val link = channel.link.toString()
        val description = channel.description.toString()
        val image = channel.image
        val lastBuildDate = channel.lastBuildDate.toString()
        val updatePeriod = channel.updatePeriod.toString()

        for (article in channel.articles) {
            articles.add(articleToArticle(article))
        }

        feed = Feed(title, link, description, lastBuildDate, image, updatePeriod, articles)

        return feed
    }

    /**
     * Convert com.prof.rssparser.Article to monster.minions.binocularss.Article.
     *
     * @param oldArticle Article to be converted.
     * @return Converted Article.
     */
    private fun articleToArticle(oldArticle: com.prof.rssparser.Article): Article {
        val article: Article

        val title = oldArticle.title.toString()
        val author = oldArticle.author.toString()
        val link = oldArticle.link.toString()
        val pubDate = oldArticle.pubDate.toString()
        val description = oldArticle.description.toString()
        val content = oldArticle.content.toString()
        val image = oldArticle.image.toString()
        val audio = oldArticle.audio.toString()
        val video = oldArticle.video.toString()
        val sourceName = oldArticle.sourceName.toString()
        val sourceUrl = oldArticle.sourceUrl.toString()
        val categories = oldArticle.categories

        article = Article(
            title,
            author,
            link,
            pubDate,
            description,
            content,
            image,
            audio,
            video,
            sourceName,
            sourceUrl,
            categories
        )

        return article
    }

    /**
     * Update feed information based on the pulledFeed with the following criteria:
     * 1. Any element in newFeed will be added to the new list
     * 2. Any element in oldFeed that is not in newFeed will be added to the list
     * 3. The user set properties like tags and priority of oldFeed will be transferred to newFeed
     *
     * @param oldFeed The feed passed in by the program to be updated
     * @param newFeed The feed pulled down from the RSS
     * @return The merge of oldFeed and newFeed as described above
     */
    private fun mergeFeeds(oldFeed: Feed, newFeed: Feed): Feed {
        val unionArticles = mutableListOf<Article>()

        // Satisfy property 2 from the docstring
        for (article in oldFeed.articles) {
            var anyEquals = false;

            for (pulledArticle in newFeed.articles) {
                if (article == pulledArticle) {
                    anyEquals = true
                }
            }

            if (!anyEquals) {
                unionArticles.add(article)
            }
        }

        // Satisfy property 1 from the docstring
        unionArticles.addAll(newFeed.articles)

        // Transfer the user-set flags
        newFeed.tags = oldFeed.tags
        newFeed.priority = oldFeed.priority
        newFeed.articles = unionArticles

        return newFeed
    }
}