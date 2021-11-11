package monster.minions.binocularss.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.ui.theme.BookmarkFlag

class BookmarksActivity : AppCompatActivity() {
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BinoculaRSSTheme() {
                Surface(
                    color = MaterialTheme.colors.background
                ){
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
    fun Bookmarks(bookmarked_articles: MutableList<Article>){

        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
           items(items = bookmarked_articles){ article ->
               Bookmark(article = article)
           }

        }
    }

    override fun onStop() {
        super.onStop()
        println("onStop: update article")
        MainActivity.feedDao.insertAll(*(MainActivity.feedGroup.feeds.toTypedArray()))
    }

    /**
     * Composable function representing a single bookmarked article
     *
     * @param article
     */
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun Bookmark(article: Article){

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
     * @param article
     */
    @ExperimentalCoilApi
    @Composable
    fun CardContent(article: Article){

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

        val feeds = MainActivity.feedGroup.feeds
        var articles: List<Article>
        val bookmarkedArticles: MutableList<Article> = mutableListOf()

        for (feed in feeds){
            articles = feed.articles
            for (article in articles){
                if (article.bookmarked){
                    bookmarkedArticles.add(article)
                }
            }

        }
        return bookmarkedArticles
    }
}

