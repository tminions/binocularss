package monster.minions.binocularss.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import monster.minions.binocularss.ui.ArticleCard
import monster.minions.binocularss.ui.BookmarkFlag

class BookmarksActivity : AppCompatActivity() {

    /**
     * Set up room database for this specific activity
     */
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    private var feedGroup = FeedGroup()

    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "feed-db"
        ).allowMainThreadQueries().build()
        feedDao = (db as AppDatabase).feedDao()
        setContent {
            BinoculaRSSTheme() {
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    Bookmarks(getAllBookmarks())
                }

            }
        }
    }

    /**
     * Composable function that generates the list of bookmarked
     * articles
     *
     * @param bookmarked_articles MutableList of articles that are bookmarked
     * across all feeds
     */
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun Bookmarks(bookmarked_articles: MutableList<Article>) {
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = bookmarked_articles) { article ->
                ArticleCard(this@BookmarksActivity, article = article)
            }

        }
    }

    /**
     * Save the list of user feeds to the Room database (feed-db) for data persistence.
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called before `onStop` and `onDestroy` or any time a "stop" happens. This
     * includes when an app is exited but not closed.
     */
    override fun onPause() {
        super.onPause()
        Log.d("BookmarksActivity", "onPause called")
        feedDao.insertAll(*(feedGroup.feeds.toTypedArray()))
    }

    /**
     * Get the list of user feeds from the Room database (feed-db).
     *
     * The database files can be found in `Android/data/data/monster.minions.binocularss.databases`.
     *
     * This function is called after `onCreate` or any time a "resume" happens. This includes
     * the app being opened after the app is exited but not closed.
     */
    override fun onResume() {
        super.onResume()
        Log.d("BookmarksActivity", "onResume called")
        feedGroup.feeds = feedDao.getAll()
    }

    /**
     * Composable function representing a single bookmarked article
     *
     * @param article Current Article being displayed
     */
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun Bookmark(article: Article) {
        Card(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            onClick = {
                val uri = Uri.parse(article.link)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        ) {
            CardContent(
                article
            )
        }
    }

    /**
     * Composable for actual content of the bookmarked
     * article
     *
     * @param article Current Article being displayed
     */
    @ExperimentalCoilApi
    @Composable
    fun CardContent(article: Article) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = article.title.toString(),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .size(120.dp)
                )
                Image(
                    painter = rememberImagePainter(
                        data = if (article.image != null) article.image else "https://miro.medium.com/max/500/0*-ouKIOsDCzVCTjK-.png",
                        builder = {
                            placeholder(R.drawable.ic_launcher_foreground)
                        }
                    ),
                    contentDescription = article.description,
                    modifier = Modifier
                        .size(120.dp)
                )
            }
            Text(
                text = article.pubDate.toString(),
                fontWeight = FontWeight(10)
            )
            BookmarkFlag(article)
        }
    }

    /**
     * Returns a list of bookmarked articles within the feedgroup
     * stored within MainActivity
     */
    private fun getAllBookmarks(): MutableList<Article> {
        val bookmarkedArticles: MutableList<Article> = mutableListOf()

        for (feed in feedGroup.feeds) {
            for (article in feed.articles) {
                if (article.bookmarked) {
                    bookmarkedArticles.add(article)
                }
            }
        }

        return bookmarkedArticles
    }
}

