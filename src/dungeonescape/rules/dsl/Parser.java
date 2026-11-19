package dungeonescape.rules.dsl;

import dungeonescape.exceptions.GameException;
import dungeonescape.exceptions.RuleParseException;
import dungeonescape.rules.Rule;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser that transforms a stream of Tokens into an executable AST.
 * 
 * Demonstrates:
 * - Grammar parsing with operator precedence (OR < AND < NOT < Comparison < Additive < Multiplicative)
 * - Checked Exception handling: throws RuleParseException with exact line and column
 * - Construction of composite expression and action hierarchies
 */
public class Parser {
    private final List<Token> tokens;
    private final String originalSource;
    private int current = 0;

    public Parser(List<Token> tokens, String originalSource) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.originalSource = originalSource != null ? originalSource : "";
    }

    public Parser(List<Token> tokens) {
        this(tokens, "");
    }

    /**
     * Parses a complete rule definition:
     * RULE "Name": WHEN <Condition> THEN <Actions>
     */
    public Rule parseRule() throws GameException {
        consume(TokenType.RULE, "Expected 'RULE' keyword at beginning of rule definition");

        Token nameToken = peek();
        String ruleName;
        if (match(TokenType.STRING)) {
            ruleName = (String) nameToken.getLiteral();
        } else if (match(TokenType.IDENTIFIER)) {
            ruleName = nameToken.getText();
        } else {
            throw error(peek(), "Expected rule name string (e.g. \"Berserker Rage\")");
        }

        // Optional colon after rule name
        match(TokenType.COLON);

        consume(TokenType.WHEN, "Expected 'WHEN' (or 'IF') keyword after rule name");
        Expression<Boolean> condition = parseCondition();

        consume(TokenType.THEN, "Expected 'THEN' keyword after rule condition");
        Action action = parseActionList();

        Rule rule = new Rule(ruleName, condition, action, originalSource);
        RuleValidator.validate(rule);
        return rule;
    }

    /**
     * Parses multiple rules in a single source text.
     */
    public List<Rule> parseRules() throws GameException {
        List<Rule> rules = new ArrayList<>();
        while (!isAtEnd()) {
            rules.add(parseRule());
            // Optional trailing semicolon or delimiter between rules
            while (match(TokenType.SEMICOLON)) {}
        }
        return rules;
    }

    // ==========================================
    // Condition & Expression Parsing (Precedence)
    // ==========================================

    public Expression<Boolean> parseCondition() throws RuleParseException {
        return parseLogicalOr();
    }

    private Expression<Boolean> parseLogicalOr() throws RuleParseException {
        Expression<Boolean> expr = parseLogicalAnd();

        while (match(TokenType.OR)) {
            Expression<Boolean> right = parseLogicalAnd();
            expr = new LogicalExpression(expr, LogicalExpression.Operator.OR, right);
        }
        return expr;
    }

    private Expression<Boolean> parseLogicalAnd() throws RuleParseException {
        Expression<Boolean> expr = parseLogicalNot();

        while (match(TokenType.AND)) {
            Expression<Boolean> right = parseLogicalNot();
            expr = new LogicalExpression(expr, LogicalExpression.Operator.AND, right);
        }
        return expr;
    }

    private Expression<Boolean> parseLogicalNot() throws RuleParseException {
        if (match(TokenType.NOT)) {
            Expression<Boolean> right = parseLogicalNot();
            return new LogicalExpression(LogicalExpression.Operator.NOT, right);
        }
        return parseComparison();
    }

    @SuppressWarnings("unchecked")
    private Expression<Boolean> parseComparison() throws RuleParseException {
        Expression<?> left = parseArithmetic();

        if (match(TokenType.EQUALS, TokenType.NOT_EQUALS,
                TokenType.LESS_THAN, TokenType.LESS_EQUALS,
                TokenType.GREATER_THAN, TokenType.GREATER_EQUALS)) {
            Token opToken = previous();
            ComparisonExpression.Operator op;
            switch (opToken.getType()) {
                case EQUALS: op = ComparisonExpression.Operator.EQUALS; break;
                case NOT_EQUALS: op = ComparisonExpression.Operator.NOT_EQUALS; break;
                case LESS_THAN: op = ComparisonExpression.Operator.LESS_THAN; break;
                case LESS_EQUALS: op = ComparisonExpression.Operator.LESS_EQUALS; break;
                case GREATER_THAN: op = ComparisonExpression.Operator.GREATER_THAN; break;
                case GREATER_EQUALS: op = ComparisonExpression.Operator.GREATER_EQUALS; break;
                default: throw error(opToken, "Unknown comparison operator");
            }
            Expression<?> right = parseArithmetic();
            return new ComparisonExpression(left, op, right);
        }

        // If no relational operator, check if left is a boolean expression
        if (left instanceof Expression) {
            return (Expression<Boolean>) left;
        }

        throw error(peek(), "Expected comparison operator (e.g. <, <=, >, >=, ==, !=)");
    }

    private Expression<?> parseArithmetic() throws RuleParseException {
        Expression<?> expr = parseTerm();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token opToken = previous();
            BinaryArithmeticExpression.Operator op = (opToken.getType() == TokenType.PLUS)
                    ? BinaryArithmeticExpression.Operator.PLUS
                    : BinaryArithmeticExpression.Operator.MINUS;
            Expression<?> right = parseTerm();
            expr = new BinaryArithmeticExpression(expr, op, right);
        }
        return expr;
    }

    private Expression<?> parseTerm() throws RuleParseException {
        Expression<?> expr = parseFactor();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            Token opToken = previous();
            BinaryArithmeticExpression.Operator op = (opToken.getType() == TokenType.MULTIPLY)
                    ? BinaryArithmeticExpression.Operator.MULTIPLY
                    : BinaryArithmeticExpression.Operator.DIVIDE;
            Expression<?> right = parseFactor();
            expr = new BinaryArithmeticExpression(expr, op, right);
        }
        return expr;
    }

    private Expression<?> parseFactor() throws RuleParseException {
        if (match(TokenType.NUMBER)) {
            return new LiteralExpression<>((Double) previous().getLiteral());
        }

        if (match(TokenType.STRING)) {
            return new LiteralExpression<>((String) previous().getLiteral());
        }

        if (match(TokenType.BOOLEAN)) {
            return new LiteralExpression<>((Boolean) previous().getLiteral());
        }

        if (match(TokenType.LPAREN)) {
            Expression<Boolean> expr = parseCondition();
            consume(TokenType.RPAREN, "Expected ')' after grouping expression");
            return expr;
        }

        if (match(TokenType.IDENTIFIER)) {
            Token idToken = previous();
            // Check for function call syntax: identifier(...)
            if (match(TokenType.LPAREN)) {
                List<Expression<?>> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(parseArithmetic());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RPAREN, "Expected ')' after function arguments");
                return new FunctionCallExpression(idToken.getText(), args);
            }
            return new VariableExpression(idToken.getText());
        }

        throw error(peek(), "Expected expression (variable, number, string, or parenthesized sub-expression)");
    }

    // ==========================================
    // Action Parsing
    // ==========================================

    public Action parseActionList() throws RuleParseException {
        List<Action> actions = new ArrayList<>();
        actions.add(parseSingleAction());

        while (match(TokenType.SEMICOLON)) {
            if (isAtEnd() || check(TokenType.RULE)) {
                break; // Trailing semicolon allowed
            }
            actions.add(parseSingleAction());
        }

        if (actions.size() == 1) {
            return actions.get(0);
        }
        return new CompositeAction(actions);
    }

    private Action parseSingleAction() throws RuleParseException {
        if (match(TokenType.IDENTIFIER)) {
            Token id = previous();
            String name = id.getText();

            // Function call actions: spawn(), unlock(), message()
            if ("spawn".equalsIgnoreCase(name)) {
                consume(TokenType.LPAREN, "Expected '(' after spawn");
                Token entityTypeToken = consume(TokenType.STRING, "Expected entity type string (e.g. \"Ghost\")");
                consume(TokenType.COMMA, "Expected ',' after entity type");
                Expression<?> xExpr = parseArithmetic();
                consume(TokenType.COMMA, "Expected ',' after x coordinate");
                Expression<?> yExpr = parseArithmetic();
                consume(TokenType.RPAREN, "Expected ')' after spawn parameters");
                return new SpawnAction((String) entityTypeToken.getLiteral(), xExpr, yExpr);
            }

            if ("unlock".equalsIgnoreCase(name)) {
                consume(TokenType.LPAREN, "Expected '(' after unlock");
                Token doorToken = consume(TokenType.STRING, "Expected door name string (e.g. \"Gate\")");
                consume(TokenType.RPAREN, "Expected ')' after unlock parameter");
                return new UnlockAction((String) doorToken.getLiteral());
            }

            if ("message".equalsIgnoreCase(name)) {
                consume(TokenType.LPAREN, "Expected '(' after message");
                Token msgToken = consume(TokenType.STRING, "Expected message text string");
                consume(TokenType.RPAREN, "Expected ')' after message text");
                return new MessageAction((String) msgToken.getLiteral());
            }

            // Variable Assignment actions: var = expr, var += expr, var -= expr
            AssignmentAction.AssignType assignType;
            if (match(TokenType.ASSIGN)) {
                assignType = AssignmentAction.AssignType.ASSIGN;
            } else if (match(TokenType.PLUS_ASSIGN)) {
                assignType = AssignmentAction.AssignType.PLUS_ASSIGN;
            } else if (match(TokenType.MINUS_ASSIGN)) {
                assignType = AssignmentAction.AssignType.MINUS_ASSIGN;
            } else {
                throw error(peek(), "Expected '=', '+=', or '-=' for assignment action");
            }

            Expression<?> valueExpr = parseArithmetic();
            return new AssignmentAction(name, assignType, valueExpr);
        }

        throw error(peek(), "Expected action (variable assignment or command like spawn, unlock, message)");
    }

    // ==========================================
    // Parser Helper Utilities
    // ==========================================

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String errorMessage) throws RuleParseException {
        if (check(type)) return advance();
        throw error(peek(), errorMessage);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuleParseException error(Token token, String message) {
        return new RuleParseException(message, token.getLine(), token.getColumn(), token.getText());
    }
}
