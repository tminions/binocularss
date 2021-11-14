package monster.minions.binocularss.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter.State.Empty.painter
import coil.compose.rememberImagePainter
import monster.minions.binocularss.R
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.Article
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao

class ArticleActivity : ComponentActivity() {
    /**
     * Set up room database for this specific activity
     */
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    private lateinit var article: Article

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
                    ArticleContainer()
                }

            }
        }

        article = intent.getParcelableExtra<Article>("article")!!
        Log.d("ArticleActivity", article.toString())
    }

    @Composable
    fun Html(text: String) {
        AndroidView(factory = { context ->
            TextView(context).apply {
                setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
            }
        })
    }

    @Composable
    @Preview
    private fun ArticleContainer() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
////        Article title
//            Text(
//                text = article.title.toString(),
//                style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight(700)),
//                modifier = Modifier.padding(bottom = 4.dp)
//            )
////        Article description
//            Text(
//                text = article.description.toString(), style = MaterialTheme.typography.body1.copy(
////                color = MaterialTheme.colors.
//                )
//            )
//
//            Image(
//                painter = rememberImagePainter(
//                    data = if (article.image != null) article.image else "https://miro.medium.com/max/500/0*-ouKIOsDCzVCTjK-.png",
//                    builder = {
//                        placeholder(R.drawable.ic_launcher_foreground)
//                    }
//                ),
//                contentDescription = article.description,
//                modifier = Modifier.fillMaxWidth()
//            )

            ArticleHeading()

            if (article.content.isNullOrEmpty()) {
                Text(
                    text = "Article has no content",
                    style = MaterialTheme.typography.body1.copy(fontStyle = FontStyle.Italic)
                )
            } else {
                Html(text = article.content!!.toString())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT, article.link)
                        startActivity( Intent.createChooser(shareIntent, "choose one"))
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

    @Composable
    private fun ArticleHeading() {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                article.title.toString(),
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight(700),
                    fontFamily = FontFamily.Serif,
                    color = Color.DarkGray
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Divider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "By ${
                    if (!article.author.isNullOrEmpty() && article.author.toString() != "null") article.author.toString()
                    else "Unknown author"
                }".uppercase(),
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight(600)),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Log.d("asdfjklasdfljkasdfjkl", article.sourceName!!::class.qualifiedName!!)

            Text(
                text = "From ${
                    if (!article.sourceName.isNullOrEmpty() && article.sourceName.toString() != "null") article.sourceName.toString()
                    else "Unknown source"
                }".uppercase(),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Published ${
                    if (!article.pubDate.isNullOrEmpty()) article.pubDate.toString() else "Unknown date"
                }".uppercase(),

                style = MaterialTheme.typography.caption
            )

            Spacer(modifier = Modifier.size(12.dp))
            Divider(color = Color.LightGray, thickness = 1.dp)
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