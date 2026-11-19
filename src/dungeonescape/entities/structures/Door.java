package dungeonescape.entities.structures;

import dungeonescape.behaviors.Interactable;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.LockedDoorException;
import dungeonescape.items.Key;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Structural transition between dungeon rooms.
 * Demonstrates:
 * - Inheritance from StaticEntity
 * - Multiple typing via Interactable
 * - Encapsulation of lock state
 * - Checked exception throwing (LockedDoorException)
 */
public class Door extends StaticEntity implements Interactable {
    private static final long serialVersionUID = 1L;

    private final String targetRoomId;
    private final Position targetPlayerPosition;
    private boolean locked;
    private final String requiredKeyId;

    public Door(String name, Position position, String targetRoomId, Position targetPlayerPosition, boolean locked, String requiredKeyId) {
        super(name, position, !locked); // passable if not locked
        this.targetRoomId = targetRoomId;
        this.targetPlayerPosition = targetPlayerPosition;
        this.locked = locked;
        this.requiredKeyId = requiredKeyId;
    }

    public String getTargetRoomId() {
        return targetRoomId;
    }

    public Position getTargetPlayerPosition() {
        return targetPlayerPosition;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setPassable(!locked);
    }

    /**
     * Unlocks the door using a provided key instance if the key identifier matches.
     */
    public boolean unlock(Key key) {
        if (key != null && key.getKeyId().equalsIgnoreCase(this.requiredKeyId)) {
            setLocked(false);
            AudioPlayer.playDoorUnlockSound();
            return true;
        }
        return false;
    }

    /**
     * Unlocks the door directly (e.g. invoked by the Rule Engine).
     */
    public void unlockDirectly() {
        setLocked(false);
        AudioPlayer.playDoorUnlockSound();
    }

    @Override
    public String interact(Player player) throws LockedDoorException {
        if (!locked) {
            return String.format("Door '%s' is open. Stepping through...", getName());
        }

        // Check if player owns the matching key
        if (requiredKeyId != null && (player.hasKey(requiredKeyId) || player.getInventory().hasItem(requiredKeyId))) {
            setLocked(false);
            AudioPlayer.playDoorUnlockSound();
            return String.format("You unlocked the '%s' using the '%s'!", getName(), requiredKeyId);
        }

        AudioPlayer.playErrorSound();
        throw new LockedDoorException(getName(), requiredKeyId != null ? requiredKeyId : "Special Key");
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Frame
        g.setColor(new Color(40, 40, 50));
        g.fillRect(px, py, tileSize, tileSize);

        // Door leaf
        if (locked) {
            g.setColor(Constants.COLOR_DOOR_LOCKED);
            g.fillRect(px + 4, py + 4, tileSize - 8, tileSize - 8);

            // Keyhole graphic
            g.setColor(Color.YELLOW);
            g.fillOval(px + tileSize / 2 - 4, py + tileSize / 2 - 6, 8, 8);
            g.fillRect(px + tileSize / 2 - 2, py + tileSize / 2 - 1, 4, 8);
        } else {
            g.setColor(Constants.COLOR_DOOR_UNLOCKED);
            g.fillRect(px + 6, py + 6, tileSize - 12, tileSize - 12);

            // Archway interior
            g.setColor(new Color(20, 20, 25));
            g.fillRect(px + 12, py + 12, tileSize - 24, tileSize - 12);
        }

        g.setColor(Color.WHITE);
        g.drawRect(px + 2, py + 2, tileSize - 4, tileSize - 4);
    }
}
