package dungeonescape.exceptions;

/**
 * Thrown when the player attempts to cross or open a locked door without the required key.
 * Contains metadata identifying the door and required key for user notification.
 */
public class LockedDoorException extends GameException {
    private final String doorName;
    private final String requiredKeyId;

    public LockedDoorException(String doorName, String requiredKeyId) {
        super(String.format("The door '%s' is securely locked! It requires the '%s'.", doorName, requiredKeyId));
        this.doorName = doorName;
        this.requiredKeyId = requiredKeyId;
    }

    public String getDoorName() {
        return doorName;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }
}
