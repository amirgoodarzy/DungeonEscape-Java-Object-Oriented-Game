package dungeonescape.items;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * High-value treasure chest or jewel relic.
 * Added to inventory and awards substantial bonus score.
 */
public class Treasure extends Item {
    private static final long serialVersionUID = 1L;

    public Treasure(String name, Position position, int value) {
        super(name, position, value, "A priceless legendary treasure. Value: " + value + " points.");
    }

    public Treasure(Position position) {
        this("Ancient Chalice", position, 100);
    }

    @Override
    public String onCollect(Player player) throws InventoryFullException {
        // Legendary relics are journal objectives; they do not consume backpack slots.
        player.addRelic();
        player.addScore(getValue());
        AudioPlayer.playPickupSound();
        return String.format("Acquired legendary treasure: %s (+%d Score)!", getName(), getValue());
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Draw treasure chest
        g.setColor(new Color(139, 69, 19)); // Brown wood
        g.fillRect(px + 10, py + 14, tileSize - 20, tileSize - 22);

        // Gold trim & lock
        g.setColor(new Color(255, 215, 0));
        g.fillRect(px + 10, py + 22, tileSize - 20, 4);
        g.fillRect(px + tileSize / 2 - 3, py + 20, 6, 8);

        g.setColor(new Color(255, 255, 255, 120));
        g.drawRect(px + 10, py + 14, tileSize - 20, tileSize - 22);
    }
}
