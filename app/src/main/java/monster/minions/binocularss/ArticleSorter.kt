package monster.minions.binocularss
import java.util.Date
import java.text.SimpleDateFormat

/**
 * A class that sorts Articles 
 */
class ArticleSorter {
    /**
     * Takes 2 Article Objects, convert them to Date object with same format
     * Compares the two Date objects and return the article that comes later.
     * (Subject to fix afterwards)
     * If article1 time == article2 time, then return article1
     */
    fun compare(article1: Article, article2: Article): Article {
        val articleDateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm z")
        val article1Date: Date = articleDateFormat.parse(article1.pubDate)
        val article2Date: Date = articleDateFormat.parse(article2.pubDate)
        if (article1Date.after(article2Date)) {
            return article2
        }
        return article1
    }
}