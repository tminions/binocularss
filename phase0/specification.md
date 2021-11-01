# BinoculaRSS Specification

## Purpose

A mobile, feature rich, RSS reader. RSS stands for Really Simply Syndication. RSS is essentially a "web feed that allows users and applications to access websites in a standardized, computer-readable format." - [wikipedia](https://en.wikipedia.org/wiki/RSS). Users can subscribe to RSS feeds to keep track of many different websites in one place. This place is an RSS reader.

## What happens when we run the program

- You see your subscribed RSS feeds
  - Mode 1: see all of the articles from all the feeds sorted reverse chronological (like reddit)
  - Mode 2: See all of the articles from one feed sorted reverse chronological (like viewing a specific subreddit)
- When you click on article, it will open the article in a new activity
- There is a button to add more RSS feeds
- You can click on an article to view the entire article (we show only a thumbnail at first)
- The thumbnail will be as follows
  - Title
  - Small image if the article includes one. Favicon if they don't
  - Start of article (maybe first 150 chars)
- Hamburger menu button on the top left for settings/other options
- Bottom bar for different viewing modes

## Priority Features

- Being an RSS reader
- Multiple feeds
- Tags to allow for pooled feeds
  - Save for later/watch later
- sort by whatever metrics we decide on later (headline, feed name, priority)
- priority (like important in mail or like a ranking)
- Offline mode? 
- Search function
- Incognito RSS feeds or hidden mode (either one time or password protected)
- Bookmarking (requiring saving the position so we need a position somewhere in some class. Maybe by line number?)
- Share function copy or share sheet
- Export/Import save files. Maybe saving which RSS feeds you are subscribed to (local, google drive, etc). This will require some form of mechanism to input the save file as well

## Secondary Features

- Share via QR code
- Preview (hold to open or something)
- Track twitter user or subreddit or youtube channel, etc. (inside the app and generates the RSS feed).
- Reading history
- Tabs
- Taking notes, highlighting
- Preview option / peek
- Light theme + Dark theme + theme switcher. More themes than just light and dark. Custom themes if possible
- Screen reader support (properly annotating the xml files)
- Change text size/font
- Pages vs Infinite Scroll
- Settings on a per feed basis
- Save images in feeds
- Possibly video
- A way to add RSS feeds from specific websites like all the twitter or youtube RSS feeds
