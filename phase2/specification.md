# Phase 2 - Updated Specification
Since phase 1, we've fulfilled our updated specification, but we've added several new features/fixes/refactors
that are worth noting.

### Search
Since phase 1, we've added search functionality to our application. In an attempt to make our
search results more accurate, we made use of a Java library that implements fuzzy string matching.
Fuzzy string matching allows us to approximate how well a string can match a given string, instead of
doing a hard equality check.


### Reading History
We've also added a feature that allows the user to view any articles that have been marked as read
should they want to go back to those articles for reference.

### Delete Feed

We also added a feature that allows the user to delete a feed. The way it works is that if the user
wants to delete a feed, they will go to the Feed view and press down on the feed they want to delete.
The user will then be presented with a pop up button that they can press to delete the feed.


### Dynamic License Information

Before phase 2, the way that we would display our list of dependencies was just by hard coding the actual text. 
Since then, we've added in a new gradle plugin that will generate a list at compile time as a JSON file. This
JSON file is then used to render the list inside the app.


### URL Trimming

A bug that was brought to our attention was when we a user tried to add a feed, and their input
contained whitespaces, the feed would not be added due to the fact that the input wasn't being trimmed.
We've since implemented a small bug fix within the appropriate activity that should trim the input.

### Database Gateway
We've also added a gateway class that encapsulates all of the database related objects such as the database
instance and the FeedDao. We then refactored all relevant activities that interact with the database to use
the gateway. This reduced a lot of the coupling we had before between the UI and the database. 

### Standardize padding, shape and typography values  

Instead of hardcoding a lot our styling, such as padding, when we draw the UI, we use standard values
that are all stored in one file. This helped make our code a lot more readable since the variable names
are more descriptive than numbers. 

### Bookmarks refactor

We decided to get rid of BookmarksActivity and reincorporate all the code within MainActivity.

When it came to the bookmarks activity, we noticed that it had a lot of the same boilerplate code as
the main activity. Secondly, we found user experience to be a lot more fluid when bookmarks weren't in
a separate activity. Finally, making BookmarksActivity into a view prevented clutter from building up
in the top bar as we added more features like Search.

## UML Diagram

See our [CRC cards for phase 1](https://github.com/tminions/binocularss/blob/main/phase1/phase1-crc.md) for a breakdown of how each class interacts with the others.