package dungeonescape.exceptions;

/**
 * Base checked exception for all gameplay and rule-engine related failures.
 * Demonstrates exception hierarchy, checked exceptions in domain modeling,
 * and contextual debugging metadata.
 */
public class GameException extends Exception {
    private final long timestamp;

    public GameException(String message) {
        super(message);
        this.timestamp = System.currentTimeMillis();
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
