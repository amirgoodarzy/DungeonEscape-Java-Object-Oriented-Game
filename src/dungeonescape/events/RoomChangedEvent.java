package dungeonescape.events;

import dungeonescape.entities.Player;
import dungeonescape.map.Room;

/**
 * Dispatched when the player crosses a doorway into a new dungeon chamber.
 */
public class RoomChangedEvent extends GameEvent {
    private static final long serialVersionUID = 1L;

    private final Player player;
    private final String fromRoomId;
    private final Room newRoom;

    public RoomChangedEvent(Player player, String fromRoomId, Room newRoom) {
        super("ROOM_CHANGED");
        this.player = player;
        this.fromRoomId = fromRoomId;
        this.newRoom = newRoom;
    }

    public Player getPlayer() {
        return player;
    }

    public String getFromRoomId() {
        return fromRoomId;
    }

    public Room getNewRoom() {
        return newRoom;
    }
}
