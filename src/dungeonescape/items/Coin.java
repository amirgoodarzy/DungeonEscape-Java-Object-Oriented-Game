package dungeonescape.items;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Currency item that immediately increments the player's coin count and score upon pickup.
 * Demonstrates direct entity interaction and polymorphic collection logic.
 */
public class Coin extends Item {
    private static final long serialVersionUID = 1L;

    public Coin(Position position, int value) {
        super("Gold Coin", position, value, "A shiny gold coin worth " + value + " points.");
    }

    public Coin(Position position) {
        this(position, 10);
    }

    @Override
    public String onCollect(Player player) throws InventoryFullException {
        player.addCoins(getValue());
        player.addScore(getValue());
        AudioPlayer.playPickupSound();
        return String.format("Collected %s (+%d Gold)!", getName(), getValue());
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Draw glittering coin
        g.setColor(new Color(255, 200, 0));
        g.fillOval(px + 14, py + 14, tileSize - 28, tileSize - 28);
        g.setColor(new Color(200, 150, 0));
        g.drawOval(px + 14, py + 14, tileSize - 28, tileSize - 28);
        g.setColor(Color.WHITE);
        g.drawString("$", px + tileSize / 2 - 4, py + tileSize / 2 + 5);
    }
}
