**Warning: This launcher isn't ready to be used on TV yet, use at your own risk**

Some Android TV internally restricts the usage of external launcher. You can use this launcher this way, 
assuming you are on Windows.

1. Enable Developer Options and USB Debugging on your TV
2. Install your TV Driver and ADB with Fastboot to your PC / Laptop.
3. Download this Launcher installer.
4. Connect your TV to your PC / Laptop.
5. On your PC / Laptop, Run "cmd".
6. Sideload this launcher installer with:
```
adb install "path/to/apk"
```
7. Uninstall default launcher with:
```
adb uninstall -k --user 0 com.google.android.leanbacklauncher
adb uninstall -k --user 0 com.google.android.tvlauncher
```
8. Now, you can enjoy this launcher on your TV.
