package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * AST node representing a dynamic game variable reference.
 * Reads runtime entity properties via RuleContext.
 */
public class VariableExpression implements Expression<Object> {
    private static final long serialVersionUID = 1L;

    private final String identifier;

    public VariableExpression(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Object evaluate(RuleContext context) {
        if (context == null) return null;
        return context.getVariable(identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
