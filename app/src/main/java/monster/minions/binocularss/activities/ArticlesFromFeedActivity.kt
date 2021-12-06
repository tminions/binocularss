package monster.minions.binocularss.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.activities.ui.theme.paddingSmall
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.*
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.ArticleCard
import kotlin.properties.Delegates

class ArticlesFromFeedActivity : ComponentActivity() {
    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

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

    @ExperimentalMaterial3Api
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

        MainActivity.currentFeedArticles.value = sortArticlesByDate(getArticlesFromFeed(MainActivity.currentFeed))
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

        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)

        // Update article lists.
        MainActivity.articleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        MainActivity.bookmarkedArticleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        MainActivity.readArticleList.value = sortArticlesByDate(getReadArticles(feedGroup))
        MainActivity.currentFeedArticles.value = sortArticlesByDate(getArticlesFromFeed(MainActivity.currentFeed))
        MainActivity.feedList.value = sortFeedsByTitle(feedGroup.feeds)
        MainActivity.currentFeedArticles.value = sortArticlesByDate(getArticlesFromFeed(MainActivity.currentFeed))
    }

    /**
     * Displays a list of articles from a specific feed in the order given by the currently
     * selected sorting method
     */
    @Composable
    fun ArticlesFromFeed() {
        // Mutable state variable that is updated when articleList is updated to force a recompose.
        val articles by MainActivity.currentFeedArticles.collectAsState()

        // LazyColumn containing the article cards.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = articles) { article ->
                ArticleCard(
                    context = this@ArticlesFromFeedActivity,
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
        // Replace article in feedGroup.
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

        // Update article lists.
        MainActivity.articleList.value = mutableListOf()
        MainActivity.bookmarkedArticleList.value = mutableListOf()
        MainActivity.readArticleList.value = mutableListOf()
        MainActivity.feedList.value = mutableListOf()
        MainActivity.currentFeedArticles.value = sortArticlesByDate(getArticlesFromFeed(MainActivity.currentFeed))
    }

    /**
     * Top navigation bar
     */
    @Composable
    fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button icon that goes back one activity.
            IconButton(onClick = { finish() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(paddingSmall))
            // Title of current page.
            Text(
                MainActivity.currentFeed.title!!,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
        }
    }

    @ExperimentalMaterial3Api
    @Preview(showBackground = true)
    @Composable
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
            Scaffold(topBar = { TopBar() }) {
                ArticlesFromFeed()
            }
        }
    }
}
