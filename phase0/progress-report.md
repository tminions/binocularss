# Progress Report
For our CSC207 project, our group (tminions) decided to create an RSS Reader.

For our Phase 0, our main goals were to determine the things we need to work on, create a thorough CRC model alongside a scenario walk-through that properly utilizes the CRC model, and then implement a basic working skeleton program. By keeping SOLID principles and Clean Architecture in mind, we created a foundation to build our project on top of.

## Specification Summary:
We decided to create a mobile (Android app), feature rich, RSS reader. An RSS feed allows users to subscribe to various feeds (websites such as CNN, blogging websites such as Tumblr, and even Youtube) and takes in a standardized, computer-readable format and outputs it as a stylized, minimalistic format that can be changed on a per-user basis. Some key features we wanted to highlight (that have not all been written yet) were:
- Object-oriented representation of the RSS Feed
- A feed that stores and view articles through a scroll format (similar to Reddit's UI)
- Different modes to view the feed (reverse chronological, from a single feed)
- Different tabs to view different articles
- Organizing the Feeds with tags
We wanted to prioritize useful features that traditional readers have such as bookmarking, offline mode and share functions while maintaining the simplicity of RSS feeds as opposed to the bloat that the modern web, and even some modern readers have.

[Click here for our specification](https://github.com/tminions/binocularss/blob/main/phase0/specification.md)

## CRC Model Summary:
Our CRC Model is based around 3 main entities: "Article", "Feed", and "FeedGroup". The rest of our classes follow SOLID principles and Clean Architecture, modifying these data structures using controllers, as well as use cases/user interface. 

We kept the CRC Model in a single Markdown file so one could easily view the different classes and dependencies, and how they integrate together.

[Click here for our CRC cards](https://github.com/tminions/binocularss/blob/main/phase0/crc.md)

## Scenario Walk-through:

Our scenario walk-through focuses on reading and adding RSS feeds and how one would go about doing so. It utilizes our skeleton program to highlight how we implemented Clean Architecture to have the controllers interact with the entities, creating our feed groups, feeds, and articles and their use cases.

[Click here for our scenario walk-through](https://github.com/tminions/binocularss/blob/main/phase0/walk-through.md)

## Skeleton program:

The skeleton program is written in the context of an Android application. We currently do not have "adding" functionality, but the app has all the classes that we talked about in our CRC model, and the ability to read RSS feeds from the web. When running the program (MainActivity.java) on an emulator or Android device via ADB, you can click on the purple button to read the default feed (for debugging purposes) which is the CBC top stories feed. If you want to view another feed, simply paste a valid RSS feed url in the box and then press the purple button. The feed information will not show in the app, but it will print in the "Run" tab in Android Studio or IntelliJ IDEA with the Android Support plugin.

[Click here for our app files](https://github.com/tminions/binocularss/tree/main/app/src/main/java/monster/minions/binocularss)

[Click here for our test files](https://github.com/tminions/binocularss/blob/main/app/src/test/java/monster/minions/binocularss/UnitTest.java)

## Group Member Breakdown:

### Benson Chou:
- Help with CRC cards.

### Eamon Ma:
- Researching different stacks to use instead of Java + Android XML.
- Help with CRC cards.
- Help with implementing skeleton program.

### Hisbaan Noorani:
- Implementing skeleton program.
- Help with CRC cards.
- Progress report, skeleton program section.

### Ismail Ahmed:
- Help with CRC cards.
- Help with skeleton program.

### Macdeini Niu:
- Help with CRC cards.
- Organizing CRC cards into Clean Architecture groups.
- Writing scenario walk-through.

### Salman Husainie:
- Help with CRC cards.

### Simon Chen:
- Help with CRC cards.
- Help with skeleton program.

### Tai Zhang:
- Help with CRC cards.
- Progress report

## What Has Worked Well:
One thing that has worked really well is breaking down the RSS feed app structure into Clean Architecture Classes. The problem domain very naturally falls into different entity objects like Article, Feed, and FeedGroup as well as controllers that change the properties of the various entities.

Another thing that has worked well is division of work. Some group members are more experienced with Android, while others are more proficient in writing and the design of the CRC cards. By having different responsibilities and having open channels for communication, we got a lot more accomplished than we would individually.

## Open Questions:
- One question we have is a possible change in the development stack we are using. We wish to switch to Kotlin, mainly due to the declarative UI library, JetBrains Compose. Additionally, Kotlin offers cleaner and easier to understand code. Professor Calver mentioned that this was okay as long as our TA did not have any objections.

- Another open question we had was about
