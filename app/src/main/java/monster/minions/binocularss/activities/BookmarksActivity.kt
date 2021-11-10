package monster.minions.binocularss.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark as FilledBookmarkIcon
import androidx.compose.material.icons.filled.BookmarkBorder as EmptyBookmarkIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.ui.theme.BinoculaRSSTheme
import kotlin.math.max

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

    @ExperimentalCoilApi
    @Composable
    fun CardContent(article: Article){

        var expanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,

        ) {
            var maxBaseline by remember { mutableStateOf(0f) }
            fun updateMaxBaseline(textLayoutResult: TextLayoutResult){
                maxBaseline = max(maxBaseline, textLayoutResult.size.height - textLayoutResult.lastBaseline)
            }
            val topBaselinePadding = with(LocalDensity.current) { maxBaseline.toDp() }


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(

                ) {
                    Text(
                        text = article.title.toString(),
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .width(120.dp)
                            .paddingFromBaseline(bottom = topBaselinePadding),
                        onTextLayout = ::updateMaxBaseline
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
                            .padding(bottom = topBaselinePadding)
                            .size(120.dp)
                    )
                }
                if (expanded){
                   Text(
                       text = article.link.toString()
                   )
                }
                BookmarkButton(isBookmarked = false, onClick = {})
            }




        }

    }

    @Composable
    fun BookmarkButton(
        isBookmarked: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ){
        IconToggleButton(
            checked = isBookmarked,
            onCheckedChange = { onClick() },
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Filled.FilledBookmarkIcon else Icons.Filled.EmptyBookmarkIcon,
                contentDescription = null
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
                    if (article.image == null && article.title.toString().startsWith("Covid-19 spreads through the air.")){
                        Log.d("NULL IMAGE", "No image for this article")
                    }
                    bookmarkedArticles.add(article)
                }
            }

        }
        return bookmarkedArticles
    }
}

