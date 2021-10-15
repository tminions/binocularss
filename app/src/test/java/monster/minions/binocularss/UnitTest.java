package monster.minions.binocularss;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Local unit tests, which will execute on the development machine (host).
 *
 */
public class UnitTest {
    /**
     * Test for url http appending
     */
    @Test
    public void feedHttpTest() {
        Feed feed = new Feed("rss.cbc.ca/lineup/topstories.xml");
        assertEquals("http://rss.cbc.ca/lineup/topstories.xml", feed.getUrl());
    }

    /**
     * Test that https is not overwritten with http
     */
    @Test
    public void feedHttpsHttpTest() {
        Feed feed = new Feed("https://rss.cbc.ca/lineup/topstories.xml");
        assertEquals("https://rss.cbc.ca/lineup/topstories.xml", feed.getUrl());
    }
}