# Cross Launcher Extension Library

## Visualizer Plugin
You can provide music visualizer to this app when user plays music from the launcher.


### How it works:
```kt
CrossLauncher ----------[ Connection ]---------- Plugin
       [Player Start]
       [Query Plugins] ------------------------> getIds()
       [Put to List]   <----------------------------┘
       [Get Active ID]
       [ID is from this plugin] ---------------> onStart(id) ->
       [Save URI as Handle] <-----------------------┘
 ┌---> [Play]
 |   ┌ [Get Sound Freq] -----------------------> processData(freq)
 |   | [Check Update] -------------------------> hasUpdated()
 |   | [Is Updated?] <------------------------------┘
 |   └ [false] [true]
 |               |
 |     [Request From Uri] ---------------------> PluginImageContentProvider.openInputStream()
 |     [Decode] <-----------------------------------┘
 |     [Display]
 |     [Still Playing?]
 └---- [true] [false] --------------------------> onTerminate()
```