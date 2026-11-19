package dungeonescape.items;

import dungeonescape.behaviors.Usable;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.map.Position;

/**
 * Abstract class for single-use items that are consumed upon activation.
 * Implements Usable, demonstrating interface specialization in the item hierarchy.
 */
public abstract class Consumable extends Item implements Usable {
    private static final long serialVersionUID = 1L;

    public Consumable(String name, Position position, int value, String description) {
        super(name, position, value, description);
    }

    @Override
    public String onCollect(Player player) throws InventoryFullException {
        player.getInventory().addItem(this);
        return String.format("Picked up %s. Added to inventory.", getName());
    }

    /**
     * Consumes this item, executing its effect and removing it from the player's inventory.
     */
    @Override
    public abstract String use(Player player) throws GameException;
}
