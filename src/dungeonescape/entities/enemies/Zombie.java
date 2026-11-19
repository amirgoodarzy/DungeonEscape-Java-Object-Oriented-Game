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

/**
 * Slow, durable melee chaser.
 * Demonstrates:
 * - Subtyping and specialization of Enemy
 * - Pathfinding behavior: moves directly toward player along the axis of greatest distance
 * - Concrete implementation of attack() and decideMove()
 */
public class Zombie extends Enemy {
    private static final long serialVersionUID = 1L;

    public Zombie(Position position) {
        super("Zombie", position,
                Constants.ZOMBIE_HEALTH,
                Constants.ZOMBIE_ATTACK,
                Constants.ZOMBIE_DEFENSE,
                7, // Detection range
                Constants.ZOMBIE_STEP_DELAY);
    }

    @Override
    public Direction decideMove(Player player, Room room) {
        if (!canDetect(player) || room == null) {
            return null;
        }

        Position current = getPosition();
        Position target = player.getPosition();

        // Already adjacent to player: no need to step into player tile, can attack!
        if (current.isAdjacentTo(target)) {
            return null;
        }

        int dx = target.getX() - current.getX();
        int dy = target.getY() - current.getY();

        Direction primary = null;
        Direction secondary = null;

        // Choose primary axis of movement based on largest delta
        if (Math.abs(dx) >= Math.abs(dy)) {
            primary = dx > 0 ? Direction.RIGHT : Direction.LEFT;
            secondary = dy > 0 ? Direction.DOWN : Direction.UP;
        } else {
            primary = dy > 0 ? Direction.DOWN : Direction.UP;
            secondary = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        }

        // Check if primary move is walkable
        Position nextPrimary = current.translate(primary);
        if (room.isWalkable(nextPrimary.getX(), nextPrimary.getY()) && !room.hasSolidEntityAt(nextPrimary)) {
            return primary;
        }

        // Otherwise try secondary direction
        Position nextSecondary = current.translate(secondary);
        if (room.isWalkable(nextSecondary.getX(), nextSecondary.getY()) && !room.hasSolidEntityAt(nextSecondary)) {
            return secondary;
        }

        return null;
    }

    @Override
    public void attack(LivingEntity target) {
        attack(target, 0);
    }

    @Override
    public void attack(LivingEntity target, int bonusDamage) {
        if (target != null && target.isAlive()) {
            int damage = getAttackPower() + bonusDamage;
            target.takeDamage(damage, DamageType.PHYSICAL);
            AudioPlayer.playHitSound();
        }
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Shadow
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(px + 8, py + tileSize - 8, tileSize - 16, 6);

        // Rotting green zombie body
        g.setColor(new Color(46, 117, 89));
        g.fillRoundRect(px + 10, py + 12, tileSize - 20, tileSize - 18, 6, 6);

        // Zombie Head
        g.setColor(new Color(85, 138, 90));
        g.fillOval(px + 14, py + 6, tileSize - 28, tileSize - 28);

        // Dull yellow undead eyes
        g.setColor(new Color(240, 230, 80));
        g.fillRect(px + 18, py + 12, 3, 3);
        g.fillRect(px + 26, py + 12, 3, 3);

        // Outstretched decaying arms
        g.setColor(new Color(85, 138, 90));
        g.fillRect(px + 6, py + 18, 6, 4);
        g.fillRect(px + tileSize - 12, py + 18, 6, 4);

        // Health bar
        renderEnemyHealthBar(g, px, py, tileSize);
    }
}
