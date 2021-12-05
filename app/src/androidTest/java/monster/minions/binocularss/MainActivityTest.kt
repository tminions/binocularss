package monster.minions.binocularss

import androidx.compose.ui.test.assertIsDisplayed

import monster.minions.binocularss.activities.MainActivity
import org.junit.Rule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun MyTest() {
        composeTestRule.onNodeWithText("BinoculaRSS").assertExists()

    }
}