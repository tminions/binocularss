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

    /**
     * Composable function for rendering HTML, since Compose does not support HTML
     *
     * @param text String of HTML
     */
    @Composable
    fun Html(text: String) {
        AndroidView(factory = { context ->
            TextView(context).apply {
                setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
            }
        })
    }

    /**
     * Composable function containing the article
     */
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
//            Article heading
            ArticleHeading()

            Box(modifier = Modifier.padding(bottom = 12.dp)) {
//            Article content
                if (article.content.isNullOrEmpty() || article.content.toString() == "null") {
                    Text(
                        text = "Article has no content.",
                        style = MaterialTheme.typography.body1.copy(fontStyle = FontStyle.Italic)
                    )
                } else {
                    Html(text = article.content!!.toString())
                }
            }

//            Buttons
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

    @Composable
    private fun ArticleInformation(
        text: String,
        typeOfInformation: String,
        atBottom: Boolean = false,
        style: TextStyle = MaterialTheme.typography.caption
    ) {
        Text(
            text = "By ${
                if (text.isNullOrEmpty() || text == "null")
                    "Unknown $typeOfInformation" else text
            }".uppercase(),
            style = style,
            modifier = if (atBottom) Modifier else Modifier.padding(bottom = 4.dp)
        )
    }

    /**
     * Composable fo the heading, including the article title, author, source, and published date
     */
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

            ArticleInformation(
                text = article.author.toString(),
                typeOfInformation = "author",
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight(600))
            )

            ArticleInformation(
                text = article.sourceName.toString(),
                typeOfInformation = "source",
            )

            ArticleInformation(
                text = article.pubDate.toString(),
                typeOfInformation = "date",
                atBottom = true
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