package monster.minions.binocularss

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import monster.minions.binocularss.activities.MainActivity
import org.junit.Rule
import org.junit.Test

class SearchActivityTest {
    @get: Rule
    val composeTestRule = createAndroidComposeRule(MainActivity::class.java)

    @Test
    fun SearchIconTest() {
        val searchbutton =
            composeTestRule.onNode(hasContentDescription("Search Activity"), useUnmergedTree = true)
        searchbutton.assertIsDisplayed()
    }

    @Test
    fun SearchBarTest() {
        composeTestRule.onNodeWithContentDescription("Search Activity").performClick()
        composeTestRule.onNodeWithText("Search for an article(s)").assertExists()
    }
}