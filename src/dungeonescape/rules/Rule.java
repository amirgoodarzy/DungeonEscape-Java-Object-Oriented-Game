package dungeonescape.rules;

import dungeonescape.rules.dsl.Action;
import dungeonescape.rules.dsl.Expression;
import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulates a complete player-defined game rule.
 * Demonstrates:
 * - Composition (Rule HAS-A Condition Expression<Boolean>, Rule HAS-A Action)
 * - Information Hiding and Encapsulation
 * - Persistence of user-authored DSL source code
 */
public class Rule implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Expression<Boolean> condition;
    private final Action action;
    private final String sourceText;
    private boolean enabled;
    private int executionCount;
    private boolean lastConditionSatisfied;

    public Rule(String name, Expression<Boolean> condition, Action action, String sourceText) {
        this.name = Objects.requireNonNull(name, "Rule name cannot be null");
        this.condition = Objects.requireNonNull(condition, "Rule condition cannot be null");
        this.action = Objects.requireNonNull(action, "Rule action cannot be null");
        this.sourceText = sourceText != null ? sourceText : "";
        this.enabled = true;
        this.executionCount = 0;
        this.lastConditionSatisfied = false;
    }

    public String getName() {
        return name;
    }

    public Expression<Boolean> getCondition() {
        return condition;
    }

    public Action getAction() {
        return action;
    }

    public String getSourceText() {
        return sourceText;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled && !this.enabled) {
            this.lastConditionSatisfied = false;
        }
        this.enabled = enabled;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    /**
     * Evaluates the rule condition against the context.
     * If condition evaluates to true and rule is enabled, executes the action.
     * 
     * @param context Active game world context.
     * @return true if the rule triggered and executed its action, false otherwise.
     */
    public boolean evaluateAndExecute(RuleContext context) {
        if (!enabled || context == null) {
            return false;
        }

        Boolean result = condition.evaluate(context);
        boolean satisfied = Boolean.TRUE.equals(result);
        boolean justActivated = satisfied && !lastConditionSatisfied;
        lastConditionSatisfied = satisfied;
        if (justActivated) {
            action.execute(context);
            executionCount++;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Rule['%s', enabled=%b, runs=%d]", name, enabled, executionCount);
    }
}
