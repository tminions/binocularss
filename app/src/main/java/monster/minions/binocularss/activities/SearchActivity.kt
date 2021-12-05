package monster.minions.binocularss.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.*
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.ArticleCard
import java.util.*

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
    private var isFirstRun = true
    private var cacheExpiration = 0L

    private var text = mutableStateOf("")

    private var feedTitles: MutableList<String> = mutableListOf()

    @ExperimentalCoilApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            BinoculaRSSTheme(
                theme = themeState.value
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
        theme = sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        if (!isFirstRun) {
            themeState.value = theme
        }
        isFirstRun = false

        MainActivity.articleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        MainActivity.bookmarkedArticleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        MainActivity.readArticleList.value = sortArticlesByDate(getReadArticles(feedGroup))
        MainActivity.feedList.value = sortFeedsByTitle(feedGroup.feeds)
        MainActivity.searchResults.value = sortArticlesByFuzzyMatch(getAllArticles(feedGroup), text.value)
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
            sortArticlesByFuzzyMatch(getAllArticles(feedGroup), text.value)

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
            // For each article in the list, render a card.
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
            placeholder = { Text("Search for an article(s)") },
            onValueChange = {
                textState.value = it
                text = mutableStateOf(textState.value.text)
            },
            singleLine = true,
            maxLines = 1,
            trailingIcon = { Icon(Icons.Filled.Search, null) },
            keyboardActions = KeyboardActions(
                onDone = {
                    submit()
                }
            )
        )
    }

    @ExperimentalCoilApi
    @Composable
    @Preview
    fun UI() {
        // Set status bar and nav bar colours.
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colors.background,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    SearchBar()
                }
                ArticleSearchResults()
            }
        }
    }
}
