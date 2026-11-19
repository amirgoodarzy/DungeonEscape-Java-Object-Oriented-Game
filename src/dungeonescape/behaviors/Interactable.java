package dungeonescape.behaviors;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;

/**
 * Behavioral interface for entities that the player can interact with directly in the dungeon.
 * Realized by both StaticEntity subtypes (Door) and LivingEntity subtypes (NPC),
 * demonstrating cross-hierarchy multiple typing and inclusion polymorphism.
 */
public interface Interactable {
    /**
     * Interacts with the player.
     * 
     * @param player The player initiating the interaction.
     * @return Descriptive interaction feedback message to display on the HUD.
     * @throws GameException if the interaction cannot be completed (e.g. LockedDoorException).
     */
    String interact(Player player) throws GameException;
}
