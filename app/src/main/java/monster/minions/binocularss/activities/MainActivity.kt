package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import monster.minions.binocularss.operations.*
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.*
import java.util.*

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var articleList: MutableStateFlow<MutableList<Article>>
        lateinit var bookmarkedArticleList: MutableStateFlow<MutableList<Article>>
        lateinit var feedList: MutableStateFlow<MutableList<Feed>>
    }

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

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
    @ExperimentalAnimationApi
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

        // Set shared preferences variables.
        sharedPref = getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref
            .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
            .toString()
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

        // Refresh LazyColumn composables
        articleList = MutableStateFlow(mutableListOf())
        bookmarkedArticleList = MutableStateFlow(mutableListOf())
        feedList = MutableStateFlow(mutableListOf())
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

        articleList.value = sortArticlesByDate(getAllArticles(feedGroup))

        if (refreshBookmark) {
            bookmarkedArticleList.value = sortArticlesByDate(getBookmarkedArticles(feedGroup))
        }

        feedList.value = sortFeedsByTitle(feedGroup.feeds)
    }

    /**
     * Replace the unmodified article with a modified article.
     * This is to be used when updating feed.tags
     *
     * @param modifiedFeed Feed with a modified property.
     */
    private fun setFeeds(modifiedFeed: Feed) {
        val feeds = feedGroup.feeds.toMutableList()
        for (unmodifiedFeed in feeds) {
            if (modifiedFeed == unmodifiedFeed) {
                feedGroup.feeds.remove(unmodifiedFeed)
                feedGroup.feeds.add(modifiedFeed)
                break
            }
        }

        feedList.value = sortFeedsByTitle(feedGroup.feeds)
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

        theme = sharedPref
            .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
            .toString()
        if (!isFirstRun) {
            themeState.value = theme
        }
        isFirstRun = false

        articleList.value = sortArticlesByDate(getAllArticles(feedGroup))
        bookmarkedArticleList.value = sortArticlesByDate(getBookmarkedArticles(feedGroup))
        feedList.value = sortFeedsByTitle(feedGroup.feeds)
    }

    /**
     * Displays the list of feeds saved
     */
    @Composable
    fun SortedFeedView() {
        var showAddFeed by remember { mutableStateOf(feedGroup.feeds.isNullOrEmpty()) }
        if (showAddFeed) {
            // Sad minion no feeds found
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Feeds Found",
                    style = MaterialTheme.typography.h5
                )
                Spacer(Modifier.padding(16.dp))
                Button(
                    onClick = {
                        showAddFeed = false
                        val intent = Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
                        feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                        startActivity(intent)
                    }
                ) {
                    Text("Add Feed")
                }
            }
        } else {
            val feeds by feedList.collectAsState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items = feeds) { feed ->
                    FeedCard(context = this@MainActivity, feed = feed)
                }
            }
        }
    }

    /**
     * Displays the list of articles associated with a given feed
     *
     * TODO to be implemented
     */
    @Composable
    fun ArticlesFromFeed() {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = currentFeed.articles) { article ->
                ArticleCard(context = this@MainActivity, article = article) { setArticle(it) }
            }
        }
    }

    /**
     * Displays a list of articles in the order given by the currently selected sorting method
     */
    @Composable
    fun SortedArticleView() {
        // Mutable state variable that is updated when articleList is updated to force a recompose.
        val articles by articleList.collectAsState()

        if (articles.isNullOrEmpty()) {
            // Sad minion no article found
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Articles Found",
                    style = MaterialTheme.typography.h5
                )
                Spacer(Modifier.padding(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
                        feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                        startActivity(intent)
                    }
                ) {
                    Text("Add Feed")
                }
            }
        } else {
            // LazyColumn containing the article cards.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // For each article in the list, render a card.
                items(items = articles) { article ->
                    ArticleCard(context = this@MainActivity, article = article) { setArticle(it) }
                }
            }
        }
    }

    /**
     * Displays a list of all bookmarked articles.
     */
    @ExperimentalAnimationApi
    @Composable
    fun BookmarksView(navController: NavHostController) {
        // Mutable state variable that is updated when articleList is updated to force a recompose.
        val articles by articleList.collectAsState()
        val bookmarkedArticles by bookmarkedArticleList.collectAsState()

        when {
            // Show "No Articles Found"
            articles.isNullOrEmpty() -> {
                // Sad minion no article found
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Articles Found",
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(Modifier.padding(16.dp))
                    Button(
                        onClick = {
                            val intent =
                                Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
                            feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                            startActivity(intent)
                        }
                    ) {
                        Text("Add Feed")
                    }
                }
            }
            // Show "No Bookmarked Articles"
            bookmarkedArticles.isNullOrEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Bookmarked Articles",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Bookmark an article and it will show up here",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(Modifier.padding(16.dp))
                    Button(onClick = {
                        // Update bookmarkedArticleList when any nav item is clicked.
                        bookmarkedArticleList.value =
                            sortArticlesByDate(getBookmarkedArticles(feedGroup))

                        navController.navigate(NavigationItem.Articles.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                // Pop up to the start destination of the graph to avoid building up
                                //  a large stack of destinations on the back stack as users select
                                //  items
                                popUpTo(route) { saveState = true }
                            }
                            // Avoid multiple copies of the same destination when re-selecting the
                            //  same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }) { Text("Go to Articles") }
                }
            }
            // Show the lazy column with the bookmarked articles
            else -> {
                // LazyColumn containing the article cards.
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // For each article in the list, render a card.
                    items(items = bookmarkedArticles) { article ->
                        ArticleCard(
                            context = this@MainActivity,
                            article = article
                        ) { setArticle(it, refreshBookmark = false) }
                    }
                }
            }
        }
    }

    /**
     * Top app bar with buttons for BookmarkActivity, SettingsActivity, and AddFeedActivity.
     */
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
                // Settings Activity Button
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
                    feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
                    startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Feed Activity"
                    )
                }
            }
        }
    }

    /**
     * Bottom app bar with buttons for article and feed views.
     */
    @Composable
    fun BottomNavigationBar(navController: NavController) {
        val items = listOf(
            NavigationItem.Articles,
            NavigationItem.Feeds,
            NavigationItem.Bookmarks
        )
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.background
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEach { item ->
                // For each item in the list, create a navigation item for it.
                BottomNavigationItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                    label = { Text(text = item.title) },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onBackground.copy(0.5f),
                    alwaysShowLabel = true,
                    selected = currentRoute == item.route,
                    onClick = {
                        // Update bookmarkedArticleList when any nav item is clicked.
                        if (item.route == NavigationItem.Bookmarks.route) bookmarkedArticleList.value =
                            sortArticlesByDate(getBookmarkedArticles(feedGroup))

                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                // Pop up to the start destination of the graph to avoid building up
                                //  a large stack of destinations on the back stack as users select
                                //  items
                                popUpTo(route) { saveState = true }
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

    /**
     * Animate the entrance animation for navigation items.
     */
    @ExperimentalAnimationApi
    @Composable
    fun EnterAnimation(content: @Composable () -> Unit) {
        // TODO figure out non-deprecated library
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { 10000 }),
            exit = fadeOut(),
            initiallyVisible = false
        ) {
            content()
        }
    }

    /**
     * Composable that loads in and out views based on the current navigation item selected.
     */
    @ExperimentalAnimationApi
    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Articles.route) {
            composable(NavigationItem.Articles.route) {
                EnterAnimation {
                    SortedArticleView()
                }
            }
            composable(NavigationItem.Feeds.route) {
                EnterAnimation {
                    SortedFeedView()
                }
            }
            composable(NavigationItem.Bookmarks.route) {
                EnterAnimation {
                    BookmarksView(navController)
                }
            }
        }
    }

    /**
     * The default UI of the app.
     */
    @ExperimentalAnimationApi
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
            // Navigation variables.
            val navController = rememberNavController()

            // Swipe refresh variables.
            val viewModel = PullFeed(this@MainActivity, feedGroup = feedGroup)
            val isRefreshing by viewModel.isRefreshing.collectAsState()

            Scaffold(
                topBar = { TopBar() },
                bottomBar = { BottomNavigationBar(navController = navController) }
            ) {
                // Update feeds when swiping down like in a browser.
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                    onRefresh = {
                        viewModel.updateRss(parser)
                    }
                ) {
                    // Navigate to whatever view is selected by the bottom bar.
                    Navigation(navController)
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        UI()
    }
}
