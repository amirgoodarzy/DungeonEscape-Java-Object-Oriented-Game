package dungeonescape.events;

import java.io.Serializable;

/**
 * Base class for all domain events generated during gameplay.
 * Demonstrates event-driven architecture, abstraction, and telemetry.
 */
public abstract class GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long timestamp;
    private final String eventType;

    public GameEvent(String eventType) {
        this.timestamp = System.currentTimeMillis();
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return String.format("[%s @ %d]", eventType, timestamp);
    }
}
