package dungeonescape.exceptions;

/**
 * Thrown when an inventory item lookup, equip, or usage is requested for an item
 * not currently present in the player's inventory.
 */
public class ItemNotFoundException extends GameException {
    private final String itemName;

    public ItemNotFoundException(String itemName) {
        super(String.format("Item '%s' was not found in the inventory!", itemName));
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }
}
