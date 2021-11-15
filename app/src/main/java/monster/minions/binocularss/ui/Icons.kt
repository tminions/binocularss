package monster.minions.binocularss.ui

import android.content.Context
import android.content.Intent
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat.startActivity
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
fun BookmarkFlag(article: Article, extraAction: (article: Article) -> Unit = { }) {
    var isBookmark by remember { mutableStateOf(article.bookmarked) }

    IconButton(
        onClick = {
            isBookmark = !isBookmark
            article.bookmarked = !article.bookmarked
            extraAction(article)
        }
    ) {
        Icon(
            imageVector = if (article.bookmarked) Icons.Filled.FilledBookmarkIcon else Icons.Filled.EmptyBookmarkIcon,
            contentDescription = if (isBookmark) "Mark as unbookmarked" else "Mark as bookmarked"
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
fun ReadFlag(article: Article, extraAction: (article: Article) -> Unit = { }) {
    var isRead by remember { mutableStateOf(article.read) }

    IconButton(
        onClick = {
            isRead = !isRead
            article.read = !article.read
            extraAction(article)
        }
    ) {
        Icon(
            imageVector = if (article.read) Icons.Filled.TaskAlt else Icons.Filled.PanoramaFishEye,
            contentDescription = if (isRead) "Mark as unread" else "Mark as read"
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