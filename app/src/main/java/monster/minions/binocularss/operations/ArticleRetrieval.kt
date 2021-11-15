package monster.minions.binocularss.operations

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup

/**
 * Returns a list of all bookmarked articles from the feedGroup.
 *
 * @param feedGroup The feedGroup to get the list articles from.
 */
fun getAllArticles(feedGroup: FeedGroup): MutableList<Article> {
    val articles: MutableList<Article> = mutableListOf()

    for (feed in feedGroup.feeds) {
        for (article in feed.articles) {
            articles.add(article)
        }
    }

    return articles
}

/**
 * Returns a list of bookmarked articles from the feedGroup
 *
 * @param feedGroup The feedGroup to get the list of bookmarked articles from.
 */
fun getBookmarkedArticles(feedGroup: FeedGroup): MutableList<Article> {
    val bookmarkedArticles: MutableList<Article> = mutableListOf()

    for (feed in feedGroup.feeds) {
        for (article in feed.articles) {
            if (article.bookmarked) {
                bookmarkedArticles.add(article)
            }
        }
    }

    return bookmarkedArticles
}
