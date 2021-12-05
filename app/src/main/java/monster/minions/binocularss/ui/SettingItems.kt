package monster.minions.binocularss.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat.startActivity
import monster.minions.binocularss.activities.ui.theme.paddingLarge
import monster.minions.binocularss.activities.ui.theme.paddingMedium
import monster.minions.binocularss.activities.ui.theme.roundedCornerLarge

/**
 * Display the title and subtitle of an item.
 */
@Composable
private fun TitleSubtitle(title: String, subtitle: String) {
    // If there is no subtitle, display only the title.
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
    if (subtitle != "") {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Light
        )
    }
}

/**
 * Item that runs a callback on click
 *
 * @param title Title of item
 * @param subtitle Subtitle of item
 * @param onClick Callback to invoke on click
 */
@Composable
fun ActionItem(
    title: String,
    subtitle: String = "",
    disabled: Boolean = false,
    onClick: () -> Unit
) {
    // Grey out text if disabled
    val alpha by remember {
        mutableStateOf(if (disabled) 0.6f else 0.8f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingLarge, vertical = paddingMedium)
            // Perform action when clicked if not disabled
            .clickable(enabled = !disabled) {
                onClick()
            }
    ) {
        // Column that renders title and subtitle.
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
            )
            if (subtitle != "") {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
                    fontWeight = FontWeight.Light
                )
            }
        }
    }

}

/**
 * Row that renders information popup with close button when clicked.
 *
 * @param title Title of the item.
 * @param subtitle Subtitle of the item.
 * @param content Content to render within the popup.
 */
@Composable
fun InformationPopupItem(
    title: String,
    subtitle: String = "",
    content: @Composable () -> Unit,
) {
    var showPopup by remember { mutableStateOf(false) }
    // Main row to be clicked on.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingLarge, vertical = paddingMedium)
            .clickable { showPopup = true }
    ) {
        // Column that contains title and subtitle if defined.
        Column {
            TitleSubtitle(title = title, subtitle = subtitle)
        }
    }

    // If the popup should be shown ...
    if (showPopup) {
        // Popup size information.
        val popupHeight = 400.dp
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { showPopup = false }
        ) {
            // FIXME Surface to dim the background while the popup is in view.
            //  Surface(modifier = Modifier.fillMaxSize(), color = Color(0x77000000)) {}
            // Set the elevation of the popup to be higher than default so it appears above
            //  the background.
            LocalElevationOverlay.current?.let {
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(popupHeight)
                    .background(
                        it.apply(MaterialTheme.colorScheme.background, 4.dp),
                        roundedCornerLarge
                    )
                    .padding(paddingLarge)
            }?.let {
                Box(
                    it
                ) {
                    // Render content of popup passed as lambda function along with title and
                    //  close button.
                    Column(modifier = Modifier.selectableGroup()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        content()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showPopup = false }) {
                                Text("CLOSE", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Row that links to a given email when clicked.
 *
 * @param title Title of the item.
 * @param subtitle Subtitle of the item.
 * @param email Email to be send to.
 */
@Composable
fun EmailItem(context: Context, title: String, subtitle: String = "", email: String = "") {
    // Main row to be clicked on.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingLarge, vertical = paddingMedium)
            .clickable {
                // on click, send to email application.
                if (email != "") {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, listOf(email).toTypedArray())
                    intent.putExtra(Intent.EXTRA_SUBJECT, "BinoculaRSS feedback/help")
                    // startActivity(Intent.createChooser(intent, "Select an email application"))
                    startActivity(context, intent, null)
                }
            }) {
        // Render title and subtitle.
        Column {
            TitleSubtitle(title = title, subtitle = subtitle)
        }
    }
}

/**
 * Item that takes user to linked website in their browser when clicked.
 */
@Composable
fun LinkItem(title: String, subtitle: String = "", link: String, openLink: (link: String) -> Unit) {
    // Main row to be clicked on.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingLarge, vertical = paddingMedium)
            // Open link when clicked.
            .clickable { openLink(link) }
    ) {
        // Column that renders title and subtitle.
        Column {
            TitleSubtitle(title = title, subtitle = subtitle)
        }
    }
}

/**
 * Item that renders a popup with radio buttons and a cancel button when clicked on that runs
 * a function passed as a lambda when an option is selected.
 *
 * @param title: Title of the item.
 * @param subtitle: Subtitle of the item.
 * @param radioOptions: List of strings that will be rendered as radio button text.
 * @param initialItem: Item to be initially selected.
 * @param onSelect: Function to be run when an option is selected.
 */
@Composable
fun MultipleOptionItem(
    title: String,
    subtitle: String = "",
    radioOptions: List<String>,
    initialItem: String = radioOptions[0],
    onSelect: (text: String) -> Unit,
) {
    var showPopup by remember { mutableStateOf(false) }
    // Main row to be clicked on.
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { showPopup = true }
            .padding(horizontal = paddingLarge, vertical = paddingMedium)
    ) {
        // Column that renders the title and subtitle.
        Column {
            TitleSubtitle(title = title, subtitle = subtitle)
        }
    }

    // If the popup is to be shown ...
    if (showPopup) {
        // Get popup height based on number of elements to display.
        var popupHeight = 100.dp
        for (i in radioOptions) {
            popupHeight += 56.dp
        }

        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { showPopup = false }
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(popupHeight)
                    .background(
                        // Elevate the popup so it is distinguishable from the background.
                        colorAtElevation(
                            color = MaterialTheme.colorScheme.background,
                            elevation = 4.dp
                        ),
                        roundedCornerLarge
                    )
                    .padding(paddingLarge)
            ) {
                val (selectedOption, onOptionSelected) = remember {
                    mutableStateOf(
                        initialItem
                    )
                }

                // Render radio buttons, title, text, and cancel button.
                Column(modifier = Modifier.selectableGroup()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    radioOptions.forEach { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = {
                                        // When a radio button is selected, set it to be s
                                        //  elected.
                                        onOptionSelected(text)
                                        // Call the provided function.
                                        onSelect(text)
                                        // Dismiss the popup.
                                        showPopup = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = paddingLarge),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge.merge(),
                                modifier = Modifier.padding(start = paddingLarge)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPopup = false }) {
                            Text("CANCEL", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Render item and toggle switch for boolean preferences
 *
 * @param title Title of the item.
 * @param subtitle Subtitle of the item.
 * @param checked Whether or not the item is initially checked.
 * @param onToggle The function that is run when the switch is toggled.
 */
@Composable
fun ToggleItem(
    title: String,
    subtitle: String = "",
    checked: Boolean = false,
    onToggle: (state: Boolean) -> Unit,
) {
    var checkedState by remember { mutableStateOf(checked) }
    // Main row to be clicked on.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { checkedState = !checkedState }
            .padding(horizontal = paddingLarge, vertical = paddingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render title and subtitle.
        Column {
            TitleSubtitle(title = title, subtitle = subtitle)
        }
        // Switch that toggles preferences.
        Switch(
            checked = checkedState,
            onCheckedChange = {
                checkedState = it
                onToggle(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedTrackAlpha = 0.54f,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackAlpha = 0.38f,
            )
        )
    }
}

/**
 * Title of a preference section like Appearance or About
 *
 * @param title Title of the preference section.
 */
@Composable
fun PreferenceTitle(title: String) {
    Text(
        modifier = Modifier.padding(horizontal = paddingLarge, vertical = paddingMedium),
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Normal,
    )
}