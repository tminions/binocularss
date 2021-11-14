 <!-- Entity:         [3/3] -->
 <!-- Use Cases:      [2/2] -->
 <!-- Controllers:    [5/1] -->
 <!-- User Interface: [1/1] -->

Note that Collaborators only includes the classes that are used by the given class.

# activities

## MainActivity

### Collaborators
- AddFeedActivity
- BookmarksActivity
- SettingsActivity
- Feed
- FeedGroup
- PullFeed
- AppDatabase
- FeedDao

## ArticleActivity

### Collaborators
- Article
- AppDatabase
- FeedDao

## AddFeedActivity

### Collaborators
- SettingsActivity
- Feed
- FeedGroup
- PullFeed
- AppDatabase
- FeedDao

## BookmarksActivity

### Collaborators
- SettingsActivity
- Article
- FeedGroup
- AppDatabase
- FeedDao

## SettingsActivity

### Collaborators
- SharedActivities

# dataclasses

## Article

### Responsibility:
- Get the text and images of a specific article
- Save (to file)
- Load (from file)
- Bookmark
- Read flag

### Collaborators:
- None

## Feed

### Responsibility:
- Aggregate all the different articles
- Have tags to be filtered by
- Save (to file)
- Load (from file)

### Collaborators:
- Article

## FeedGroup
The main group of RSS feeds. This contains and saves any feeds that the user adds. This will be persistent across applications lifecycles. We may choose to have multiple feed groups or have them all in one and distinguish groupings with 'tags'.


### Responsibility:
- Create Feed objects
- Delete Feed objects
- Filter feeds by tag
- Filter articles in feeds by date
- Sort articles in feeds chronologically (and reverse)

### Collaborators:
- Feed

# operations

## ArticleDateComparator

### Collaborators
- Article

## ArticleTitleComparator

### Collaborators
- Article

## FeedTitleComparator

### Collaborators
- Feed

## PullFeed

### Collaborators
- MainActivity
- Article 
- Feed
- FeedGroup 
- AppDatabase
- FeedDao

# room

##  AppDatabase

### Collaborators
- Feed

## ArticleListConverter

### Collaborators
- Article

## FeedDao

### Collaborators
- None 

## TagsListConverter

### Collaborators
- None

# ui

## Icons

### Collaborators
- Article 

# activities/ui
None of these classes have collaborators. 
