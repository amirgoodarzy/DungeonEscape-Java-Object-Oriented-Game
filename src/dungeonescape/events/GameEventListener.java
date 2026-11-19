package dungeonescape.events;

/**
 * Generic listener interface demonstrating Parametric Polymorphism.
 * 
 * @param <T> Any subclass of GameEvent.
 */
@FunctionalInterface
public interface GameEventListener<T extends GameEvent> {
    /**
     * Handles the dispatched event notification.
     * 
     * @param event The concrete event instance.
     */
    void onEvent(T event);
}
