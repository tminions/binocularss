package monster.minions.binocularss.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.annotation.ExperimentalCoilApi
import com.prof.rssparser.Parser
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.ArticleSearchComparator
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.ArticleCard

class SearchActivity : ComponentActivity() {


    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Parser variable
    private lateinit var parser: Parser

    // Room Database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao


    // User Preferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var isFirstRun = true
    private var cacheExpiration = 0L


    private var text = mutableStateOf("")
    private var searchResults: MutableList<Article> = mutableListOf()
    private var feedTitles: MutableList<String> = mutableListOf()


    @ExperimentalCoilApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            themeState = remember { mutableStateOf(theme) }
            BinoculaRSSTheme(
                theme = themeState.value
            ) {
               UI()
            }
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


        searchResults = getAllArticles().sortedWith(ArticleSearchComparator(text.toString())).toMutableList()
        feedTitles = getFeedTitles()



        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        if (!isFirstRun) {
            themeState.value = theme
        }
        isFirstRun = false
    }

    /**
     * Retrieve all articles from the database
     */
    private fun getAllArticles(): MutableList<Article> {
        val articles: MutableList<Article> = mutableListOf()

        for (feed in feedGroup.feeds) {
            for (article in feed.articles) {
                articles.add(article)
            }
        }

        return articles
    }


    private fun getFeedTitles(): MutableList<String>{

        val feeds = feedGroup.feeds
        val feedTitles = mutableListOf("All feeds")

        for (feed in feeds){
            feedTitles.add(feed.title.toString())
        }
        return feedTitles
    }

    /**
     * Retrieve all articles that could match the given
     * query
     */
    private fun submit(){

        val articles: MutableList<Article> = getAllArticles()
        Log.d("OP", "Submitting")
        searchResults = articles.sortedWith(ArticleSearchComparator(text.value)).toMutableList()

        if (searchResults.isEmpty()){
            Toast.makeText(this@SearchActivity, "No matches", Toast.LENGTH_SHORT).show()
        }

    }


    @ExperimentalCoilApi
    @Composable
    fun ArticleSearchResults(){

        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = searchResults){article ->
                ArticleCard(context = this@SearchActivity, article = article)
            }

        }

    }

    @Composable
    fun SpinnerButton(text: String, suggestions: List<String>){
        var expanded by remember { mutableStateOf(false) }

        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .padding(12.dp)
        ){
            Text (text)
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            suggestions.forEach { label ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        //do something ...
                    },
                ) {
                    Text(text = label)
                }
            }
        }
    }


    @Composable
    fun SearchBar(displayResult: Boolean, onDisplayChange:(Boolean) -> Unit) {
        val textState = remember { mutableStateOf(TextFieldValue()) }


        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = textState.value,
            placeholder = { Text("Search for an article(s)") },
            onValueChange = {
                textState.value = it
                text = mutableStateOf(textState.value.text)
            },
            singleLine = true,
            maxLines = 1,
            trailingIcon = { Icon(Icons.Filled.Search, null) },
            keyboardActions = KeyboardActions(
                onDone = {
                    submit()
                    onDisplayChange(true)
                }
            )
        )
    }

    @ExperimentalCoilApi
    @Composable
    @Preview
    fun UI(){

        var displayResults by rememberSaveable { mutableStateOf(true) }

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colors.background,
        ) {

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    SearchBar(
                        displayResult = displayResults,
                        onDisplayChange = { displayChange: Boolean ->
                            displayResults = false
                            displayResults = true
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                   SpinnerButton(
                       text = "Source",
                       suggestions = feedTitles
                   )
                   SpinnerButton(
                       text = "Time range",
                       suggestions = listOf("None", "Past 24 hours", "Past Week", "Past Month")
                   )
                }
                ArticleSearchResults()
            }

        }

    }
}
















