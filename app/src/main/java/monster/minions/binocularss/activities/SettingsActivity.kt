package monster.minions.binocularss.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import androidx.compose.material.RadioButtonDefaults.colors as radioButtonColors
import androidx.compose.material.SwitchDefaults.colors as switchColors

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BinoculaRSSTheme {
                UI()
            }
        }
    }

    private fun openLink(link: String) {
        if (link != "") {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(link)
            startActivity(intent)
        }
    }

    @Composable
    fun InformationPopupItem(
        title: String,
        subtitle: String = "",
        content: @Composable () -> Unit
    ) {
        var showPopup by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clickable { showPopup = true }
        ) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 12.sp
                                )
                            ) {
                                append(subtitle)
                            }
                        }
                    )
                }
            }
        }

        if (showPopup) {
            // TODO possibly find a way to get 70% of parent width
            val popupWidth = 300.dp
            val popupHeight = 400.dp
            val cornerSize = 16.dp

            // Create popup
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPopup = false }
            ) {
                LocalElevationOverlay.current?.let {
                    Modifier
                        .size(popupWidth, popupHeight)
                        .background(
                            it.apply(MaterialTheme.colors.background, 4.dp),
                            RoundedCornerShape(cornerSize)
                        )
                        .padding(16.dp)
                }?.let {
                    Box(
                        it
                    ) {
                        Column(modifier = Modifier.selectableGroup()) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(title)
                                    }
                                }
                            )
                            content()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showPopup = false }) {
                                    Text("CLOSE", color = MaterialTheme.colors.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun EmailItem(title: String, subtitle: String = "", email: String = "") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clickable {
                    if (email != "") {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.data = Uri.parse("mailto:")
                        intent.putExtra(Intent.EXTRA_EMAIL, listOf(email).toTypedArray())
                        intent.putExtra(Intent.EXTRA_SUBJECT, "BinoculaRSS feedback/help")
                        startActivity(Intent.createChooser(intent, "Select an email application"))
                    }
                }) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 12.sp
                                )
                            ) {
                                append(subtitle)
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun LinkItem(title: String, subtitle: String = "", link: String = "") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clickable { openLink(link) }
        ) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 12.sp
                                )
                            ) {
                                append(subtitle)
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun MultipleOptionItem(title: String, subtitle: String = "", radioOptions: List<String>) {
        var showPopup by remember { mutableStateOf(false) }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { showPopup = true }
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 12.sp
                                )
                            ) {
                                append(subtitle)
                            }
                        }
                    )
                }
            }
        }

        if (showPopup) {
            // TODO possibly find a way to get 70% of parent width
            val popupWidth = 300.dp

            // Get popup height based on number of elements to display
            var popupHeight = 90.dp
            for (i in radioOptions) {
                popupHeight += 56.dp
            }
            val cornerSize = 16.dp

            // Create popup
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPopup = false }
            ) {
                LocalElevationOverlay.current?.let {
                    Modifier
                        .size(popupWidth, popupHeight)
                        .background(
                            it.apply(MaterialTheme.colors.background, 4.dp),
                            RoundedCornerShape(cornerSize)
                        )
                        .padding(16.dp)
                }?.let {
                    Box(
                        it
                    ) {
                        // TODO Maybe pass index of initial state as parameter or just the text itself
                        val (selectedOption, onOptionSelected) = remember {
                            mutableStateOf(
                                radioOptions[0]
                            )
                        }

                        Column(modifier = Modifier.selectableGroup()) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(title)
                                    }
                                }
                            )
                            radioOptions.forEach { text ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = {
                                                onOptionSelected(text)
                                                // TODO make appropriate function calls here to save data
                                                showPopup = false
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (text == selectedOption),
                                        onClick = null,
                                        colors = radioButtonColors(
                                            selectedColor = MaterialTheme.colors.primary
                                        )
                                    )
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.body1.merge(),
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showPopup = false }) {
                                    Text("CANCEL", color = MaterialTheme.colors.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ToggleItem(title: String, subtitle: String = "") {
        var checkedState by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { checkedState = !checkedState }
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(subtitle)
                }
            }
            Switch(
                checked = checkedState,
                onCheckedChange = { checkedState = it },
                colors = switchColors(
                    checkedThumbColor = MaterialTheme.colors.primary,
                    checkedTrackColor = MaterialTheme.colors.primary,
                    checkedTrackAlpha = 0.54f
                )
            )
        }
    }

    @Composable
    fun PreferenceTitle(title: String) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            text = buildAnnotatedString {
                withStyle(style = ParagraphStyle(lineHeight = 30.sp)) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                        append(title)
                    }
                }
            },
            fontSize = 20.sp
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun UI() {
        val padding = 16.dp

        Surface(color = MaterialTheme.colors.background) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = padding, bottom = padding)
                    .verticalScroll(rememberScrollState())
            ) {
                PreferenceTitle(title = "Appearance")
                MultipleOptionItem(
                    title = "Theme",
                    subtitle = "Get Current Setting",
                    radioOptions = listOf("Light Theme", "Dark Theme", "System Default")
                )
                ToggleItem(title = "Material You Theme")
                Divider(modifier = Modifier.padding(bottom = 16.dp))

                PreferenceTitle(title = "Preferences")
                MultipleOptionItem(
                    title = "Cache Expiration Time",
                    subtitle = "1 hour",
                    radioOptions = listOf(
                        "24 Hours",
                        "12 Hours",
                        "6 Hours",
                        "1 Hour",
                        "30 Minutes",
                        "15 Minutes",
                        "Off"
                    )
                )
                Divider(modifier = Modifier.padding(bottom = 16.dp))

                PreferenceTitle(title = "Support")
                EmailItem(
                    title = "Contact",
                    email = "hisbaan@gmail.com"
                )
                LinkItem(
                    title = "Feedback",
                    link = "google form or something"
                )
                LinkItem(
                    title = "Bug Report",
                    link = "https://github.com/tminions/binocularss/issues/new"
                )
                Divider(modifier = Modifier.padding(bottom = 16.dp))

                PreferenceTitle(title = "About")
                LinkItem(
                    title = "GitHub",
                    subtitle = "github.com/tminions/binocularss",
                    link = "https://github.com/tminions/binocularss"
                )
                LinkItem(
                    title = "Version",
                    subtitle = "1.0",
                    link = "https://github.com/tminions/binocularss/releases"
                )
                InformationPopupItem(title = "Open Source Libraries") {
                    Text(
                        "RSS-Parser",
                        Modifier
                            .padding(top = 4.dp)
                            .clickable { openLink("https://github.com/prof18/RSS-Parser") })
                    Text(
                        "Coil",
                        Modifier
                            .padding(top = 4.dp)
                            .clickable { openLink("https://github.com/coil-kt/coil") })
                }
            }
        }
    }
}