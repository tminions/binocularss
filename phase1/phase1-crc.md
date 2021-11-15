# activities

## MainActivity
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
- PullFeed
- AppDatabase
- FeedDao
- Date
- Cards
- NavigationItem

## ArticleActivity
### Responsibility
- Render the content of one given Article including the author, title, time since publication, image, and article content.
- Allow the user to share the feed, mark it as bookmarked, mark as read, or view the article on the web.
### Collaborators
- Article
- AppDatabase
- FeedDao
- Date

## AddFeedActivity
### Responsibility
- Add feeds to the database and refresh the list by fetching feeds after that.
### Collaborators
<!-- - SettingsActivity -->
- Feed
- FeedGroup
- PullFeed
- AppDatabase
- FeedDao
- RetrieveArticles
- MainActivity
- Sorter

## BookmarksActivity
### Responsibility
- Render all the articles that are bookmarked.
- Perform actions to these articles.
### Collaborators
- SettingsActivity
- Article
- FeedGroup
- AppDatabase
- FeedDao
- Cards

## SettingsActivity
### Responsibility
- Display a list of settings and ways to action them.
- Edit settings in sharedPreferences.
### Collaborators
- Theme

# dataclasses

## Article
### Responsibility
- Represent an article.
### Collaborators:
- None

## Feed
### Responsibility
- Represent a feed.
### Collaborators:
- Article

## FeedGroup
### Responsibility
- Represent a group of feeds.
### Collaborators:
- Feed

# operations

## ArticleDateComparator
### Responsibility
- Compare the dates of two articles.
### Collaborators
- Article

## ArticleTitleComparator
### Responsibility
- Compare the title of two articles.
### Collaborators
- Article

## FeedTitleComparator
### Responsibility
- Compare the title of two feeds.
### Collaborators
- Feed

## PullFeed
### Responsibility
- Fetch feeds from the RSS feeds.
### Collaborators
- MainActivity
- Article
- Feed
- FeedGroup
- AppDatabase
- FeedDao

## Sorter
### Responsibility
- Sort a given list of feeds or articles based on the comparator passed.
### Collaborators
- Article
- Feed

# room

##  AppDatabase
### Responsibility
- Communicate with the Room database abstraction layer.
### Collaborators
- Feed
- FeedDao

## ArticleListConverter
### Responsibility
- Convert Articles to and from a JSON representation since SQLite does not support lists.
### Collaborators
- Article

## FeedDao
### Responsibility
- Link SQLite database queries to Kotlin functions
### Collaborators
- Feed

## TagsListConverter
### Responsibility
- Convert a list of tags to and from a CSV representation since SQLite does not support lists.
### Collaborators
- Feed

# ui

## Icons
### Responsibility
- Have buttons that can be called from activities that perform an action based on a given article.
  - BookmarkFlag: Toggles the bookmarked state of an article.
  - ReadFlag: Toggles the read state of an article.
  - ShareFlag: Shares the link of an article.
### Collaborators
- Article 

## Cards
### Responsibility
- Render cards representing articles and feeds.
### Collaborators
- ArticleActivity
- Article
- Icons
- Feed
- Date

## Date
### Responsibility
- Get the time since the publication date of an article in minutes, hourrs, days, months, or years.
### Collaborators
- None

## NavigationItem
### Responsibility
- Contain information for the navigation bar at the bottom of MainActivity such as route, icon, and title.
### Collaborators
- None

## SettingItems
### Responsibility
- Contain all of the composables for SettingsActivity
### Collabourators
- None

# activities/ui.theme

## Color
### Responsibility
- Store color variables.
### Collabourators
- None

## Shape
### Responsibility
- Store shapes like roudned corners so they remain consistent accross the UI.
### Collabourators
- None

## Theme
### Responsibility
- Setup light and dark theme and dynamically change between them based on given variables.
### Collabourators
- None

## Type
### Responsibility
- Store text styles to keep text consistent accross the UI.
### Collabourators
- None
