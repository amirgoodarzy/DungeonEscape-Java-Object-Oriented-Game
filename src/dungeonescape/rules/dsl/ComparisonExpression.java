package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;
import java.util.Objects;

/**
 * AST node evaluating comparison predicates (<, <=, >, >=, ==, !=).
 * Implements Expression<Boolean>.
 * Supports both numeric relational comparisons and generic object/string equality.
 */
public class ComparisonExpression implements Expression<Boolean> {
    private static final long serialVersionUID = 1L;

    public enum Operator {
        EQUALS("=="),
        NOT_EQUALS("!="),
        LESS_THAN("<"),
        LESS_EQUALS("<="),
        GREATER_THAN(">"),
        GREATER_EQUALS(">=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private final Expression<?> left;
    private final Operator operator;
    private final Expression<?> right;

    public ComparisonExpression(Expression<?> left, Operator operator, Expression<?> right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression<?> getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression<?> getRight() {
        return right;
    }

    @Override
    public Boolean evaluate(RuleContext context) {
        Object lVal = left.evaluate(context);
        Object rVal = right.evaluate(context);

        // Numeric comparison
        if (lVal instanceof Number && rVal instanceof Number) {
            double lNum = ((Number) lVal).doubleValue();
            double rNum = ((Number) rVal).doubleValue();

            switch (operator) {
                case LESS_THAN: return lNum < rNum;
                case LESS_EQUALS: return lNum <= rNum;
                case GREATER_THAN: return lNum > rNum;
                case GREATER_EQUALS: return lNum >= rNum;
                case EQUALS: return Math.abs(lNum - rNum) < 0.0001;
                case NOT_EQUALS: return Math.abs(lNum - rNum) >= 0.0001;
            }
        }

        // Object / String comparison
        if (operator == Operator.EQUALS) {
            if (lVal == null && rVal == null) return true;
            if (lVal == null || rVal == null) return false;
            return String.valueOf(lVal).equalsIgnoreCase(String.valueOf(rVal));
        } else if (operator == Operator.NOT_EQUALS) {
            if (lVal == null && rVal == null) return false;
            if (lVal == null || rVal == null) return true;
            return !String.valueOf(lVal).equalsIgnoreCase(String.valueOf(rVal));
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, operator.getSymbol(), right);
    }
}
