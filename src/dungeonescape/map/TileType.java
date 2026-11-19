package dungeonescape.map;

import java.io.Serializable;

/**
 * Enumeration representing distinct floor and structure types on the dungeon grid.
 */
public enum TileType implements Serializable {
    FLOOR(true, '.'),
    WALL(false, '#'),
    DOOR(true, '+'),
    EXIT(true, 'X'),
    TRAP(true, '^');

    private final boolean walkableByDefault;
    private final char symbol;

    TileType(boolean walkableByDefault, char symbol) {
        this.walkableByDefault = walkableByDefault;
        this.symbol = symbol;
    }

    public boolean isWalkableByDefault() {
        return walkableByDefault;
    }

    public char getSymbol() {
        return symbol;
    }
}
