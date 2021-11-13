package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Article
import java.text.SimpleDateFormat
import java.util.Date

class ArticleDateComparator: Comparator<Article>{
    /**
     * Compares its two arguments for order.
     *
     * Returns a negative integer, zero, or a positive integer
     * as article1 is less than, equal to, or greater than article2
     * in terms of article's publish date.
     *
     * @param article1 the first Article to compare
     * @param article2 the second Article to compare
     * @return a negative integer, zero, or a positive integer
     *      as article1 is less than, equal to, or greater than article2
     */

    override fun compare(article1: Article, article2: Article): Int{
        val articleDateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm z")
        val currentDate: Date = articleDateFormat.parse(article1.pubDate)
        val otherDate: Date = articleDateFormat.parse(article2.pubDate)
        return currentDate.compareTo(otherDate)
    }
}