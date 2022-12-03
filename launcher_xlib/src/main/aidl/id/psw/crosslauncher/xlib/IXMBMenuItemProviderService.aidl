// IXMBIconProviderService.aidl
package id.psw.crosslauncher.xlib;

interface IXMBMenuItemProviderService {

    /**
    *
    */
    List<String> getItemIds(String id);
    String getItemName(String id);
    long getItemUpdateInterval();

    boolean isItemHasDescription(String id);
    boolean isItemHasMenu(String id);
    boolean isItemHasBackground(String id);
    boolean isItemHasBacksound(String id);
    boolean isItemHasIcon(String id);
    boolean isItemTriggersGameboot(String id);
    boolean isItemHasValue(String id);
    /**
    * Item is a category (horizontal item)
    */
    boolean isItemCategory(String id);

    String getIconUri(String id, boolean nonAnimated);
    String getBackgroundUri(String id);
    String getBacksoundUri(String id);
    String getDescription(String id);
    List<String> getItemMenuIds(String id);
    String getSystemCategory(String id);
    String getStringBacksound(String id);

    void onItemClick(String id);
}