package dungeonescape.entities.enemies;

import dungeonescape.behaviors.Attackable;
import dungeonescape.behaviors.Direction;
import dungeonescape.behaviors.Movable;
import dungeonescape.entities.LivingEntity;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.InvalidMoveException;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Common abstract superclass for all hostile creatures in the dungeon.
 * Demonstrates:
 * - Abstraction (declares abstract AI decision methods)
 * - Multiple typing via Movable and Attackable
 * - Encapsulation of AI detection parameters
 * - Subtyping: Enemy references dynamically dispatch to Zombie, Ghost, or Boss
 */
public abstract class Enemy extends LivingEntity implements Movable, Attackable {
    private static final long serialVersionUID = 1L;

    private final int detectionRange;
    private final int stepDelay;
    private long elapsedSinceActionMs;

    public Enemy(String name, Position position, int maxHealth, int attackPower, int defense, int detectionRange, int stepDelay) {
        super(name, position, maxHealth, attackPower, defense);
        this.detectionRange = detectionRange;
        this.stepDelay = Math.max(1, stepDelay);
        this.elapsedSinceActionMs = 0;
    }

    public int getDetectionRange() {
        return detectionRange;
    }

    public int getStepDelay() {
        return stepDelay;
    }

    /**
     * Checks if the target player is within the enemy's sensory detection radius.
     */
    public boolean canDetect(Player player) {
        if (player == null || !player.isAlive()) return false;
        return getPosition().manhattanDistanceTo(player.getPosition()) <= detectionRange;
    }

    /**
     * Evaluates the surrounding game room and player location to determine the next movement direction.
     * Polymorphic contract overridden by each concrete enemy type with unique AI logic.
     * 
     * @param player The active player entity.
     * @param room The current room context.
     * @return Chosen Direction, or null if remaining stationary.
     */
    public abstract Direction decideMove(Player player, Room room);

    @Override
    public void move(Direction direction) throws InvalidMoveException {
        if (direction != null) {
            setPosition(getPosition().translate(direction));
        }
    }

    @Override
    public void update(long deltaMs) {
        elapsedSinceActionMs += Math.max(0, deltaMs);
    }

    /** Milliseconds between enemy actions, with a single cooldown for BOTH attacks and movement. */
    protected long getActionDelayMs() {
        return (long) stepDelay * Constants.TIMER_DELAY_MS;
    }

    public boolean isReadyToStep() {
        if (elapsedSinceActionMs >= getActionDelayMs()) {
            elapsedSinceActionMs = 0;
            return true;
        }
        return false;
    }

    /**
     * Helper to render health status bar above the enemy.
     */
    protected void renderEnemyHealthBar(Graphics2D g, int px, int py, int tileSize) {
        int barW = tileSize - 10;
        int barH = 3;
        int barX = px + 5;
        int barY = py + 2;

        g.setColor(new Color(60, 20, 20));
        g.fillRect(barX, barY, barW, barH);

        int filledW = (int) Math.round(barW * getHealthRatio());
        g.setColor(new Color(220, 40, 40));
        g.fillRect(barX, barY, filledW, barH);
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barW, barH);
    }
}
