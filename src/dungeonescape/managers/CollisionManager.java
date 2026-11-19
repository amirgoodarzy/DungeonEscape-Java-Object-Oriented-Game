package dungeonescape.managers;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Entity;
import dungeonescape.entities.enemies.Ghost;
import dungeonescape.entities.structures.Door;
import dungeonescape.entities.structures.Wall;
import dungeonescape.exceptions.InvalidMoveException;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import java.io.Serializable;
import java.util.Optional;

/**
 * Dedicated manager responsible for spatial collision detection and navigation validation.
 * Demonstrates:
 * - Single Responsibility Principle (SRP)
 * - Domain checked exception throwing (InvalidMoveException)
 * - Subtype-specific collision rules (e.g. Ghost phases through walls)
 */
public class CollisionManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Validates whether an entity can legally step from its current position to the target.
     * 
     * @param entity The entity attempting to move.
     * @param target The requested destination coordinate.
     * @param direction The cardinal direction heading.
     * @param room The active room containing the entity.
     * @throws InvalidMoveException if the destination is illegal or blocked.
     */
    public void validateMove(Entity entity, Position target, Direction direction, Room room) throws InvalidMoveException {
        if (entity == null || target == null || room == null) {
            throw new InvalidMoveException("Invalid parameters for movement validation", 0, 0, direction);
        }

        int tx = target.getX();
        int ty = target.getY();

        // 1. Boundary checking
        if (tx < 0 || tx >= room.getWidth() || ty < 0 || ty >= room.getHeight()) {
            throw new InvalidMoveException("Move out of room boundaries", tx, ty, direction);
        }

        // 2. Tile walkability check (Ghosts can phase through walls!)
        boolean isGhost = (entity instanceof Ghost);
        if (!room.isWalkable(tx, ty) && !isGhost) {
            throw new InvalidMoveException("Blocked by wall structure", tx, ty, direction);
        }

        // 3. Locked door check
        Door door = room.getDoorAt(target);
        if (door != null && door.isLocked() && !isGhost) {
            throw new InvalidMoveException("Door is currently locked", tx, ty, direction);
        }

        // 4. Solid entity collision check
        Optional<Entity> solidObstacle = room.getEntities().stream()
                .filter(e -> e != entity && !e.isPassable() && e.getPosition().equals(target))
                .findFirst();

        if (solidObstacle.isPresent() && !(isGhost && solidObstacle.get() instanceof Wall)) {
            throw new InvalidMoveException("Tile occupied by " + solidObstacle.get().getName(), tx, ty, direction);
        }
    }

    /**
     * Non-throwing convenience method for AI pathfinding queries.
     */
    public boolean canMoveTo(Entity entity, Position target, Room room) {
        try {
            validateMove(entity, target, Direction.UP, room);
            return true;
        } catch (InvalidMoveException e) {
            return false;
        }
    }
}
