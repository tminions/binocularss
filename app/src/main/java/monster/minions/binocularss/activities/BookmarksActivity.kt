package monster.minions.binocularss.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.*
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.*

class BookmarksActivity : AppCompatActivity() {
    companion object {
        lateinit var bookmarkedArticleList: MutableStateFlow<MutableList<Article>>
    }

    // Set up room database for this specific activity
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    private var feedGroup = FeedGroup()

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var cacheExpiration = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            BinoculaRSSTheme(
                theme = themeState.value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    UI(sortArticlesByDate(getBookmarkedArticles(feedGroup)))
                }
            }
        }

        sharedPref = this.getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref
            .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
            .toString()
        cacheExpiration = sharedPref.getLong(SettingsActivity.PreferenceKeys.CACHE_EXPIRATION, 0L)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "feed-db"
        ).allowMainThreadQueries().build()
        feedDao = (db as AppDatabase).feedDao()

        bookmarkedArticleList = MutableStateFlow(mutableListOf())
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
            IconButton(onClick = {
                finish()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(4.dp))
            // Title of current page.
            Text("Bookmarks", style = MaterialTheme.typography.h5)
        }
    }

    // FIXME do the same state stuff to this one but only for bookmarked articles
    /**
     * Composable function that generates the list of bookmarked
     * articles
     *
     * @param bookmarked_articles MutableList of articles that are bookmarked
     * across all feeds
     */
    @Composable
    fun UI(bookmarked_articles: MutableList<Article>) {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(color = MaterialTheme.colors.background) {
            Scaffold(topBar = { TopBar() }) {
                // Swipe refresh variables.
                var isRefreshing by remember { mutableStateOf(false) }
                val list by bookmarkedArticleList.collectAsState()

                // SwipeRefresh to refresh the list of bookmarked articles
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                    onRefresh = {
                        // update the list
                        isRefreshing = true
                        bookmarkedArticleList.value = sortArticlesByDate(getBookmarkedArticles(feedGroup))
                        isRefreshing = false
                    }
                ) {
                    // LazyColumn containing the bookmarked article cards.
                    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                        items(items = list) { article ->
                            // For each article in list, render a card.
                            ArticleCard(this@BookmarksActivity,
                                article = article) { setArticle(article) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Save the list of user feeds to the Room database (feed-db) for data persistence.
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called before `onStop` and `onDestroy` or any time a "stop" happens. This
     * includes when an app is exited but not closed.
     */
    override fun onPause() {
        super.onPause()
        Log.d("BookmarksActivity", "onPause called")
        feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))

        MainActivity.articleList.value = mutableListOf()
        MainActivity.feedList.value = mutableListOf()
    }

    /**
     * Replace the unmodified article with a modified article.
     * This is to be used when updating article.bookmarked and article.read
     *
     * @param modifiedArticle Article with a modified property.
     */
    private fun setArticle(modifiedArticle: Article) {
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
    }

    /**
     * Get the list of user feeds from the Room database (feed-db).
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called after `onCreate` or any time a "resume" happens. This includes
     * the app being opened after the app is exited but not closed.
     */
    override fun onResume() {
        super.onResume()
        Log.d("BookmarksActivity", "onResume called")
        feedGroup.feeds = feedDao.getAll()

        // MainActivity.articleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        // MainActivity.feedList.value = sortFeedsByTitle(feedGroup.feeds)
        bookmarkedArticleList.value = sortArticlesByDate(getBookmarkedArticles(feedGroup))
    }
}