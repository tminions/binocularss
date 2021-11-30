# Design Document

### To Add
- Talk about testing 
- Talk about code style and documentation

## Updated Specification
Overall we followed our initial specification closely but there are multiple new additions to highlight.

### Proper UI
Our skeleton program for phase 0 was barebones in terms of the user interface. Since then we've worked on implementing a functional and good-looking user interface that enhances the user experience of our app.  

### Room Database
Since phase 0 we've added the Room library to implement data persistence in our program. This allows our users to save their feeds and articles over multiple lifecycles of the program, without having to fetch them again.

### Priority
We decided against implementing a priority feature as it proved too complex for this program.

## UML Diagram

See our [CRC cards for phase 1](https://github.com/tminions/binocularss/blob/main/phase1/phase1-crc.md) for a breakdown of how each class interacts with the others.

## Major Design Decisions   

### Kotlin
Early in the development period, we decided to change our language from Java to Kotlin. This was done primarily for two reasons, the first being general readability. Kotlin removes a lot of the boilerplate code used in Java which improves readability and makes our code more concise (example under refactoring). The second reason was so we could use the Jetpack Compose library for building our UI. Compared to using XML files Compose is a more intuitive and less clunky way for creating UI, making the development process more streamlined and making our code cleaner. 

### RRS Parser Library
We originally intended to write our class to parse an RSS feed but we eventually decided to use a library instead.  We decided to use a library for a couple of reasons, but the main one was the insane complexity of the RSS standard. It was much more complex than we initially assumed. There are multiple competing standards and to create a system capable of handling all of the cases, we would need approximately 3600 lines of Kotlin code (approximated from the used library) - far too much to write within the scope of this project on one of many functions.

### Room Database
To add data persistence to our program we were debating whether to use the Room Database library or savedInstanceState. We decided on using Room because savedInstanceState doesn't persist data beyond one life cycle, however, with Room we can save our data to a local database that will persist our data over multiple lifecycles. This saves bandwidth as we don't have to retrieve feeds every time the lifecycle renews and users can choose to persist articles of their choice.

## Clean Architecture

### Adherence to Clean Architecture

Although there are some violations in our code (see below) our code does a good job following clean architecture. Our innermost layer of entities consists primarily of Feed and Article which only rely on each other. FeedGroup could reasonably belong to entities or use cases but either way, Feedgroup only relies on the innermost entities. Likewise our comparator classes also only rely on entities. Our database classes and activity classes fall into the outermost layer of frameworks and drivers and the database classes only act to interface with our database, and the activities mostly render our UI. These classes belong to the outermost layer so they may rely on any class from any inner layer without violating clean architecture. 

### Violations

Our code fell into three camps. The first was good, "clean", code. This did not necessitate fixing. The second and third are both considered "unclean" codes. This unclean branch was further divided into violations that we knew how to fix, and violations that we did not know how to fix. 

<!-- is this in solid design principles or clean architecture or code smells? -->
One such violation of clean architecture that we knew how to fix was duplication of code within our project. An example of this was our `updateRss()` function. This function called a series of other functions that needed to be called in a specific order with specific arguments and many different places. Hence, we thought it would be a good idea to pull that code into a function, but even though we were trying to reduce duplication, there were still multiple `updateRss()` functions across multiple classes where the RSS feeds needed to be updated from. There were two solutions to this. The first, the one that we did not do, we could have only called this function from the main task and simply returned a signal from other tasks telling this function to be called. While this would have arguably been more "clean," it is not as efficient and for an integral part of our program that is run many, many times, efficiency is of the utmost importance. The second, the way that we chose to do it, is to extract this function into another class and simply call it through an instance of that class whenever necessary. This eliminates the need for code duplication and allows all the classes to communicate with one common class that will handle all the data transmission.

Another violation of clean architecture that we found within our code was accessing variables from one class in others. We initially had a global `feedGroup` variable (commit: [Merge pull request #7 from tminions/bookmarking](https://github.com/tminions/binocularss/commit/d83a9d3ee2c00b7249960b67bbaeedc00978c381)). We would access and update this in other classes like `AddFeedActivity` to change the state of the feeds in the application. We realized that this was bad practice so we tasked each activity with communicating with the database layer to retrieve the feeds on startup and write the feeds on change/exit. This meant that we were no longer "reaching" across class borders to access variables that should be private. There is one exception to this, however, that we were not able to do the same with. There is a variable `feedGroupText` that represents the text of a UI element that we need to update upon completion of an asynchronous function. Since this asynchronous function is in another class (`PullFeed`), we need to allow that function access to this variable. We could not work out a way of going around this as attempting to update this text after the asynchronous function completes but on the main thread leads to timing complications as the asynchronous function can take an arbitrary amount of time to complete.

In addition to that, we also found a violation in BookmarksActivity. The specific violation was that we had direct interaction of UI components with Entities, instead of using Controllers and Use Cases. We believe that we could fix this violation through a technique in Jetpack Compose known as State Hoisting. The idea behind it is that we take the parts of a Composable representing the state of it and promote it to the parent Composable (this is essentially a dependency injection). The parent composable would then pass down a state variable and some onChange function to its children so they can implement their UI and use the onChange function to pass events back up to trigger a recomposition. However, we found that this would have taken too long to implement given our time, but we could look into this more for phase 2.

## Solid Design Principles

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix

### Single Responsibility Principle 
Besides a minor error mentioned below our code closely follows the single responsibility principle. We've done this by separating our critical data classes into multiple files (`Article.kt, Feed.kt, FeedGroup.kt`), keeping our important data operations in separate files (our code for sorting by date, article title, and feed title are all separated), and by keeping our UI activities in separate files as well.

For example `Cards.kt` and `Icons.kt` are both elements of our UI, but as they play separate roles within our UI we have separated them into separate files.

### Open-closed Principle
One way our code follows the Open-closed principle is through our UI. We can easily extend our UI by adding more options to our settings, adding more feeds and articles to their respective views, and adding more selectable views without editing the functionality of our code directly. This is done by creating more composables and adding them to our UI which can be done without editing our UI classes.

### Vacuous SOLID
Two principles of SOLID, Liskov Substitution and Interface Segregation, are never violated in our code as we don't include any chains of inheritance nor define any interfaces. As we don't allow the problem to manifest to begin with we can conclude that our code follows these two principles of SOLID.

### Dependency Inversion
For our database implementation, we used the Room library to interface with SQLite. Room itself is nothing more than an abstraction layer for SQLite so we remove our dependency of the lower level SQLite code by interfacing with it through Room.

Likewise, the Jetpack Compose library provides a layer of abstraction over the UI. Instead of worrying about the implementation of Jetpack Compose, we can call the interface it provides to allow us to create our UIs.

### SettingsActivity.kt
Under SettingsActivity.kt where we handle our user settings, we violate the first principle of clean architecture by including the front-end UI of the settings page with the back-end functionality under the same file. Ideally, we would want to separate these responsibilities into separate files so we don't run the risk of altering the front-end when working on the back-end and vice-versa. To fix this we would separate the respective code into two files and only link the front-end to the back-end to maintain clean architecture as well.

## Packaging Strategies
We have our files organized into packages by their clean architecture layer. Our frameworks and drivers are composed of the `room`, `ui`, and `activities/ui` folders. These encapsulate the UI of our program and our database interface. Our entities are organized into the `dataclasses` folder and our uses cases are organized into the `activities` and `operations` folders. Our main interface adapters however are also mixed in with the `activities` folder. To improve our package organization we would want to better separate our use cases and interface adapters into their folders.

We chose this packaging strategy as it allows up to better adhere to clean architecture by making it clear what file belongs to what clean architecture layer. Overall this makes our code cleaner and easier to read.

## Design Patterns

Any design patterns that we used or plan to use

### Dependency Injection
For our UI we made sure to pass lambda functions to our Composables, allowing us to execute that code in the current scope, even if the Composables are in a different location. This ensures our Composables do not cause a circular dependency, but are still able to be used and run.

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

### Memento
We utilized Room to save and restore states, enabling persistence, allowing for the memento design pattern. Snapshots and values can be retrieved.

This was done using our room database. Any time something was added or changed, we would update our database.

And any time something is changed and reflected in the UI, we would refresh and pull from the database.

### Observer
We implemented the "observer" design pattern through our mutable states variables and remember keyword.

Going into our code, there’s many instances of mutable state variables, and remember keywords. They constantly check for changes, and if found, will change to reflect in our UI. This observer design pattern allows for real-time user interaction with the interface.


## Use of Github Features

### Pull Requests

Pull Requests were used in the fairly traditional way that you would expect. We each created our branches from the main branch and then once we were done working on our features, we created a pull request to merge our code into the main branch. We also got a bit of experience resolving merge conflicts which will be useful once our project starts to get larger in Phase 2.

### Projects

We used project boards to help us organize and prioritize our work. Our mainboard was
for Phase 1 was our Features board which is where we would create new features to implement and keep track of features that were in progress, under review, or completed. We also marked each task within the project board with our initials so that other members could easily see who was working on what.

### Discord 

Although not a Github feature, our team's primary source of communication has been through Discord. Instead of using Github Issues, we went through any errors and issues through Discord as it's more convenient to use for all of us.

## Refactoring

### Java to Kotlin conversion

One major refactoring that we made early on was the migration from Java to Kotlin for
our Entity dataclasses. 

This greatly reduced the amount of code we'd need to read, as Kotlin classes don't need a lot of boilerplate code (getters and setters), as well not needing keywords such as "new".

<table>
<tr>
<th>Java</th>
<th>Kotlin</th>
</tr>
<tr>
<td>
<pre>
package monster.minions.binocularss;

public class Article {
private String title;
private String date;
private String url;
private String source;
private String author;
private String text;
private String description;

    public Article(String title, String date, String url, String source, String author, String text, String description) {
        this.title = title;
        this.date = date;
        this.url = url;
        this.source = source;
        this.author = author;
        this.text = text;
        this.description = description;
    }

    public Article(String title, String url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }

</pre>
</td>
<td>

```kotlin
package monster.minions.binocularss

data class Article(
  var title:String,
  var date:String,
  var url:String,
  var publisher:String,
  var author:String,
  var text:String,
  var description:String,
)
```

</td>
</tr>
</table>

This refactoring greatly reduces the amount of code that we would have to read through
when looking through these dataclasses due to Kotlin classes not needing getters and setters.
This is because Kotlin implements direct access to fields in a safe manner.


### Code smells

One Code smell that we found falls under the Bloaters category. Specifically, we had a long
method in our branch that was implementing a settings page.

Here is the following code for our long method.

```kotlin

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
        // A lot more lines of code...
    }



```
This composable is for selecting from multiple themes on our settings page. Obviously,
this code spans far more than 10 lines. One way that we could fix this is by extracting some of the inner Composables and placing them in different functions. This would make each Composable function easier to read and debug.

##Testing

We implemented multiple tests for the various entities such as Articles, Feeds and FeedGroups.

Specifically, we had tests to compare examples, as well as comparing equalities.

```
@Test
    fun feedEqualsTest(){
        val feed1 = Feed(source = "http://www.feedforall.com")
        val feed2 = Feed(source = "http://www.feedforall.com/industry-solutions.htm")
        val feed3 = Feed(source = "http://www.feedforall.com")

        val article3 = Article(link = "http://www.feedforall.com")
        val article4 = Article(link = "http://www.feedforall.com/restaurant.htm")

        assertEquals(false, feed1 == feed2)
        assertEquals(true, feed1 == feed3)
        assertEquals(false, feed1.equals(article3))
        assertEquals(false, feed1.equals(article4))
    }
```

In the future, I think we would all like to see how we could implement testing for UI as well as persistence.



## Progress report

### Open Questions

How do we avoid burnout?

### What has worked well so far

The transition from Java to Kotlin was smooth as there was a lot of good documentation and guides online to help us with our code.

Our team communication has been very strong. Through Discord, we have been able to keep in contact with every group member and keep up to date with all of our progress.

### Group Member Breakdown


#### Benson Chou
- Implementing the Sorting/Filtering system
- Added tests (sorting, comparing Articles and Feeds)

#### Eamon Ma
- Created functions for article renderer/Viewer
- Refactoring duplicate Composables
- Sharing button/functionality

#### Hisbaan Noorani
- RSS parser
- Data persistence 
- Add feeds
- Fix direct access to MainActivity.feedGroup
- Connect Main Page to different features/other pages.
- Settings page


#### Ismail Ahmed
- Bookmarking system
- Began working on Search Function

<!-- - Priority score -->

#### Macdeini Niu
- Main page as well as UI

#### Salman Husainie
- Sorting/Filtering system for the Feeds (alongside Benson)

#### Simon Chen
- Refactoring Settings

#### Tai Zhang
- Tags System
- Tests for entities, functionality and UI

### Plans for Phase 2

#### Benson Chou
- Add more tests (UI, Database)
- Work on Feed Deleting system

#### Eamon Ma
- Curated Feeds in AddFeedActivity
- Improve HTML rendering

#### Hisbaan Noorani
- Article Preview (long press to show)
- Add more settings

#### Ismail Ahmed
- Search Function
    - Allow users to look for articles within feeds they're subscribed to
    - Considering the use of a third-party library for fuzzy word matching to give an
    approximation about which articles match a user's query
- Priority
    - Apply a measure of priority to each feed based on how much they view or like articles
    within a feed
    - Considering adding a like/dislike button for articles so we can base the priority of a feed
    on some calculation of those likes and dislikes

#### Macdeini Niu
- Export/Import save files
- Reading history

#### Salman Husainie
- Work on Search function
- Add more UI tests

#### Simon Chen
- General help with any new features that come up

#### Tai Zhang
- Create the UI for tags.
- Contribute more to tests (importantly UI, tags)