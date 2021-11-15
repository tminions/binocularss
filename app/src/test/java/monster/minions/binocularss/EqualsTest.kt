package monster.minions.binocularss

import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class EqualsTest {

    @Test
    fun articleEqualsTest(){
        val article1 = Article(link = "http://www.feedforall.com/industry-solutions.htm")
        val article2 = Article(link = "http://www.feedforall.com/schools.htm")
        val article3 = Article(link = "http://www.feedforall.com/industry-solutions.htm")
        val article4 = Article(link = "http://www.feedforall.com/restaurant.htm")

        val feed1 = Feed(link = "http://www.feedforall.com")
        val feed2 = Feed(link = "http://www.feedforall.com/industry-solutions.htm")

        assertEquals(false, article1 == article2)
        assertEquals(true, article1 == article3)
        assertEquals(false, article1 == article4)
        assertEquals(false, article1.equals(feed1))
        assertEquals(false, article1.equals(feed2))
    }

    @Test
    fun feedEqualsTest(){
        val feed1 = Feed(source = "http://www.feedforall.com")
        val feed2 = Feed(source = "http://www.feedforall.com/industry-solutions.htm")
        val feed3 = Feed(source = "http://www.feedforall.com")

        val article3 = Article(link = "http://www.feedforall.com")
        val article4 = Article(link = "http://www.feedforall.com/restaurant.htm")

        assertEquals(false, feed1 == feed2)
        assertEquals(true, feed1 == feed3)
        assertEquals(false, feed1.equals(article3))
        assertEquals(false, feed1.equals(article4))
    }

    @Test
    fun feedGroupEqualsTest(){
        val feed1 = Feed(source = "http://www.feedforall.com")
        val feed2 = Feed(source = "http://www.feedforall.com/industry-solutions.htm")
        val feed3 = Feed(source = "https://www.androidauthority.com/feed/")
        val feeds1: MutableList<Feed> = mutableListOf()
        val feeds2: MutableList<Feed> = mutableListOf()
        val feeds3: MutableList<Feed> = mutableListOf()
        feeds1.add(feed1)
        feeds2.add(feed1)
        feeds3.add(feed1)
        feeds1.add(feed2)
        feeds2.add(feed2)
        feeds3.add(feed3)

        val feedGroup1 = FeedGroup(feeds1)
        val feedGroup2 = FeedGroup(feeds1)
        val feedGroup3 = FeedGroup(feeds1)

        assertEquals(true, feedGroup1 == feedGroup2)
        assertEquals(false, feedGroup1 == feedGroup3)
    }
}