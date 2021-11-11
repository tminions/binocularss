package monster.minions.binocularss.ui.theme

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import monster.minions.binocularss.dataclasses.Article
import androidx.compose.material.icons.filled.Bookmark as FilledBookmarkIcon
import androidx.compose.material.icons.filled.BookmarkBorder as EmptyBookmarkIcon

/**
 *
 *
 * Composable representing a flag for each bookmarked
 * article
 *
 * @param article
 *
 *
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
            contentDescription = null
        )
    }

}