<!--

TODOs:
- Turn sorter into a class so it can be an "interface" for Comparators

-->

# activities

## MainActivity
Frameworks & Drivers - UI
### Responsibility
- Render the main UI.
  - First view is all the articles.
  - Second view is all the feeds.
  - Third view is all the articles in a given feed.
- Render the top bar with buttons top open BookmarksActivity, SettingsActivity, and AddFeedActivity.
- Render the bottom bar to switch between the first and second view.
### Collaborators
- AddFeedActivity
- BookmarksActivity
- SettingsActivity
- Article 
	- FIXME: UI talking to entity
- Feed
	- FIXME: UI talking to entity
- FeedGroup
	- FIXME: UI talking to entity
- PullFeed
	- FIXME: this will be separated into a ViewModel and PullFeed and a use case
	- ViewModel will contain updateRss and PullFeed will contain the rest of the things
- AppDatabase 
	- Add a controller so the UI isn't directly talking to the DB, although that should be okay if we can't
- FeedDao
	- FIXME: this is a part of the database -- same advice as above
- Cards
- NavigationItem
- Date

## ArticleActivity
Frameworks & Drivers - UI
### Responsibility
- Render the content of one given Article including the author, title, time since publication, image, and article content.
- Allow the user to share the feed, mark it as bookmarked, mark as read, or view the article on the web.
### Collaborators
- Article
	- FIXME: UI talking to entity
- AppDatabase
	- Add a controller so the UI isn't directly talking to the DB, although that should be okay if we can't
- FeedDao
	- FIXME: this is a part of the database -- same advice as above
- Date

## AddFeedActivity
Frameworks & Drivers - UI
### Responsibility
- Add feeds to the database and refresh the list by fetching feeds after that.
### Collaborators
- Feed
	- FIXME: UI talking to entity
- FeedGroup
	- FIXME: UI talking to entity
- PullFeed
	- FIXME: this will be separated into a ViewModel and PullFeed and a use case
	- ViewModel will contain updateRss and PullFeed will contain the rest of the things
- AppDatabase
	- Add a controller so the UI isn't directly talking to the DB, although that should be okay if we can't
- FeedDao
	- FIXME: this is a part of the database -- same advice as above
- RetrieveArticles
- MainActivity
- Sorter
	- This is an abstraction layer for [Article,Feed]^ Comparator

## SettingsActivity
Frameworks & Drivers - UI
### Responsibility
- Display a list of settings and ways to action them.
- Edit settings in sharedPreferences.
### Collaborators
- Theme

# dataclasses

<!-- TODO: Make a builder instead of a method that directly asssigns things -->
## Article
Enterprise Business Rules - Entities
### Responsibility
- Represent an article.
### Collaborators:
- None

<!-- TODO: Make a builder instead of a method that directly asssigns things -->
## Feed
Enterprise Business Rules - Entities
### Responsibility
- Represent a feed.
### Collaborators:
- Article

<!-- TODO: Is this redundant? It could just as properly represented with a mutableListOf<Feed>() -->
## FeedGroup
Enterprise Business Rules - Entities
### Responsibility
- Represent a group of feeds.
### Collaborators:
- Feed

# operations

## ArticleDateComparator
Application Business Rules - Use Cases
### Responsibility
- Compare the dates of two articles.
### Collaborators
- Article

## ArticleTitleComparator
Application Business Rules - Use Cases
### Responsibility
- Compare the title of two articles.
### Collaborators
- Article

## FeedTitleComparator
Application Business Rules - Use Cases
### Responsibility
- Compare the title of two feeds.
### Collaborators
- Feed

## PullFeed
Application Business Rules - Use Cases
### Responsibility
- Fetch feeds from the RSS feeds.
### Collaborators
- ViewModel
- Article
- Feed
- FeedGroup
- AppDatabase
	- This needs to talk to the controller
- FeedDao
	- This needs to talk to the controller

## Sorter
Interface Adapters - Gateways
### Responsibility
- Sort a given list of feeds or articles based on the comparator passed.
### Collaborators
- Article
- Feed

# room

##  AppDatabase
Frameworks & Drivers - DB
### Responsibility
- Communicate with the Room database abstraction layer.
### Collaborators
- Feed
- FeedDao

## ArticleListConverter
Frameworks & Drivers - DB
### Responsibility
- Convert Articles to and from a JSON representation since SQLite does not support lists.
### Collaborators
- Article

## FeedDao
Frameworks & Drivers - DB
### Responsibility
- Link SQLite database queries to Kotlin functions
### Collaborators
- Feed

## TagsListConverter
Frameworks & Drivers - DB
### Responsibility
- Convert a list of tags to and from a CSV representation since SQLite does not support lists.
### Collaborators
- Feed

# ui

## Icons
Frameworks & Drivers - UI
### Responsibility
- Have buttons that can be called from activities that perform an action based on a given article.
  - BookmarkFlag: Toggles the bookmarked state of an article.
  - ReadFlag: Toggles the read state of an article.
  - ShareFlag: Shares the link of an article.
### Collaborators
- Article 

## Cards
Frameworks & Drivers - UI
### Responsibility
- Render cards representing articles and feeds.
### Collaborators
- Article
	- FIXME: UI talking to entity
- Feed
	- FIXME: UI talking to entity
- Icons
- Date

## Date
Frameworks & Drivers - UI
### Responsibility
- Get the time since the publication date of an article in minutes, hours, days, months, or years.
### Collaborators
- None

## NavigationItem
Frameworks & Drivers - UI
### Responsibility
- Contain information for the navigation bar at the bottom of MainActivity such as route, icon, and title.
### Collaborators
- None

## SettingItems
Frameworks & Drivers - UI
### Responsibility
- Contain all of the composables for SettingsActivity
### Collabourators
- None

# activities/ui.theme

## Color
Frameworks & Drivers - UI
### Responsibility
- Store color variables.
### Collabourators
- None

## Shape
Frameworks & Drivers - UI
### Responsibility
- Store shapes like roudned corners so they remain consistent accross the UI.
### Collabourators
- None

## Theme
Frameworks & Drivers - UI
### Responsibility
- Setup light and dark theme and dynamically change between them based on given variables.
### Collabourators
- None

## Type
Frameworks & Drivers - UI
### Responsibility
- Store text styles to keep text consistent accross the UI.
### Collabourators
- None
