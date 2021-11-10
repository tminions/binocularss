package monster.minions.binocularss.dataclasses

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.*
import kotlinx.parcelize.Parcelize
import monster.minions.binocularss.room.ArticleListConverter
import monster.minions.binocularss.room.TagsListConverter

/**
 * A dataclass object representing an RSS Feed.
 *
 * Info on possible tags from https://validator.w3.org/feed/docs/rss2.html
 *
 * @param title The name of the channel. It's how people refer to your service. If you have an HTML website that contains the same information as your RSS file, the title of your channel should be the same as the title of your website.
 * @param link The URL to the HTML website corresponding to the channel.
 * @param description Phrase or sentence describing the channel.
 * @param lastBuildDate The last time the content of the channel changed.
 * @param image Specifies the url for a GIF, JPEG or PNG image that can be displayed with the channel.
 * @param updatePeriod Specifies the amount of time between updates for RSS aggregators.
 * @param articles A list of articles that the feed contains.
 * @param tags The user assigned tags on a feed.
 * @param priority The computed priority score of a feed.
 */
@Parcelize
@Entity(tableName = "feeds")
@TypeConverters(ArticleListConverter::class, TagsListConverter::class)
data class Feed(
    @NonNull
    @PrimaryKey
    var source: String = "",
    var title: String? = "",
    var link: String? = "",
    var description: String? = "",
    var lastBuildDate: String? = "",
    var image: String = "",
    var updatePeriod: String? = "",
    var articles: MutableList<Article> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf(),
    var priority: Int = 0,
) : Parcelable {
    /**
     * Check if a feed is equal to another by checking the source, which usually does not change
     *
     * @param other Another object. If it is not an article, return false immediately.
     */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Feed -> {
                this.source == other.source
            }
            else -> {
                false
            }
        }
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (lastBuildDate?.hashCode() ?: 0)
        result = 31 * result + image.hashCode()
        result = 31 * result + (updatePeriod?.hashCode() ?: 0)
        result = 31 * result + articles.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + priority
        return result
    }
}