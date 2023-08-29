package id.psw.crosslauncher.xlib

enum class ExtensionType {
    None,
    StatusText,
    Icon,
    Visualizer
}

enum class ExtensionErrorReason {
    None,
    NoDefinition,
    EmptyClass,
    PartialLoadException,
    TotalLoadException
}