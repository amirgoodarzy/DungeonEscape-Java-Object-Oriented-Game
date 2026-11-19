package dungeonescape.items;

import dungeonescape.behaviors.Usable;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.map.Position;

/**
 * Abstract class representing wearable or equippable gear.
 * Implements Usable to allow equipping/unequipping.
 */
public abstract class Equipment extends Item implements Usable {
    private static final long serialVersionUID = 1L;

    private boolean equipped;

    public Equipment(String name, Position position, int value, String description) {
        super(name, position, value, description);
        this.equipped = false;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    @Override
    public String onCollect(Player player) throws InventoryFullException {
        player.getInventory().addItem(this);
        return String.format("Acquired equipment: %s. Added to inventory.", getName());
    }
}
