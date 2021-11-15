package monster.minions.binocularss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.CACHE_EXPIRATION
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.SETTINGS
import monster.minions.binocularss.activities.SettingsActivity.PreferenceKeys.THEME
import monster.minions.binocularss.activities.ui.theme.BinoculaRSSTheme
import monster.minions.binocularss.dataclasses.FeedGroup
import monster.minions.binocularss.room.AppDatabase
import monster.minions.binocularss.room.FeedDao
import androidx.compose.material.RadioButtonDefaults.colors as radioButtonColors
import androidx.compose.material.SwitchDefaults.colors as switchColors

/**
 * Settings Activity responsible for the settings UI and saving changes to settings.
 */
class SettingsActivity : ComponentActivity() {

    // FeedGroup object
    private var feedGroup: FeedGroup = FeedGroup()

    // Room database variables
    private lateinit var db: RoomDatabase
    private lateinit var feedDao: FeedDao

    // SharedPreferences variables.
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var theme: String
    private lateinit var themeState: MutableState<String>
    private var cacheExpiration = 0L

    /**
     * Create method that sets the UI and initializes lateinit variables.
     *
     * @param savedInstanceState bundle to retrieve saved information from.
     */
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

        // Initialize lateinit variables.
        sharedPref = this.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        sharedPrefEditor = sharedPref.edit()
        theme = sharedPref.getString(THEME, "System Default").toString()
        cacheExpiration = sharedPref.getLong(CACHE_EXPIRATION, 0L)

        db = Room
            .databaseBuilder(this, AppDatabase::class.java, "feed-db")
            .allowMainThreadQueries()
            .build()
        feedDao = (db as AppDatabase).feedDao()
    }

    @Composable
    fun ClearFeeds() {
        Button(
            onClick = {
                Log.d("feeds", feedGroup.feeds.toString())
                for (feed in feedGroup.feeds) {
                    feedDao.deleteBySource(feed.source)
                    Log.d("SettingsActivity", feedDao.getAll().toString())
                }
                feedGroup.feeds = mutableListOf()
                Toast.makeText(
                    this@SettingsActivity,
                    "Feeds cleared",
                    Toast.LENGTH_LONG
                ).show()

            }
        ) {
            Text("Clear DB")
        }
    }

    // Global preference keys to retrieve settings from shared preferences.
    object PreferenceKeys {
        const val SETTINGS = "settings"
        const val THEME = "theme"
        const val CACHE_EXPIRATION = "cacheExpiration"
    }

    /**
     * Open link with in the user's default browser.
     */
    private fun openLink(link: String) {
        if (link != "") {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(link)
            startActivity(intent)
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
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clickable { showPopup = true }
        ) {
            // Column that contains title and subtitle if defined.
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
            }
        }

        // If the popup should be shown ...
        if (showPopup) {
            // Popup size information.
            val popupHeight = 400.dp
            val cornerSize = 16.dp

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
                            it.apply(MaterialTheme.colors.background, 4.dp),
                            RoundedCornerShape(cornerSize)
                        )
                        .padding(16.dp)
                }?.let {
                    Box(
                        it
                    ) {
                        // Render content of popup passed as lambda function along with title and
                        //  close button.
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

    /**
     * Row that links to a given email when clicked.
     *
     * @param title Title of the item.
     * @param subtitle Subtitle of the item.
     * @param email Email to be send to.
     */
    @Composable
    fun EmailItem(title: String, subtitle: String = "", email: String = "") {
        // Main row to be clicked on.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clickable {
                    // on click, send to email application.
                    if (email != "") {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.data = Uri.parse("mailto:")
                        intent.putExtra(Intent.EXTRA_EMAIL, listOf(email).toTypedArray())
                        intent.putExtra(Intent.EXTRA_SUBJECT, "BinoculaRSS feedback/help")
                        startActivity(Intent.createChooser(intent, "Select an email application"))
                    }
                }) {
            // Render title and subtitle.
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
            }
        }
    }

    /**
     * Item that takes user to linked website in their browser when clicked.
     */
    @Composable
    fun LinkItem(title: String, subtitle: String = "", link: String = "") {
        // Main row to be clicked on.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                // Open link when clicked.
                .clickable { openLink(link) }
        ) {
            // Column that renders title and subtitle.
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
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
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)) {
            // Column that renders the title and subtitle.
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
            }
        }

        // If the popup is to be shown ...
        if (showPopup) {
            // Get popup height based on number of elements to display.
            var popupHeight = 90.dp
            for (i in radioOptions) {
                popupHeight += 56.dp
            }
            val cornerSize = 16.dp

            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPopup = false }
            ) {
                // Elevate the popup so it is distinguishable from the background.
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
                                initialItem
                            )
                        }

                        // Render radio buttons, title, text, and cancel button.
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
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Render title and subtitle.
            Column {
                Text(title)
                if (subtitle != "") {
                    Text(subtitle)
                }
            }
            // Switch that toggles preferences.
            Switch(
                checked = checkedState,
                onCheckedChange = {
                    checkedState = it
                    onToggle(it)
                },
                colors = switchColors(
                    checkedThumbColor = MaterialTheme.colors.primary,
                    checkedTrackColor = MaterialTheme.colors.primary,
                    checkedTrackAlpha = 0.54f
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

    /**
     * Top navigation bar
     */
    @Composable
    fun TopBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button icon that goes back one activity.
            IconButton(onClick = {
                finish()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
            Spacer(Modifier.padding(4.dp))
            // Title of current page.
            Text("Settings", style = MaterialTheme.typography.h5)
        }
    }


    /**
     * Compilation of UI elements in the correct order.
     */
    @Preview(showBackground = true)
    @Composable
    fun UI() {
        // Set status bar and nav bar colours.
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val color = MaterialTheme.colors.background
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
        }

        // Surface as a background.
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
                    // Column of all the preference items.
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(vertical = padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PreferenceTitle(title = "Appearance")
                        // Theme selector.
                        MultipleOptionItem(
                            title = "Theme",
                            subtitle = themeSubtitle,
                            radioOptions = listOf("Light Theme", "Dark Theme", "System Default"),
                            initialItem = themeSubtitle,
                            onSelect = {
                                // Update the subtitle and theme of the current activity.
                                themeSubtitle = it
                                themeState.value = it
                                // Update the shared preferences.
                                sharedPrefEditor.putString(THEME, it)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                            }
                        )
                        // Material You toggle.
                        ToggleItem(
                            title = "Material You Theme",
                            checked = false, // TODO get this value from shared preferences
                            onToggle = { println(it)/* TODO set shared preferences here */ }
                        )
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle(title = "Preferences")
                        // Cache expiration time selector.
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
                            initialItem = cacheSubtitle,
                            onSelect = {
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
                                // Update the subtitle.
                                cacheSubtitle = it
                                // Update the shared preferences.
                                sharedPrefEditor.putLong(CACHE_EXPIRATION, cacheExpiration)
                                sharedPrefEditor.apply()
                                sharedPrefEditor.commit()
                            }
                        )
//                        ClearFeeds()
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle(title = "Support")
                        // Email item
                        EmailItem(
                            title = "Contact",
                            email = "hisbaan@gmail.com"
                        )
                        // Item that links to feedback form.
                        LinkItem(
                            title = "Feedback",
                            link = "google form or something"
                        )
                        // Item that links to github issues page.
                        LinkItem(
                            title = "Bug Report",
                            link = "https://github.com/tminions/binocularss/issues/new"
                        )
                        Divider(modifier = Modifier.padding(bottom = 16.dp))

                        PreferenceTitle(title = "About")
                        // Item that links to github source code page.
                        LinkItem(
                            title = "GitHub",
                            subtitle = "github.com/tminions/binocularss",
                            link = "https://github.com/tminions/binocularss"
                        )
                        // Item that links to github releases.
                        LinkItem(
                            title = "Version",
                            subtitle = "1.0",
                            link = "https://github.com/tminions/binocularss/releases"
                        )
                        // Popup information on all the open source libraries used.
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
                            // TODO finish adding libraries then size the popup accordinly
                        }
                    }
                }
            }
        }
    }
}
