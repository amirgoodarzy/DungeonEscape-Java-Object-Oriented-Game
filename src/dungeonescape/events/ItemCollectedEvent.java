package dungeonescape.events;

import dungeonescape.entities.Player;
import dungeonescape.items.Item;

/**
 * Dispatched when the player picks up any collectible item.
 */
public class ItemCollectedEvent extends GameEvent {
    private static final long serialVersionUID = 1L;

    private final Player player;
    private final Item item;

    public ItemCollectedEvent(Player player, Item item) {
        super("ITEM_COLLECTED");
        this.player = player;
        this.item = item;
    }

    public Player getPlayer() {
        return player;
    }

    public Item getItem() {
        return item;
    }
}
