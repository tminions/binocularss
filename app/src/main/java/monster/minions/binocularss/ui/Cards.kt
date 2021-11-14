package monster.minions.binocularss.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.dataclasses.Feed
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
                // TODO temporary until article view
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
                Column(
                    modifier = Modifier.width(200.dp)
                ) {
                    feed.title?.let { title ->
                        Text(text = title, fontWeight = FontWeight.SemiBold)
                    }
                }

                LocalElevationOverlay.current?.let {
                    Modifier
                        .size(150.dp, 150.dp)
                        .background(
                            it.apply(MaterialTheme.colors.background, 4.dp),
                            RoundedCornerShape(4.dp)
                        )
                }?.let {
                    Box(
                        it
                    ) {
                        // TODO rounded corners
                        Image(
                            painter = rememberImagePainter(
                                data = if (feed.image != "") feed.image else "https://avatars.githubusercontent.com/u/91392435?s=200&v=4",
                                builder = {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
            }
        }
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
fun ArticleCard(context: Context, article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // TODO temporary until article view
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(article.link)
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
                Column(
                    modifier = Modifier.width(200.dp)
                ) {
                    article.title?.let { title ->
                        Text(text = title, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = article.sourceTitle)
                    val formatter: DateFormat =
                        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                    var time = ""
                    try {
                        val date =
                            if (article.pubDate != null) formatter.parse(article.pubDate!!)
                            else formatter.parse("Mon, 01 Jan 1970 00:00:00 GMT")

                        val diff: Long = Date().time - date!!.time

                        when {
                            diff < 1000L * 60L * 60L -> {
                                time = "${diff / (1000L * 60L)}m"
                            }
                            diff < 1000L * 60L * 60L * 24L -> {
                                time = "${diff / (1000L * 60L * 60L)}h"
                            }
                            diff < 1000L * 60L * 60L * 24L * 30L -> {
                                time = "${diff / (1000L * 60L * 60L * 24L)}d"
                            }
                            diff < 1000L * 60L * 60L * 24L * 30L * 12L -> {
                                time = "${diff / (1000L * 60L * 60L * 24L * 30L)}M"
                            }
                            else -> {
                                time = "${diff / (1000L * 60L * 60L * 24L * 30L * 12L)}Y"
                            }
                        }
                    } catch (e: ParseException) {
//                        e.printStackTrace()
                        // time = "Invalid Date"
                        time = article.pubDate.toString()
                    }
                    Text(text = time)
                }

                Box(
                    modifier = Modifier
                        .size(150.dp, 150.dp)
                        .background(MaterialTheme.colors.background, RoundedCornerShape(4.dp))
                ) {
                    // TODO rounded corners
                    Image(
                        painter = rememberImagePainter(
                            // TODO make the banana greyed out
                            data = if (article.image != null && article.image != "") article.image else "https://avatars.githubusercontent.com/u/91392435?s=200&v=4",
                            builder = {
                                placeholder(R.drawable.ic_launcher_foreground)
                            }
                        ),
                        contentScale = ContentScale.Crop,
                        contentDescription = article.description,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookmarkFlag(article = article)
                ShareFlag(context = context, article = article)
                ReadFlag(article = article)
            }
        }
    }
}
