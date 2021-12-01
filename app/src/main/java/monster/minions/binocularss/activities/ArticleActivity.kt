package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import monster.minions.binocularss.R
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.BookmarkFlag
import monster.minions.binocularss.ui.ReadFlag
import monster.minions.binocularss.ui.getTime

class ArticleActivity : ComponentActivity() {
    /**
     * Set up room database for this specific activity
     */
    // User Preferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var cacheExpiration = 0L

    // Room database variables
    private var feedGroup: FeedGroup = FeedGroup()
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    private lateinit var article: Article

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

        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()

        article = intent.getParcelableExtra("article")!!
    }

    private fun setArticle(article: Article) {
        for (feed in feedGroup.feeds) {
            val articles = feed.articles.toMutableList()
            for (possibleArticle in articles) {
                if (article == possibleArticle) {
                    feed.articles.remove(possibleArticle)
                    feed.articles.add(article)
                    break
                }
            }
        }

        // Recompose LazyColumn
        MainActivity.articleList.value = mutableListOf()
        MainActivity.bookmarkedArticleList.value = mutableListOf()
        MainActivity.searchResults.value = mutableListOf()
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

        MainActivity.bookmarkedArticleList.value = mutableListOf()
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
    }

    /**
     * Composable function for rendering HTML, since Compose does not support HTML
     *
     * @param text String of HTML
     */
    @Composable
    fun Html(text: String) {
        val onBackground = MaterialTheme.colors.onBackground
        val primary = MaterialTheme.colors.primary
        AndroidView(factory = { context ->
            TextView(context).apply {
                setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
                setTextColor(
                    // Set color with values from MaterialTheme:
                    //  We have to do it like this because this is a Java class
                    //  that expects java variables that our kotlin structures are
                    //  not directly compatible with.
                    Color.argb(
                        onBackground.alpha,
                        onBackground.red,
                        onBackground.green,
                        onBackground.blue
                    )
                )
                setLinkTextColor(
                    // Set color with values from MaterialTheme:
                    //  We have to do it like this because this is a Java class
                    //  that expects java variables that our kotlin structures are
                    //  not directly compatible with.
                    Color.argb(
                        primary.alpha,
                        primary.red,
                        primary.green,
                        primary.blue
                    )
                )
                isClickable = true
                linksClickable = true
            }
        })
    }

    @Composable
    fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { finish() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back to Article Page"
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookmarkFlag(article) { setArticle(it) }
                ReadFlag(article) { setArticle(it) }
            }
        }
    }

    /**
     * Composable function containing the article
     */
    @Composable
    private fun UI() {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        // Get elevated color to match the bottom bar that is also elevated by 8.dp
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(topBar = { TopBar() }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Article heading
                    ArticleHeading()

                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        // Article content
                        if (article.content.isNullOrEmpty() || article.content.toString() == "null") {
                            Text(
                                text = "Article has no content.",
                                style = MaterialTheme.typography.body1.copy(fontStyle = FontStyle.Italic)
                            )
                        } else {
                            Html(text = article.content!!.toString())
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "text/plain"
                                shareIntent.putExtra(Intent.EXTRA_TEXT, article.link)
                                startActivity(Intent.createChooser(shareIntent, "choose one"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 5.dp)
                        ) {
                            Text(text = "Share")
                        }
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse(article.link)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 5.dp)
                        ) {
                            Text(text = "View article")
                        }

                    }
                }
            }
        }
    }

    @Composable
    private fun ArticleInformation(
        text: String,
        typeOfInformation: String,
        atBottom: Boolean = false,
        style: TextStyle = MaterialTheme.typography.caption,
        prefix: String,
    ) {
        Text(
            text = "$prefix${
                if (text.isEmpty() || text == "NULL")
                    "UNKNOWN ${typeOfInformation.uppercase()}" else text
            }",
            style = style,
            modifier = if (atBottom) Modifier else Modifier.padding(bottom = 4.dp)
        )
    }

    /**
     * Composable fo the heading, including the article title, author, source, and published date
     */
    @ExperimentalCoilApi
    @Composable
    private fun ArticleHeading() {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                article.title.toString(),
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight(700),
                    color = MaterialTheme.colors.onBackground
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.size(12.dp))

            ArticleInformation(
                text = article.author.toString().uppercase(),
                typeOfInformation = "author",
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight(600)),
                prefix = "BY "
            )

            ArticleInformation(
                text = "${article.sourceTitle.uppercase()} / ${getTime(article.pubDate.toString())}",
                typeOfInformation = "source",
                atBottom = true,
                prefix = "",
            )

            Spacer(modifier = Modifier.size(12.dp))
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.size(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                elevation = 5.dp
            ) {
                Box(
                    modifier = Modifier.height(200.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = if (article.image != null) article.image else "https://miro.medium.com/max/500/0*-ouKIOsDCzVCTjK-.png",
                            builder = {
                                placeholder(R.drawable.ic_launcher_foreground)
                            }
                        ),
                        contentDescription = article.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}