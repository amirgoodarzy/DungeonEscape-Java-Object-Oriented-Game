package dungeonescape.items;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Key item required to unlock specific locked doors.
 * Stored in the player's inventory upon pickup.
 */
public class Key extends Item {
    private static final long serialVersionUID = 1L;

    private final String keyId;

    public Key(String name, Position position, String keyId) {
        super(name, position, 0, "A shimmering key capable of unlocking the '" + keyId + "' door.");
        this.keyId = keyId;
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public String onCollect(Player player) throws InventoryFullException {
        // Quest keys live in a separate keyring and cannot soft-lock the campaign if the backpack is full.
        player.storeKey(keyId);
        AudioPlayer.playPickupSound();
        return String.format("Acquired key: %s!", getName());
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Draw golden key icon
        g.setColor(new Color(255, 215, 0));
        // Bow (ring)
        g.drawOval(px + 12, py + 12, 10, 10);
        g.drawOval(px + 13, py + 13, 8, 8);
        // Shaft
        g.fillRect(px + 22, py + 16, 14, 3);
        // Teeth
        g.fillRect(px + 30, py + 19, 3, 5);
        g.fillRect(px + 34, py + 19, 3, 7);
    }
}
