package dungeonescape.entities;

import dungeonescape.map.Position;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Objects;

/**
 * Root abstract superclass for every entity occupying spatial presence on the dungeon map.
 * Demonstrates Abstraction, Hierarchy root, and Encapsulation.
 */
public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Position position;
    private boolean passable;
    private boolean visible;

    public Entity(String name, Position position, boolean passable) {
        this.name = Objects.requireNonNull(name, "Entity name cannot be null");
        this.position = Objects.requireNonNull(position, "Entity position cannot be null");
        this.passable = passable;
        this.visible = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be set to null");
    }

    public boolean isPassable() {
        return passable;
    }

    public void setPassable(boolean passable) {
        this.passable = passable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Renders the entity visually on the Java2D Graphics context.
     * Polymorphic call overridden by each concrete subtype.
     * 
     * @param g 2D Graphics rendering context.
     * @param tileSize Pixel size of a single grid tile.
     */
    public abstract void render(Graphics2D g, int tileSize);

    /**
     * Updates internal entity dynamics (AI timers, animation counters, status effects).
     * 
     * @param deltaMs Elapsed time in milliseconds since the last frame.
     */
    public abstract void update(long deltaMs);

    @Override
    public String toString() {
        return String.format("%s[name='%s', pos=%s]", getClass().getSimpleName(), name, position);
    }
}
