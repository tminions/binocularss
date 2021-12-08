package monster.minions.binocularss.operations

import android.annotation.SuppressLint
import monster.minions.binocularss.dataclasses.Article
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ArticleDateComparator : Comparator<Article> {
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
    @SuppressLint("SimpleDateFormat")
    override fun compare(article1: Article, article2: Article): Int {
        val dateFormats = listOf("EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm zzz", "EEE, dd MMM yyyy HH:mm zzz")
        var currentDate: Date? = Date()
        var otherDate: Date? = Date()

        for (dateFormat in dateFormats) {
            try {
                val articleDateFormat = SimpleDateFormat(dateFormat)
                currentDate = articleDateFormat.parse(article1.pubDate!!)
                otherDate = articleDateFormat.parse(article2.pubDate!!)
            } catch (e: ParseException) {}
        }

        return otherDate!!.compareTo(currentDate!!)
    }
}