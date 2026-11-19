package dungeonescape.exceptions;

import dungeonescape.behaviors.Direction;

/**
 * Thrown when an entity attempts an illegal spatial translation (e.g. into a wall,
 * locked door without key, or out of map bounds).
 * Models domain-level navigation errors cleanly without crashing the game loop.
 */
public class InvalidMoveException extends GameException {
    private final int attemptedX;
    private final int attemptedY;
    private final Direction direction;

    public InvalidMoveException(String message, int attemptedX, int attemptedY, Direction direction) {
        super(String.format("%s at (%d, %d) heading %s", message, attemptedX, attemptedY, direction));
        this.attemptedX = attemptedX;
        this.attemptedY = attemptedY;
        this.direction = direction;
    }

    public int getAttemptedX() {
        return attemptedX;
    }

    public int getAttemptedY() {
        return attemptedY;
    }

    public Direction getDirection() {
        return direction;
    }
}
