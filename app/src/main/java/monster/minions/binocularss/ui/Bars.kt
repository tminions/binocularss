package monster.minions.binocularss.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import monster.minions.binocularss.activities.ui.theme.paddingSmall

/**
 * Top navigation bar
 *
 * @param title The title of the current view.
 * @param backButtonAction A lambda function that will exit the current activity and return to the parent.
 */
@Composable
fun TopBar(title: String, backButtonAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button icon that goes back one activity.
        IconButton(onClick = { backButtonAction() }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back Arrow"
            )
        }
        Spacer(Modifier.padding(paddingSmall))
        // Title of current page.
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
