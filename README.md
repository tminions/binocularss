# BinocularRSS

This is an Android RSS reader application.

## Installation

Install Android Studio, clone the project, and open the project.

## Contribution

### Naming Convention

- Java variables should be named in `camelCase`.
- Java variables that reference Android UI elements (defined in XML) should be prefixed with `m` so a variable for a `TextView` should be called `mTextView` or similar.
- Android UI IDs in XML should be named in `snake_case`.

### Values

- All size measurements should ideally be in `dp` (Density independent pixels), but exceptions may apply
- All string values should be defined in `@strings` (`res/values(-night)/strings.xml`).
- All dimensions should be defined in `@dimen` (`res/values(-night)/dimens.xml`).
- All colours should be defined in `@colors` (`res/values/colors.xml`).
- All theme values should be defined in `@themes` (`res/values(-night)/themes.xml`).