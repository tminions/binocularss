package monster.minions.binocularss.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.activities.ArticleActivity
import monster.minions.binocularss.dataclasses.Article

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
                ContextCompat.startActivity(context, intent, null)
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
                    modifier = Modifier
                        .width(200.dp)
                        .padding(end = 12.dp)
                ) {
                    article.title?.let { title ->
                        Text(text = title, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = article.sourceTitle)
                    Text(text = getTime(article.pubDate!!))
                }

                CardImage(image = article.image!!, description = article.description!!)
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
                BrowserFlag(context = context, article = article)
            }
        }
    }
}


/**
 * Formats the image and article/feed description if available.
 * If not, put in a placeholder image (tminions logo) or empty string for description
 *
 * @param image string that represents the URL of the image
 * @param description Article/Feed descriptions
 */
@Composable
fun CardImage(image: String, description: String = "") {

    // Color matrix to turn image grayscale
    val grayScaleMatrix = ColorMatrix(
        floatArrayOf(
            0.33f, 0.33f, 0.33f, 0f, 0f,
            0.33f, 0.33f, 0.33f, 0f, 0f,
            0.33f, 0.33f, 0.33f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    val imageExists = image != "null" && image != null

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
            Image(
                painter = rememberImagePainter(
                    data =
                    if (imageExists) image
                    else "https://avatars.githubusercontent.com/u/91392435?s=200&v=4",
                    builder = {
                        // Placeholder when the image hasn't loaded yet.
                        placeholder(R.drawable.ic_launcher_foreground)
                    }
                ),
                contentScale = ContentScale.Crop,
                contentDescription = description,
                colorFilter = if (imageExists) null else ColorFilter.colorMatrix(
                    grayScaleMatrix
                ),
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}