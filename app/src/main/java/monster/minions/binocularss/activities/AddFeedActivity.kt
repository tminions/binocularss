package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.activities.ui.theme.paddingLarge
import monster.minions.binocularss.activities.ui.theme.paddingMedium
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.*
import kotlin.properties.Delegates
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.CuratedFeeds

// TODO check that this is an RSS feed (probably in pull feed or something of the sort and send a
//  toast to the user if it is not. Try and check this when initially adding maybe? Basically deeper
//  checking than just is this a URL so we don't encounter errors later. Or we could leave it and
//  have PullFeed silently remove a feed if it is invalid like it currently does.
class AddFeedActivity : ComponentActivity() {

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room database variables
    private lateinit var dataGateway: DatabaseGateway

    private var text = mutableStateOf("")

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var materialYou by Delegates.notNull<Boolean>()
    private lateinit var materialYouState: MutableState<Boolean>
    private var cacheExpiration = 0L

    /**
     * The function that is run when the activity is created. This is on app launch in this case.
     * It is also called when the activity is destroyed then recreated. It initializes the main
     * functionality of the application (UI, companion variables, etc.)
     *
     * @param savedInstanceState A bundle of parcelable information that was previously saved.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            materialYouState = remember { mutableStateOf(materialYou) }
            BinoculaRSSTheme(
                theme = themeState.value,
                materialYou = materialYouState.value
            ) {
                UI()
            }
        }

        sharedPref = this.getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)
        cacheExpiration = sharedPref.getLong(SettingsActivity.PreferenceKeys.CACHE_EXPIRATION, 0L)

        // Set private variables. This is done here as we cannot initialize objects that require context
        //  before we have context (generated during onCreate)

        dataGateway = DatabaseGateway(context = this)


        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(cacheExpirationMillis = cacheExpiration)
            .build()

        // Handle a link being shared to the application
        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            setTextView(intent)
        }
    }

    /**
     * Save the list of user feeds to the Room database (feed-db) for data persistence.
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called before `onDestroy` or any time a "stop" happens. This
     * includes when an app is exited but not closed.
     */
    override fun onStop() {
        super.onStop()
        Log.d("AddFeedActivity", "onStop called")
        dataGateway.addFeeds(feedGroup.feeds)
    }

    /**
     * Gets the list of user feeds from the Room database (feed-db).
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called after `onCreate` or any time a "resume" happens. This includes
     * the app being opened after the app is exited but not closed.
     */
    override fun onResume() {
        super.onResume()
        Log.d("AddFeedActivity", "onResume called")
        feedGroup.feeds = dataGateway.read()

        theme = sharedPref
            .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
            .toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)

        MainActivity.articleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        MainActivity.bookmarkedArticleList.value =
            sortArticlesByDate(getBookmarkedArticles(feedGroup))
        MainActivity.readArticleList.value = sortArticlesByDate(getReadArticles(feedGroup))
        MainActivity.feedList.value = sortFeedsByTitle(feedGroup.feeds)
        MainActivity.currentFeedArticles.value = sortArticlesByDate(getArticlesFromFeed(MainActivity.currentFeed))
    }

    /**
     * Set the value of the text view from an extra stored in the intent
     *
     * @param intent The intent passed to the activity upon launch.
     */
    private fun setTextView(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            text.value = it
        }
    }

    fun addTofeedGroup(url: String, feedExistsCallback: () -> Unit) {
        // Check if the feed is already in the feedGroup
        val feedToAdd = Feed(source = url)
        var inFeedGroup = false
        for (feed in feedGroup.feeds) {
            if (feedToAdd == feed) {
                inFeedGroup = true
                feedExistsCallback()
                break
            }
        }

        // Add feed and update feeds if the feed is not in the feedGroup
        if (!inFeedGroup) {
            feedGroup.feeds.add(Feed(source = url))
            val viewModel = PullFeed(this, feedGroup)
            viewModel.updateRss(parser)
            finish()
        }
    }


    /**
     * Add a feed with the given source url to the feedGroup if the source url is valid.
     */
    private fun submit() {
        // Add https:// or http:// to the front of the url if not present
        val url = addHttps(trimWhitespace(text.value))

        // If the url is valid ...
        if (Patterns.WEB_URL.matcher(url).matches()) {
            addTofeedGroup(url) {
                Toast.makeText(
                    this@AddFeedActivity,
                    "You've already added that RSS feed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@AddFeedActivity, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * TextField where feed url is inputted.
     */
    @Composable
    fun FeedTextField(textValue: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = textValue,
            placeholder = { Text("Enter Feed URL") },
            onValueChange = onValueChange,
            singleLine = true,
            maxLines = 1,
            keyboardActions = KeyboardActions(
                // When you press the enter button on the keyboard.
                onDone = { submit() }
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.disabled),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.disabled),
                backgroundColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity)
                    .copy(alpha = ContentAlpha.disabled),
                errorLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                trailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = TextFieldDefaults.IconOpacity)
                    .copy(alpha = ContentAlpha.disabled),
                errorTrailingIconColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = ContentAlpha.high),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(ContentAlpha.medium)
                    .copy(ContentAlpha.disabled),
                errorLabelColor = MaterialTheme.colorScheme.error,
                placeholderColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.disabled),
                disabledPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(ContentAlpha.disabled)
            )
        )
    }

    /**
     * Main UI of the AddFeedActivity.
     */
    @Composable
    fun UI() {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = when (theme) {
            "Dark Theme" -> false
            "Light Theme" -> true
            else -> !isSystemInDarkTheme()
        }
        val color = MaterialTheme.colorScheme.background
        var textState by remember { mutableStateOf(TextFieldValue()) }

        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(paddingLarge),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        FeedTextField(textState, onValueChange = {
                            textState = it
                            text = mutableStateOf(textState.text)
                        })
                    }
                    Spacer(modifier = Modifier.padding(paddingMedium))
                    FloatingActionButton(
                        onClick = { submit() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Add feed"
                        )
                    }
                }
                CuratedFeeds(
                    feeds = listOf(
                        Feed(
                            source = "https://rss.cbc.ca/lineup/topstories.xml",
                            title = "CBC Top Stories"
                        ),
                        Feed(source = "https://rss.cbc.ca/lineup/world.xml", title = "CBC World"),
                        Feed(source = "https://rss.cbc.ca/lineup/canada.xml", title = "CBC Canada"),
                        Feed(
                            source = "https://rss.cbc.ca/lineup/politics.xml",
                            title = "CBC Politics"
                        ),
                        Feed(
                            source = "https://androidauthority.com/feed",
                            title = "Android Authority"
                        ),
                        Feed(
                            source = "https://www.ctvnews.ca/rss/ctvnews-ca-top-stories-public-rss-1.822009",
                            title = "CTV Top Stories"
                        ),
                    ), addFeedToGroup = ::addTofeedGroup, existingFeeds = feedGroup.feeds
                )
            }
        }
    }
}