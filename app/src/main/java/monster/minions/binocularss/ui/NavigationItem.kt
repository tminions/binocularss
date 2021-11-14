package monster.minions.binocularss.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.ui.graphics.vector.ImageVector
import monster.minions.binocularss.R

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Article: NavigationItem("article", Icons.Filled.Article, "Article")
    object Feed: NavigationItem("feed", Icons.Filled.RssFeed, "Feed")
}
