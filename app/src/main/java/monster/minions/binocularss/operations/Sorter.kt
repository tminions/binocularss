package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed

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
 * Sort the given list of feeds by date and return another list
 *
 * @param feeds The list of feeds to be sorted.
 * @return The sorted list of feeds.
 */
fun sortFeedsByTitle(feeds: MutableList<Feed>): MutableList<Feed> {
    val titleComparator = FeedTitleComparator()
    return feeds.sortedWith(comparator = titleComparator).toMutableList()
}
