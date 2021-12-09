package monster.minions.binocularss

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.operations.ArticleDateComparator
import monster.minions.binocularss.operations.ArticleTitleComparator
import monster.minions.binocularss.operations.FeedTitleComparator
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleCompareTest {

    @Test
    fun articleDateCompareTest(){
        val article1: Article = Article(pubDate = "Wed, 8 Dec 2021 18:50 EST")
        val article2: Article = Article(pubDate = "Wed, 8 Dec 2021 19:50 EST")
        val article3: Article = Article(pubDate = "Wed, 8 Dec 2021 18:50 EST")
        val article4: Article = Article(pubDate = "Wed, 8 Dec 2021 17:50 EST")
        val article5: Article = Article(pubDate = "Wed, 8 Dec 2021 18:50:21 EST")
        val article6: Article = Article(pubDate = "Wed, 8 Dec 2021 18:50:21 EST")
        val article7: Article = Article(pubDate = "Wed, 8 Dec 2021 17:50:21 EST")

        val articleDateComparator = ArticleDateComparator()

        /* Comparing Articles without seconds */

        assertEquals(true, articleDateComparator.compare(article1, article2) > 0)
        assertEquals(true, articleDateComparator.compare(article1, article3) == 0)
        assertEquals(true, articleDateComparator.compare(article1, article4) < 0)

        /* Comparing Articles with Seconds */
        assertEquals(true, articleDateComparator.compare(article6, article5) == 0)
        assertEquals(true, articleDateComparator.compare(article6, article7) < 0)

        /* Comparing Articles with Seconds and without seconds */
        Assert.assertEquals(true, articleDateComparator.compare(article1, article5) > 0)
        Assert.assertEquals(true, articleDateComparator.compare(article1, article6) > 0)
        Assert.assertEquals(true, articleDateComparator.compare(article1, article7) > 0)
    }

    @Test
    fun articleTitleCompareTest(){
        val article1: Article = Article(title = "The compareTo() method")
        val article2: Article = Article(title = "CompareTo() method")
        val article3: Article = Article(title = "The compareTo() method")
        val article4: Article = Article(title = "compareTo() method")

        val articleTitleComparator: ArticleTitleComparator = ArticleTitleComparator()

        Assert.assertEquals(true, articleTitleComparator.compare(article1, article4) > 0)
        Assert.assertEquals(true, articleTitleComparator.compare(article1, article3) == 0)
        Assert.assertEquals(true, articleTitleComparator.compare(article1, article2) > 0)
    }

    @Test
    fun feedTitleCompareTest(){
        val feed1: Feed = Feed(title = "The compareTo() method")
        val feed2: Feed = Feed(title = "ZcompareTo() method")
        val feed3: Feed = Feed(title = "The compareTo() method")
        val feed4: Feed = Feed(title = "compareTo() method")

        val feedTitleComparator: FeedTitleComparator = FeedTitleComparator()

        Assert.assertEquals(true, feedTitleComparator.compare(feed1, feed2) < 0)
        Assert.assertEquals(true, feedTitleComparator.compare(feed1, feed3) == 0)
        Assert.assertEquals(true, feedTitleComparator.compare(feed1, feed4) > 0)
    }
}