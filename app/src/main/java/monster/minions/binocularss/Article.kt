package monster.minions.binocularss

import java.net.URL

/**
 * from https://validator.w3.org/feed/docs/rss2.html
 */

data class Article(
    var title: String = "", // The title of the item.
    var link: String = "", // The URL of the item.
    var description: String = "", // The item synopsis.
    var author: String = "", // Email address of the author of the item.
    var category: String = "", // Includes the item in one or more categories.
    var comments: String = "", // URL of a page for comments relating to the item.
    var enclosure: String = "", // Describes a media object that is attached to the item.
    var guid: String = "", // A string that uniquely identifies the item.
    var pubDate: String = "", // Indicates when the item was published.
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