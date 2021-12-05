package monster.minions.binocularss

import androidx.compose.ui.test.assertIsDisplayed

import monster.minions.binocularss.activities.MainActivity
import org.junit.Rule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import monster.minions.binocularss.activities.AddFeedActivity
import org.junit.Test

class AddFeedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    @Test
    fun MyTest() {
        composeTestRule.onNodeWithContentDescription("Add Feed Activity").performClick()
        composeTestRule.onNodeWithText("Enter Feed URL").assertExists()
    }
}