package dungeonescape.entities.enemies;

import dungeonescape.behaviors.DamageType;
import dungeonescape.behaviors.Direction;
import dungeonescape.entities.LivingEntity;
import dungeonescape.entities.Player;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.utils.AudioPlayer;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Spectral apparition enemy.
 * Demonstrates:
 * - Subtyping and behavioral differentiation: can phase through walls!
 * - Unpredictable floating AI movement
 * - Spectral damage type (DamageType.SPECTRAL) partially bypassing player armor
 */
public class Ghost extends Enemy {
    private static final long serialVersionUID = 1L;

    private final boolean phasesThroughWalls;
    private final Random random;

    public Ghost(Position position) {
        super("Ghost", position,
                Constants.GHOST_HEALTH,
                Constants.GHOST_ATTACK,
                Constants.GHOST_DEFENSE,
                6, // Detection range
                Constants.GHOST_STEP_DELAY);
        this.phasesThroughWalls = true;
        this.random = new Random();
    }

    public boolean isPhasesThroughWalls() {
        return phasesThroughWalls;
    }

    @Override
    public Direction decideMove(Player player, Room room) {
        if (!canDetect(player) || room == null) {
            // Idle wandering if player not detected
            Direction[] dirs = Direction.values();
            return dirs[random.nextInt(dirs.length)];
        }

        Position current = getPosition();
        Position target = player.getPosition();

        // If adjacent or floating right over, attack range
        if (current.isAdjacentTo(target)) {
            return null;
        }

        // Ghost has erratic phase movement: 75% direct tracking, 25% erratic sidestep
        if (random.nextInt(100) < 25) {
            Direction[] dirs = Direction.values();
            return dirs[random.nextInt(dirs.length)];
        }

        int dx = target.getX() - current.getX();
        int dy = target.getY() - current.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (dy != 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        } else {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        }
    }

    @Override
    public void attack(LivingEntity target) {
        attack(target, 0);
    }

    @Override
    public void attack(LivingEntity target, int bonusDamage) {
        if (target != null && target.isAlive()) {
            // Ghost inflicts SPECTRAL damage which pierces physical defense
            int damage = getAttackPower() + bonusDamage;
            target.takeDamage(damage, DamageType.SPECTRAL);
            AudioPlayer.playTone(600, 90, 0.4);
        }
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Ethereal glowing aura
        g.setColor(new Color(160, 220, 255, 60));
        g.fillOval(px + 4, py + 4, tileSize - 8, tileSize - 6);

        // Ghost body (Translucent cyan-white)
        g.setColor(new Color(220, 240, 255, 190));
        g.fillRoundRect(px + 10, py + 6, tileSize - 20, tileSize - 16, 12, 12);

        // Ghostly flowing tail wisps
        int[] tailX = {px + 10, px + 16, px + 24, px + 30, px + 38};
        int[] tailY = {py + tileSize - 10, py + tileSize - 4, py + tileSize - 9, py + tileSize - 4, py + tileSize - 10};
        g.drawPolyline(tailX, tailY, 5);

        // Glowing blue eyes
        g.setColor(new Color(0, 190, 255));
        g.fillOval(px + 16, py + 14, 5, 5);
        g.fillOval(px + 26, py + 14, 5, 5);

        // Health bar
        renderEnemyHealthBar(g, px, py, tileSize);
    }
}
