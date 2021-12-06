package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed

interface SortingArticleStrategy {
    val comparator: Comparator<Article>
}

/**
 * Strategy for sorting articles by date
 */
class SortArticlesByDateStrategy: SortingArticleStrategy {
    override val comparator: Comparator<Article> = ArticleDateComparator()
}

/**
 * Strategy for sorting articles by query, fuzzily
 *
 * @constructor query The query
 */
class SortArticlesByFuzzyMatchStrategy(query: String): SortingArticleStrategy {
    override val comparator: Comparator<Article> = ArticleSearchComparator(query)
}

/**
 * Strategy for sorting articles by read date
 */
class SortArticlesByReadDate: SortingArticleStrategy {
    override val comparator: Comparator<Article> = ArticleReadDateComparator()
}

/**
 * Sort articles
 */
class ArticleSorter(private val sortingArticleStrategy: SortingArticleStrategy) {
    fun sort(articles: MutableList<Article>): MutableList<Article> {
        val comparator = sortingArticleStrategy.comparator
        return articles.sortedWith(comparator = comparator).toMutableList()
    }
}

/**
 * Sort the given list of articles by date and return another list
 *
 * @param articles The list of articles to be sorted.
 * @return The sorted list of articles.
 */
fun sortArticlesByDate(articles: MutableList<Article>): MutableList<Article> {
    val dateComparator = ArticleDateComparator()
    return articles.sortedWith(comparator = dateComparator).toMutableList()
}


/**
 * Sort the given list of articles by date and return another list
 *
 * @param articles The list of articles to be sorted.
 * @return The sorted list of articles.
 */
fun sortArticlesByFuzzyMatch(articles: MutableList<Article>, query: String): MutableList<Article> {
    val searchComparator = ArticleSearchComparator(query)
    return articles.sortedWith(comparator = searchComparator).toMutableList()
}


/**
 * Sort the given list of articles by read date and return another list
 *
 * @param articles The list of articles to be sorted.
 * @return The sorted list of articles.
 */
fun sortArticlesByReadDate(articles: MutableList<Article>): MutableList<Article> {
    val dateComparator = ArticleReadDateComparator()
    return articles.sortedWith(comparator = dateComparator).toMutableList()
}

/**
 * Sort the given list of feeds by date and return another list
 *
 * @param feeds The list of feeds to be sorted.
 * @return The sorted list of feeds.
 */
fun sortFeedsByTitle(feeds: MutableList<Feed>): MutableList<Feed> {
    val titleComparator = FeedTitleComparator()
    return feeds.sortedWith(comparator = titleComparator).toMutableList()
}


