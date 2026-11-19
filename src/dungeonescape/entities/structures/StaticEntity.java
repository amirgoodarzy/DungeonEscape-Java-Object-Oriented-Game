package dungeonescape.entities.structures;

import dungeonescape.entities.Entity;
import dungeonescape.map.Position;

/**
 * Abstract superclass for all non-living, structural, or environmental elements.
 * Extends Entity to demonstrate multi-tier inheritance hierarchy.
 */
public abstract class StaticEntity extends Entity {
    private static final long serialVersionUID = 1L;

    public StaticEntity(String name, Position position, boolean passable) {
        super(name, position, passable);
    }

    @Override
    public void update(long deltaMs) {
        // Static entities typically do not need per-frame state updates,
        // but can be extended if animated (e.g. flickering torches, animated portals).
    }
}
