package monster.minions.binocularss.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorFilter.Companion.colorMatrix
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.activities.ArticleActivity
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed

/**
 * Displays a single feed in a card view format
 *
 * @param feed The feed to be displayed
 */
@ExperimentalCoilApi
@Composable
fun FeedCard(context: Context, feed: Feed) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // TODO temporary until articleFromFeed
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(feed.link)
                startActivity(context, intent, null)
            },
        elevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column for feed title.
                Column(
                    modifier = Modifier.width(200.dp)
                ) {
                    feed.title?.let { title ->
                        Text(text = title, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Box for feed image if there is one.
                Box(
                    Modifier
                        .size(150.dp, 150.dp)
                        .background(MaterialTheme.colors.background, RoundedCornerShape(4.dp))
                ) {
                    // TODO rounded corners
                    Image(
                        painter = rememberImagePainter(
                            // TODO eamon (maybe) make the banana greyed out. I don't even think
                            //  that it ever shows up, it usually just shows an empty view so
                            //  getting this working in the first place would be good too
                            data = if (feed.image != "null") feed.image else "https://avatars.githubusercontent.com/u/91392435?s=200&v=4",
                            builder = {
                                // Placeholder when the image hasn't loaded yet.
                                placeholder(R.drawable.ic_launcher_foreground)
                            }
                        ),
                        contentScale = ContentScale.Crop,
                        contentDescription = feed.description,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }

        // Row for buttons in the future that is currently not used
        // Row(
        //     modifier = Modifier
        //         .fillMaxWidth()
        // ) {
        // }
    }
}

/**
 * Displays a single article in a card view format
 *
 * @param article The article to be displayed
 */
@SuppressLint("SimpleDateFormat")
@ExperimentalCoilApi
@Composable
fun ArticleCard(context: Context, article: Article, updateValues: (article: Article) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, ArticleActivity::class.java)
                intent.putExtra("article", article)
                article.read = true
                updateValues(article)
                startActivity(context, intent, null)
            },
        elevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column for title, feed, and time.
                Column(
                    modifier = Modifier.width(200.dp).padding(end = 12.dp)
                ) {
                    article.title?.let { title ->
                        Text(text = title, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = article.sourceTitle)
                    Text(text = getTime(article.pubDate!!))
                }

                val grayScaleMatrix = ColorMatrix(
                    floatArrayOf(
                        0.33f, 0.33f, 0.33f, 0f, 0f,
                        0.33f, 0.33f, 0.33f, 0f, 0f,
                        0.33f, 0.33f, 0.33f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )

                val imageExists = article.image != "null"

                // Box for image on the right.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    elevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp, 150.dp)
                            .background(MaterialTheme.colors.background, RoundedCornerShape(4.dp))
                    ) {
                        // TODO rounded corners
                        Image(
                            painter = rememberImagePainter(
                                // TODO eamon (maybe) make the banana greyed out. I don't even think
                                //  that it ever shows up, it usually just shows an empty view so
                                //  getting this working in the first place would be good too
                                data =
                                if (imageExists) article.image
                                else "https://avatars.githubusercontent.com/u/91392435?s=200&v=4",
                                builder = {
                                    // Placeholder when the image hasn't loaded yet.
                                    placeholder(R.drawable.ic_launcher_foreground)
                                }
                            ),
                            contentScale = ContentScale.Crop,
                            contentDescription = article.description,
                            colorFilter = if (imageExists) null else colorMatrix(
                                grayScaleMatrix
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }

            // Row for buttons on the bottom.
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookmarkFlag(article = article) { updateValues(article) }
                ShareFlag(context = context, article = article)
                ReadFlag(article = article) { updateValues(article) }
                // TODO eamon add open link in browser icon
            }
        }
    }
}
