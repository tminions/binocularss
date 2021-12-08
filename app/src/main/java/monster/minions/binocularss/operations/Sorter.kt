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
 * @param query The query
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
 * Sort articles class
 *
 * @param sortingArticleStrategy The strategy to use to sort articles
 */
class SortArticles(private val sortingArticleStrategy: SortingStrategy<Article>) {
    fun sort(articles: MutableList<Article>): MutableList<Article> {
        val comparator = sortingArticleStrategy.comparator
        return articles.sortedWith(comparator = comparator).toMutableList()
    }
}

/**
 * Strategy for sorting feeds by title
 */
class SortFeedsByTitleStrategy: SortingStrategy<Feed> {
    override val comparator: Comparator<Feed> = FeedTitleComparator()
}

/**
 * Sort feeds class
 *
 * @param sortingFeedStrategy The strategy to use to sort feeds
 */
class SortFeeds(private val sortingFeedStrategy: SortingStrategy<Feed>) {
    fun sort(feeds: MutableList<Feed>): MutableList<Feed> {
        val comparator = sortingFeedStrategy.comparator
        return feeds.sortedWith(comparator = comparator).toMutableList()
    }
}