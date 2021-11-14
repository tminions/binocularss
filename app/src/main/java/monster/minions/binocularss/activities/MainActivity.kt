package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.ArticleDateComparator
import monster.minions.binocularss.operations.FeedTitleComparator
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.*
import java.util.*

class MainActivity : ComponentActivity() {
    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()
    private lateinit var sortedArticles: MutableList<Article>

    // Parser variable
    private lateinit var parser: Parser

    // Room database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    // User Preferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var isFirstRun = true
    private var cacheExpiration = 0L

    // Companion object as this variable needs to be updated from other asynchronous classes.
    companion object {
        var feedGroupText = MutableStateFlow("Empty\n")
//        lateinit var list: SnapshotStateList<Article>
    }


    // Local variables
    private lateinit var currentFeed: Feed
    private lateinit var currentArticle: Article

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
            BinoculaRSSTheme(
                theme = themeState.value
            ) {
                UI()
            }

            currentFeed = Feed("default")
        }

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
        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()
        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(cacheExpirationMillis = cacheExpiration)
            .build()

        //list = getAllArticles().toMutableStateList()
    }

    /**
     * Save the list of user feeds to the Room database (feed-db) for data persistence.
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called before `onDestroy` or any time a "stop" happens. This
     * includes when an app is exited but not closed.
     */
    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
        feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
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
        Log.d("MainActivity", "onResume called")

        val feeds: MutableList<Feed> = feedDao.getAll()

        feedGroup.feeds = feeds

        sortedArticles = getAllArticlesSortedByDate()

        ///////////////////////////////////////////////////////////////////////////////////////////
        // NOT PERMANENT: If the user does not have any feeds added, add some.
        if (feedGroup.feeds.isNullOrEmpty()) {
            // Add some feeds to the feedGroup
            feedGroup.feeds.add(Feed(source = "https://rss.cbc.ca/lineup/topstories.xml"))
            feedGroup.feeds.add(Feed(source = "https://androidauthority.com/feed"))
            // feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
            // feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))

            // Inform the user of this
            Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
                .show()
        }
        ///////////////////////////////////////////////////////////////////////////////////////////

        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        if (!isFirstRun) {
            themeState.value = theme
        }
        isFirstRun = false
        updateText()
    }

    /**
     * Update the text for UI elements
     *
     * TODO this function can be modified to update other UI elements after the
     *  feeds have been fetched as well at which point it should be named updateUI
     *  or something along those lines.
     */
    private fun updateText() {
        var text = ""
        for (feed in feedGroup.feeds) {
            text += feed.title
            text += "\n"
        }
        feedGroupText.value = text
    }

    /**
     * Displays the list of feeds saved
     * TODO sort feeds alphabetically
     */
    @Composable
    fun FeedTitles() {
        if (feedGroup.feeds.isNullOrEmpty()) {
            // Sad minion no feeds found
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "No Feeds Found")
                Spacer(Modifier.padding(16.dp))
                AddFeedButton()
            }
        } else {
            val feedTitleComparator = FeedTitleComparator()
            feedGroup.feeds.sortWith(comparator = feedTitleComparator)
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .border(1.dp, Color.Black)
            ) {
                items(items = feedGroup.feeds) { feed ->
                    FeedCard(context = this@MainActivity, feed = feed)
                }
            }
        }
    }

    /**
     * Displays the list of articles associated with a given feed
     */
    @Composable
    fun ArticlesFromFeed() {
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = currentFeed.articles) { article ->
                ArticleCard(context = this@MainActivity, article = article)
            }
        }
    }

    private fun getAllArticles(): MutableList<Article> {
        val articles: MutableList<Article> = mutableListOf()

        for (feed in feedGroup.feeds) {
            for (article in feed.articles) {
                articles.add(article)
            }
        }

        return articles
    }

    private fun getAllArticlesSortedByDate(): MutableList<Article> {
        val articles = getAllArticles()
        val dateComparator = ArticleDateComparator()
        return articles.sortedWith(comparator = dateComparator).toMutableList()
    }

    /**
     * Displays a list of articles in the order given by the currently selected sorting method
     */
    @Composable
    fun SortedArticleView(articles: MutableList<Article>) {
        // val list = remember { articles.toMutableStateList() }

        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = articles) { article ->
                ArticleCard(context = this@MainActivity, article = article)
            }
        }
    }

    /**
     * The default UI state of the app.
     */
    @Composable
    fun UI() {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        // Get elevated color to match the bottom bar that is also elevated by 8.dp
        val elevatedColor = LocalElevationOverlay.current?.apply(color = color, elevation = 8.dp)
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )

            systemUiController.setNavigationBarColor(
                color = elevatedColor!!
            )
        }

        Surface(color = MaterialTheme.colors.background) {

            val navController = rememberNavController()
            val viewModel = PullFeed(this@MainActivity, feedGroup = feedGroup)
            val isRefreshing by remember { mutableStateOf(viewModel.isRefreshing) }

            Scaffold(
                topBar = { TopBar() },
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                    onRefresh = {
                        viewModel.updateRss(parser)
                    }
                ) {
                    Navigation(navController)
                }
            }
        }
    }

    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Article.route) {
            composable(NavigationItem.Article.route) {
                // TODO either do this or just call getAll articles then sort them
                SortedArticleView(sortedArticles)
            }
            composable(NavigationItem.Feed.route) {
                FeedTitles()
            }
        }
    }

    @Composable
    fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("BinoculaRSS", style = MaterialTheme.typography.h5)
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmarks Activity Button
                IconButton(onClick = {
                    val intent = Intent(this@MainActivity, BookmarksActivity::class.java).apply {}
                    startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Bookmark Activity"
                    )
                }

                IconButton(onClick = {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java).apply {}
                    startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings Activity"
                    )
                }

                // Add Feed Activity Button
                IconButton(onClick = {
                    val intent = Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
                    startActivity(intent)
                    feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Feed Activity"
                    )
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavController) {
        val items = listOf(
            NavigationItem.Article,
            NavigationItem.Feed,
        )
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground
        ) {
            items.forEach { item ->
                BottomNavigationItem(
                    icon = {
                        Icon(
                            painterResource(id = item.icon),
                            contentDescription = item.title
                        )
                    },
                    label = { Text(text = item.title) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(0.4f),
                    alwaysShowLabel = true,
                    selected = false,
                    onClick = {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                // Pop up to the start destination of the graph to avoid building up
                                //  a large stack of destinations on the back stack as users select
                                //  items
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            // Avoid multiple copies of the same destination when re-selecting the
                            //  same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun AddFeedButton() {
        Button(
            onClick = {
                val intent = Intent(this, AddFeedActivity::class.java).apply {}
                feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                startActivity(intent)
            }
        ) {
            Text("Add Feed")
        }
    }

    @Composable
    fun ClearFeeds() {
        Button(
            onClick = {
                for (feed in feedGroup.feeds) {
                    feedDao.deleteBySource(feed.source)
                }
                feedGroup.feeds = mutableListOf()
                updateText()
            }
        ) {
            Text("Clear DB")
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        UI()
    }
}