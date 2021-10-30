package monster.minions.binocularss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A dataclass object representing a group of RSS Feeds.
 */
@Parcelize
data class FeedGroup(
    var feeds: MutableList<Feed> = mutableListOf()
): Parcelable