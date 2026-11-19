package dungeonescape.exceptions;

/**
 * Thrown when an item pickup is attempted but the player's inventory has reached capacity.
 * Prevents item loss and prompts player feedback without corrupting room state.
 */
public class InventoryFullException extends GameException {
    private final String itemName;
    private final int capacity;

    public InventoryFullException(String itemName, int capacity) {
        super(String.format("Cannot pick up '%s': Inventory is full (max capacity: %d items)!", itemName, capacity));
        this.itemName = itemName;
        this.capacity = capacity;
    }

    public String getItemName() {
        return itemName;
    }

    public int getCapacity() {
        return capacity;
    }
}
