package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.activities.ui.theme.*
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.*
import monster.minions.binocularss.operations.ViewModel
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.*
import java.util.*
import kotlin.properties.Delegates

class MainActivity : ComponentActivity() {
    companion object {
        // Lists of articles used in various points throughout the app.
        lateinit var articleList: MutableStateFlow<MutableList<Article>>
        lateinit var bookmarkedArticleList: MutableStateFlow<MutableList<Article>>
        lateinit var searchResults: MutableStateFlow<MutableList<Article>>
        lateinit var feedList: MutableStateFlow<MutableList<Feed>>
        lateinit var readArticleList: MutableStateFlow<MutableList<Article>>
        lateinit var currentFeed: Feed
        lateinit var currentFeedArticles: MutableStateFlow<MutableList<Article>>

        // Function to update feedGroup from other activities to avoid
        // 	bugs with returning to the main activity.
        fun updateFeedGroup(feeds: MutableList<Feed>) {
            feedGroup.feeds = feeds
        }

        // FeedGroup object.
        private var feedGroup: FeedGroup = FeedGroup()
    }

    // Parser for RSS parsing.
    private lateinit var parser: Parser

    // Room database variables.
    private lateinit var dataGateway: DatabaseGateway

    // User Preferences.
    private lateinit var sharedPref: SharedPreferences
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var materialYou by Delegates.notNull<Boolean>()
    private lateinit var materialYouState: MutableState<Boolean>
    private var isFirstRun = true
    private var cacheExpiration = 0L

    /**
     * The function that is run when the activity is created. This is on app launch in this case.
     * It is also called when the activity is destroyed then recreated. It initializes the main
     * functionality of the application (UI, companion variables, etc.)
     *
     * @param savedInstanceState A bundle of parcelable information that was previously saved.
     */
    @ExperimentalCoilApi
    @ExperimentalMaterial3Api
    @ExperimentalAnimationApi
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

        // Set shared preferences variables.
        sharedPref = getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        theme = sharedPref
            .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
            .toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)
        cacheExpiration = sharedPref.getLong(SettingsActivity.PreferenceKeys.CACHE_EXPIRATION, 0L)

        // Initialize database gateway.
        dataGateway = DatabaseGateway(context = this)

        // Initialize parser
        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(cacheExpirationMillis = cacheExpiration)
            .build()

        // Refresh LazyColumn composables
        articleList = MutableStateFlow(mutableListOf())
        bookmarkedArticleList = MutableStateFlow(mutableListOf())
        searchResults = MutableStateFlow(mutableListOf())
        feedList = MutableStateFlow(mutableListOf())
        readArticleList = MutableStateFlow(mutableListOf())
        currentFeed = Feed()
        currentFeedArticles = MutableStateFlow(mutableListOf())
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
        Log.d("MainActivity", "onPause called")
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
        Log.d("MainActivity", "onResume called")

        val feeds: MutableList<Feed> = dataGateway.read()

        feedGroup.feeds = feeds

        theme = sharedPref
            .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
            .toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)
        if (!isFirstRun) {
            themeState.value = theme
            materialYouState.value = materialYou
        }
        isFirstRun = false

        val sortArticlesByDate = SortArticles(SortArticlesByDateStrategy())
        articleList.value = sortArticlesByDate.sort(getAllArticles(feedGroup))
        bookmarkedArticleList.value = sortArticlesByDate.sort(getBookmarkedArticles(feedGroup))
        currentFeedArticles.value = sortArticlesByDate.sort(getArticlesFromFeed(currentFeed))
        feedList.value = SortFeeds(SortFeedsByTitleStrategy()).sort((feedGroup.feeds))
        readArticleList.value =
            SortArticles(SortArticlesByReadDateStrategy()).sort(getReadArticles(feedGroup))
    }

    /**
     * Replace the unmodified article with a modified article.
     * This is to be used when updating article.bookmarked and article.read
     *
     * @param modifiedArticle Article with a modified property.
     */
    private fun setArticle(
        modifiedArticle: Article, refreshBookmark: Boolean = true, refreshRead: Boolean = true
    ) {
        // Replace article in feedGroup
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
        val sortArticlesByDate = SortArticles(SortArticlesByDateStrategy())
        articleList.value = sortArticlesByDate.sort(getAllArticles(feedGroup))
        // currentFeedArticles.value = sortArticlesByDate.sort(getArticlesFromFeed(currentFeed))
        currentFeedArticles.value = mutableListOf()
        if (refreshBookmark) {
            bookmarkedArticleList.value = sortArticlesByDate.sort(getBookmarkedArticles(feedGroup))
        }
        if (refreshRead) {
            readArticleList.value =
                SortArticles(SortArticlesByReadDateStrategy()).sort(getReadArticles(feedGroup))
        }
        feedList.value = SortFeeds(SortFeedsByTitleStrategy()).sort(feedGroup.feeds)
    }

    @Composable
    fun AddFeedPrompt(text: String, buttonText: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.padding(paddingLarge))
            Button(
                onClick = {
                    val intent = Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
                    dataGateway.addFeeds(feedGroup.feeds)
                    startActivity(intent)
                }
            ) {
                Text(text = buttonText)
            }
        }
    }

    @Composable
    fun ArticleActionPrompt(
        text: String, description: String, buttonText: String, navController: NavHostController
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.padding(paddingSmall))
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.padding(paddingLarge))
            Button(onClick = {
                // Update bookmarkedArticleList when any nav item is clicked.
                bookmarkedArticleList.value =
                    SortArticles(SortArticlesByDateStrategy()).sort(getBookmarkedArticles(feedGroup))

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
            }) { Text(buttonText) }
        }
    }

    /**
     * Displays the list of feeds saved
     */
    @ExperimentalCoilApi
    @Composable
    fun SortedFeedView() {
        val feeds by feedList.collectAsState()

        // If there are no feeds, prompt the user to add some.
        if (feeds.isNullOrEmpty()) {
            AddFeedPrompt(text = "No Feeds Found", buttonText = "Add a Feed")
        } else {
            // List of feed cards for the user to interact with.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items = feeds) { feed ->
                    FeedCard(
                        context = this@MainActivity,
                        feed = feed,
                        deleteFeed = { deleteFeed(feed) })
                }
            }
        }
    }

    /**
     * Delete feeds through deleting the source from feedDao
     *
     * @param feed A feed that the user wants to delete
     */
    private fun deleteFeed(feed: Feed) {
        dataGateway.removeFeedBySource(feed.source)
        feedGroup.feeds.remove(feed)

        val sortArticlesByDate = SortArticles(SortArticlesByDateStrategy())
        articleList.value = sortArticlesByDate.sort(getAllArticles(feedGroup))
        bookmarkedArticleList.value = sortArticlesByDate.sort(getBookmarkedArticles(feedGroup))
        currentFeedArticles.value = sortArticlesByDate.sort(getArticlesFromFeed(currentFeed))
        readArticleList.value =
            SortArticles(SortArticlesByReadDateStrategy()).sort(getReadArticles(feedGroup))
        feedList.value = dataGateway.read()
    }

    @ExperimentalCoilApi
    @Composable
    fun ArticleCardList(
        articles: MutableList<Article>, context: Context, updateValues: (article: Article) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = articles) { article ->
                ArticleCard(context = context, article = article, updateValues = updateValues)
            }
        }
    }

    /**
     * Displays a list of articles in the order given by the currently selected sorting method
     */
    @ExperimentalCoilApi
    @Composable
    fun SortedArticleView() {
        // Mutable state variable that is updated when articleList is updated to force a recompose.
        val articles by articleList.collectAsState()

        // If there are no articles, prompt the user to add some.
        if (articles.isNullOrEmpty()) AddFeedPrompt(
            text = "No Articles Found",
            buttonText = "Add a Feed"
        ) else ArticleCardList(
            articles = articles,
            context = this@MainActivity
        ) {
            setArticle(it)
        }
    }

    /**
     * Displays a list of all read articles.
     */
    @ExperimentalCoilApi
    @ExperimentalAnimationApi
    @Composable
    fun ReadingHistoryView(navController: NavHostController) {
        val articles by articleList.collectAsState()
        val readArticles by readArticleList.collectAsState()

        when {
            // If there are no articles, prompt the user to add some.
            articles.isNullOrEmpty() -> AddFeedPrompt(
                text = "No Articles Found",
                buttonText = "Add a Feed"
            )
            // If there are no read articles, prompt the user to read some.
            readArticles.isNullOrEmpty() -> ArticleActionPrompt(
                text = "No Read Articles",
                description = "Read an article and it will show up here",
                buttonText = "Go to Articles",
                navController = navController
            )
            // Show the lazy column with the bookmarked articles
            else -> ArticleCardList(
                articles = readArticles,
                context = this@MainActivity
            ) {
                setArticle(it, refreshRead = false)
            }
        }
    }

    /**
     * Displays a list of all bookmarked articles.
     */
    @ExperimentalCoilApi
    @ExperimentalAnimationApi
    @Composable
    fun BookmarksView(navController: NavHostController) {
        // Mutable state variable that is updated when articleList is updated to force a recompose.
        val articles by articleList.collectAsState()
        val bookmarkedArticles by bookmarkedArticleList.collectAsState()

        when {
            // Show "No Articles Found"
            articles.isNullOrEmpty() -> AddFeedPrompt(
                text = "No Article Found",
                buttonText = "Add a Feed"
            )
            // Show "No Bookmarked Articles"
            bookmarkedArticles.isNullOrEmpty() -> ArticleActionPrompt(
                text = "No Bookmarked Articles",
                description = "Bookmark an article and it will show up here",
                buttonText = "Go to articles",
                navController = navController
            )
            // Show the lazy column with the bookmarked articles
            else -> ArticleCardList(
                articles = bookmarkedArticles,
                context = this@MainActivity
            ) { setArticle(it, refreshBookmark = false) }
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
                .padding(horizontal = paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("BinoculaRSS", style = MaterialTheme.typography.headlineMedium)
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Search Activity Button
                IconButton(onClick = {
                    val intent =
                        Intent(this@MainActivity, SearchActivity::class.java).apply {}
                    startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Activity"
                    )
                }

                // Add Feed Activity Button
                IconButton(onClick = {
                    val intent =
                        Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
                    dataGateway.addFeeds(feedGroup.feeds)
                    startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Feed Activity"
                    )
                }

                // Settings Activity Button
                IconButton(onClick = {
                    val intent =
                        Intent(this@MainActivity, SettingsActivity::class.java).apply {}
                    startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings Activity"
                    )
                }
            }
        }
    }

    /**
     * Object to replace the ripple theme to remove the "ripple" effect from the navigation bar
     * buttons.
     */
    object ClearRippleTheme : RippleTheme {
        @Composable
        override fun defaultColor(): Color = Color.Transparent

        @Composable
        override fun rippleAlpha() = RippleAlpha(
            draggedAlpha = 0.0f,
            focusedAlpha = 0.0f,
            hoveredAlpha = 0.0f,
            pressedAlpha = 0.0f,
        )
    }

    /**
     * Navigation bar at the bottom to go between articles, feeds, bookmarks, and history.
     */
    @Composable
    fun BottomNavigationBar(navController: NavController) {
        var selectedItem by remember { mutableStateOf(0) }
        val items = listOf(
            NavigationItem.Articles,
            NavigationItem.Feeds,
            NavigationItem.Bookmarks,
            NavigationItem.ReadingHistory
        )
        CompositionLocalProvider(LocalRippleTheme provides ClearRippleTheme) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                items.forEachIndexed { index, item ->
                    // Draw a button for each item in the list
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (item.route) {
                                // Update bookmarkedArticleList when the Bookmarks is clicked
                                NavigationItem.Bookmarks.route -> {
                                    bookmarkedArticleList.value =
                                        SortArticles(SortArticlesByDateStrategy()).sort(
                                            getBookmarkedArticles(feedGroup)
                                        )
                                }
                                // Update readArticleList when History is clicked
                                NavigationItem.ReadingHistory.route -> {
                                    readArticleList.value =
                                        SortArticles(SortArticlesByReadDateStrategy()).sort(
                                            getReadArticles(feedGroup)
                                        )
                                }
                                // Update currentFeedArticles when Feed is clicked.
                                NavigationItem.Feeds.route -> {
                                    currentFeedArticles.value =
                                        SortArticles(SortArticlesByDateStrategy()).sort(
                                            getArticlesFromFeed(currentFeed)
                                        )
                                }
                            }

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
    }

    /**
     * Composable that loads in and out views based on the current navigation item selected.
     */
    @ExperimentalCoilApi
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
            composable(NavigationItem.ReadingHistory.route) {
                EnterAnimation {
                    ReadingHistoryView(navController)
                }
            }
        }
    }

    /**
     * Animate the entrance animation for navigation items.
     */
    @ExperimentalAnimationApi
    @Composable
    fun EnterAnimation(content: @Composable () -> Unit) {
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
     * The default UI of the app.
     */
    @ExperimentalCoilApi
    @ExperimentalMaterial3Api
    @ExperimentalAnimationApi
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
        // Get elevated color to match the bottom bar that is also elevated by 4.dp
        val elevatedColor =
            colorAtElevation(color = MaterialTheme.colorScheme.background, elevation = 4.dp)
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
            systemUiController.setNavigationBarColor(
                color = elevatedColor
            )
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            // Navigation variables.
            val navController = rememberNavController()

            // Swipe refresh variables.
            val viewModel =
                ViewModel(this, feedGroup = feedGroup)
            val isRefreshing by viewModel.isRefreshing.collectAsState()

            Scaffold(
                topBar = { TopBar() },
                bottomBar = { BottomNavigationBar(navController) }
            ) {
                // Update feeds when swiping down like in a browser.
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                    onRefresh = { viewModel.updateRss(parser) },
                    indicator = { state, trigger ->
                        // Custom swipe refresh indicator because of material3
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = trigger,
                            scale = true,
                            backgroundColor = colorAtElevation(
                                MaterialTheme.colorScheme.background,
                                4.dp
                            ),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    // Navigate to whatever view is selected by the bottom bar.
                    Navigation(navController)
                }
            }
        }
    }
}