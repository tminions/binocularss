package monster.minions.binocularss.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A class to contain the navigation items for the Navigation function.
 */
sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Articles: NavigationItem("articles", Icons.Filled.Article, "Articles")
    object Feeds: NavigationItem("feeds", Icons.Filled.RssFeed, "Feeds")
    object Bookmarks: NavigationItem("bookmarks", Icons.Filled.Bookmark, "Bookmarks")
    object ReadingHistory: NavigationItem("readingHistory", Icons.Filled.History, "History")
}
