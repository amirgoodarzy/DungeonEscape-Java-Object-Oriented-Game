package dungeonescape.map;

import dungeonescape.behaviors.Direction;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object representing 2D discrete grid coordinates.
 * Demonstrates value object pattern, encapsulation, and spatial utility methods.
 */
public final class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Calculates new position offset by the given direction.
     * @param direction Direction to step in.
     * @return New translated Position instance.
     */
    public Position translate(Direction direction) {
        if (direction == null) return this;
        return new Position(this.x + direction.getDeltaX(), this.y + direction.getDeltaY());
    }

    /**
     * Calculates new position offset by arbitrary dx, dy.
     */
    public Position translate(int dx, int dy) {
        return new Position(this.x + dx, this.y + dy);
    }

    /**
     * Calculates Manhattan distance to target coordinate: |x1 - x2| + |y1 - y2|.
     */
    public int manhattanDistanceTo(Position other) {
        if (other == null) return Integer.MAX_VALUE;
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Calculates Euclidean distance to target coordinate.
     * Demonstrates coercion: Math.sqrt returns double from int arithmetic.
     */
    public double euclideanDistanceTo(Position other) {
        if (other == null) return Double.MAX_VALUE;
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    /**
     * Checks if this position is directly orthogonally adjacent to another.
     */
    public boolean isAdjacentTo(Position other) {
        return manhattanDistanceTo(other) == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
