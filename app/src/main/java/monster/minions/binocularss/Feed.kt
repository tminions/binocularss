package monster.minions.binocularss

import com.prof.rssparser.Image

/**
 * from https://validator.w3.org/feed/docs/rss2.html
 */
data class Feed(
    var title: String = "", // The name of the channel. It's how people refer to your service. If you have an HTML website that contains the same information as your RSS file, the title of your channel should be the same as the title of your website.
    var link: String = "", // The URL to the HTML website corresponding to the channel.
    var description: String = "", // Phrase or sentence describing the channel.
    var lastBuildDate: String = "", // The last time the content of the channel changed.
    var image: Image? = null, // Specifies a GIF, JPEG or PNG image that can be displayed with the channel.
    var updatePeriod: String = "", // Specifies the amount of time between updates for RSS aggregators.
    var articles: MutableList<Article> = mutableListOf<Article>(), // A list of articles that the feed contains.
    var tags: List<String> = mutableListOf<String>(), // The user assigned tags on a feed
    var priority: Int = 0 // The computed priority score of a feed.
)
