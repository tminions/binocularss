# Phase 2 - Progress report

a brief summary of what each group member has been working on since phase 1
Each group member should include a link to a significant pull request (or two if you can't pick just one) that they made throughout the term. Include a sentence or two explaining why you think this demonstrates a significant contribution to the team.

## Group Member Breakdown since Phase 1

#### Benson Chou
- Added feature that allows user to delete feeds
- Added more tests

#### Eamon Ma
- Improved HTML rendering
- Implemented curated feeds in AddFeedActivity

#### Hisbaan Noorani
- Standardize padding, shape and typograhy values
- Added dyanmic open-source license information
- General refactoring
- Material3 UI Library
- View articles from a given feed

#### Ismail Ahmed
- Added search function
- Added Gateway class to encapsulate database variables

#### Macdeini Niu
- Added read history view to main activity

#### Salman Husainie
- Implement builder design pattern for Article and Feeds
- Add boundary between UI and FetchFeed (called ViewModel)

#### Simon Chen
- Add URL trimming and associated tests.
- Set up Github Actions CI/CD pipeline

#### Tai Zhang
- Added more tests

## Significant PRs:

#### Benson Chou

Pull Request - [Delete Feed](https://github.com/tminions/binocularss/pull/20)

This pull request added the ability to delete a feed which was important for giving the user more freedom.

#### **Eamon Ma**

Pull Request - [Curated Feeds](https://github.com/tminions/binocularss/pull/33)

This pull request implemented curated feeds, which are handpicked feeds, as suggestions for the user. This was important for improving the overall user experience.  

#### **Hisbaan Noorani**

Pull Request - [Material3 UI Library](https://github.com/tminions/binocularss/pull/29)

This pull request swaps out the material design 2 library for the material design 3 library. This enables a new design language focused on accessibility, and allows for wallpaper/user-based themes. This means that the user can select a theme that works best for their situation. Whether that be a disability, or they just like the colour green. The wallpaper-generated/user-based colours that material design 3 provides are created with luminosity principles to follow contrast accessibility requirements.

![out](https://user-images.githubusercontent.com/34548959/144769819-50678085-0da2-4e52-90fd-eb3cc1a25ea0.png)

Pull Request - [UI Refactor](https://github.com/tminions/binocularss/pull/37)

This pull request reduced the amount of code duplication in the UI by a lot. It mainly focused on MainActivity but covered other areas as well.

#### **Ismail Ahmed**

Pull Request - [Search Function](https://github.com/tminions/binocularss/pull/23)

This pull request added search functionality to our application which was critical to making our application fulfill our specification. 

Pull Request - [Database Gateway](https://github.com/tminions/binocularss/pull/26)

This pull request was significant because it added a gateway class that reduces coupling between the UI and the database. 

#### **Macdeini Niu**

Pull Request - [Reading History](https://github.com/tminions/binocularss/pull/22)

This pull request implements reading history that will display a list of articles that have been marked as read.  

#### **Salman Husainie**

Pull Request - [Fetch Feed](https://github.com/tminions/binocularss/pull/30)

This pull request implemented a boundary between our UI as well as an interface between our Use Case and UI which was important for adhering to clean architecture.


#### **Simon Chen**

Pull Request - [URL Trimming](https://github.com/tminions/binocularss/pull/19)

This pull request implements url trimming when the user inputs a url for a feed they want to add. This was important because it could potentially have been a very inconvenient bug. 

#### **Tai Zhang**

Pull Request - [Testing](https://github.com/tminions/binocularss/pull/38)

This pull request implements many tests including tests of our use cases and entities, as well as tests for our UI (end-to-end tests).

