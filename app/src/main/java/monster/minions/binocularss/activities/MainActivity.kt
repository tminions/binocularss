package monster.minions.binocularss.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.R
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
    }

    /**
     * Formats the image and article/feed description if available.
     * If not, put in a placeholder image (tminions logo) or empty string for description
     *
     * @param image string that represents the URL of the image
     * @param description Article/Feed descriptions
     */
    @Composable
    fun CardImage(image: String, description: String = "") {

        // Color matrix to turn image grayscale
        val grayScaleMatrix = ColorMatrix(
            floatArrayOf(
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val imageExists = image != "null" && image != null

        // Box for image on the right.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp, 150.dp)
                    .background(MaterialTheme.colors.background, RoundedCornerShape(4.dp))
            ) {
                Image(
                    painter = rememberImagePainter(
                        data =
                        if (imageExists) image
                        else "https://avatars.githubusercontent.com/u/91392435?s=200&v=4",
                        builder = {
                            // Placeholder when the image hasn't loaded yet.
                            placeholder(R.drawable.ic_launcher_foreground)
                        }
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = description,
                    colorFilter = if (imageExists) null else ColorFilter.colorMatrix(
                        grayScaleMatrix
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
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
                }, elevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier
                                .background(MaterialTheme.colors.background),
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
    }

    /**
     * Displays a single article in a card view format
     *
     * @param article The article to be displayed
     */
    @SuppressLint("SimpleDateFormat")
    @ExperimentalCoilApi
    @Composable
    fun ArticleCard(context: Context, article: Article, updateValues: (article: Article) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    val intent = Intent(context, ArticleActivity::class.java)
                    intent.putExtra("article", article)
                    article.read = true
                    updateValues(article)
                    ContextCompat.startActivity(context, intent, null)
                },
            elevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Column for title, feed, and time.
                    Column(
                        modifier = Modifier
                            .width(200.dp)
                            .padding(end = 12.dp)
                    ) {
                        article.title?.let { title ->
                            Text(text = title, fontWeight = FontWeight.SemiBold)
                        }
                        Text(text = article.sourceTitle)
                        Text(text = getTime(article.pubDate!!))
                    }

                    CardImage(image = article.image!!, description = article.description!!)
                }

                // Row for buttons on the bottom.
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BookmarkFlag(article = article) { updateValues(article) }
                    ShareFlag(context = context, article = article)
                    ReadFlag(article = article) { updateValues(article) }
                    BrowserFlag(context = context, article = article)
                }
            }
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
                    style = MaterialTheme.typography.h5
                )
                Spacer(Modifier.padding(16.dp))
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
        } else {
            // LazyColumn containing the article cards.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // For each article in the list, render a card.
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(items = currentFeed.articles) { article ->
                ArticleCard(
                    context = this@MainActivity,
                    article = article
                ) { setArticle(it) }
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
     * Bottom app bar with buttons for article, feed, and bookmark views.
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
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
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
        val elevatedColor =
            LocalElevationOverlay.current?.apply(color = color, elevation = 8.dp)
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
