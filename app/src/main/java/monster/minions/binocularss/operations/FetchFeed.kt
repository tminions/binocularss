package monster.minions.binocularss.operations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.withContext
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup

class FetchFeed: ViewModel() {

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
                Log.d("PullFeed", "Pulling: " + feed.link)
                try {
                    val pulledFeed = mergeFeeds(feed, channelToFeed(parser.getChannel(feed.source)))

                    // Set article sourceTitle to be the same as feed.title
                    for (article in feed.articles) {
                        article.sourceTitle = feed.title.toString()
                    }

                    feedList.add(pulledFeed)
                } catch (e: Exception) {
                    Log.e(
                        "PullFeed",
                        "Feed ${feed.title} ignored as there is an error with the source"
                    )
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
        var image = channel.image?.url.toString()
        if (image == "") {
            image = "$link/favicon.ico"
        }
        val lastBuildDate = channel.lastBuildDate.toString()
        val updatePeriod = channel.updatePeriod.toString()

        for (article in channel.articles) {
            articles.add(articleToArticle(article, title))
        }

        feed = Feed("", title, link, description, lastBuildDate, image, updatePeriod, articles)

        return feed
    }

    /**
     * Convert com.prof.rssparser.Article to monster.minions.binocularss.dataclasses.Article.
     *
     * @param oldArticle Article to be converted.
     * @return Converted Article.
     */
    private fun articleToArticle(oldArticle: com.prof.rssparser.Article, sourceTitle: String): Article {
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
        val guid = oldArticle.guid.toString()
        val sourceName = oldArticle.sourceName.toString()
        val sourceUrl = oldArticle.sourceUrl.toString()
        val categories = oldArticle.categories

        article = Article(
            title = title,
            author = author,
            link = link,
            pubDate = pubDate,
            description = description,
            content = content,
            image = image,
            audio = audio,
            video = video,
            guid = guid,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            sourceTitle = sourceTitle,
            categories = categories,
            bookmarked = false,
            read = false,
            readDate = "",
        )

        return article
    }

    /**
     * Update feed information based on the pulledFeed with the following criteria:
     * 1. Any element in newFeed that is not in oldFeed will be added to the list.
     * 2. Any element in oldFeed that is not in newFeed will be added to the list.
     * 3. Any element in newFeed and oldFeed will be merged and added to the list.
     * 4. The user set properties like tags and priority of oldFeed will be transferred to newFeed.
     *
     * @param oldFeed The feed passed in by the program to be updated
     * @param newFeed The feed pulled down from the RSS
     * @return The merge of oldFeed and newFeed as described above
     */
    private fun mergeFeeds(oldFeed: Feed, newFeed: Feed): Feed {
        val unionArticles = mutableListOf<Article>()

        for (article in oldFeed.articles) {
            var anyEquals = false

            for (pulledArticle in newFeed.articles) {
                if (article == pulledArticle) {
                    // Satisfy property 3
                    // pulledArticle.sourceTitle = article.sourceTitle
                    pulledArticle.bookmarked = article.bookmarked
                    pulledArticle.read = article.read
                    pulledArticle.readDate = article.readDate
                    unionArticles.add(pulledArticle)
                    anyEquals = true
                    break
                }
            }

            if (!anyEquals) {
                // Satisfy property 2
                unionArticles.add(article)
            }
        }

        // Satisfy property 1
        for (pulledArticle in newFeed.articles) {
            var anyEquals = false
            for (article in unionArticles) {
                if (pulledArticle == article) {
                    anyEquals = true
                    break
                }
            }

            if (!anyEquals) {
                unionArticles.add(pulledArticle)
            }
        }

        // Transfer the user-set flags
        newFeed.source = oldFeed.source
        newFeed.tags = oldFeed.tags
        newFeed.priority = oldFeed.priority
        newFeed.articles = unionArticles

        return newFeed
    }
}