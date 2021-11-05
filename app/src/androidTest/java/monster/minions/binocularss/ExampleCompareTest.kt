package monster.minions.binocularss

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.operations.ArticleDateComparator
import monster.minions.binocularss.operations.ArticleTitleComparator
import monster.minions.binocularss.operations.FeedTitleComparator
import org.junit.Test
import org.junit.Assert.*

/**
 *
 */
class ExampleCompareTest {

    @Test
    fun articleDateCompareTest(){
        val article1: Article = Article(pubDate = "Fri, 29 Oct 2021 10:02 EDT")
        val article2: Article = Article(pubDate = "Thu, 28 Oct 2021 10:02 EDT")
        val article3: Article = Article(pubDate = "Fri, 29 Oct 2021 10:02 EDT")
        val article4: Article = Article(pubDate = "Sat, 31 Oct 2021 10:02 EDT")

        val articleDateComparator: ArticleDateComparator = ArticleDateComparator()

        assertEquals(true, articleDateComparator.compare(article1, article4) < 0)
        assertEquals(true, articleDateComparator.compare(article1, article3) == 0)
        assertEquals(true, articleDateComparator.compare(article1, article2) > 0)
    }

    @Test
    fun articleTitleCompareTest(){
        val article1: Article = Article(title = "The compareTo() method")
        val article2: Article = Article(title = "ZcompareTo() method")
        val article3: Article = Article(title = "The compareTo() method")
        val article4: Article = Article(title = "compareTo() method")

        val articleTitleComparator: ArticleTitleComparator = ArticleTitleComparator()

        assertEquals(true, articleTitleComparator.compare(article1, article4) > 0)
        assertEquals(true, articleTitleComparator.compare(article1, article3) == 0)
        assertEquals(true, articleTitleComparator.compare(article1, article2) < 0)
    }

    @Test
    fun feedTitlteCompareTest(){
        val feed1: Feed = Feed(title = "The compareTo() method")
        val feed2: Feed = Feed(title = "ZcompareTo() method")
        val feed3: Feed = Feed(title = "The compareTo() method")
        val feed4: Feed = Feed(title = "compareTo() method")

        val feedTitleComparator: FeedTitleComparator = FeedTitleComparator()

        assertEquals(true, feedTitleComparator.compare(feed1, feed2) < 0)
        assertEquals(true, feedTitleComparator.compare(feed1, feed3) == 0)
        assertEquals(true, feedTitleComparator.compare(feed1, feed4) > 0)
    }
}