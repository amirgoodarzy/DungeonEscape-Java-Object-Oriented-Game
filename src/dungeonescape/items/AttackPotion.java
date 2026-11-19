package dungeonescape.items;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Consumable potion that permanently or temporarily boosts player attack strength.
 * Demonstrates item hierarchy extensibility.
 */
public class AttackPotion extends Consumable {
    private static final long serialVersionUID = 1L;

    private final int attackBoost;

    public AttackPotion(Position position, int attackBoost) {
        super("Elixir of Might", position, 25, "Permanently increases player attack power by " + attackBoost + ".");
        this.attackBoost = attackBoost;
    }

    public AttackPotion(Position position) {
        this(position, 5);
    }

    public int getAttackBoost() {
        return attackBoost;
    }

    @Override
    public String use(Player player) throws GameException {
        player.setAttackPower(player.getAttackPower() + attackBoost);
        player.getInventory().removeItem(this);
        AudioPlayer.playTone(440, 100, 0.5);
        return String.format("Drank %s: Attack permanently increased by +%d (Current Attack: %d)!",
                getName(), attackBoost, player.getAttackPower());
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Flask neck
        g.setColor(new Color(200, 200, 220));
        g.fillRect(px + tileSize / 2 - 3, py + 12, 6, 6);
        // Flask liquid bulb (Orange / Amber)
        g.setColor(new Color(240, 130, 20));
        g.fillOval(px + 14, py + 18, tileSize - 28, tileSize - 28);
        g.setColor(Color.WHITE);
        g.drawOval(px + 14, py + 18, tileSize - 28, tileSize - 28);
    }
}
