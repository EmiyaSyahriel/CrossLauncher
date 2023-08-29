# Cross Launcher Extension Library

## Developing an Extension
- Create an Android App Project
- Download the latest `launcher_xlib.jar` file from the release page and put it to your app subproject's `libs` directory
  - Not every release have one, latest `launcher_app` uses the latest `launcher_xlib` that is already released before
- Create `xmb_plugins.xml` in your `res/xml` directory, this file contains the list of class names that is the plugins:
```xml
<?xml version="1.0" encoding="utf-8"?>
<cxl:extensions apiVersion="0.1" xmlns:cxl="https://raw.githubusercontent.com/EmiyaSyahriel/CrossLauncher/master/launcher_xlib/PluginDefinitionScheme.xsd">
    <cxl:extension className="id.psw.crosslauncher.xlib.sample.InfoTextSamplePlugin" displayName="@string/app_name"/>
</cxl:extensions>
```
- Implement your extension classes by extending the `XmbPlugin{Type}` class

## How CrossLauncher works with your Extension
The extension is loaded using `ClassLoader` and therefore developing extensions for CrossLauncher will
not be the same as developing the majority Android app plugin system that is using AIDL. The code is directly
loaded as if it was a part of launcher's code instead of running as standalone app doing the standard IPC. and
therefore may have some limitation, e.g resources and assets.

And also, before installing an extension, make sure that it is not a virus or malware, or else CrossLauncher may be
accused by Anti-viruses as the virus itself!

### About `CoreExtension` activity
This activity is an identifier for CrossLauncher and is implicitly included to `AndroidManifest.xml`.
You do not need to implement it in your app.

## Extension Types
### Visualizer Extension
You can provide music visualizer to the app's internal music player

### Icon Extension
This provides an additional icon to the main menu

### Status Text Extension
This adds new format key to status bar