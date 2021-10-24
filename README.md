# BinoculaRSS

This is an Android RSS reader application.

## Installation

Install Android Studio, clone the project, and open the project. Follow any prompts that Android Studio gives you about installing a JDK or Android SDK.

You can choose to run the code on a physical Android device or an Android emulator.

### Physical Device

Ensure that ADB is installed. This is so that Android Studio can communicate with your phone. You will need to activate developer mode. For instructions on how to install ADB, see the following [article](https://www.xda-developers.com/install-adb-windows-macos-linux/#adbsetup).

1. Enable developer mode:
   - Go into `About this Phone`.
   - Find the `Build Number` heading, and click on it until a toast message pops up saying something along the lines of `You are a developer`.
2. Go into developer settings (location varies by device), and enable `USB Debugging`.
3. In Android Studio select the `APP` build configuration, select your device from the device drop down, and press the run button.

### Emulator

Ensure that ADB is installed. This is so that Android Studio can communicate with the emulator. For instructions on how to install ADB, see the following [article](https://www.xda-developers.com/install-adb-windows-macos-linux/#adbsetup).

1. Open the `AVD Manager` (Android Virtual Device Manager)
2. Click `Create Virtual Device`
3. Select any phone model and click `Next`
4. Click `Download` next to an Android System image from the `Recommended` tab. We recommend Android R. Press `Finish`. Wait for the download to complete and click `Next`.
5. Press `Finish`.
3. In Android Studio select the `APP` build configuration on the top right, select your device from the device drop down, and press the run button.

## Contribution

### Naming Convention

- Variables should be named in `camelCase`.
- Functions should be named with `camelCase`.
- Classes should be named with `PascalCase`.

<!-- - Kotlin variables that reference Android UI elements (defined in XML) should be prefixed with `m` so a variable for a `TextView` should be called `mTextView` or similar. -->
<!-- - Android UI IDs in XML should be named in `snake_case`. -->

<!-- ### Values -->

<!-- - All size measurements should ideally be in `dp` (Density independent pixels), but exceptions may apply -->
<!-- - All string values should be defined in `@strings` (`res/values(-night)/strings.xml`). -->
<!-- - All dimensions should be defined in `@dimen` (`res/values(-night)/dimens.xml`). -->
<!-- - All colours should be defined in `Color.kt` (`ui/theme/Color`). -->
<!-- - All theme values should be defined in `@themes` (`res/values(-night)/themes.xml`). -->
