package monster.minions.binocularss.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Contacts
import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.*
import java.util.*

class MainActivity : ComponentActivity() {
    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    // Companion object as this variable needs to be updated from other asynchronous classes.
    companion object {
        var feedGroupText = MutableStateFlow("Empty\n")
        lateinit var list: SnapshotStateList<Article>
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

        // TODO this code goes with onSaveInstanceState and onRestoreInstanceState
        // if (savedInstanceState != null) {
        // feedGroup = savedInstanceState.getParcelable<FeedGroup>("feedGroup")!!
        //    Toast.makeText(this@MainActivity, feedGroup.feeds[0].title, Toast.LENGTH_SHORT).show()
        // }

        setContent {
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    UI()
                }
            }

            currentFeed = Feed("default")
        }

        // Set private variables. This is done here as we cannot initialize objects that require context
        //  before we have context (generated during onCreate)
        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()
        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(60L * 60L * 100L) // Set the cache to expire in one hour
            // Different options for cacheExpiration
            // .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
            // .cacheExpirationMillis(0)
            .build()

        list = getAllArticles().toMutableStateList()
    }

    // /**
    //  * Saves instance state when activity is destroyed
    //  *
    //  * TODO: We do not need onSaveInstanceState (goes with onRestoreInstanceState) for the current
    //  *  use case. The room database handles
    //  *  it all. I am leaving it here so that whoever uses it has a base for the code
    //  *
    //  * @param outState A bundle of instance state information stored using key-value pairs
    //  */
    // override fun onSaveInstanceState(outState: Bundle) {
    //     Log.d("MainActivity", "onSaveInstanceState called")
    //     super.onSaveInstanceState(outState)
    //     outState.putParcelable(FEED_GROUP_KEY, feedGroup)
    // }

    // /**
    //  * Restores instance state when activity is recreated
    //  *
    //  * TODO: We do not need onRestoreInstanceState (goes with onSaveInstanceState) for the current
    //  *  use case. The room database handles it all. I am leaving it here so that whoever uses it
    //  *  has a base for the code
    //  *
    //  * @param savedInstanceState A bundle of instance state information stored using key-value pairs
    //  */
    // override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    //     Log.d("MainActivity", "onRestoreInstanceState called")
    //     super.onRestoreInstanceState(savedInstanceState)
    //     feedGroup = savedInstanceState.getParcelable<FeedGroup>(FEED_GROUP_KEY)!!

    //     var text = ""
    //     for (feed in feedGroup.feeds) {
    //         text += feed.title
    //         text += "\n"
    //     }
    //     feedGroupText.value = text
    // }

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

        ///////////////////////////////////////////////////////////////////////////////////////////
        // NOT PERMANENT: If the user does not have any feeds added, add some.
//        if (feedGroup.feeds.isNullOrEmpty()) {
//            // Add some feeds to the feedGroup
//            feedGroup.feeds.add(Feed(source = "https://rss.cbc.ca/lineup/topstories.xml"))
//            feedGroup.feeds.add(Feed(source = "https://androidauthority.com/feed"))
//            feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
//            feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))
//
//            // Inform the user of this
//            Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
//                .show()
//        }
//        ///////////////////////////////////////////////////////////////////////////////////////////
//        updateText()
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

    /**
     * Displays a list of articles in the order given by the currently selected sorting method
     */
    @Composable
    fun SortedArticleView(getArticles: () -> MutableList<Article>) {
        val articles = getArticles()
//        val list = remember { articles.toMutableStateList() }

        // TODO sort articles based on criteria

        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = list) { article ->
                ArticleCard(context = this@MainActivity, article = article)
            }
        }
    }
    // Text(text = "To be implemented")

    /**
     * A button used to update the RSS feeds
     */
    @Composable
    fun UpdateFeedButton() {
        Button(
            onClick = {
                val viewModel = PullFeed(this, feedGroup)
                viewModel.updateRss(parser)
            }
        ) {
            Text("Update RSS Feeds")
        }
    }

    /**
     * The default UI state of the app.
     */
    @Composable
    fun UI() {
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

    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItem.Article.route) {
            composable(NavigationItem.Article.route) {
                // TODO either do this or just call getAll articles then sort them
                SortedArticleView { getAllArticles() }
            }
            composable(NavigationItem.Feed.route) {
                FeedTitles()
            }
        }
    }

    @Composable
    fun TopBar() {
        TopAppBar(title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BinoculaRSS")
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
                            contentDescription = "Bookmarks Activity"
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
        })
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
                startActivity(intent)
                feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
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

}