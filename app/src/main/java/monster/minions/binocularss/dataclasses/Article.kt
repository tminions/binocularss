package monster.minions.binocularss.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.text.SimpleDateFormat

/**
 * A dataclass object representing an Article that is part of an RSS Feed.
 *
 * Info on possible tags from https://validator.w3.org/feed/docs/rss2.html
 *
 * @param title The title of the item.
 * @param author Email address of the author of the item.
 * @param link The URL of the item.
 * @param pubDate Indicates when the item was published.
 * @param description The item synopsis.
 * @param content The content of an item, also known as enclosure.
 * @param image An href to a header image.
 * @param audio An href to companion audio.
 * @param video An href to companion video.
 * @param guid A string that uniquely identifies the item.
 * @param sourceName The source name (name of the feed it came from).
 * @param sourceUrl The source URL (url of the feed it came from).
 * @param categories A list of Source-defined categories.
 */
@Parcelize
data class Article(
    var title: String? = "",
    var author: String? = "",
    var link: String? = "",
    var pubDate: String? = "",
    var description: String? = "",
    var content: String? = "",
    var image: String? = "",
    var audio: String? = "",
    var video: String? = "",
    var guid: String? = "",
    var sourceName: String? = "",
    var sourceUrl: String? = "",
    var categories: MutableList<String>? = mutableListOf()
) : Parcelable, Comparable<Article>{
    /**
     * Check if an article is equal to another by checking the
     * link, which usually does not change
     *
     * @param other Another object. If it is not an article, return false immediately.
     */
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

    /**
     * Calculate a hash code for the Article.
     */
    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (pubDate?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (audio?.hashCode() ?: 0)
        result = 31 * result + (video?.hashCode() ?: 0)
        result = 31 * result + (guid?.hashCode() ?: 0)
        result = 31 * result + (sourceName?.hashCode() ?: 0)
        result = 31 * result + (sourceUrl?.hashCode() ?: 0)
        result = 31 * result + (categories?.hashCode() ?: 0)
        return result
    }
    /**
     * Compares it's date with another Article object.
     *
     * Returns a negative integer, zero, or a positive integer
     * as the date of this object is less than, equal to, or greater
     * than the specified object.
     *
     *
     *
     * @param other the Article to be compared
     * @return a negative integer, zero, or a positive integer
     * as the date of this object is less than, equal to, or greater
     * than the specified object.
     */
    override fun compareTo(other: Article): Int {
        val articleDateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm z")
        val currentDate: Date = articleDateFormat.parse(this.pubDate)
        val otherDate: Date = articleDateFormat.parse(other.pubDate)
        return currentDate.compareTo(otherDate)
    }
}