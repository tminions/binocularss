package monster.minions.binocularss.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
data class Article  (
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
    var sourceTitle: String = "",
    var categories: MutableList<String>? = mutableListOf(),
    var bookmarked: Boolean = false,
    var read: Boolean = false,
    var readDate: String? = "",
) : Parcelable {
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
        result = 31 * result + sourceTitle.hashCode()
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + bookmarked.hashCode()
        result = 31 * result + read.hashCode()
        result = 31 * result + (readDate?.hashCode() ?: 0)
        return result
    }

    class Builder {
        var title: String? = ""
            private set

        var author: String? = ""
            private set

        var link: String? = ""
            private set

        var pubDate: String? = ""
            private set

        var description: String? = ""
            private set

        var content: String? = ""
            private set

        var image: String? = ""
            private set

        var audio: String? = ""
            private set

        var video: String? = ""
            private set

        var guid: String? = ""
            private set

        var sourceName: String? = ""
            private set

        var sourceUrl: String? = ""
            private set

        var sourceTitle: String = ""
            private set

        var categories: MutableList<String>? = mutableListOf()
            private set

        var bookmarked: Boolean = false
            private set

        var read: Boolean = false
            private set

        var readDate: String? = ""
            private set

        fun title (title: String) = apply { this.title = title }

        fun author (author: String) = apply { this.author = author }

        fun link (link: String) = apply { this.link = link }

        fun pubDate (pubDate: String) = apply { this.pubDate = pubDate }

        fun description (description: String) = apply { this.description = description }

        fun content (content: String) = apply { this.content = content }

        fun image (image: String) = apply { this.image = image }

        fun audio (audio: String) = apply { this.audio = audio }

        fun video (video: String) = apply { this.video = video }

        fun guid (guid: String) = apply { this.guid = guid }

        fun sourceName (sourceName: String) = apply { this.sourceName = sourceName }

        fun sourceUrl (sourceUrl: String) = apply { this.sourceUrl = sourceUrl }

        fun sourceTitle (sourceTitle: String) = apply { this.sourceTitle = sourceTitle }

        fun categories (categories: MutableList<String>) = apply { this.categories = categories }

        fun bookmarked (bookmarked: Boolean) = apply { this.bookmarked = bookmarked }

        fun read (read: Boolean) = apply { this.read = read }

        fun readDate (readDate: String) = apply { this.readDate = readDate }

        fun build() = Article(
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
    }
}