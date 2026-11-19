package dungeonescape.items;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Concrete equippable weapon that enhances the player's physical attack damage.
 * Demonstrates:
 * - Specialization of Equipment and Item
 * - Behavioral polymorphism vs Consumables (equipping rather than destroying)
 * - State management and audio feedback
 */
public class Weapon extends Equipment {
    private static final long serialVersionUID = 1L;

    private final int attackBonus;

    public Weapon(String name, Position position, int attackBonus, int value) {
        super(name, position, value, String.format("%s (Grants +%d Attack Power)", name, attackBonus));
        this.attackBonus = attackBonus;
    }

    public Weapon(String name, Position position, int attackBonus) {
        this(name, position, attackBonus, 50);
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    @Override
    public String use(Player player) throws GameException {
        // Equip weapon to player
        Weapon previous = player.getEquippedWeapon();
        player.equipWeapon(this);
        AudioPlayer.playTone(520, 100, 0.5);

        if (previous != null) {
            return String.format("Swapped '%s' for '%s' (+%d Attack)!",
                    previous.getName(), getName(), attackBonus);
        } else {
            return String.format("Equipped '%s' (+%d Attack)!", getName(), attackBonus);
        }
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Draw sword blade & hilt
        g.setColor(new Color(220, 225, 235)); // Silver blade
        int[] bladeX = {px + tileSize - 12, px + tileSize - 18, px + 16, px + 18};
        int[] bladeY = {py + 10, py + 12, py + tileSize - 16, py + tileSize - 20};
        g.fillPolygon(bladeX, bladeY, 4);

        // Guard cross
        g.setColor(new Color(200, 160, 40)); // Golden crossguard
        g.fillRect(px + 14, py + tileSize - 22, 10, 4);

        // Pommel / grip
        g.setColor(new Color(110, 60, 20));
        g.fillRect(px + 12, py + tileSize - 18, 4, 8);
    }
}
