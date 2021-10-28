package monster.minions.binocularss

import android.os.Parcel
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
 * @param sourceName The source name (name of the feed it came from).
 * @param sourceUrl The source URL (url of the feed it came from).
 * @param categories A list of the appropriate categories (defined by RSS).
 * @param guid A string that uniquely identifies the item.
 * @param source The RSS channel that the item came from.
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
    var sourceName: String? = "",
    var sourceUrl: String? = "",
    var categories: MutableList<String>? = mutableListOf<String>(),
    var guid: String? = "",
    var source: String? = "",
) : Parcelable