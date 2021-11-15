package monster.minions.binocularss.ui

import android.content.Context
import android.content.Intent
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat.startActivity
import androidx.compose.material.icons.filled.Search

import androidx.compose.ui.tooling.preview.Preview
import monster.minions.binocularss.dataclasses.Article
import androidx.compose.material.icons.filled.Bookmark as FilledBookmarkIcon
import androidx.compose.material.icons.filled.BookmarkBorder as EmptyBookmarkIcon


/**
 * Composable representing a flag for each bookmarked
 * article
 *
 * @param article Article that is currently being displayed
 */
@Composable
fun BookmarkFlag(article: Article) {
    var isBookmarked by remember { mutableStateOf(article.bookmarked) }

    IconButton(
        onClick = {
            isBookmarked = !isBookmarked
            article.bookmarked = !article.bookmarked
        }
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Filled.FilledBookmarkIcon else Icons.Filled.EmptyBookmarkIcon,
            contentDescription = if (isBookmarked) "Click to unbookmark" else "Click to bookmark"
        )
    }
}

/**
 * Composable representing a flag for each bookmarked
 * article
 *
 * @param context The application context from which to share
 * @param article Article that is currently being displayed
 */
@Composable
fun ShareFlag(context: Context, article: Article) {
    IconButton(
        onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, article.link)
            startActivity(context, Intent.createChooser(shareIntent, "choose one"), null)
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = "Share"
        )
    }
}

/**
 * Composable representing a flag for each bookmarked
 * article
 *
 * @param article Article that is currently being displayed
 */
@Composable
fun ReadFlag(article: Article) {
    var isRead by remember { mutableStateOf(article.read) }

    IconButton(
        onClick = {
            isRead = !isRead
            article.read = !article.read
        }
    ) {
        Icon(
            imageVector = if (article.read) Icons.Filled.TaskAlt else Icons.Filled.PanoramaFishEye,
            contentDescription = if (isRead) "Mark as unread" else "Mark as read"
        )
    }
}

@Composable
@Preview
fun SearchIcon(){


    IconButton(
        onClick = {}
    ){
       Icon(
           imageVector = Icons.Filled.Search,
           contentDescription = null
       )
    }
}
