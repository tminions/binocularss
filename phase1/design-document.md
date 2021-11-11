# Design Document

## Updated Specification

## UML Diagram

Also talk about clean architecture here

## Major Design Decisions

- How we chose to do workflows
- For each major design decision, justify the choice we made
  - like the library

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

## Progress report

### Open Questions

### What has worked well so far

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

#### Tai Zhang

- Tags System
