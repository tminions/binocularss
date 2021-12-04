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
TODO Expand more on how we utilized save states for Phase 2 additions

### Projects
TODO Expand more on how we utilized save states for Phase 2 additions

### Discord
TODO Expand more on how we utilized save states for Phase 2 additions

## Refactoring


### Code smells
TODO Expand more on how we utilized save states for Phase 2 additions


##Testing

BIG TODO: Expand more on how we implemented rigorous testing for classes/UI for better coverage.
