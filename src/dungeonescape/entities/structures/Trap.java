package dungeonescape.entities.structures;

import dungeonescape.behaviors.DamageType;
import dungeonescape.entities.Player;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Floor spike trap that damages living entities stepping onto it.
 * Demonstrates damage types and interactive static entities.
 */
public class Trap extends StaticEntity {
    private static final long serialVersionUID = 1L;

    private final int trapDamage;
    private boolean sprung;

    public Trap(Position position, int trapDamage) {
        super("Floor Spike Trap", position, true); // Passable so entity can step onto it
        this.trapDamage = trapDamage;
        this.sprung = false;
    }

    public int getTrapDamage() {
        return trapDamage;
    }

    public boolean isSprung() {
        return sprung;
    }

    public void trigger(Player player) {
        if (!sprung && player != null) {
            sprung = true;
            player.takeDamage(trapDamage, DamageType.TRAP);
            AudioPlayer.playHitSound();
        }
    }

    public void reset() {
        this.sprung = false;
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        g.setColor(sprung ? new Color(180, 40, 40) : new Color(90, 90, 100));
        // Draw spiked grate pattern
        int[] xs = {px + 10, px + 20, px + 30, px + 40};
        for (int x : xs) {
            g.drawLine(x, py + 10, x, py + tileSize - 10);
            g.drawLine(px + 10, py + (x - px), px + tileSize - 10, py + (x - px));
        }
        g.drawRect(px + 8, py + 8, tileSize - 16, tileSize - 16);
    }
}
