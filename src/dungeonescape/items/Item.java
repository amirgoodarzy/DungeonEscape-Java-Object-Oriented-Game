package dungeonescape.items;

import dungeonescape.behaviors.Collectible;
import dungeonescape.entities.Entity;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.map.Position;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Common abstract superclass for all collectible objects in the dungeon.
 * Extends Entity and implements Collectible, demonstrating class hierarchy,
 * abstraction, and interface realization.
 */
public abstract class Item extends Entity implements Collectible {
    private static final long serialVersionUID = 1L;

    private final int value;
    private final String description;

    public Item(String name, Position position, int value, String description) {
        super(name, position, true); // Items are passable so player can step on them to collect
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void update(long deltaMs) {
        // Items are stationary by default
    }

    @Override
    public abstract String onCollect(Player player) throws InventoryFullException;

    /**
     * Helper to render a generic circular item base with custom icon/border color.
     */
    protected void renderItemBase(Graphics2D g, int tileSize, Color innerColor, Color borderColor) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;
        int pad = tileSize / 4;
        int size = tileSize - (pad * 2);

        g.setColor(innerColor);
        g.fillOval(px + pad, py + pad, size, size);

        g.setColor(borderColor);
        g.drawOval(px + pad, py + pad, size, size);
    }
}
