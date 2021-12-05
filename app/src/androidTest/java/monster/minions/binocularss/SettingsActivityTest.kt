package monster.minions.binocularss

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import monster.minions.binocularss.activities.MainActivity
import org.junit.Rule
import org.junit.Test

class SettingsActivityTest {
    @get: Rule
    val composeTestRule = createAndroidComposeRule(MainActivity::class.java)

    @Test
    fun SettingsIconTest() {
    composeTestRule.onNodeWithContentDescription("Settings Activity").assertIsDisplayed()
    }

    @Test
    fun SettingsBackButtonTest() {
        composeTestRule.onNodeWithContentDescription("Settings Activity").performClick()
        composeTestRule.onNodeWithContentDescription("Back Arrow").assertIsDisplayed()
    }
}