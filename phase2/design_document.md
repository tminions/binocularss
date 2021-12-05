# Phase 2 - Design Document

### Important parts to work on from Phase 1 feedback:
- Talk about testing
- Talk about code style and documentation
-

## Clean Architecture

### Adherence to Clean Architecture

Although there are some violations in our code (see below) our code does a good job following clean architecture. Our innermost layer of entities consists primarily of Feed and Article which only rely on each other. FeedGroup could reasonably belong to entities or use cases but either way, Feedgroup only relies on the innermost entities. Likewise our comparator classes also only rely on entities. Our database classes and activity classes fall into the outermost layer of frameworks and drivers and the database classes only act to interface with our database, and the activities mostly render our UI. These classes belong to the outermost layer so they may rely on any class from any inner layer without violating clean architecture.

### Fixing violations


## Solid Design Principles

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix

### Single Responsibility Principle

### Open-closed Principle

### Vacuous SOLID

### Dependency Inversion


## Packaging Strategies


## Design Patterns

Any design patterns that we used or plan to use

### Memento
TODO Expand more on how we utilized save states for Phase 2 additions

### Observer
TODO Expand more on how we utilized save states for Phase 2 additions

## Use of Github Features

### Pull Requests

Pull Requests were used in the fairly traditional way that you would expect. We each created our branches from the main branch and then once we were done working on our features, we created a pull request to merge our code into the main branch. We also got a bit of experience resolving merge conflicts which was useful as our project started getting
more complicated in Phase 2.

### Projects

We used project boards to help us organize and prioritize our work. Our mainboard was
for Phase 2 was our Features board which is where we would create new features to implement and keep track of features that were in progress, under review, or completed. We also marked each task within the project board with our initials so that other members could easily see who was working on what.

### Issues (New for Phase 2)

We since expanded upon our use of Github features with Issues. This allowed us to more easily keep track of any bugs within our application. This also provided
opportunities for other group members to find something to work on if they weren't sure. One aspect of Issues that we found helpful was that we were able to reference issues in both pull requests and project boards.

### Discord

Although not a Github feature, our team's primary source of communication has been through Discord. Instead of using Github Issues, we went through any errors and issues through Discord as it's more convenient to use for all of us.


## Refactoring


### Code smells
TODO Expand more on how we utilized save states for Phase 2 additions


##Testing

BIG TODO: Expand more on how we implemented rigorous testing for classes/UI for better coverage.
