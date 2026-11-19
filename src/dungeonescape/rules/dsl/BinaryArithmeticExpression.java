package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * AST node for mathematical expressions (+, -, *, /).
 * Demonstrates:
 * - Composition (HAS-A left and right Expression)
 * - Inclusion Polymorphism (evaluates arbitrary sub-expressions polymorphically)
 * - Coercion (coerces sub-expression outputs to Double)
 */
public class BinaryArithmeticExpression implements Expression<Double> {
    private static final long serialVersionUID = 1L;

    public enum Operator {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/");

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

    public BinaryArithmeticExpression(Expression<?> left, Operator operator, Expression<?> right) {
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
    public Double evaluate(RuleContext context) {
        Object lVal = left.evaluate(context);
        Object rVal = right.evaluate(context);

        double lNum = (lVal instanceof Number) ? ((Number) lVal).doubleValue() : 0.0;
        double rNum = (rVal instanceof Number) ? ((Number) rVal).doubleValue() : 0.0;

        switch (operator) {
            case PLUS: return lNum + rNum;
            case MINUS: return lNum - rNum;
            case MULTIPLY: return lNum * rNum;
            case DIVIDE: return rNum != 0.0 ? (lNum / rNum) : 0.0;
            default: return 0.0;
        }
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, operator.getSymbol(), right);
    }
}
