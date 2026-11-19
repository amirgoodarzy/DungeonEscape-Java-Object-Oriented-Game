package dungeonescape.events;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Player;
import dungeonescape.map.Position;

/**
 * Dispatched when the player translates across the dungeon grid.
 */
public class PlayerMoveEvent extends GameEvent {
    private static final long serialVersionUID = 1L;

    private final Player player;
    private final Position from;
    private final Position to;
    private final Direction direction;

    public PlayerMoveEvent(Player player, Position from, Position to, Direction direction) {
        super("PLAYER_MOVE");
        this.player = player;
        this.from = from;
        this.to = to;
        this.direction = direction;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Direction getDirection() {
        return direction;
    }
}
