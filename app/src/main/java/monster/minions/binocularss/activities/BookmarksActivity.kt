package monster.minions.binocularss.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.DummyData
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme

class BookmarksActivity : AppCompatActivity() {
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
    @ExperimentalCoilApi
    @Composable
    fun Bookmarks(bookmarked_articles: MutableList<Article>){

        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
           items(items = bookmarked_articles){ article ->
               Bookmark(article = article)
           }

        }
    }

    /**
     * Composable function representing a single bookmarked article
     *
     * @param article
     */
    @ExperimentalCoilApi
    @Composable
    fun Bookmark(article: Article){

        Card(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            CardContent(
                article
            )

        }

    }

    @ExperimentalCoilApi
    @Composable
    fun CardContent(article: Article){

        //var expanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = if (article.image != null) article.image else "https://miro.medium.com/max/500/0*-ouKIOsDCzVCTjK-.png",
                        builder = {
                            placeholder(R.drawable.ic_launcher_foreground)
                        }
                    ),
                    contentDescription = article.description,
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                )


            }
            Text(
                text = article.title.toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Right,
            )
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

