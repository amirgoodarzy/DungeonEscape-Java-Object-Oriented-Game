package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;

/**
 * Action that mutates an entity variable (e.g. player.attack = player.attack + 10, or player.health += 25).
 * Demonstrates the Command pattern and safe attribute mutation.
 */
public class AssignmentAction implements Action {
    private static final long serialVersionUID = 1L;

    public enum AssignType {
        ASSIGN,         // =
        PLUS_ASSIGN,    // +=
        MINUS_ASSIGN    // -=
    }

    private final String targetIdentifier;
    private final AssignType assignType;
    private final Expression<?> expression;

    public AssignmentAction(String targetIdentifier, AssignType assignType, Expression<?> expression) {
        this.targetIdentifier = targetIdentifier;
        this.assignType = assignType;
        this.expression = expression;
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public AssignType getAssignType() {
        return assignType;
    }

    public Expression<?> getExpression() {
        return expression;
    }

    @Override
    public void execute(RuleContext context) {
        if (context == null) return;

        Object evalValue = expression.evaluate(context);

        if (assignType == AssignType.ASSIGN) {
            context.setVariable(targetIdentifier, evalValue);
        } else {
            Object currentVal = context.getVariable(targetIdentifier);
            double currentNum = (currentVal instanceof Number) ? ((Number) currentVal).doubleValue() : 0.0;
            double changeNum = (evalValue instanceof Number) ? ((Number) evalValue).doubleValue() : 0.0;

            if (assignType == AssignType.PLUS_ASSIGN) {
                context.setVariable(targetIdentifier, currentNum + changeNum);
            } else if (assignType == AssignType.MINUS_ASSIGN) {
                context.setVariable(targetIdentifier, currentNum - changeNum);
            }
        }
    }

    @Override
    public String toString() {
        String op = (assignType == AssignType.PLUS_ASSIGN) ? "+="
                : (assignType == AssignType.MINUS_ASSIGN) ? "-=" : "=";
        return String.format("%s %s %s", targetIdentifier, op, expression);
    }
}
