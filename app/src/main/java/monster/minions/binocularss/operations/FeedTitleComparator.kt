package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Feed
import java.util.*

class FeedTitleComparator: Comparator<Feed> {
    /**
     * Compares its two arguments for order.
     *
     * Returns a negative integer, zero, or a positive integer
     * as feed1 is alphabetically before, equal to, or after feed2
     * in terms of feed's title.
     *
     * @param feed1 the first Feed to compare
     * @param feed2 the second Feed to compare
     * @return a negative integer, zero, or a positive integer
     *      as feed1 is alphabetically before, equal to, or after feed2
     */

    override fun compare(feed1: Feed, feed2: Feed): Int{
        val feed1Title = feed1.title!!.lowercase()
        val feed2Title = feed2.title!!.lowercase()

        return feed1Title.compareTo(feed2Title)
    }
}