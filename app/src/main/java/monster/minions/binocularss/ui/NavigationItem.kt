package monster.minions.binocularss.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import monster.minions.binocularss.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Article: NavigationItem("article", R.drawable.ic_baseline_article_24, "Article")
    object Feed: NavigationItem("feed", R.drawable.ic_baseline_rss_feed_24, "Feed")
}
