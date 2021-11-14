package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.CACHE_EXPIRATION
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.SETTINGS
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.THEME
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import androidx.compose.material.RadioButtonDefaults.colors as radioButtonColors
import androidx.compose.material.SwitchDefaults.colors as switchColors

class SettingsActivity : ComponentActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var cacheExpiration = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            themeState = remember { mutableStateOf(theme) }
            BinoculaRSSTheme(
                theme = themeState.value
            ) {
                UI()
            }
        }

        sharedPref = this.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref.getString(THEME, "System Default").toString()
        cacheExpiration = sharedPref.getLong(CACHE_EXPIRATION, 0L)
    }

    object PreferenceKeys {
        const val SETTINGS = "settings"
        const val THEME = "theme"
        const val CACHE_EXPIRATION = "cacheExpiration"
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
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
            }
        }

        if (showPopup) {
            val popupHeight = 400.dp
            val cornerSize = 16.dp

            // Create popup
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPopup = false }
            ) {
                LocalElevationOverlay.current?.let {
                    Modifier
                        .fillMaxWidth(0.8f)
                        .height(popupHeight)
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
                            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
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
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
            }
        }
    }

    @Composable
    fun MultipleOptionItem(
        title: String,
        subtitle: String = "",
        radioOptions: List<String>,
        currentItem: String = radioOptions[0],
        onChangeFunction: (text: String) -> Unit
    ) {
        var showPopup by remember { mutableStateOf(false) }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { showPopup = true }
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)) {
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
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
                        .fillMaxWidth(0.8f)
                        .height(popupHeight)
                        .background(
                            it.apply(MaterialTheme.colors.background, 4.dp),
                            RoundedCornerShape(cornerSize)
                        )
                        .padding(16.dp)
                }?.let {
                    Box(
                        it
                    ) {
                        val (selectedOption, onOptionSelected) = remember {
                            mutableStateOf(
                                currentItem
                            )
                        }

                        Column(modifier = Modifier.selectableGroup()) {
                            Text(text = subtitle, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            radioOptions.forEach { text ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = {
                                                onOptionSelected(text)
                                                onChangeFunction(text)
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

    @Composable
    fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                finish()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(4.dp))
            Text("Settings", style = MaterialTheme.typography.h5)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun UI() {
        // Set status bar and nav bar colours
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        Surface(color = MaterialTheme.colors.background) {
            val padding = 16.dp

            var themeSubtitle by remember { mutableStateOf(theme) }
            var cacheExpirationString = ""
            when (cacheExpiration) {
                24L * 60L * 60L * 1000L -> cacheExpirationString = "24 Hours"
                12L * 60L * 60L * 1000L -> cacheExpirationString = "12 Hours"
                6L * 60L * 60L * 1000L -> cacheExpirationString = "6 Hours"
                60L * 60L * 1000L -> cacheExpirationString = "1 Hour"
                30L * 60L * 1000L -> cacheExpirationString = "30 Minutes"
                15L * 60L * 1000L -> cacheExpirationString = "15 Minutes"
                0L -> cacheExpirationString = "Off"
            }
            var cacheSubtitle by remember { mutableStateOf(cacheExpirationString) }

            Surface(color = MaterialTheme.colors.background) {
                Scaffold(topBar = { TopBar() }) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = padding, bottom = padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PreferenceTitle(title = "Appearance")
                        MultipleOptionItem(
                            title = "Theme",
                            subtitle = themeSubtitle,
                            radioOptions = listOf("Light Theme", "Dark Theme", "System Default"),
                            currentItem = themeSubtitle,
                            onChangeFunction = {
                                sharedPrefEditor.putString(THEME, it)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                                themeSubtitle = it
                                themeState.value = it
                            }
                        )
                        ToggleItem(title = "Material You Theme")
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle(title = "Preferences")
                        MultipleOptionItem(
                            title = "Cache Expiration Time",
                            subtitle = cacheSubtitle,
                            radioOptions = listOf(
                                "24 Hours",
                                "12 Hours",
                                "6 Hours",
                                "1 Hour",
                                "30 Minutes",
                                "15 Minutes",
                                "Off"
                            ),
                            currentItem = cacheSubtitle,
                            onChangeFunction = {
                                var cacheExpiration = 0L
                                when (it) {
                                    "24 Hours" -> cacheExpiration = 24L * 60L * 60L * 1000L
                                    "12 Hours" -> cacheExpiration = 12L * 60L * 60L * 1000L
                                    "6 Hours" -> cacheExpiration = 6L * 60L * 60L * 1000L
                                    "1 Hour" -> cacheExpiration = 60L * 60L * 1000L
                                    "30 Minutes" -> cacheExpiration = 30L * 60L * 1000L
                                    "15 Minutes" -> cacheExpiration = 15L * 60L * 1000L
                                    "Off" -> cacheExpiration = 0L
                                }
                                cacheSubtitle = it
                                sharedPrefEditor.putLong(CACHE_EXPIRATION, cacheExpiration)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                            }
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
                            Text(
                                "Room",
                                Modifier
                                    .padding(top = 4.dp)
                                    .clickable { openLink("https://developer.android.com/training/data-storage/room") })
                            Text(
                                "Material.io Theming Information",
                                Modifier
                                    .padding(top = 4.dp)
                                    .clickable { openLink("https://material.io/design/color/the-color-system.html#color-theme-creation") })
                        }
                    }
                }
            }
        }
    }
}
