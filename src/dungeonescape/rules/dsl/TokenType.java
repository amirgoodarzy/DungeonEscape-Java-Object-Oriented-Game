package dungeonescape.rules.dsl;

/**
 * Tokens recognized by the Rule DSL Lexer.
 */
public enum TokenType {
    // Keywords
    RULE,
    WHEN,
    THEN,

    // Logical Operators
    AND,
    OR,
    NOT,

    // Identifiers and Literals
    IDENTIFIER,
    NUMBER,
    STRING,
    BOOLEAN,

    // Comparison Operators
    EQUALS,         // ==
    NOT_EQUALS,     // !=
    LESS_THAN,      // <
    LESS_EQUALS,    // <=
    GREATER_THAN,   // >
    GREATER_EQUALS, // >=

    // Arithmetic Operators
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /

    // Assignment Operators
    ASSIGN,         // =
    PLUS_ASSIGN,    // +=
    MINUS_ASSIGN,   // -=

    // Delimiters & Punctuation
    LPAREN,         // (
    RPAREN,         // )
    COMMA,          // ,
    COLON,          // :
    SEMICOLON,      // ;
    DOT,            // .

    // End of Input
    EOF
}
