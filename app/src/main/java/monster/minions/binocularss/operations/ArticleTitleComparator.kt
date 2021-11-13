package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Article
import java.util.*

class ArticleTitleComparator: Comparator<Article> {
    /**
     * Compares its two arguments for order.
     *
     * Returns a negative integer, zero, or a positive integer
     * as article1 is alphabetically before, equal to, or after article2
     * in terms of article's title.
     *
     * @param article1 the first Article to compare
     * @param article2 the second Article to compare
     * @return a negative integer, zero, or a positive integer
     *      as article1 is alphabetically before, equal to, or after article2
     */

    override fun compare(article1: Article, article2: Article): Int{
        val article1Title = article1.title!!.lowercase()
        val article2Title = article2.title!!.lowercase()

        return article1Title.compareTo(article2Title)
    }
}