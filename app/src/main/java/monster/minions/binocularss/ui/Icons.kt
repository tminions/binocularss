package monster.minions.binocularss.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import monster.minions.binocularss.dataclasses.Article
import androidx.compose.material.icons.filled.Bookmark as FilledBookmarkIcon
import androidx.compose.material.icons.filled.BookmarkBorder as EmptyBookmarkIcon

/**
 *
 *
 * Composable representing a flag for each bookmarked
 * article
 *
 * @param article Article that is currently being displayed
 *
 * This is a violation of clean architecture, but the
 * alternative is state hoisting, which we did not have
 * time to implement.
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