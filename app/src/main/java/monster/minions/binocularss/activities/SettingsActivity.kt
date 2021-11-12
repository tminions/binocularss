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

    @Composable
    fun InformationPopupItem(title: String, subtitle: String = "") {
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

        Spacer(modifier = Modifier.padding(4.dp))
    }

    @Composable
    fun EmailItem(title: String, subtitle: String = "", email: String = "") {
        Row(
            modifier = Modifier.clickable {
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
        Spacer(modifier = Modifier.padding(8.dp))
    }

    @Composable
    fun LinkItem(title: String, subtitle: String = "", link: String = "") {
        Row(
            modifier = Modifier.clickable {
                if (link != "") {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(link)
                    startActivity(intent)
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
        Spacer(modifier = Modifier.padding(8.dp))
    }

    @Composable
    fun MultipleOptionItem(title: String, subtitle: String = "", radioOptions: List<String>) {
        var showPopup by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.clickable {
                showPopup = true
            }
        ) {
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
        Spacer(modifier = Modifier.padding(8.dp))

        if (showPopup) {
            // TODO find a better way to get the popup width. 70% of parent? Use what play store did?
            val popupWidth = 300.dp
            var popupHeight = 50.dp
            for (i in radioOptions) {
                popupHeight += 60.dp
            }
            val cornerSize = 16.dp

            // TODO find a way to dim the rest of the screen. Maybe another popup?
            Popup(alignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(popupWidth, popupHeight)
                        .background(MaterialTheme.colors.background, RoundedCornerShape(cornerSize))
                        .padding(16.dp)
                ) {
                    // TODO Maybe pass index of initial state as parameter or just the text itself
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

                    Column(modifier = Modifier.selectableGroup()) {
                        radioOptions.forEach { text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = {
                                            onOptionSelected(text)
                                            // TODO make appropriate function calls here
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
                    }
                }
            }
        }
    }

    @Composable
    fun ToggleItem(title: String, subtitle: String = "") {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(subtitle)
                }
            }
            val checkedState = remember { mutableStateOf(true) }
            Switch(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it },
                colors = switchColors(
                    checkedThumbColor = MaterialTheme.colors.primary,
                    checkedTrackColor = MaterialTheme.colors.primary,
                    checkedTrackAlpha = 0.54f
                )
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }

    @Composable
    fun PreferenceTitle(title: String) {
        Text(
            buildAnnotatedString {
                withStyle(style = ParagraphStyle(lineHeight = 30.sp)) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                        append(title)
                    }
                }
            },
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.padding(8.dp))
    }

    @Preview(showBackground = true)
    @Composable
    fun UI() {
        val padding = 16.dp

        Surface(color = MaterialTheme.colors.background) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                PreferenceTitle(title = "Appearance")
                MultipleOptionItem(
                    title = "Theme",
                    subtitle = "Get Current Setting",
                    radioOptions = listOf("Light Theme", "Dark Theme", "System Default")
                )
                ToggleItem(title = "Material You Theme")
                Divider()
                Spacer(modifier = Modifier.padding(16.dp))

                PreferenceTitle(title = "Preferences")
                MultipleOptionItem(
                    title = "Cache Expiration Time",
                    subtitle = "1 hour",
                    radioOptions = listOf(
                        "24 hours",
                        "12 hours",
                        "6 hours",
                        "1 hour",
                        "15 minutes",
                        "off"
                    )
                )
                Divider()
                Spacer(modifier = Modifier.padding(16.dp))

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
                Divider()
                Spacer(modifier = Modifier.padding(16.dp))

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
                InformationPopupItem(title = "Open Source Libraries")


            }
        }

        /*
        _Appearance_
        MultipleOption:     dark mode/light mode
        Toggle:             material you mode
        _Preferences_
        MultipleOption:     cache invalidation time
        _Support_
        Information:        contact
        Information:        feedback
        Information:        bug report
        _About_
        Information:        gitHub link
        Information:        version number
        Information popup:  open source libraries
         */
    }
}