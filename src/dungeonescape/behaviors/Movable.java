package dungeonescape.behaviors;

import dungeonescape.exceptions.InvalidMoveException;

/**
 * Behavioral interface implemented by any entity capable of moving on the 2D grid.
 * Demonstrates multiple typing and checked exception declaration in interface contracts.
 */
public interface Movable {
    /**
     * Attempts to move the entity in the given direction.
     * 
     * @param direction The cardinal direction to move toward.
     * @throws InvalidMoveException if the target tile is blocked, impassable, or out of bounds.
     */
    void move(Direction direction) throws InvalidMoveException;
}
