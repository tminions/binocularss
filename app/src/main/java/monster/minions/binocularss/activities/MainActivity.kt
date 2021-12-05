package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.ContentView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.BottomNavigationDefaults.Elevation
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.RoomDatabase
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
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.*
import kotlin.math.ln

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var articleList: MutableStateFlow<MutableList<Article>>
        lateinit var bookmarkedArticleList: MutableStateFlow<MutableList<Article>>
        lateinit var searchResults: MutableStateFlow<MutableList<Article>>
        lateinit var feedList: MutableStateFlow<MutableList<Feed>>
        lateinit var readArticleList: MutableStateFlow<MutableList<Article>>

        // Function to update feedGroup from other activities to avoid
        // 	bugs with returning to the main activity.
        fun updateFeedGroup(feeds: MutableList<Feed>) {
            feedGroup.feeds = feeds
        }

        // FeedGroup object
        private var feedGroup: FeedGroup = FeedGroup()
    }

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
    @ExperimentalMaterial3Api
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        WindowCompat.setDecorFitsSystemWindows(window, false)

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
        searchResults = MutableStateFlow(mutableListOf())
        feedList = MutableStateFlow(mutableListOf())
        readArticleList = MutableStateFlow(mutableListOf())
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
        readArticleList.value = sortArticlesByReadDate(getReadArticles(feedGroup))
    }

    /**
     * Replace the unmodified article with a modified article.
     * This is to be used when updating article.bookmarked and article.read
     *
     * @param modifiedArticle Article with a modified property.
     */
    private fun setArticle(
        modifiedArticle: Article,
        refreshBookmark: Boolean = true,
        refreshRead: Boolean = true
    ) {
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
        if (refreshRead) {
            readArticleList.value = sortArticlesByReadDate(getReadArticles(feedGroup))
        }

        feedList.value = sortFeedsByTitle(feedGroup.feeds)
    }

    /**
     * Replace the unmodified feeds with a modified feed.
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
     * Delete feeds through deleting the source from feedDao
     *
     * @param feed A feed that the user wants to delete
     */
    private fun deleteFeed(feed: Feed) {
        feedDao.deleteBySource(feed.source)
        feedGroup.feeds.remove(feed)

        articleList.value = mutableListOf()
        feedList.value = mutableListOf()
        bookmarkedArticleList.value = mutableListOf()
        readArticleList.value = mutableListOf()
    }

    /**
     * Displays a single feed in a card view format
     * Includes a long hold function to delete a feed
     *
     * @param context The application context
     * @param feed The feed to be displayed
     */
    @ExperimentalCoilApi
    @Composable
    fun FeedCard(context: Context, feed: Feed) {
        var showDropdown by remember { mutableStateOf(false) }
        // Location where user long pressed.
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingMedium)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showDropdown = true
                            offset = it
                        },
                        onTap = {
                            // TODO temporary until articleFromFeed
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(feed.link)
                            ContextCompat.startActivity(context, intent, null)
                        }
                    )
                }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingLarge),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Column for feed title.
                    Column(
                        modifier = Modifier.width(200.dp)
                    ) {
                        feed.title?.let { title ->
                            Text(text = title, fontWeight = FontWeight.SemiBold)
                        }
                        val items = listOf("Delete")
                        // Convert pixel to dp
                        val xDp = with(LocalDensity.current) { (offset.x).toDp() } - 15.dp
                        val yDp = with(LocalDensity.current) { (offset.y).toDp() } - 35.dp
                        // Draw the dropdown menu
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background),
                            offset = DpOffset(xDp, yDp)
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(onClick = {
                                    when (item) {
                                        "Delete" -> {
                                            deleteFeed(feed)
                                        }
                                    }
                                }) {
                                    Text(text = item)
                                }
                            }
                        }
                    }
                    CardImage(image = feed.image, description = feed.description!!)
                }
            }

            // TODO Row for buttons in the future that is currently not used
            // Row(
            //     modifier = Modifier
            //         .fillMaxWidth()
            // ) {
            // }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(
                thickness = 0.7.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxSize(0.9f),
            )
        }
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
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.padding(paddingLarge))
                Button(
                    onClick = {
                        showAddFeed = false
                        val intent =
                            Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
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
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.padding(paddingLarge))
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
        } else {
            // LazyColumn containing the article cards.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items = articles) { article ->
                    ArticleCard(
                        context = this@MainActivity,
                        article = article
                    ) { setArticle(it) }
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
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(bottom = 80.dp)
//        ) {
//            itemsIndexed(items = articles) { index, article ->
//                ArticleCard(
//                    context = this@MainActivity,
//                    article = article
//                ) { setArticle(it) }
//
//                // Do not draw the divider below the last article
//                if (index < articles.lastIndex) {
//                    Column(
//                        modifier = Modifier.fillMaxSize(),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Divider(
//                            thickness = 0.7.dp,
//                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
//                            modifier = Modifier.fillMaxSize(0.9f),
//                        )
//                    }
//                }
//            }
//        }
    }

    /**
     * Displays a list of all read articles.
     */
    @ExperimentalAnimationApi
    @Composable
    fun ReadingHistoryView(navController: NavHostController) {
        val articles by articleList.collectAsState()
        val readArticles by readArticleList.collectAsState()

        when {
            articles.isNullOrEmpty() -> {
                // Sad minion no article found
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Articles Found",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.padding(paddingLarge))
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
            // Show "No Read Articles"
            readArticles.isNullOrEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Read Articles",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.padding(paddingSmall))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Read an article and it will show up here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.padding(paddingLarge))
                    Button(onClick = {
                        // Update readArticleList when any nav item is clicked.
                        readArticleList.value =
                            sortArticlesByReadDate(getReadArticles(feedGroup))

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
                    items(items = readArticles) { article ->
                        ArticleCard(
                            context = this@MainActivity,
                            article = article
                        ) { setArticle(it) }
                    }
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.padding(paddingLarge))
                    Button(
                        onClick = {
                            val intent =
                                Intent(
                                    this@MainActivity,
                                    AddFeedActivity::class.java
                                ).apply {}
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.padding(paddingSmall))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Bookmark an article and it will show up here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.padding(paddingLarge))
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

                    items(items = bookmarkedArticles) { article ->
                        ArticleCard(
                            context = this@MainActivity,
                            article = article
                        ) { setArticle(it) }
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

                // Add Feed Activity Button
                IconButton(onClick = {
                    val intent =
                        Intent(this@MainActivity, AddFeedActivity::class.java).apply {}
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
                            // Update bookmarkedArticleList when the bookmarks are clicked
                            if (item.route == NavigationItem.Bookmarks.route) {
                                bookmarkedArticleList.value =
                                    sortArticlesByDate(getBookmarkedArticles(feedGroup))
                                // Update readArticleList when the bookmarks are clicked
                            } else if (item.route == NavigationItem.ReadingHistory.route) {
                                readArticleList.value =
                                    sortArticlesByReadDate(getReadArticles(feedGroup))
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
     * Get the any color at an elevation. This is the same algorithm that google uses so we can get
     * get the same colors for our custom elements.
     */
    @Composable
    fun colorAtElevation(color: Color, elevation: Dp): Color {
        if (elevation == 0.dp) return color
        val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
        return MaterialTheme.colorScheme.primary.copy(alpha = alpha).compositeOver(color)
    }

    /**
     * The default UI of the app.
     */
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
            val viewModel = PullFeed(this@MainActivity, feedGroup = feedGroup)
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

    @ExperimentalMaterial3Api
    @ExperimentalAnimationApi
    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        UI()
    }
}