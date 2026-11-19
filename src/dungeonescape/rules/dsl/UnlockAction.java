package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * Action that triggers unlocking a named door in the current room.
 * Syntax: unlock("Gilded Skull Door")
 */
public class UnlockAction implements Action {
    private static final long serialVersionUID = 1L;

    private final String doorName;

    public UnlockAction(String doorName) {
        this.doorName = doorName;
    }

    public String getDoorName() {
        return doorName;
    }

    @Override
    public void execute(RuleContext context) {
        if (context != null) {
            context.unlockDoor(doorName);
        }
    }

    @Override
    public String toString() {
        return String.format("unlock(\"%s\")", doorName);
    }
}
