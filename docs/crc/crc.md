# Class: Article

## Responsibility:

- Get the text and images of a specific article
- Save (to file)
- Load (from file)
- Bookmark
- Read flag

### Collaborators:

- Feed

# Class: FeedGroup

The main group of RSS feeds. This contains and saves any feeds that the user adds. This will be persistent across applications lifecycles.

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

# Class: Feed

## Responsibility:

- Aggregate all the different articles
- Have tags to be filtered by
- Save (to file)
- Load (from file)

## Collaborators:

- Article
- FeedGroup

# Class: Operations

A class that contains all of the operations (like sorting, searching, etc) we need to perform on lists of articles.

## Responsibility:

- Return a list that is sorted
- Return a list that is filtered by search term
- Return a list that is filtered by date

## Collaborators:

- UI elements

# Class: UserData

## Responsibility:

- Save articles (bookmark)
- Save lists of FeedGroups
- Save UserPreferences
- Save history

## Collaborators:

- FeedGroup
