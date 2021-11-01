package monster.minions.binocularss

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import org.junit.Test
import org.junit.Assert.*

/**
 *
 */
class ExampleCompareTest {

    @Test
    fun articleCompareToTest(){
        val article1: Article = Article(pubDate = "Fri, 29 Oct 2021 10:02 EDT")
        val article2: Article = Article(pubDate = "Thu, 28 Oct 2021 10:02 EDT")
        val article3: Article = Article(pubDate = "Fri, 29 Oct 2021 10:02 EDT")
        val article4: Article = Article(pubDate = "Sat, 31 Oct 2021 10:02 EDT")

        assertEquals(-1, article1.compareTo(article4))
        assertEquals(0, article1.compareTo(article3))
        assertEquals(1, article1.compareTo(article2))
    }

    @Test
    fun feedCompareToTest(){
        val feed1: Feed = Feed(title = "The compareTo() method")
        val feed2: Feed = Feed(title = "ZcompareTo() method")
        val feed3: Feed = Feed(title = "The compareTo() method")
        val feed4: Feed = Feed(title = "compareTo() method")

        assertEquals(true, feed1.compareTo(feed2) < 0)
        assertEquals(true, feed1.compareTo(feed3) == 0)
        assertEquals(true, feed1.compareTo(feed4) > 0)
    }

}