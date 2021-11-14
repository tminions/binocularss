# Design Document

## Updated Specification

## UML Diagram

Also talk about clean architecture here

## Major Design Decisions

- How we chose to do workflows
- For each major design decision, justify the choice we made
  - like the library

### Kotlin:
Early in the develpoment period we decided to change our language from Java to Kotlin. This was done primarily for two reasons, the first being general readability. Kotlin removes a lot of the boilerplate code used in Java which improves readability and makes our code more concise (example under refactoring). The second reason was so we could use the Jetpack Compose library for building our UI. Compared to using XML files Compose is a more intuitive and less clunky way for creating UI, making the development process more streamlined and making our code cleaner. 

### RRS Parser Library:
We originally intended to write our own class to parse an RSS feed but we eventually decided to use a library instead.  We decided to use a library for a couple of reasons, but the main one was the insane complexity of the RSS standard. It was much more complex than we initially assumed. There are multiple competing standards and in order to create a system capable of handling all of the cases, we would need approximately 3600 lines of kotlin code (approximated from the used library) - far too much to write within the scope of this project on one of many functions.

### Room Database:
In order to add data persistence to our program we were debating whether to use the Room Database library or savedInstanceState. We decided on using Room because savedInstanceState doesn't persist data beyond one life cycle, however with Room we can save our data to a local database that will perserve our data over multiple lifecycles. This saves bandwidth as we don't have to retrieve feeds everytime the lifecycle renews and users can choose to perserve articles of their choice.




## Clean Architecture

Our code fell into three camps. The first was good, "clean", code. This did not necessitate fixing. The second and third are both considered "unclean" code. This unclean branch was further divided into violations that we knew how to fix, and violations that we did not know how to fix. 

<!-- is this in solid design principles or clean architecture or code smells? -->
One such violation of clean architecture that we knew how to fix was duplication of code within our project. An example of this was our `updateRss()` function. This funciton called a series of other functions that needed to be called in a specific order with specific arguments, and in many different places. Hence, we though it a good idea to pull that code into a function, but even though we were trying to reduce duplication, there were still multiple `updateRss()` functions accross multiple classes where the RSS feeds needed to be updated from. There were two solutions to this. The first, the one that we did not do, we could have only called this function from the main task and simply returned a signal from other tasks telling this function to be called. While this would have arugably been more "clean," it is not as efficient and for an integral part of our program that is run many, many times, efficiency is of the utmost importance. The second, the way that we chose to do it, is to extract this function into another class and simply call it through an instance of that class whenever necessary. This eliminates the need for code duplication and allows all the classes to communicate with one common class that will handle all the data transmission.

Another violation of clean architecture that we found within our code was accessing variables from one class in others. We initially had a global `feedGroup` variable (commit: [Merge pull request #7 from tminions/bookmarking](https://github.com/tminions/binocularss/commit/d83a9d3ee2c00b7249960b67bbaeedc00978c381)). We would access and update this in other classes like `AddFeedActivity` in order to change the state of the feeds in the application. We realized that this was bad practice so we tasked each activity with communicating with the database layer to retrieve the feeds on startup and write the feeds on change/exit. This meant that we were no longer "reaching" accross class borders to access variables that should be private. There is one exception to this, however, that we were not able to do the same with. There is a variable `feedGroupText` that represents the text of a UI element that we need to update upon completion of an asynchronous funciton. Since this asynchronous function is in another class (`PullFeed`), we need to allow that funciton access to this variable. We could not work out a way of going around this as attempting to update this text after the asynchronous function completes but on the main thread leads to timing complications as the asynchronous function can take an arbitrary amount of time to complete.

In addition to that, we also found a violation in BookmarksActivity. The specific violation was that we
had direct interaction of UI components with Entities, instead of using Controllers and Use Cases. We believe
that we could fix this violation through a technique in Jetpack Compose known as State Hoisting. The idea
behind it is that we take the parts of a Composable representing the state of it and promote it to the 
parent Composable (this is essentially a dependency injection). The parent composable would then pass
down a state variable and some onChange function to its children so they can implement their UI and
use the onChange function to pass events back up to trigger a recomposition. However, we found that 
this would have taken too long to implement given our time, but we could look into this more for phase 2.

## Solid Design Principles

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix

### Single Responsability Principle 
Besides a minor error mentioned below our code closely follows the single responsability principle. We've done this by seperating our critical data classes into multiple files (Article.kt, Feed.kt, FeedGroup.kt), keepinng our important data operations in seperate files (our code for sorting by date, article title, and feed title are all seperated), and by keeping our UI activites in seperate files as well.

### Open-closed Principle

### Vacuous SOLID
Two principles of SOLID, Liskov Substitution and Interface Segregation, are never violated in our code as we don't include any chains of inheritance nor define any interfaces. As we don't allow the problem to manifest to begin with we can conclude that our code vacously follows these two principles of SOLID.

### SettingsActivity.kt
Under SettingsActivity.kt where we handle our user settings, we violate the first principle of clean architecture by including the front-end UI of the settings page with the back-end functinoality under the same file. Ideally we would want to seperate these responsabilities into seperate files so we don't run the rist of alterning the front-end when working on the back-end and vice-versa. To fix this we would seperate the respective code into two files and only link the front-end to the back-end to maintain clean architecture as well.

### Composable Functions


## Packaging Strategies

Android APK vs Android App Bundle. Signed vs Unsigned (we will need to sign it if we publish it) (see slide 7)

## Design Patterns

Any design patterns that we used or plan to use

### Dependency Injection

When we display all of our bookmarks in the Bookmarks Composable, instead of calling ```getAllBookmarks()```
directly within the composable, we declared it in the method header as parameter thus eliminating this hard
dependency and making our code more easily testable.

Here was our code before

```kotlin
fun Bookmarks() {
        
        bookmarked_articles = getAllBookmarks()
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = bookmarked_articles) { article ->
                Bookmark(article = article)
            }

        }
    }
```

And here it is with Dependency Injection applied.

```kotlin
fun Bookmarks(bookmarked_articles: MutableList<Article>) {
        
        
        LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
            items(items = bookmarked_articles) { article ->
                Bookmark(article = article)
            }

        }
    }
```
### Builder

If we have any comple objects that we want to build (Parser for example), the builder design pattern allows us to do that well - pull from Salman's design pattern work.

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

Pull Requests were used in the fairly traditional way that you would expect. We each created our own individual
branches from the main branch and then once we were done working on our features, we created a pull request
to merge our code into the main branch. We also got a bit of experience resolving merge conflicts which will
be useful once our project starts to get larger in Phase 2.

### Projects

We used project boards to help us organize and prioritize our work. Our main board was
for Phase 1 was our Features board which is where we would create new features to implement, and keep track
features that were in progress, under review or completed. We also marked each task within the project
board with our initials so that other members could easily see who was working on what.

### Discord 

Althought not a Github feature, our team's primary source of communication has been through Discord. Instead of using Github Issues, we went through any errors and issues through Discord as it's more convenient to use for all of us.

## Refactoring

### Java to Kotlin conversion

One major refactoring that we made early on was the migration from Java to Kotlin for
our Entity dataclasses. From pull request #2


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
This composable is for selecting from multiple themes in our settings page. Obviously
this code spans far more than 10 lines. One way that we could fix this is by extracting some of the
inner Composables and placing them in different functions. This would make each Composable function
easier to read and debug.


## Progress report

### Open Questions

Avoiding burnout?

### What has worked well so far

The transition from Java to Kotlin was smooth as there was a lot of good documentation and guides online to help us with our code.

Our team communication has been very strong. Through Discord we have been able to keep in contact with every group member and keep up to date with all of our progress.

### Group Member Breakdown

#### Benson Chou

- Sorting/Filtering system

#### Eamon Ma

- Article Renderer/Viewer
- Sharing functionality

#### Hisbaan Noorani

- RSS parser
- Data persistence 
- Add feeds
- Fix direct access to `MainActivity.feedGroup`
- Settings page

#### Ismail Ahmed

- Bookmarking system
<!-- - Priority score -->

#### Macdeini Niu

- Main page (Mostly UI)

#### Salman Husainie

- Sorting/Filtering system

#### Simon Chen
- TBD

#### Tai Zhang

- Tags System
