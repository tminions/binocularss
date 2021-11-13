package monster.minions.binocularss.activities

import android.content.Context
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
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.PullFeed
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme

class MainActivity : ComponentActivity() {

    // Variables that can be accessed from other classes by calling `MainActivity.<variable>`.
    companion object {

        // Global FeedGroup object
        var feedGroup: FeedGroup = FeedGroup()
        // Parser variable using lateinit because we want to get the context
        private lateinit var parser: Parser

        // Setup parser
        private fun setParser(context: Context) {
            parser = Parser.Builder()
                .context(context)
                // .charset(Charset.forName("ISO-8859-7")) // Default is UTF-8
                .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
                .build()
        }

        // Setup Room database
        private lateinit var db: RoomDatabase
        private lateinit var feedDao: FeedDao
        private fun setDb(context: Context) {
            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "feed-db"
            ).allowMainThreadQueries().build()
            feedDao = (db as AppDatabase).feedDao()
        }

        // Key for accessing feed group in onSaveInstanceState
        // const val FEED_GROUP_KEY = "feedGroup"
    }

    // Local variables
    private lateinit var currentFeed: Feed
    private lateinit var currentArticle: Article
    private val feedGroupText = MutableStateFlow("Empty\n")


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
            val navController = configureNavhost()
        }


        // Set feed-db in companion object
        setDb(applicationContext)
        // Set parser in companion object
        setParser(this@MainActivity)
    }

    /**
     * Call the required functions to update the Rss feed.
     *
     * @param parser A parser with preconfigured settings.
     */
    @DelicateCoroutinesApi
    private fun updateRss(parser: Parser) {
        val viewModel = PullFeed()

        GlobalScope.launch(Dispatchers.Main) {
            feedGroup = viewModel.pullRss(feedGroup, parser)
            // Debug toast to confirm that the data has come back to the MainActivity variable.
            // for (feed in MainActivity.feedGroup.feeds) {
            //     Toast.makeText(context, "Pulled: ${feed.title}", Toast.LENGTH_SHORT).show()
            // }
            var text = ""
            for (feed in feedGroup.feeds) {
                text += feed.title
                text += "\n"
            }
            feedGroupText.value = text
        }
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
     * This function is called before `onStop` and `onDestroy` or any time a "pause" happens. This
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
        // if (feedGroup.feeds.isNullOrEmpty()) {

        // Adding test feeds and articles
        for (i in 1..5) {
            feedGroup.feeds.add(
                Feed(
                    title = i.toString(),
                    link = "https://rss.cbc.ca/lineup/topstories.xml"
                )
            )
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


        //          feedGroup.feeds.add(Feed(link = "https://androidauthority.com/feed"))
        // TODO This feed is malformed according to the exception that the xml parser throws.
        //  We can use this to develop a bad formatting indication to the user
        //  feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Gravity-Assist.rss"))
//            feedGroup.feeds.add(Feed(link = "https://www.nasa.gov/rss/dyn/Houston-We-Have-a-Podcast.rss"))

        // Tell the user that this change happened
        Toast.makeText(this@MainActivity, "Added Sample Feeds to feedGroup", Toast.LENGTH_SHORT)
            .show()
        // }
        ///////////////////////////////////////////////////////////////////////////////////////////
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
            onClick = { updateRss(parser) },
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
    fun myApp(navController: NavController){
        var showFeedView by remember { mutableStateOf(true) }

        BinoculaRSSTheme {
            if (showFeedView) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
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
    }
    
    // @Preview(showBackground = true)
    @Composable
    fun DefaultPreview(navController: NavController) {
        myApp(navController = navController)
    }
}


