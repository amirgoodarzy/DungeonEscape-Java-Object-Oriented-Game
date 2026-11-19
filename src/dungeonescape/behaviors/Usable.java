package dungeonescape.behaviors;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;

/**
 * Behavioral interface for inventory items that produce active effects when used/equipped.
 * Demonstrates polymorphism: potions heal or buff, while weapons equip and raise attack.
 */
public interface Usable {
    /**
     * Applies the item's primary effect to the player.
     * 
     * @param player The target player using the item.
     * @return Status message reporting the outcome of using the item.
     * @throws GameException if the item cannot be used in the current context.
     */
    String use(Player player) throws GameException;
}
