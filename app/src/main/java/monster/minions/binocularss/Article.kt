package monster.minions.binocularss

import java.net.URL

/**
 * from https://validator.w3.org/feed/docs/rss2.html
 */

data class Article(
    var title: String = "", // The title of the item.
    var author: String = "", // Email address of the author of the item.
    var link: String = "", // The URL of the item.
    var pubDate: String = "", // Indicates when the item was published.
    var description: String = "", // The item synopsis.
    var content: String = "", // The content of an item, also known as enclosure
    var image: String = "", // An href to a header image
    var audio: String = "", // An href to companion audio
    var video: String = "", // An href to companion video
    var sourceName: String = "", // The source name (name of the feed it came from)
    var sourceUrl: String = "", // The source URL (url of the feed it came from)
    var categories: MutableList<String> = mutableListOf<String>(), // A list of the appropriate categories (predefiend)
    var guid: String = "", // A string that uniquely identifies the item.
    var source: String = "", // The RSS channel that the item came from.
) {

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Article -> {
                this.link == other.link
            }
            else -> {
                false
            }
        }
    }
}