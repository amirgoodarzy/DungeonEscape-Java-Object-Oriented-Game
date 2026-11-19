package dungeonescape.items;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Consumable potion that restores player health.
 * Demonstrates state mutation through encapsulated public methods (player.heal).
 */
public class HealthPotion extends Consumable {
    private static final long serialVersionUID = 1L;

    private final int healAmount;

    public HealthPotion(Position position, int healAmount) {
        super("Health Potion", position, 15, "Restores up to " + healAmount + " health points.");
        this.healAmount = healAmount;
    }

    public HealthPotion(Position position) {
        this(position, 35);
    }

    public int getHealAmount() {
        return healAmount;
    }

    @Override
    public String use(Player player) throws GameException {
        if (player.getHealth() >= player.getMaxHealth()) {
            throw new GameException("Health is already at maximum! Potion not consumed.");
        }
        player.heal(healAmount);
        player.getInventory().removeItem(this);
        AudioPlayer.playTone(587, 120, 0.5); // Healing chime
        return String.format("Drank %s: Restored %d HP (Current: %d/%d)!",
                getName(), healAmount, player.getHealth(), player.getMaxHealth());
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Flask neck
        g.setColor(new Color(200, 200, 220));
        g.fillRect(px + tileSize / 2 - 3, py + 12, 6, 6);
        // Flask cork
        g.setColor(new Color(150, 100, 50));
        g.fillRect(px + tileSize / 2 - 4, py + 9, 8, 3);
        // Flask liquid bulb (Red)
        g.setColor(new Color(220, 30, 60));
        g.fillOval(px + 14, py + 18, tileSize - 28, tileSize - 28);
        g.setColor(Color.WHITE);
        g.drawOval(px + 14, py + 18, tileSize - 28, tileSize - 28);
    }
}
