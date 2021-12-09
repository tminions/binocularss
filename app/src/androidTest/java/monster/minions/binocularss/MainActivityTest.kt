package monster.minions.binocularss

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import monster.minions.binocularss.activities.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test all buttons from MainActivity are displayed/working as intended.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun HeaderExistsTest() {
        val header = composeTestRule.onNodeWithText("BinoculaRSS")
        header.assertIsDisplayed()
    }

    @Test
    fun AddFeedButtonExistsTest() {
        val addFeedButton = composeTestRule.onNodeWithContentDescription("Add Feed Activity")
        addFeedButton.assertIsDisplayed()
    }

    @Test
    fun SearchIconTest() {
        val searchbutton =
            composeTestRule.onNode(hasContentDescription("Search Activity"), useUnmergedTree = true)
        searchbutton.assertIsDisplayed()
    }

    @Test
    fun SearchBarTest() {
        composeTestRule.onNodeWithContentDescription("Search Activity").performClick()
        val searchBar = composeTestRule.onNodeWithText("Search for an Article")
        searchBar.assertIsDisplayed()
    }

    @Test
    fun SettingsIconTest() {
        val settingsIcon = composeTestRule.onNodeWithContentDescription("Settings Activity")
        settingsIcon.assertIsDisplayed()
    }

    @Test
    fun SettingsBackButtonTest() {
        composeTestRule.onNodeWithContentDescription("Settings Activity").performClick()
        val settingsBackButton = composeTestRule.onNodeWithContentDescription("Back Arrow")
        settingsBackButton.assertIsDisplayed()
    }

    @Test
    fun SettingsTextTest(){
        composeTestRule.onNodeWithContentDescription("Settings Activity").performClick()
        val settingsTextButton = composeTestRule.onNodeWithText("Settings")
        settingsTextButton.assertIsDisplayed()
    }


//    @Test
//    fun OpenSourceLicensesTest(){
//        composeTestRule.onNodeWithContentDescription("Settings Activity").performClick()
//        composeTestRule.onNode
//    }
}