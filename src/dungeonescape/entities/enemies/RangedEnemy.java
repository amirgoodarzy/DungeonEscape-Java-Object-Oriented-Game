package dungeonescape.entities.enemies;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Player;
import dungeonescape.map.Position;
import dungeonescape.map.Room;

/** Abstract ranged archetype: straight-line sight and tactically keeping distance. */
public abstract class RangedEnemy extends Enemy {
    private static final long serialVersionUID = 1L;
    private final int range;
    protected RangedEnemy(String name, Position pos, int hp, int atk, int armor, int range, int cooldown) {
        super(name, pos, hp, atk, armor, 9, cooldown);
        this.range = range;
    }
    public int getRange() { return range; }
    public boolean canShoot(Player target, Room room) {
        Position a = getPosition(), b = target.getPosition();
        int dx = Integer.compare(b.getX(), a.getX()), dy = Integer.compare(b.getY(), a.getY());
        if (a.getX() != b.getX() && a.getY() != b.getY()) return false;
        int distance = a.manhattanDistanceTo(b);
        if (distance > range || distance == 0) return false;
        for (int s = 1; s < distance; s++) {
            int x = a.getX() + dx * s, y = a.getY() + dy * s;
            if (!room.isWalkable(x, y) || room.hasSolidEntityAt(new Position(x, y))) return false;
        }
        return true;
    }
    @Override
    public Direction decideMove(Player target, Room room) {
        if (!canDetect(target)) return null;
        Position p = getPosition(), t = target.getPosition();
        int dx = t.getX() - p.getX(), dy = t.getY() - p.getY();
        Direction[] candidates;
        boolean retreat = p.manhattanDistanceTo(t) < 3;
        if (Math.abs(dx) >= Math.abs(dy)) {
            candidates = new Direction[] {dx > 0 ? Direction.RIGHT : Direction.LEFT,
                dy > 0 ? Direction.DOWN : Direction.UP};
        } else {
            candidates = new Direction[] {dy > 0 ? Direction.DOWN : Direction.UP,
                dx > 0 ? Direction.RIGHT : Direction.LEFT};
        }
        for (Direction direction : candidates) {
            Direction move = retreat ? direction.opposite() : direction;
            Position next = p.translate(move);
            if (room.isWalkable(next.getX(), next.getY()) && !room.hasSolidEntityAt(next)
                    && !next.equals(t)) return move;
        }
        return null;
    }
}
