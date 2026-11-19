package dungeonescape.behaviors;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.InventoryFullException;

/**
 * Behavioral interface for objects that can be picked up and stored into an inventory.
 * Demonstrates checked exception propagation when collection cannot proceed.
 */
public interface Collectible {
    /**
     * Called when a player walks onto the collectible's position to pick it up.
     * 
     * @param player The player collecting the item.
     * @return Descriptive message indicating collection success.
     * @throws InventoryFullException if the player's inventory lacks capacity.
     */
    String onCollect(Player player) throws InventoryFullException;
}
