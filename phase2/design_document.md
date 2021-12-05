# Phase 2 - Design Document

### Important parts to work on from Phase 1 feedback:
- Talk about testing
- Talk about code style and documentation
-

## Clean Architecture

### Adherence to Clean Architecture

Although there are some violations in our code (see below) our code does a good job following clean architecture. Our innermost layer of entities consists primarily of Feed and Article which only rely on each other. FeedGroup could reasonably belong to entities or use cases but either way, Feedgroup only relies on the innermost entities. Likewise our comparator classes also only rely on entities. Our database classes and activity classes fall into the outermost layer of frameworks and drivers and the database classes only act to interface with our database, and the activities mostly render our UI. These classes belong to the outermost layer so they may rely on any class from any inner layer without violating clean architecture.

### Fixing violations


## Solid Design Principles

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix

### Single Responsibility Principle
Our code closely follows the single responsibility principle. We've done this by separating our critical data classes into multiple files (`Article.kt, Feed.kt, FeedGroup.kt`), keeping our important data operations in separate files (our code for sorting by date, article title, and feed title are all separated), and by keeping our UI activities in separate files as well.

For example `Cards.kt` and `Icons.kt` are both elements of our UI, but as they play separate roles within our UI we have separated them into separate files.


### Open-closed Principle
One way our code follows the Open-closed principle is through our UI. We can easily extend our UI by adding more options to our settings, adding more feeds and articles to their respective views, and adding more selectable views without editing the functionality of our code directly. This is done by creating more composables and adding them to our UI which can be done without editing our UI classes.



### Vacuous SOLID
Two principles of SOLID, Liskov Substitution and Interface Segregation, are never violated in our code as we don't include any chains of inheritance nor define any interfaces. As we don't allow the problem to manifest to begin with we can conclude that our code follows these two principles of SOLID.


### Dependency Inversion
For our UI we made sure to pass lambda functions to our Composables, allowing us to execute that code in the current scope, even if the Composables are in a different location. This ensures our Composables do not cause a circular dependency, but are still able to be used and run.



## Packaging Strategies
We have our files organized into packages by their clean architecture layer. Our frameworks and drivers are composed of the `room`, `ui`, and `activities/ui` folders. These encapsulate the UI of our program and our database interface. Our entities are organized into the `dataclasses` folder and our uses cases are organized into the `activities` and `operations` folders. Our main interface adapters however are also mixed in with the `activities` folder. To improve our package organization we would want to better separate our use cases and interface adapters into their folders.

We chose this packaging strategy as it allows up to better adhere to clean architecture by making it clear what file belongs to what clean architecture layer. Overall this makes our code cleaner and easier to read.



## Design Patterns

Any design patterns that we used

### Memento
We utilized Room to save and restore states, enabling persistence, allowing for the memento design pattern. Snapshots and values can be retrieved.

This was done using our room database. Any time something was added or changed, we would update our database.

And any time something is changed and reflected in the UI, we would refresh and pull from the database.

### Observer
We implemented the "observer" design pattern through our mutable states variables and remember keyword.

Going into our code, there’s many instances of mutable state variables, and remember keywords. They constantly check for changes, and if found, will change to reflect in our UI. This observer design pattern allows for real-time user interaction with the interface.

### Builder
If we have any complex objects that we want to build (Parser for example), the builder design pattern allows us to do that well - pull from Salman's design pattern work.

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

## Use of Github Features

### Pull Requests

Pull Requests were used in the fairly traditional way that you would expect. We each created our branches from the main branch and then once we were done working on our features, we created a pull request to merge our code into the main branch. We also got a bit of experience resolving merge conflicts which was useful as our project started getting
more complicated in Phase 2.

### Projects

We used project boards to help us organize and prioritize our work. Our mainboard was
for Phase 2 was our Features board which is where we would create new features to implement and keep track of features that were in progress, under review, or completed. We also marked each task within the project board with our initials so that other members could easily see who was working on what.

### Issues (New for Phase 2)

We since expanded upon our use of Github features with Issues. This allowed us to more easily keep track of any bugs within our application. This also provided
opportunities for other group members to find something to work on if they weren't sure. One aspect of Issues that we found helpful was that we were able to reference issues in both pull requests and project boards.

### Discord

Although not a Github feature, our team's primary source of communication has been through Discord. Instead of using Github Issues, we went through any errors and issues through Discord as it's more convenient to use for all of us.


## Refactoring


### Code smells

The most apparent code smell that we have would be the Long Class (Bloater) smell that exists within `MainActivity.kt`. We spent a lot of time trying to reduce the size
of the class by separating things into different composables or activities.


Another code smell that we found was an instance of Data Clumps. Each activity had their own group of identical variables like `feedDao`, `db`, `sharedPref`, `theme` etc. When other group members were making or refactoring any activities, they would often have to copy and paste these variables to be consistent. 

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

Another code smell that we found was Long Method (Bloater). We see this code smell a lot throughout our code. Most of the time this code smell occurs in lifecycle methods such as `onCreate` and `onResume`, as well as in our Composables such as `UI`, `FeedCard` and `FeedView`.

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
         // More lines of code below

```

##Testing

BIG TODO: Expand more on how we implemented rigorous testing for classes/UI for better coverage.
