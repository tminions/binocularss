# Design Document

## Updated Specification

## UML Diagram

Also talk about clean architecture here

## Major Design Decisions

- How we chose to do workflows
- For each major design decision, justify the choice we made
  - like the library

## Clean Architecture

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix

## Solid Design Principles

Examples of code that we fixed, violations that we found that we could not fix or are unsure of how to fix

## Packaging Strategies

Android APK vs Android App Bundle. Signed vs Unsigned (we will need to sign it if we publish it) (see slide 7)

## Design Patterns

Any design patterns that we used or plan to use

### Builder

If we have any comple objects that we want to build (Parser for example), the builder design pattern allows us to do that well - pull from Salman's design pattern work.

```kotlin
parser = Parser.Builder()
    .context(context)
    .charset(Charset.forName("UTF_8"))
    .cacheExpirationMillis(24L * 60L * 60L * 100L) // Set the cache to expire in one day
    .build()
```

## Progress report

### Open Questions

### What has worked well so far

### Group Member Breakdown

#### Benson Chou

- Sorting/Filtering system

#### Eamon Ma

- Article Renderer/Viewer
- Sharing functionality

#### Hisbaan Noorani

- RSS parser
- Data persistence 
- Add, remove and organize feeds

#### Ismail Ahmed

- Bookmarking system
<!-- - Priority score -->

#### Macdeini Niu

- Main page (Mostly UI)

#### Salman Husainie

- Sorting/Filtering system

#### Simon Chen

#### Tai Zhang

- Tags System
