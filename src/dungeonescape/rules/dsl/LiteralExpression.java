package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * AST leaf node holding a constant literal value (Number, String, Boolean).
 * Demonstrates Parametric Polymorphism with generic type T.
 * 
 * @param <T> Type of the literal value.
 */
public class LiteralExpression<T> implements Expression<T> {
    private static final long serialVersionUID = 1L;

    private final T value;

    public LiteralExpression(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public T evaluate(RuleContext context) {
        return value;
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }
}
