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

### Collaborators:
- None

## Feed


### Collaborators:
- Article

## FeedGroup

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
