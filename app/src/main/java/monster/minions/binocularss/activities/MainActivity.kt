package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao

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
            // val navController = configureNavhost()
            BinoculaRSSTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    UI()
                }
            }
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
    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
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

        // TODO maybe suggest some? This sounds like a phase 2 idea.
        if (feedGroup.feeds.isNullOrEmpty()) {
            // Add some feeds to the feedGroup
            feedGroup.feeds.add(Feed(source = "https://rss.cbc.ca/lineup/topstories.xml"))
            feedGroup.feeds.add(Feed(source = "https://androidauthority.com/feed"))
            feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
            feedGroup.feeds.add(Feed(source = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))

            // Inform the user of this
            Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
                .show()
        }
        // Adding sample articles for testing
        for (feed in feedGroup.feeds) {
            for (i in 1..5) {
                feed.articles.add(
                    Article(
                        title = i.toString().plus("th article of ").plus(feed.title.toString())
                    )
                )
            }
        }

        // Tell the user that this change happened
        Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
            .show()
        // }
        ///////////////////////////////////////////////////////////////////////////////////////////
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
     * Configures the navigation controller for use
     *
     * @param startDestination Designates what state to start the app in. Default in the list of
     * feeds state.
     */
    @Composable
    // TODO: Add an enumaration for the UI state names
    fun configureNavhost(startDestination: String = "FeedTitles"): NavHostController {
        val navController = rememberNavController()
        NavHost(navController, startDestination = startDestination) {
            composable("FeedTitles") { DefaultPreview(navController) }
            composable("ArticleTitles") { ArticleTitles(navController) }
            composable("Article") { ArticleView() } // Add article view panel here
            composable("sortedArticles") { sortedArticleView(navController) } // Add article view panel here
        }
        return navController
    }

    /**
     * Displays the contents of a given article
     */
    // TODO: Add actual article view from Eamon
    @Composable
    fun ArticleView() {
        Text(text = "Reading Article: ".plus(currentArticle.title))
    }

    /**
     * Displays the list of feeds saved
     *
     * @param navController The controller used to navigate between the app
     */
    @Composable
    fun FeedTitles(navController: NavController) {
        if (MainActivity.feedGroup.feeds.isNullOrEmpty()){
            Text(text = "No Feeds Found")
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .border(1.dp, Color.Black)
            ) {
                items(items = MainActivity.feedGroup.feeds) { feed ->
                    displayFeed(feed = feed, navController)
                }
            }
        }
    }

    /**
     * Displays a single feed in a card view format
     *
     * @param feed The feed to be displayed
     * @param navController The controller used to navigate between states of the UI
     */
    @Composable
    private fun displayFeed(feed: Feed, navController: NavController) {
        Surface(
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                val feedTitle = if (feed.title != null) feed.title else "Placeholder"
                var title = "Cannot Find Title"
                if (feedTitle != null) {
                    title = feedTitle
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = title)
                }
                OutlinedButton(
                    onClick = {
                        currentFeed = feed
                        navController.navigate("ArticleTitles")
                    }
                ) {
                    Text("Read")
                }
            }
        }
    }

    /**
     * Displays the list of articles associated with a given feed
     *
     * @param navController The controller used to nagivate between states of the app
     */
    @Composable
    fun ArticleTitles(navController: NavHostController) {
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            currentFeed?.let {
                items(items = it.articles) { article ->
                    displayArticle(article = article, navController = navController)
                }
            }
        }
    }


    // TODO: Get from Salman/Ben
    /**
     * Displays a list of articles in reverse chronological order
     *
     * @param navController The controller used to nagivate between states of the app
     */
    @Composable
    fun sortedArticleView(navController: NavController) {
//        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {\
//        }
//            var sortedArticles = getSortedArticles()
//            items(items = sortedArticles) { article ->
//                    displayArticle(article = article)
//            }
//        }
        Text(text = "To be implemented")
    }


    /**
     * Displays a single article in a card view format
     *
     * @param article The article to be displayed
     * @param navController The controller used to nagivate between states of the app
     */
    @Composable
    fun displayArticle(article: Article, navController: NavController) {
        Surface(
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                val articleTitle = if (article.title != null) article.title else "Placeholder"
                var title = "Cannot Find Title"
                if (articleTitle != null) {
                    title = articleTitle
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = title)
                }
                OutlinedButton(
                    onClick = {
                        currentArticle = article
                        navController.navigate("Article")
                    }
                ) {
                    Text("Read")
                }
            }
        }
    }

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
     * A button used to switch the UI state from the feed view to sorted article view
     *
     * @param onClicked The function called when the button is clicked
     */
    @Composable
    fun SwitchViewButton(onClicked: () -> Unit) {
        Button(
            onClick = onClicked
        ) {
            Text(text = "Switch View")
        }
    }

    /**
     * The default UI state of the app.
     *
     * @param navController The controller used to switch between UI states
     */
    @Composable
    fun UI(navController: NavController){
        val navController = configureNavhost()
        var showFeedView by remember { mutableStateOf(true) }

      
        if (showFeedView) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {end with t
                    UpdateFeedButton()
                    SwitchViewButton(onClicked = { showFeedView = !showFeedView })
                }
                FeedTitles(navController)
            }
        } else {
            Column {
                Row {
                    UpdateFeedButton()
                    SwitchViewButton(onClicked = { showFeedView = !showFeedView })
                }
                sortedArticleView(navController)
            }
        }
    }

    // @Composable
    // fun UI() {
    //     val padding = 16.dp
    //     Column(
    //         modifier = Modifier.fillMaxSize(),
    //         verticalArrangement = Arrangement.Center,
    //         horizontalAlignment = Alignment.CenterHorizontally
    //     ) {
    //         FeedTitles()
    //         UpdateFeedButton()
    //         Spacer(Modifier.size(padding))
    //         AddFeedButton()
    //         Spacer(modifier = Modifier.size(padding))
    //         ClearFeeds()
    //         Spacer(modifier = Modifier.size(padding))
    //         BookmarksButton()
    //     }
    // }
    
                
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

    @Composable
    fun BookmarksButton() {
        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent(context, BookmarksActivity::class.java)
                context.startActivity(intent)
            }){
          Text("Bookmarks")
        }
     }

    // @Preview(showBackground = true)
    @Composable
    fun Preview() {
        Surface(color = MaterialTheme.colors.background) {
            UI()
        }
    }
}


