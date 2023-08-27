![Cross Launcher Logo](readme_asset/logo_base.png)

[Baca dalam Bahasa Indonesia](README_ID.md)

## **⚠ Warning! ⚠**
This app is in heavy alpha stage development and therefore not really stable! If you have
technical issue like crash or system bug, Please refer to [this page](https://github.com/EmiyaSyahriel/CrossLauncher/wiki/Error-Reporting)

## Main Focus
This launcher is not really focused to be used on touch-screen Android, it's mostly targeted
for devices that uses physical navigation like Gamepad, TV Remote or Keyboard.

The launcher is still fairly usable on touch screen. Just with a bit struggle if you have a
lot of apps to navigate.

## Memory Usage
08/07/2023 : Normally, at least on my Nokia T20, It should require around 50MB to 200MB of RAM, depending on :
- Count of icons currently displayed (animated icon is obviously uses larger RAM)
- Is currently playing audio file
- How many layer of backdrop is now showing (currently only max 1 layer, the backdrop)
- The resolution of the each backdrop
- Any leak is there?

## Screenshots
![App List](readme_asset/0.png)
![Custom Video Icon dan Backdrop](readme_asset/1.png)
![Apps Options](readme_asset/2.png)
![Android Settings](readme_asset/3.png)

## Progress
see [Main Project](https://github.com/EmiyaSyahriel/CrossLauncher/projects/1)

## Supported customizations
see [Supported customization](https://github.com/EmiyaSyahriel/CrossLauncher/wiki/Customization)

## Releases
You can build it yourself, Or go to [Release](https://github.com/EmiyaSyahriel/CrossLauncher/releases)
page for pre-built packages

## Building
### Prerequisites
- Android Studio with:
    - Android SDK 33+
    - NDK 21+
    - CMake 3.8+

### Steps
- Clone or Download this repository
- Open this project directory at Android Studio
- Build

To compile without the entire Android Studio package, please refer to this page : 
[Build your app from the command line | Android Developers](https://developer.android.com/studio/build/building-cmdline)
However, you would still need the Android SDK, NDK, CMake and Gradle.

Note : If you edit any text resource file located in `launcher_app/src/main/cpp/res`, run gradle task `:launcher_app:generateEmbeddedNativeSource`
to include the change to the source code since these files were all embedded into source code

## Contribution
Translations and fixes are welcome.

## License
The main project is licensed under MIT License.
Some build-helper tool files is licensed under CC0 Public Domain.
