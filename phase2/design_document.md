# Phase 2 - Design Document

*Note that this design document only applies to additions/changes made in Phase 2.

## Clean Architecture

### Adherence to Clean Architecture

Although there are some violations in our code (see below), our code does an excellent job following clean architecture. Our innermost layer of entities consists primarily of Feed and Article, which only rely on each other. FeedGroup could reasonably belong to entities or use cases, but either way, Feedgroup only relies on the innermost entities. Likewise, our comparator classes also only rely on entities. Our database classes and activity classes fall into the outermost layer of frameworks and drivers. The database classes only act to interface with our database, and the activities mostly render our UI. These classes belong to the outermost layer, so they may rely on any class from any inner layer without violating clean architecture.


## Solid Design Principles

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix.

### Single Responsibility Principle
Our code closely follows the single responsibility principle. We've done this by separating our critical data classes into multiple files (`Article.kt, Feed.kt, FeedGroup.kt`), keeping our important data operations in separate files (our code for sorting by date, article title, and feed title are all separated), and by keeping our UI activities in separate files as well.

For example, `Cards.kt` and `Icons.kt` are both elements of our UI, but as they play separate roles within our UI, we have separated them into separate files.


### Open-closed Principle
One way our code follows the Open-closed principle is through our UI classes. Our UI can be easily extended with new features. For example, to add a settings option, one simply adds a new instance of a composable from the `SettingItems` file to the `UI` composable function in the `SettingsActivity`. If we wanted to add a new type of view to our main activity, we would create a new composable function in the `MainActivity` and call it from the `Navigation` function to view it. As illustrated in these examples, we do not need to modify existing code; we need to add new code to represent additional functionality, even within the same UI class. This adheres to the open-closed principle.


### Vacuous SOLID
Two principles of SOLID, Liskov Substitution and Interface Segregation, are never violated in our code as we do not include any chains of inheritance nor define any interfaces. As we do not allow the problem to manifest, to begin with, we can conclude that our code follows these two principles of SOLID.


### Dependency Inversion
For our UI, we made sure to pass lambda functions to our Composables, allowing us to execute that code in the current scope, even if the Composables are in a different location. This ensures our Composables do not cause a circular dependency but can still be used and run.


## Packaging Strategies
We have our files organized into packages by their clean architecture layer. Our frameworks and drivers are composed of the `room`, `ui`, and `activities/ui` folders. These encapsulate the UI of our program and our database interface. Our entities are organized into the `dataclasses` folder, and our uses cases are organized into the `activities` and `operations` folders. Our main interface adapters, however, are also mixed in with the `activities` folder. To improve our package organization, we would want to better separate our use cases and interface adapters into their folders.

We chose this packaging strategy as it allows up to adhere better to clean architecture by clarifying what file belongs to what clean architecture layer. Overall this makes our code cleaner and easier to read.


## Design Patterns

Any design patterns that we used

### Memento
We utilized Room to save and restore states, enabling persistence, allowing for the memento design pattern. Snapshots and values can be retrieved.

This was done using our room database. Any time something was added or changed, we would update our database.

Moreover, any time something is changed and reflected in the UI, we would refresh and pull from the database.

### Observer
We implemented the "observer" design pattern through our mutable states variables and `remember` keyword.

Going into our code, there are many instances of mutable state variables, and remember keywords. They constantly check for changes, and if found, will change to reflect in our UI. This observer design pattern allows for real-time user interaction with the interface.

### Builder
If we have any complex objects that we want to build (Parser, for example), the builder design pattern allows us to do that well - pull from Salman's design pattern work.

```kotlin
parser = Parser.Builder()
    .context(context)
    .charset(Charset.forName("UTF_8"))
    .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
    .build()
```

```kotlin
db = Room
    .databaseBuilder(this, AppDatabase::class.java, "feed-db")
    .allowMainThreadQueries()
    .build()
```

We also implemented the builder design pattern in our [Article](https://github.com/tminions/binocularss/blob/main/app/src/main/java/monster/minions/binocularss/dataclasses/Article.kt) and [Feed](https://github.com/tminions/binocularss/blob/main/app/src/main/java/monster/minions/binocularss/dataclasses/Feed.kt) data classes. This allows us to build them more quickly and have, essentially, self-documenting code. We have left out code snippets as these are very long due to the nature of the Article and Feed classes.

### Stategy
We implemented the strategy design pattern in our `Sorter.kt` file. This design pattern allows us to define a family of similar algorithms in a neat, clean, and organized manner. This allowed us to make our code easier to read and helped with refactoring. The following is an example of our implementation; a full display is available in `Sorter.kt`:

"`kotlin
interface SortingStrategy<T> {
    val comparator: Comparator<T>
}

/**
 * Strategy for sorting articles by date
 */
class SortArticlesByDateStrategy: SortingStrategy<Article> {
    override val comparator: Comparator<Article> = ArticleDateComparator()
}
```


## Use of Github Features

### Pull Requests

Pull Requests were used in the fairly traditional way that you would expect. We each created our branches from the main branch, and then once we were done working on our features, we created a pull request to merge our code into the main branch. We also got a bit of experience resolving merge conflicts which was helpful as our project started getting
more complicated in Phase 2.

### Projects

We used project boards to help us organize and prioritize our work. Our mainboard was
for Phase 2 was our Features board which is where we would create new features to implement and keep track of features that were in progress, under review, or completed. We also marked each task within the project board with our initials so that other members could easily see who was working on what.

### Issues (New for Phase 2)

We have since expanded upon our use of Github features with Issues. This allowed us to keep track of any bugs within our application more efficiently. This also provided opportunities for other group members to find something to work on if they were not sure. One aspect of Issues that we found helpful was that we could reference issues in both pull requests and project boards.

### Discord

Although not a Github feature, our team's primary source of communication has been through Discord. Instead of using Github Issues, we went through any errors and issues through Discord as it is more convenient to use for all of us.


## Refactoring


### Code smells

The most apparent code smell we have would be the Long Class (Bloater) smell within `MainActivity.kt`. We spent a lot of time trying to reduce the size
of the class by separating things into different composables or activities.


Another code smell that we found was an instance of Data Clumps. Each activity had its own group of identical variables like `feedDao`, `db`, `sharedPref`, `theme` etc. When other group members were making or refactoring any activities, they would often have to copy and paste these variables to be consistent. 

```kotlin
// MainActivity

// Parser variable
private lateinit var parser: Parser

// Room database variables
private lateinit var dataGateway: DatabaseGateway

// User Preferences
private lateinit var sharedPref: SharedPreferences
private lateinit var sharedPrefEditor: SharedPreferences.Editor
private lateinit var theme: String
private lateinit var themeState: MutableState<String>
private var isFirstRun = true
private var cacheExpiration = 0L
```

```kotlin
// SettingsActivity

// Room database variables
private lateinit var dataGateway: DatabaseGateway

// SharedPreferences variables.
private lateinit var sharedPref: SharedPreferences
private lateinit var sharedPrefEditor: SharedPreferences.Editor
private lateinit var theme: String
private lateinit var themeState: MutableState<String>
private var cacheExpiration = 0L
```

Another code smell that we found was Long Method (Bloater). We see this code smell a lot throughout our code. Most of the time, this code smell occurs in lifecycle methods such as `onCreate` and `onResume`, as well as in our Composables such as `UI`, `FeedCard` and `FeedView`.

```kotlin
override fun onResume() {
    super.onResume()
    Log.d("MainActivity", "onResume called")

    val feeds: MutableList<Feed> = dataGateway.read()

    feedGroup.feeds = feeds

    theme = sharedPref
        .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
        .toString()
    if (!isFirstRun) {
        themeState.value = theme
    }
    isFirstRun = false

    articleList.value = sortArticlesByDate(getAllArticles(feedGroup))
    bookmarkedArticleList.value = sortArticlesByDate(getBookmarkedArticles(feedGroup))
    feedList.value = sortFeedsByTitle(feedGroup.feeds)
    readArticleList.value = sortArticlesByReadDate(getReadArticles(feedGroup))
}
```

```kotlin
var showDropdown by remember { mutableStateOf(false) }
	// Location where user long pressed.
	var offset by remember { mutableStateOf(Offset(0f, 0f)) }
	Card(
	    modifier = Modifier
	        .fillMaxWidth()
	        .padding(paddingMedium)
	        .pointerInput(Unit) {
	            detectTapGestures(
	                onLongPress = {
	                    showDropdown = true
	                    offset = it
	                },
	                onTap = {
	                    // TODO temporary until articleFromFeed
	                    val intent = Intent(Intent.ACTION_VIEW)
	                    intent.data = Uri.parse(feed.link)
	                    ContextCompat.startActivity(context, intent, null)
	                }
	            )
	        }, elevation = 8.dp
	) {
	    Column {
	        Row(
	            modifier = Modifier
	                .fillMaxWidth()
	                .padding(paddingLarge),
	            horizontalArrangement = Arrangement.SpaceBetween
	        ) {
		// More lines of code ...
```

## Testing
For testing, we split it into three different categories.

The first would be entity/sorting testing. We implemented multiple tests for the various entities such as Articles, Feeds and FeedGroups, as well as other uses cases.

The second large portion was UI testing. We utilized AndroidComposeRules to make automatic testing that various elements exist and are displayed.

We had tests for each activity, such as MainActivity and addFeedActivity, testing their respective elements and functionalities. This ensured that we could efficiently and rigorously test our app's UI, as a part of our continuous testing.

We also utilized AndroidComposeTools for functional testing. Using functions such as `performClick`, we can simulate an entire process from start to finish, such as adding a feed. This would involve creating a dummy article, and manipulating it with commands and asserting that it shows up in the feed and in the history.

Finally, a large component of our UI/UX testing happened to be physical. We had three members with Android phones who could test the accessibility and usability of features on their physical devices. This type of testing became a part of our Github repository, ensuring that bugs were documented with steps to reproduce as well as potential fixes.

As an example, we were simulating on various virtual devices; we found that the cards displayed were not a square aspect ratio for each screen size. This ended up being an entire pull request and refactoring on how we were approaching sizing each element.

One thing to note is that UI tests do not add to the testing coverage metric that Android Studio and IntelliJ IDEA calculate. This means that the testing coverage will appear to be lower than it actually is, and you will see little to no testing coverage in the UI layer. Please view this manually.

## Code Style and Documentation

To maintain a consistent code style, we ensured to run the Android Studio auto-format before each pull request. This not only maintained consistent code style, but it maintained readability (by ensuring that lines are not too long), and optimized imports.

In the case of documentation, we ensured that almost all functions have Javadoc comments on them to allow for easy use by other developers and our team members who may not be familiar with the code they are working with. With more time, we may have written a wiki explaining the reusable parts of the system so others can extend our project in the future.

To keep the actual code consistent (function names and such), we adhered to the following standards (also outlined in the README for other developers wishing to contribute):

### Variable Naming Convention

- Variables should be named in `camelCase`.
- Functions should be named with `camelCase`.
- Composable functions should be `PascalCase`.
- Classes should be named with `PascalCase`.

### UI Conventions

#### Colours

For theming, refer to the `MaterialTheme` package. For example, to get the primary colour, reference `MaterialTheme.colorScheme.primary`. The following is a list of colours that are available:
- `background`
- `error`
- `errorContainer`
- `inverseOnSurface`
- `inversePrimary`
- `inverseSurface`
- `onBackground`
- `onError`
- `onErrorContainer`
- `onPrimary`
- `onPrimaryContainer`
- `onSecondary`
- `onSecondaryContainer`
- `onSurface`
- `onSurfaceVariant`
- `onTertiary`
- `onTertiaryContainer`
- `outline`
- `primary`
- `primaryContainer`
- `secondary`
- `secondaryContainer`
- `surface`
- `surfaceVariant`
- `tertiary`
- `tertiaryContainer`

View `activities/ui.theme/*` for more MaterialTheme listing convensions.

#### UI Composables

In your `Activity` classes, try and break up your composables as much as possible. Make things very modular, so the average function line count remains low. Compile all of your composables into a composable function called `UI` at the bottom. The outermost composable in this `UI` composable should be a `Surface` with color set to `MaterialTheme.colorScheme.background`. 

#### Theming

To ensure that your activity is themed in accordance to the theming standards we have put in place, adhere to the following steps:

1. At the top of your activity, include the following lines to make the theming work:

```kotlin
private lateinit var sharedPref: SharedPreferences
private lateinit var sharedPrefEditor: SharedPreferences.Editor
private lateinit var theme: String
private lateinit var themeState: MutableState<String>
private var materialYou by Delegates.notNull<Boolean>()
private lateinit var materialYouState: MutableState<Boolean>
```

2. Include the following code in the `onCreate` function:

```kotlin
setContent {
    themeState = remember { mutableStateOf(theme) }
    materialYouState = remember { mutableStateOf(materialYou) }
    BinoculaRSSTheme(theme = themeState.value, materialYou = materialYouState.value) {
        UI()
    }
}

// Initialize shared preferences theme variables
sharedPref = this.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
sharedPrefEditor = sharedPref.edit()
theme = sharedPref.getString(THEME, "System Default").toString()
materialYou = sharedPref.getBoolean(MATERIAL_YOU, false)
```

3. Include the following code in your `onResume` function:

```kotlin
theme = sharedPref
    .getString(SettingsActivity.PreferenceKeys.THEME, "System Default")
    .toString()
materialYou = sharedPref.getBoolean(SettingsActivity.PreferenceKeys.MATERIAL_YOU, false)
```

4. At the top of your `UI` composable function, you should include the following code to make the status bar and navigation bar theming work:

```kotlin
// Set status bar and nav bar colours.
val systemUiController = rememberSystemUiController()
val useDarkIcons = when (theme) {
    "Dark Theme" -> false
    "Light Theme" -> true
    else -> !isSystemInDarkTheme()
}
val color = MaterialTheme.colorScheme.background
SideEffect {
    systemUiController.setSystemBarsColor(
        color = color,
        darkIcons = useDarkIcons
    )
}
```
