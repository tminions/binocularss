# Class: Article

## Clean Architecture Group: Enterprise Business Rule

## Responsibility:

- Get the text and images of a specific article
- Save (to file)
- Load (from file)
- Bookmark
- Read flag

### Collaborators:

- Feed

# Class: Feed

## Clean Architecture Group: Enterprise Business Rule 
<!-- TODO: maybe application business rule? -->

## Responsibility:

- Aggregate all the different articles
- Have tags to be filtered by
- Save (to file)
- Load (from file)

## Collaborators:

- Article
- FeedGroup

# Class: FeedGroup

The main group of RSS feeds. This contains and saves any feeds that the user adds. This will be persistent across applications lifecycles. We may choose to have multiple feed groups or have them all in one and distinguish groupings with 'tags'.

## Clean Architecture Group: Interface Adapter

## Responsibility:

- Create Feed objects
- Read Feed object
- Delete Feed objects
- Filter feeds by tag
- Filter articles in feeds by date
- Sort articles in feeds chronologically (and reverse)

## Collaborators:

- Feed
- Higher UI Elements

# Class: PullFeedTask

## Clean Architecture Group: Interface Adapter
<!-- TODO: I think this is a gateway? -->

## Responsibility

- Get the XML of an RSS feed from the web

## Collaborators

- FeedGroup
- Feed
- AsyncTask

# Class: Operations

A class that contains all of the operations (like sorting, searching, etc) we need to perform on lists of articles.

## Clean Architecture Group: Interface Adapter

## Responsibility:

- Return a list that is sorted
- Return a list that is filtered by search term
- Return a list that is filtered by date

## Collaborators:

- UI elements

# Class: UserData

## Clean Architecture Group: Enterprise Business Rule

## Responsibility:

- Save articles (bookmark)
- Save lists of FeedGroups
- Save UserPreferences
- Save history

## Collaborators:

- FeedGroup

