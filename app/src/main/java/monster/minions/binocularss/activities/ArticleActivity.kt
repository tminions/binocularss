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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import monster.minions.binocularss.R
import monster.minions.binocularss.activities.ui.theme.*
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.operations.SortArticles
import monster.minions.binocularss.operations.SortArticlesByDateStrategy
import monster.minions.binocularss.operations.getArticlesFromFeed
import monster.minions.binocularss.room.DatabaseGateway
import monster.minions.binocularss.ui.BookmarkFlag
import monster.minions.binocularss.ui.ReadFlag
import monster.minions.binocularss.ui.getTime
import kotlin.properties.Delegates

class ArticleActivity : ComponentActivity() {
    /**
     * Set up room database for this specific activity
     */
    // User Preferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var materialYou by Delegates.notNull<Boolean>()
    private lateinit var materialYouState: MutableState<Boolean>

    // Room database variables
    private var feedGroup: FeedGroup = FeedGroup()
    private lateinit var dataGateway: DatabaseGateway

    private lateinit var article: Article

    @ExperimentalCoilApi
    @ExperimentalMaterial3Api
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

        sharedPref = getSharedPreferences(
            SettingsActivity.PreferenceKeys.SETTINGS,
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)

        dataGateway = DatabaseGateway(context = this)


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
        MainActivity.readArticleList.value = mutableListOf()
        MainActivity.searchResults.value = mutableListOf()
        MainActivity.currentFeedArticles.value = mutableListOf()
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
        dataGateway.addFeeds(feedGroup.feeds)

        MainActivity.articleList.value = mutableListOf()
        MainActivity.bookmarkedArticleList.value = mutableListOf()
        MainActivity.currentFeedArticles.value = mutableListOf()
        MainActivity.readArticleList.value = mutableListOf()
        MainActivity.searchResults.value = mutableListOf()
        MainActivity.feedList.value = mutableListOf()
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
        feedGroup.feeds = dataGateway.read()
        theme =
            sharedPref.getString(SettingsActivity.PreferenceKeys.THEME, "System Default").toString()
        materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)
    }

    /**
     * Composable function for rendering HTML, since Compose does not support HTML
     *
     * @param text String of HTML
     */
    @Composable
    fun Html(text: String) {
        val onBackground = MaterialTheme.colorScheme.onBackground
        val primary = MaterialTheme.colorScheme.primary
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
                .padding(end = paddingLarge),
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
    @ExperimentalCoilApi
    @ExperimentalMaterial3Api
    @Composable
    private fun UI() {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = when (theme) {
            "Dark Theme" -> false
            "Light Theme" -> true
            else -> !isSystemInDarkTheme()
        }
        val color = MaterialTheme.colorScheme.background
        // Get elevated color to match the bottom bar that is also elevated by 8.dp
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(topBar = { TopBar() }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = paddingLargeMedium)
                        .padding(bottom = paddingLargeMedium)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Article heading
                    ArticleHeading()

                    Box(modifier = Modifier.padding(bottom = paddingLargeMedium)) {
                        // Article content
                        if (article.content.isNullOrEmpty() || article.content.toString() == "null") {
                            Text(
                                text = "Article has no content.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
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
                                .padding(end = paddingSmall)
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
                                .padding(start = paddingSmall)
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
        style: TextStyle = MaterialTheme.typography.labelMedium,
        prefix: String,
    ) {
        Text(
            text = "$prefix${
                if (text.isEmpty() || text == "NULL")
                    "UNKNOWN ${typeOfInformation.uppercase()}" else text
            }",
            style = style,
            modifier = if (atBottom) Modifier else Modifier.padding(bottom = paddingSmall)
        )
    }

    /**
     * Composable fo the heading, including the article title, author, source, and published date
     */
    @ExperimentalCoilApi
    @Composable
    private fun ArticleHeading() {
        Column(modifier = Modifier.padding(bottom = paddingLargeMedium)) {
            Text(
                article.title.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight(700),
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(vertical = paddingLargeMedium)
            )

            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.size(paddingLargeMedium))

            ArticleInformation(
                text = article.author.toString().uppercase(),
                typeOfInformation = "author",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight(600)),
                prefix = "BY "
            )

            ArticleInformation(
                text = "${article.sourceTitle.uppercase()} / ${getTime(article.pubDate.toString())}",
                typeOfInformation = "source",
                atBottom = true,
                prefix = "",
            )

            Spacer(modifier = Modifier.size(paddingLargeMedium))
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.size(paddingLargeMedium))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = roundedCornerSmall,
                elevation = 4.dp
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