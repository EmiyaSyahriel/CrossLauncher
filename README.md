# Cross Launcher
Sony XMB like Android Launcher, Mainly inspired by Sony PlayStation 3(TM) XMB.

## Main Focus
This launcher is not really focused to Android touch-based devices, But for Android devices that 
naturally doesn't have a native touch interface like TV, PC, Laptop, and Emulators.

The launcher is still fairly usable on Phones. Just with a bit struggle if you have a
lot of apps to navigate.

## Usage
| Function          | Keyboard | DualShock | Xbox     | Touch            |
|-------------------|----------|-----------|----------|------------------|
| Navigate Items    | Arrow Pad| D-Pad     | D-Pad    | Swipe            |
| Execute Item      | Enter    | X/O       | A/B      | Icon touch       |
| Hide/Show Options*| Menu/Tab | Triangle  | Y        | Two-finger Touch |

*Not yet available

## Memory Usage
On my test devices, Base memory usage (without items) is about 10MB.
With about 500Kb~1MB per item since icons will be cached in memory.

## Available Features
- [x] Switchable Confirm Button (Cross / Circle)
- [x] Android built-in and Dictionary-based Game Detection
- [x] Music and Video Gallery
- [x] A Minimal Built-in Music Player
- [x] Gamepad Support (partial)
- [x] Keystroke-to-Name Item finding
- [x] Launcher Startup & App Launch Animation (Switchable), see [Animation Modding](https://github.com/EmiyaSyahriel/CrossLauncher#animation-modding) to modify it
- [ ] PS3-like Dialogs
- [ ] Item Options (Just like when you press Triangle on PS3)
- [ ] Item Hide
- [ ] Open video file with default apps

## TODOs:
- Soft-coding strings
- Some optimizations regarding file loading

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
You can build it yourself, Or go to [Release](https://github.com/EmiyaSyahriel/CrossLauncher/releases) page for built packages

## License
MIT License.
