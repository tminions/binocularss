package monster.minions.binocularss

import androidx.compose.ui.test.*

import monster.minions.binocularss.activities.MainActivity
import org.junit.Rule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import monster.minions.binocularss.activities.AddFeedActivity
import org.junit.Test

class ArticleAsReadTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    @Test
    fun MyTest() {
        composeTestRule.onNodeWithText("article").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Add Feed Activity").performClick()
        composeTestRule.onNodeWithText("Enter Feed URL").assertExists()
        composeTestRule.onNodeWithText("Enter Feed URL").performTextInput("https://www.cbc.ca/cmlink/rss-topstories")
        composeTestRule.onNodeWithContentDescription("Add feed").performClick()
    }
}