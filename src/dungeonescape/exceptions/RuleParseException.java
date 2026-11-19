package dungeonescape.exceptions;

/**
 * Thrown by the Rule Lexer or Parser when a player-defined DSL rule contains syntax errors.
 * Stores line number, column number, and error context so the Rule Editor GUI can point
 * directly to the student's syntax mistake.
 */
public class RuleParseException extends GameException {
    private final int line;
    private final int column;
    private final String snippet;

    public RuleParseException(String message, int line, int column, String snippet) {
        super(String.format("Syntax Error [Line %d, Col %d]: %s (near '%s')", line, column, message, snippet));
        this.line = line;
        this.column = column;
        this.snippet = snippet;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getSnippet() {
        return snippet;
    }
}
