package dungeonescape.map;

import dungeonescape.behaviors.Direction;
import dungeonescape.behaviors.Interactable;
import dungeonescape.entities.Entity;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.entities.structures.Door;
import dungeonescape.items.Item;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a single chamber or zone in the dungeon graph.
 * Demonstrates:
 * - Composition (Room HAS-A 2D array of TileType, Room HAS-A collection of Entity instances)
 * - Information Hiding (internal entity list and tile array are encapsulated behind query methods)
 * - Polymorphic rendering dispatch: iterates through Entity list invoking entity.render()
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final List<Entity> entities;
    private final Map<Direction, Door> doors;

    public Room(String id, String name, int width, int height) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.tiles = new TileType[width][height];
        this.entities = new ArrayList<>();
        this.doors = new EnumMap<>(Direction.class);

        // Initialize default floor tiles with perimeter walls
        initializeDefaultLayout();
    }

    public Room(String id, String name) {
        this(id, name, Constants.ROOM_WIDTH_TILES, Constants.ROOM_HEIGHT_TILES);
    }

    private void initializeDefaultLayout() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    tiles[x][y] = TileType.WALL;
                } else {
                    tiles[x][y] = TileType.FLOOR;
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return TileType.WALL;
        }
        return tiles[x][y];
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = type;
        }
    }

    /**
     * Checks if coordinates are within the room and the tile itself is walkable.
     */
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return tiles[x][y].isWalkableByDefault();
    }

    public void addEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public boolean removeEntity(Entity entity) {
        return entities.remove(entity);
    }

    /**
     * Returns an unmodifiable view of entities currently residing in this room.
     * Demonstrates Information Hiding.
     */
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Queries the first entity matching the given position.
     */
    public Optional<Entity> getEntityAt(Position pos) {
        if (pos == null) return Optional.empty();
        return entities.stream()
                .filter(e -> e.getPosition().equals(pos))
                .findFirst();
    }

    /**
     * Queries all entities at the given position (e.g. an item lying on a trap tile).
     */
    public List<Entity> getEntitiesAt(Position pos) {
        if (pos == null) return Collections.emptyList();
        return entities.stream()
                .filter(e -> e.getPosition().equals(pos))
                .collect(Collectors.toList());
    }

    /**
     * Queries whether any solid (impassable) entity occupies the target tile.
     */
    public boolean hasSolidEntityAt(Position pos) {
        if (pos == null) return false;
        return entities.stream()
                .filter(e -> e.getPosition().equals(pos))
                .anyMatch(e -> !e.isPassable());
    }

    /**
     * Finds an enemy residing at the given coordinate.
     */
    public Optional<Enemy> getEnemyAt(Position pos) {
        if (pos == null) return Optional.empty();
        return entities.stream()
                .filter(e -> e instanceof Enemy && e.getPosition().equals(pos))
                .map(e -> (Enemy) e)
                .findFirst();
    }

    /**
     * Finds an interactable entity at the given coordinate (Door or NPC).
     * Demonstrates Subtyping and Inclusion Polymorphism across interfaces.
     */
    public Optional<Interactable> getInteractableAt(Position pos) {
        if (pos == null) return Optional.empty();
        return entities.stream()
                .filter(e -> e instanceof Interactable && e.getPosition().equals(pos))
                .map(e -> (Interactable) e)
                .findFirst();
    }

    /**
     * Finds an item resting at the given coordinate.
     */
    public Optional<Item> getItemAt(Position pos) {
        if (pos == null) return Optional.empty();
        return entities.stream()
                .filter(e -> e instanceof Item && e.getPosition().equals(pos))
                .map(e -> (Item) e)
                .findFirst();
    }

    public void addDoor(Direction direction, Door door) {
        if (direction == null || door == null) {
            throw new IllegalArgumentException("A door requires a direction and a destination");
        }
        Position position = door.getPosition();
        int x = position.getX();
        int y = position.getY();
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Door must be inside the room boundaries");
        }
        // Boundary tiles start as walls. Every entrance needs a traversable FLOOR
        // underneath its door entity, otherwise even unlocked doors are impassable.
        setTile(x, y, TileType.FLOOR);
        doors.put(direction, door);
        addEntity(door);
    }

    public Door getDoor(Direction direction) {
        return doors.get(direction);
    }

    public Door getDoorAt(Position pos) {
        for (Door door : doors.values()) {
            if (door.getPosition().equals(pos)) {
                return door;
            }
        }
        return null;
    }

    /**
     * Renders the room floor tiles, structural grid, and delegates to each Entity's render method.
     * Demonstrates Polymorphism / Dynamic Dispatch: all entities are held as Entity references,
     * but each executes its own specialized Java2D graphics drawing.
     */
    public void render(Graphics2D g, int tileSize) {
        // Distinct, deterministic atmospheres for the three acts. These are
        // rendered, not stored, so they stay identical after save/load.
        Color floor = Constants.COLOR_FLOOR;
        Color grout = Constants.COLOR_FLOOR_GRID;
        Color wall = Constants.COLOR_WALL;
        Color highlights = new Color(72, 84, 94, 105);
        if (id.startsWith("F1_")) {
            floor = new Color(35, 49, 49);
            grout = new Color(44, 63, 61);
            wall = new Color(60, 85, 80);
            highlights = new Color(77, 150, 121, 105);
        } else if (id.startsWith("F2_")) {
            floor = new Color(61, 40, 38);
            grout = new Color(83, 55, 47);
            wall = new Color(103, 73, 64);
            highlights = new Color(219, 106, 59, 90);
        } else if (id.startsWith("F3_")) {
            floor = new Color(41, 35, 63);
            grout = new Color(61, 49, 85);
            wall = new Color(77, 62, 105);
            highlights = new Color(159, 105, 222, 110);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int px = x * tileSize, py = y * tileSize;
                int pattern = Math.floorMod(id.hashCode() + x * 37 + y * 97, 13);
                if (tiles[x][y] == TileType.WALL) {
                    g.setColor(wall);
                    g.fillRect(px, py, tileSize, tileSize);
                    g.setColor(grout);
                    g.drawRect(px, py, tileSize, tileSize);
                    // Staggered masonry, for a dungeon rather than a flat board.
                    g.drawLine(px + tileSize / 2, py, px + tileSize / 2, py + tileSize / 3);
                    if (pattern == 0) {
                        g.setColor(highlights);
                        g.fillRoundRect(px + 15, py + 16, 18, 4, 2, 2);
                    }
                } else {
                    g.setColor(floor);
                    g.fillRect(px, py, tileSize, tileSize);
                    g.setColor(grout);
                    g.drawRect(px, py, tileSize, tileSize);
                    if (pattern <= 2) {
                        g.setColor(highlights);
                        int ox = 8 + (pattern * 8);
                        g.drawLine(px + ox, py + 11, px + ox + 8, py + 11);
                        g.drawLine(px + ox + 8, py + 11, px + ox + 10, py + 17);
                    }
                }
            }
        }

        // 2. Polymorphically render all entities (Items, Traps, Doors, NPCs, Enemies)
        for (Entity entity : entities) {
            if (entity.isVisible()) {
                entity.render(g, tileSize);
            }
        }
    }
}
