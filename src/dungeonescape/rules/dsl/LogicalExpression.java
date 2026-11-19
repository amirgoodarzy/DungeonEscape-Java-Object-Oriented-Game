package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * AST node evaluating Boolean compound operations (AND, OR, NOT).
 * Implements Expression<Boolean>.
 * Demonstrates recursive tree structure and short-circuit evaluation.
 */
public class LogicalExpression implements Expression<Boolean> {
    private static final long serialVersionUID = 1L;

    public enum Operator {
        AND,
        OR,
        NOT
    }

    private final Expression<Boolean> left;
    private final Operator operator;
    private final Expression<Boolean> right;

    // Binary constructor for AND / OR
    public LogicalExpression(Expression<Boolean> left, Operator operator, Expression<Boolean> right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    // Unary constructor for NOT
    public LogicalExpression(Operator operator, Expression<Boolean> right) {
        this.left = null;
        this.operator = operator;
        this.right = right;
    }

    public Expression<Boolean> getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression<Boolean> getRight() {
        return right;
    }

    @Override
    public Boolean evaluate(RuleContext context) {
        switch (operator) {
            case NOT:
                return right != null && !Boolean.TRUE.equals(right.evaluate(context));
            case AND:
                if (left == null || !Boolean.TRUE.equals(left.evaluate(context))) {
                    return false; // Short-circuit
                }
                return right != null && Boolean.TRUE.equals(right.evaluate(context));
            case OR:
                if (left != null && Boolean.TRUE.equals(left.evaluate(context))) {
                    return true; // Short-circuit
                }
                return right != null && Boolean.TRUE.equals(right.evaluate(context));
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        if (operator == Operator.NOT) {
            return String.format("NOT (%s)", right);
        }
        return String.format("(%s %s %s)", left, operator, right);
    }
}
