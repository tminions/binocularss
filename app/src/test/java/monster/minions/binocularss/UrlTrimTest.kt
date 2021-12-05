package monster.minions.binocularss

import androidx.test.ext.junit.runners.AndroidJUnit4
import monster.minions.binocularss.activities.AddFeedActivity
import monster.minions.binocularss.operations.trimWhitespace
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * Test to see if URL trimming works properly.
 *
 */
class UrlTrimTest {
    @Test
    fun windowsLineEndingTrimTest() {
        val actual = trimWhitespace("hello world!\r\n")
        assertEquals("hello world!", actual)
    }
}