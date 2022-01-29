// IXMBIconListProvider.aidl
package id.psw.vshlauncher;

// Declare any non-default types here with import statements
import id.psw.vshlauncher.types.ExternalXMBItem;
import id.psw.vshlauncher.types.ExternalXMBItemHandle;
import id.psw.vshlauncher.types.ExternalXMBItemMenu;

interface IXMBIconListProvider {
// Metadata
    String getName();
    String getDescription();
    String getVersionString();

// Update Signal
    boolean shouldUpdateCategory(in String categoryId);
    boolean shouldUpdateItem(in String itemId, in int subId);

// Get Item Handle List
/// Get List of Category should be listed at menu
    List<ExternalXMBItemHandle> getCategories();
/// Get List of Items in the category
    List<ExternalXMBItemHandle> getItemsAtCategory(in String categoryId);
/// Get Sub-item / child of this item
    List<ExternalXMBItemHandle> getChildOf(in String itemId, in int subId);

// Get Item Data (Icon, Name, etc.)
/// Get Category Data
    ExternalXMBItem getCategoryData(in String itemId);
/// Get Item Data
    ExternalXMBItem getItemData(in String itemId, in int subId);
/// Get List of Menu for this item
    List<ExternalXMBItemMenu> getMenu(in String itemId, in int subId);

// Execution Functions
/// Called when an item with the specified IDs is launched
    void run(in String itemId, in int subId);
/// Called when a menu item of an item with the specified IDs is launched
    void runMenu(in String itemId, in int subId, in int menuId);
}