package monster.minions.binocularss.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
     var isBookmarked by remember { mutableStateOf(article.bookmarked) }

    IconButton(
        onClick = {
             isBookmarked = !isBookmarked
            article.bookmarked = !article.bookmarked
            extraAction(article)
        }
    ) {
        Icon(
            imageVector = if (article.bookmarked) Icons.Filled.FilledBookmarkIcon else Icons.Filled.EmptyBookmarkIcon,
            contentDescription = if (isBookmarked) "Mark as unbookmarked" else "Mark as bookmarked"
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


/**
 * Composable representing a flag for each bookmarked
 * article
 *
 * @param context The application context from which to share
 * @param article Article that is currently being displayed
 */
@Composable
fun BrowserFlag(context: Context, article: Article) {
    IconButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(article.link)
            startActivity(context, intent, null)
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Public,
            contentDescription = "Open in browser"
        )
    }
}