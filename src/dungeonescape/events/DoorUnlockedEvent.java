package dungeonescape.events;

import dungeonescape.entities.structures.Door;

/**
 * Dispatched when a previously locked door is successfully unlocked.
 */
public class DoorUnlockedEvent extends GameEvent {
    private static final long serialVersionUID = 1L;

    private final Door door;
    private final String keyUsed;

    public DoorUnlockedEvent(Door door, String keyUsed) {
        super("DOOR_UNLOCKED");
        this.door = door;
        this.keyUsed = keyUsed;
    }

    public Door getDoor() {
        return door;
    }

    public String getKeyUsed() {
        return keyUsed;
    }
}
