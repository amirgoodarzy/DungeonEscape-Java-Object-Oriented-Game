package dungeonescape.rules.dsl;

import dungeonescape.exceptions.RuleParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexical analyzer (tokenizer) for player-defined game rules DSL.
 * Scans raw source string into a sequential stream of typed Token objects.
 * Tracks line and column positions for precise student syntax error messages.
 */
public class Lexer {
    private final String source;
    private final List<Token> tokens;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int lineStart = 0;

    private static final Map<String, TokenType> KEYWORDS;

    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("RULE", TokenType.RULE);
        KEYWORDS.put("WHEN", TokenType.WHEN);
        KEYWORDS.put("IF", TokenType.WHEN); // Allows intuitive IF/THEN syntax as well
        KEYWORDS.put("THEN", TokenType.THEN);
        KEYWORDS.put("AND", TokenType.AND);
        KEYWORDS.put("OR", TokenType.OR);
        KEYWORDS.put("NOT", TokenType.NOT);
        KEYWORDS.put("true", TokenType.BOOLEAN);
        KEYWORDS.put("false", TokenType.BOOLEAN);
    }

    public Lexer(String source) {
        this.source = source != null ? source : "";
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() throws RuleParseException {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line, getColumn()));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private int getColumn() {
        return (start - lineStart) + 1;
    }

    private void scanToken() throws RuleParseException {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case ',': addToken(TokenType.COMMA); break;
            case ':': addToken(TokenType.COLON); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+':
                addToken(match('=') ? TokenType.PLUS_ASSIGN : TokenType.PLUS);
                break;
            case '-':
                addToken(match('=') ? TokenType.MINUS_ASSIGN : TokenType.MINUS);
                break;
            case '*': addToken(TokenType.MULTIPLY); break;
            case '/':
                if (match('/')) {
                    // Line comment: skip until newline
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.DIVIDE);
                }
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUALS : TokenType.ASSIGN);
                break;
            case '!':
                if (match('=')) {
                    addToken(TokenType.NOT_EQUALS);
                } else {
                    throw new RuleParseException("Unexpected '!' character. Did you mean '!=' or 'NOT'?", line, getColumn(), "!");
                }
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUALS : TokenType.LESS_THAN);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUALS : TokenType.GREATER_THAN);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    throw new RuleParseException("Unexpected '&'. Use 'AND' or '&&' for logical conjunction.", line, getColumn(), "&");
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    throw new RuleParseException("Unexpected '|'. Use 'OR' or '||' for logical disjunction.", line, getColumn(), "|");
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            case '\n':
                line++;
                lineStart = current;
                break;
            case '"':
                scanString();
                break;
            default:
                if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifierOrKeyword();
                } else {
                    throw new RuleParseException("Unrecognized character: '" + c + "'", line, getColumn(), String.valueOf(c));
                }
                break;
        }
    }

    private void scanString() throws RuleParseException {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                lineStart = current;
            }
            advance();
        }

        if (isAtEnd()) {
            throw new RuleParseException("Unterminated string literal", line, getColumn(), "\"");
        }

        advance(); // Closing quote
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void scanNumber() {
        while (isDigit(peek())) advance();

        // Check for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        }

        double val = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, val);
    }

    private void scanIdentifierOrKeyword() {
        // Allows alphanumeric, underscore, and dots for compound variables (e.g. player.health)
        while (isAlphaNumeric(peek()) || peek() == '.') {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        if (type == TokenType.BOOLEAN) {
            addToken(type, Boolean.parseBoolean(text));
        } else if (type != null) {
            addToken(type);
        } else {
            addToken(TokenType.IDENTIFIER, text);
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, getColumn()));
    }
}
