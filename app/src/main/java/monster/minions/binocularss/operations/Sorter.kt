package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed

interface SortingStrategy<T> {
    val comparator: Comparator<T>
}

/**
 * Strategy for sorting articles by date
 */
class SortArticlesByDateStrategy: SortingStrategy<Article> {
    override val comparator: Comparator<Article> = ArticleDateComparator()
}

/**
 * Strategy for sorting articles by query, fuzzily
 *
 * @constructor query The query
 */
class SortArticlesByFuzzyMatchStrategy(query: String): SortingStrategy<Article> {
    override val comparator: Comparator<Article> = ArticleSearchComparator(query)
}

/**
 * Strategy for sorting articles by read date
 */
class SortArticlesByReadDateStrategy: SortingStrategy<Article> {
    override val comparator: Comparator<Article> = ArticleReadDateComparator()
}

/**
 * Sort articles
 */
class SortArticles(private val sortingArticleStrategy: SortingStrategy<Article>) {
    fun sort(articles: MutableList<Article>): MutableList<Article> {
        val comparator = sortingArticleStrategy.comparator
        return articles.sortedWith(comparator = comparator).toMutableList()
    }
}

class SortFeedsByTitleStrategy: SortingStrategy<Feed> {
    override val comparator: Comparator<Feed> = FeedTitleComparator()
}

class SortFeeds(private val sortingFeedStrategy: SortingStrategy<Feed>) {
    fun sort(feeds: MutableList<Feed>): MutableList<Feed> {
        val comparator = sortingFeedStrategy.comparator
        return feeds.sortedWith(comparator = comparator).toMutableList()
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


