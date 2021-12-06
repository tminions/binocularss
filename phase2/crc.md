<!--

TODOs:
- Turn sorter into a class so it can be an "interface" for Comparators

-->
# activities

## MainActivity
Frameworks & Drivers - UserInterface
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
- Feed
- FeedGroup
- ViewModel
- DatabaseGateway
- Cards
- NavigationItem
- Date
- Icons
- Calculations
- ui.theme/\*

## ArticleActivity
Frameworks & Drivers - UserInterface
### Responsibility
- Render the content of one given Article including the author, title, time since publication, image, and article content.
- Allow the user to share the article, mark it as bookmarked, mark as read, or view the article on the web.
### Collaborators
- Article
- DatabaseGateway
- Date
- Icons

## ArticleFromFeedActivity
Frameworks & Drivers - UserInterface
### Responsibility
- Display a list of articles from a given feed.
- Allow the user to share any article, mark it as bookmarked, mark as read, or view the article on the web.
### Collaborators
- Article
- DatabaseGateway
- Date
- Feed
- Icons

## AddFeedActivity
Frameworks & Drivers - UserInterface
### Responsibility
- Add feeds to the database and refresh the list by fetching feeds after that.
### Collaborators
- Feed
- FeedGroup
- ViewModel
- DatabaseGateway
- RetrieveArticles
- MainActivity
- Sorter
	- This is an abstraction layer for [Article,Feed]^ Comparator

## LicensesActivity.kt
Frameworks & Drivers - UserInterface
### Responsibility
- Display a list of open-source libraries we used in our project, along with their licenses.
### Collaborators
- None

## SearchActivity
Frameworks & Drivers - UserInterface
### Responsibility
- Render the content of all matching articles in the feedGroup.
- Allow the user to changet the search term.
### Collaborators
- Article 
- Feed
- FeedGroup
- DatabaseGateway
- Date
- Icons

## SettingsActivity
Frameworks & Drivers - UserInterface
### Responsibility
- Display a list of settings and ways to action them.
- Edit settings in sharedPreferences.
### Collaborators
- Theme

# dataclasses

## Article
Enterprise Business Rules - Entities
### Responsibility
- Represent an article.
### Collaborators:
- None

## Feed
Enterprise Business Rules - Entities
### Responsibility
- Represent a feed.
### Collaborators:
- Article

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

## ArticleReadDateComparator
Application Business Rules - Use Cases
### Responsibility
- Compare the read dates of two articles.
### Collaborators
- Article

## ArticleSearchComparator
Application Business Rules - Use Cases
### Responsibility
- Compare which article more closely matches the search term.
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

## ViewModel
Application Business Rules - Interface
### Responsibility
- Call the 
### Collaborators
- Article
- Feed
- FeedGroup
- databaseGateway

## FetchFeed
Application Business Rules - Use Cases
### Responsibility
- Fetch feed contents from the internet.
### Collaborators
- Article
- Feed

## Sorter
Interface Adapters - Gateways
### Responsibility
- Sort a given list of feeds or articles based on the comparator passed.
### Collaborators
- Article
- Feed

## StringOperations
Interface Adapters - Use Cases
### Responsibility
- Perform operations on a string like adding `https://` to the front or trimming whitespace characters.
### Collaborators
- None

# room

## DatabaseGateway
Interface Adapters - Gateway
### Responsibility
### Collabourators
- FeedDao
- AppDatabase

##  AppDatabase
Frameworks & Drivers - Database
### Responsibility
- Communicate with the Room database abstraction layer.
### Collaborators
- Feed
- FeedDao

## ArticleListConverter
Frameworks & Drivers - Database
### Responsibility
- Convert Articles to and from a JSON representation since SQLite does not support lists.
### Collaborators
- Article

## FeedDao
Frameworks & Drivers - Database
### Responsibility
- Link SQLite database queries to Kotlin functions
### Collaborators
- Feed

## TagsListConverter
Frameworks & Drivers - Database
### Responsibility
- Convert a list of tags to and from a CSV representation since SQLite does not support lists.
### Collaborators
- Feed

# ui

## Icons
Frameworks & Drivers - User Interface
### Responsibility
- Have buttons that can be called from activities that perform an action based on a given article.
  - BookmarkFlag: Toggles the bookmarked state of an article.
  - ReadFlag: Toggles the read state of an article.
  - ShareFlag: Shares the link of an article.
### Collaborators
- Article 

## CuratedFeeds
Frameworks & Drivers - User Interface
### Responsibility
- Display a composable with a list of pre-curated feeds that the user can add.
### Collabourators
- Feed

## Calculations
Frameworks & Drivers - User Interface
### Responsibility
- Calculate dynamic colour values for use in the UI classes.
### Collabourators
- None

## Cards
Frameworks & Drivers - UserInterface
### Responsibility
- Render cards representing articles and feeds.
### Collaborators
- Article
- Feed
- Icons
- Date

## Date
Frameworks & Drivers - UserInterface
### Responsibility
- Get the time since the publication date of an article in minutes, hours, days, months, or years, depending on how long ago the article was released.
### Collaborators
- None

## NavigationItem
Frameworks & Drivers - UserInterface
### Responsibility
- Contain information for the navigation bar at the bottom of MainActivity such as route, icon, and title.
### Collaborators
- None

## SettingItems
Frameworks & Drivers - UserInterface
### Responsibility
- Contain all of the composables for SettingsActivity
### Collabourators
- None

# activities/ui.theme

## Color
Frameworks & Drivers - UserInterface
### Responsibility
- Store color variables.
### Collabourators
- None

## Shape
Frameworks & Drivers - UserInterface
### Responsibility
- Store shapes like roudned corners so they remain consistent accross the UI.
### Collabourators
- None

## Theme
Frameworks & Drivers - UserInterface
### Responsibility
- Setup light and dark themes (both dynamic and static) and automatically change between them based on the given configuration variables.
### Collabourators
- None

## Type
Frameworks & Drivers - UserInterface
### Responsibility
- Store typography styles to keep text consistent accross the UI.
### Collabourators
- None
