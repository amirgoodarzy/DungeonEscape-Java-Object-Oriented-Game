package dungeonescape.exceptions;

/**
 * Thrown when saving or loading game state or custom rule files fails.
 * Demonstrates the Exception Translation pattern by wrapping lower-level
 * java.io.IOException or java.lang.ClassNotFoundException while preserving stack traces.
 */
public class SaveLoadException extends GameException {
    private final String filePath;

    public SaveLoadException(String message, String filePath) {
        super(String.format("%s [File: %s]", message, filePath));
        this.filePath = filePath;
    }

    public SaveLoadException(String message, String filePath, Throwable cause) {
        super(String.format("%s [File: %s]", message, filePath), cause);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
