// IXMBStatusTextKey.aidl
package id.psw.crosslauncher.xlib;

// Declare any non-default types here with import statements

interface IXMBStatusTextKey {
    List<String> getKeys();
    String getKeyDescription(String key);
    String format(String key, String param);
}