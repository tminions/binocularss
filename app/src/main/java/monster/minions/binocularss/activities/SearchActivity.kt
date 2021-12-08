package monster.minions.binocularss.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.activities.ui.theme.paddingMedium
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.*
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.ArticleCard
import java.util.*
import kotlin.properties.Delegates

class SearchActivity : ComponentActivity() {
    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room Database variables
    private lateinit var dataGateway: DatabaseGateway

    // User Preferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var materialYou by Delegates.notNull<Boolean>()
    private lateinit var materialYouState: MutableState<Boolean>
    private var cacheExpiration = 0L

    private var text = mutableStateOf("")

    private var feedTitles: MutableList<String> = mutableListOf()

    @ExperimentalCoilApi
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

        // Restore shared preferences
        sharedPref = getSharedPreferences(
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
            .cacheExpirationMillis(60L * 60L * 100L)
            .build()
    }

    /**
     * Save the list of user feeds to the Room database (feed-db) for data persistence.
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called before `onDestroy` or any time a "pause" happens. This
     * includes when an app is exited but not closed.
     */
    override fun onPause() {
        super.onPause()
        Log.d("SearchActivity", "onPause called")
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
        Log.d("SearchActivity", "onResume called")
        feedGroup.feeds = dataGateway.read()
        feedTitles = getFeedTitles()

        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)

        val sortArticlesByDate = SortArticles(SortArticlesByDateStrategy())
        MainActivity.articleList.value = sortArticlesByDate.sort(getAllArticles(feedGroup))
        MainActivity.bookmarkedArticleList.value =
            sortArticlesByDate.sort(getAllArticles(feedGroup))
        MainActivity.readArticleList.value = sortArticlesByDate.sort(getReadArticles(feedGroup))
        MainActivity.currentFeedArticles.value =
            sortArticlesByDate.sort(getArticlesFromFeed(MainActivity.currentFeed))
        MainActivity.feedList.value = SortFeeds(SortFeedsByTitleStrategy()).sort(feedGroup.feeds)
        MainActivity.searchResults.value =
            SortArticles(SortArticlesByFuzzyMatchStrategy(text.value)).sort(
                getAllArticles(feedGroup)
            )
    }

    private fun getFeedTitles(): MutableList<String> {
        val feeds = feedGroup.feeds
        val feedTitles = mutableListOf("All feeds")

        for (feed in feeds) {
            feedTitles.add(feed.title.toString())
        }
        return feedTitles
    }

    /**
     * Retrieve all articles that could match the given
     * query
     */
    private fun submit() {
        Log.d("SearchActivity", "Submitting")
        MainActivity.searchResults.value =
            SortArticles(SortArticlesByFuzzyMatchStrategy(text.value)).sort(
                getAllArticles(feedGroup)
            )

        if (MainActivity.searchResults.value.isEmpty()) {
            Toast.makeText(this@SearchActivity, "No matches", Toast.LENGTH_SHORT).show()
        }
    }

    @ExperimentalCoilApi
    @Composable
    fun ArticleSearchResults() {
        val articles by MainActivity.searchResults.collectAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = articles) { article ->
                ArticleCard(
                    context = this@SearchActivity,
                    article = article
                ) { setArticle(it) }
            }
        }
    }

    /**
     * Replace the unmodified article with a modified article.
     * This is to be used when updating article.bookmarked and article.read
     *
     * @param modifiedArticle Article with a modified property.
     */
    private fun setArticle(modifiedArticle: Article, refreshBookmark: Boolean = true) {
        for (feed in feedGroup.feeds) {
            val articles = feed.articles.toMutableList()
            for (unmodifiedArticle in articles) {
                if (modifiedArticle == unmodifiedArticle) {
                    feed.articles.remove(unmodifiedArticle)
                    feed.articles.add(modifiedArticle)
                    break
                }
            }
        }

        MainActivity.articleList.value = mutableListOf()
        MainActivity.bookmarkedArticleList.value = mutableListOf()
        MainActivity.currentFeedArticles.value = mutableListOf()
        MainActivity.readArticleList.value = mutableListOf()
        MainActivity.feedList.value = mutableListOf()
    }

    /**
     * SearchBar Composable
     */
    @Composable
    fun SearchBar() {
        val textState = remember { mutableStateOf(TextFieldValue()) }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = textState.value,
            placeholder = { Text("Search for an Article") },
            onValueChange = {
                textState.value = it
                text = mutableStateOf(textState.value.text)
            },
            singleLine = true,
            maxLines = 1,
            keyboardActions = KeyboardActions(
                onDone = {
                    submit()
                }
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

    @ExperimentalCoilApi
    @Composable
    @Preview
    fun UI() {
        // Set status bar and nav bar colours.
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = when (theme) {
            "Dark Theme" -> false
            "Light Theme" -> true
            else -> !isSystemInDarkTheme()
        }
        val color = MaterialTheme.colorScheme.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingMedium)
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        SearchBar()
                    }
                    Spacer(modifier = Modifier.padding(paddingMedium))
                    FloatingActionButton(
                        onClick = { submit() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Add feed"
                        )
                    }
                }
                ArticleSearchResults()
            }
        }
    }
}
