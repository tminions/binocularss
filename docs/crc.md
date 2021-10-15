 <!-- Entity:         [3/3] -->
 <!-- Use Cases:      [2/2] -->
 <!-- Controllers:    [5/1] -->
 <!-- User Interface: [1/1] -->

# Clean Architecture Group: Enterprise Business Rules

## Class Article

### Responsibility:
- Get the text and images of a specific article
- Save (to file)
- Load (from file)
- Bookmark
- Read flag

### Collaborators:
- Feed

## Class UserData

### Responsibility:
- Save articles (bookmark)
- Save lists of FeedGroups
- Save UserPreferences
- Save history

### Collaborators:
- FeedGroup

## Class: Feed

### Responsibility:
- Aggregate all the different articles
- Have tags to be filtered by
- Save (to file)
- Load (from file)

### Collaborators:
- Article
- FeedGroup

# Clean Architecture Group: Application Business Rules

## Class: FeedGroup
The main group of RSS feeds. This contains and saves any feeds that the user adds. This will be persistent across applications lifecycles. We may choose to have multiple feed groups or have them all in one and distinguish groupings with 'tags'.


### Responsibility:
- Create Feed objects
- Read Feed object
- Delete Feed objects
- Filter feeds by tag
- Filter articles in feeds by date
- Sort articles in feeds chronologically (and reverse)

### Collaborators:
- Feed

## Class: UserDataUpdater

### Responsability
- Rewrites saved information in UserData

### Collaborators
- UserData

# Clean Architecture Group: Interface Adapter

## Class: FeedReader

### Responsability
- Gets a list of feeds satisfying a condition if given
- Formats feeds for display 

### Collaborators
- FeedGroup
- UI Classes (Not yet written)

## Class: ArticleReader

### Responsability
- Gets a single article from a given feed
- Formats article for display

### Collaborators
- FeedGroup
- UI Classes (Not yet written)

## Class: PullFeedTask

### Responsibility
- Get the XML of an RSS feed from the web

### Collaborators
- FeedGroup
- Feed
- AsyncTask

## Class: Operations

A class that contains all of the operations (like sorting, searching, etc) we need to perform on lists of articles.

### Responsibility:
- Return a list that is sorted by date
- Return a list that is filtered by search term
- Return a list that is filtered by date

### Collaborators:
- UI elements
- Article

## Class: UserDataReader:

### Responsibility
- Gets the saved UserData preferences
- Formats user date information for display

### Collaborators
- UserData

#  Clean Architecture Group: Frameworks and Drivers

## Class CommandLineInterface

### Responsibility
- Displays received Feeds to the command line
- Displays received Articles to the command line
- Displays the current user data to the command line

### Collaborators
- UserDataReader
- FeedReader
- ArticleReader

