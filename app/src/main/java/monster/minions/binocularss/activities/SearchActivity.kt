package monster.minions.binocularss.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.SearchIcon

class SearchActivity : ComponentActivity() {


    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room Database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    private var text = mutableStateOf("")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
        Log.d("SearchActivity", "onStop called")
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
        Log.d("SearchActivity", "onResume called")
        feedGroup.feeds = feedDao.getAll()
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
     * Retrieve all articles that could match the given
     * query
     */
    private fun submit(query: String): MutableList<Article>{

        val articles: MutableList<Article> = getAllArticles()
        val matchedArticles: MutableList<Article> = mutableListOf()

        for (article in articles){
            if (matches(query = query, article = article)){
                matchedArticles.add(article)
            }
        }

        return matchedArticles

    }


    /**
     * Return whether the article matches the
     * given query
     *
     *
     */
    private fun matches(query: String, article: Article): Boolean {




        return true
    }

    @Composable
    @Preview
    fun UI(){

        Column(

        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colors.primary,
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ){
                    SearchBar()
                }

            }
        }

    }


    @Composable
    fun SearchBar() {
        val textState = remember { mutableStateOf(TextFieldValue()) }

        Surface(
            color = MaterialTheme.colors.background
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.7f),
                value = textState.value,
                placeholder = { Text("Search for an article") },
                onValueChange = {
                    textState.value = it
                    text = mutableStateOf(textState.value.text)
                },
                singleLine = true,
                maxLines = 1,
                trailingIcon = { SearchIcon() },

                )
        }
    }
}
















