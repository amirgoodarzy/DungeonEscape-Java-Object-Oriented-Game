package dungeonescape.rules.dsl;

import java.io.Serializable;

/**
 * Encapsulates a lexical token generated during DSL scanning.
 * Tracks source line and column numbers to provide precise diagnostic messages.
 */
public final class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TokenType type;
    private final String text;
    private final Object literal;
    private final int line;
    private final int column;

    public Token(TokenType type, String text, Object literal, int line, int column) {
        this.type = type;
        this.text = text;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, '%s', line %d, col %d)", type, text, line, column);
    }
}
