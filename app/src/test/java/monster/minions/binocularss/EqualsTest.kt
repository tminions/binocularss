package monster.minions.binocularss

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import org.junit.Assert.assertEquals
import org.junit.Test

class EqualsTest {

    @Test
    fun articleEqualsTest(){
        val article1: Article = Article(link = "http://www.feedforall.com/industry-solutions.htm")
        val article2: Article = Article(link = "http://www.feedforall.com/schools.htm")
        val article3: Article = Article(link = "http://www.feedforall.com/industry-solutions.htm")
        val article4: Article = Article(link = "http://www.feedforall.com/restaurant.htm")

        val feed1: Feed = Feed(link = "http://www.feedforall.com")
        val feed2: Feed = Feed(link = "http://www.feedforall.com/industry-solutions.htm")
        val feed3: Feed = Feed(link = "http://www.feedforall.com")

        assertEquals(false, article1.equals(article2))
        assertEquals(true, article1.equals(article3))
        assertEquals(false, article1.equals(article4))
        assertEquals(false, article1.equals(feed1))
        assertEquals(false, article1.equals(feed2))
    }

    @Test
    fun feedEqualsTest(){
        val feed1: Feed = Feed(source = "http://www.feedforall.com")
        val feed2: Feed = Feed(source = "http://www.feedforall.com/industry-solutions.htm")
        val feed3: Feed = Feed(source = "http://www.feedforall.com")

        val article3: Article = Article(link = "http://www.feedforall.com")
        val article4: Article = Article(link = "http://www.feedforall.com/restaurant.htm")

        assertEquals(false, feed1.equals(feed2))
        assertEquals(true, feed1.equals(feed3))
        assertEquals(false, feed1.equals(article3))
        assertEquals(false, feed1.equals(article4))
    }
}