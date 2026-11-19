package dungeonescape.entities;

import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.exceptions.ItemNotFoundException;
import dungeonescape.items.Item;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the collection of items held by an entity (e.g. Player).
 * Demonstrates:
 * - Composition (Player HAS-A Inventory)
 * - Information Hiding (internal List is strictly private and exposed only via unmodifiable views)
 * - Checked Exception handling (InventoryFullException, ItemNotFoundException)
 */
public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Item> items;
    private int capacity;

    public Inventory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Inventory capacity must be strictly positive: " + capacity);
        }
        this.capacity = capacity;
        this.items = new ArrayList<>(capacity);
    }
  
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity < items.size()) {
            throw new IllegalArgumentException("New capacity cannot be smaller than current item count");
        }
        this.capacity = capacity;
    }

    public int size() {
        return items.size();
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Adds an item to the inventory.
     * 
     * @param item Item to insert.
     * @throws InventoryFullException if the container has reached maximum capacity.
     */
    public void addItem(Item item) throws InventoryFullException {
        if (item == null) return;
        if (isFull()) {
            throw new InventoryFullException(item.getName(), capacity);
        }
        items.add(item);
    }

    /**
     * Removes an item instance from the inventory.
     * 
     * @param item Item to remove.
     * @throws ItemNotFoundException if the item does not reside in the inventory.
     */
    public void removeItem(Item item) throws ItemNotFoundException {
        if (item == null || !items.remove(item)) {
            throw new ItemNotFoundException(item != null ? item.getName() : "null");
        }
    }

    /**
     * Removes an item by exact or case-insensitive name match.
     * 
     * @param itemName Name of item to locate and remove.
     * @return The removed Item instance.
     * @throws ItemNotFoundException if no matching item is present.
     */
    public Item removeItemByName(String itemName) throws ItemNotFoundException {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equalsIgnoreCase(itemName)) {
                return items.remove(i);
            }
        }
        throw new ItemNotFoundException(itemName);
    }

    /**
     * Checks whether an item with the given name is present in the inventory.
     */
    public boolean hasItem(String itemName) {
        if (itemName == null) return false;
        return items.stream().anyMatch(it -> it.getName().equalsIgnoreCase(itemName));
    }

    /**
     * Retrieves an item by index.
     */
    public Item get(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * Finds the first item with the specified name.
     */
    public Optional<Item> findByName(String itemName) {
        if (itemName == null) return Optional.empty();
        return items.stream()
                .filter(it -> it.getName().equalsIgnoreCase(itemName))
                .findFirst();
    }

    /**
     * Returns an unmodifiable list of items to callers.
     * Demonstrates Information Hiding: external code cannot add or remove
     * elements without going through the validated addItem / removeItem methods.
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void clear() {
        items.clear();
    }
}
