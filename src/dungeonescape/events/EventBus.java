package dungeonescape.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decoupled event broker managing pub/sub communication across the game system.
 * Demonstrates:
 * - Parametric Polymorphism (type-safe subscription with generic wildcards)
 * - Observer pattern without hardcoded direct dependencies
 * - Extensibility: new event types can be published without changing the bus
 */
public class EventBus implements Serializable {
    private static final long serialVersionUID = 1L;

    // Transient map so serialization does not attempt to serialize active lambda listeners
    private transient Map<Class<? extends GameEvent>, List<GameEventListener<?>>> listeners;

    public EventBus() {
        this.listeners = new HashMap<>();
    }

    private Map<Class<? extends GameEvent>, List<GameEventListener<?>>> getListenersMap() {
        if (listeners == null) {
            listeners = new HashMap<>();
        }
        return listeners;
    }

    /**
     * Subscribes a listener to a specific class of GameEvent.
     * 
     * @param eventType The specific Class object for the event.
     * @param listener The callback listener receiving the event.
     * @param <T> The concrete GameEvent subtype.
     */
    public synchronized <T extends GameEvent> void subscribe(Class<T> eventType, GameEventListener<T> listener) {
        if (eventType == null || listener == null) return;
        Map<Class<? extends GameEvent>, List<GameEventListener<?>>> map = getListenersMap();
        map.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Dispatches an event to all registered listeners subscribed to its runtime class or supertypes.
     * 
     * @param event The event instance to broadcast.
     */
    @SuppressWarnings("unchecked")
    public synchronized void publish(GameEvent event) {
        if (event == null) return;
        Map<Class<? extends GameEvent>, List<GameEventListener<?>>> map = getListenersMap();

        for (Map.Entry<Class<? extends GameEvent>, List<GameEventListener<?>>> entry : map.entrySet()) {
            if (entry.getKey().isAssignableFrom(event.getClass())) {
                List<GameEventListener<?>> list = entry.getValue();
                for (GameEventListener<?> listener : list) {
                    ((GameEventListener<GameEvent>) listener).onEvent(event);
                }
            }
        }
    }

    public synchronized void clear() {
        getListenersMap().clear();
    }
}
