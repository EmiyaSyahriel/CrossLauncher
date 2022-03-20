# Cross Launcher
Sony XMB-like Android Launcher, Mainly inspired by Sony PlayStation 3(TM) XMB.

[Baca dalam Bahasa Indonesia](README_ID.md)

## Main Focus
This launcher is not really focused to Android touch-based devices, But for Android devices that 
naturally doesn't have a native touch interface like TV, PC, Laptop, and Emulators.

The launcher is still fairly usable on Phones. Just with a bit struggle if you have a
lot of apps to navigate.

## Memory Usage
On my test devices, Base memory usage (without items) is about 10MB.
With about 500Kb~1MB per item since icons will be cached in memory.

18/11/2020: Icon bitmap loading is more dynamic now, but possibly caused hiccups when
an icon is first drawn on screen after hidden before

20/03/2022: The launcher is currently very RAM demanding, in my phone which currently
have around 75 launch-able icons. It requires almost 512-700MB even with a very tight
bitmap lifecycling

## Screenshots
![App List](readme_asset/0.png)
![Custom Video Icon dan Backdrop](readme_asset/1.png)
![Apps Options](readme_asset/2.png)
![Android Settings](readme_asset/3.png)

## Progress
see [Main Project](https://github.com/EmiyaSyahriel/CrossLauncher/projects/1)

## Supported customizations
see [Supported customization](CUSTOM.MD)

## Releases
You can build it yourself, Or go to [Release](https://github.com/EmiyaSyahriel/CrossLauncher/releases)
page for pre-built packages

## Building
### Prerequisites
- Android Studio with:
    - Android SDK 29+
    - NDK 21+
    - CMake 3.8+
- .NET Scripting tool (Build script only, optional)
    - .NET Core 3.1 (.NET 5.0 is recommended)
### Steps
- Clone or Download this repository
- Open this project directory at Android Studio
- Build

To compile without the entire Android Studio package, please refer to this page : 
[Build your app from the command line | Android Developers](https://developer.android.com/studio/build/building-cmdline)
However, you would still need the Android SDK, NDK, CMake and optionally .NET Scripting tool.

.NET Scripting Tool is used to convert C++ resources to C++ source code, it's only used if you changes
resources file such as shader source file (`frag` and `vert` files), etc.

## Contribution
Translations and fixes are welcome.

## License
The main project is licensed under MIT License.
Some build-helper tool files is licensed under CC0 Public Domain.