# Cross Launcher
Sony XMB like Android Launcher, Mainly inspired by Sony PlayStation 3(TM) XMB.

## Main Focus
This launcher is not really focused to Android touch-based devices, But for Android devices that 
naturally doesn't have a native touch interface like TV, PC, Laptop, and Emulators.

The launcher is still fairly usable on Phones. Just with a bit struggle if you have a
lot of apps to navigate.

## Usage
| Function          | Keyboard | Gamepad   | Touch            |
|-------------------|----------|-----------|------------------|
| Navigate Items    | Arrow Pad| D-Pad     | Swipe            |
| Execute Item      | Enter    | X/O       | Icon touch       |
| Hide/Show Options*| Menu/Tab | Triangle  | Two-finger Touch |

*Not yet available

## Memory Usage
On my test devices, Base memory usage (without items) is about 10MB.
With about 500Kb~1MB per item since icons will be cached in memory.

## Available Features
- Switchable Confirm Button (Cross / Circle)
- Android built-in and Dictionary-based Game Detection
- Music and Video Gallery
- A Minimal Built-in Music Player
- Partial Gamepad Support
- Keystroke-to-Name Item finding
- Launcher Startup & App Launch Animation (Switchable), see [Animation Modding](https://github.com/EmiyaSyahriel/CrossLauncher#animation-modding) to modify it

## WIP Features
- PS3-like Dialogs
- Item Options (Just like when you press Triangle on PS3)
- Item Hide
- Open video file with default apps

## Planned Features
- Item Icon and name customization

## TODO:
- Soft-coding strings

## Animation Modding
You can modify the Launcher Startup / App Launch Animation and Sound by
adding/changing files in `/sdcard/Android/data/id.psw.vshlauncher/files/`.
| File name    | Correspond to   |
|--------------|-----------------|
| coldboot.png | Startup logo    |
| coldboot.mp3 | Startup Audio   |
| gameboot.png | App launch logo |
| gameboot.mp3 | App launch audio|

## Releases
None yet, Build it yourself if you want to test it.

## License
MIT License.
