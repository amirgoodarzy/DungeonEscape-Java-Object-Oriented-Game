package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite AST node executing a list of sequential actions.
 * Demonstrates the Composite design pattern: CompositeAction is an Action
 * that itself aggregates and executes multiple Action instances.
 */
public class CompositeAction implements Action {
    private static final long serialVersionUID = 1L;

    private final List<Action> actions;

    public CompositeAction(List<Action> actions) {
        this.actions = new ArrayList<>();
        if (actions != null) {
            this.actions.addAll(actions);
        }
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public void addAction(Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    @Override
    public void execute(RuleContext context) {
        for (Action action : actions) {
            action.execute(context);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            sb.append(actions.get(i));
            if (i < actions.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }
}
