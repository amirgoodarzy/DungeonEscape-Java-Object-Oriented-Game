package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;
import java.util.ArrayList;
import java.util.List;

/**
 * AST node for querying specialized game functions (e.g. player.hasItem("Key"), enemies.nearby(2)).
 * Evaluates function argument expressions polymorphically before invoking the sandboxed context.
 */
public class FunctionCallExpression implements Expression<Object> {
    private static final long serialVersionUID = 1L;

    private final String functionName;
    private final List<Expression<?>> arguments;

    public FunctionCallExpression(String functionName, List<Expression<?>> arguments) {
        this.functionName = functionName;
        this.arguments = arguments != null ? arguments : new ArrayList<>();
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression<?>> getArguments() {
        return arguments;
    }

    @Override
    public Object evaluate(RuleContext context) {
        if (context == null) return false;
        List<Object> evaluatedArgs = new ArrayList<>(arguments.size());
        for (Expression<?> arg : arguments) {
            evaluatedArgs.add(arg.evaluate(context));
        }
        return context.callFunction(functionName, evaluatedArgs);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", functionName, arguments);
    }
}
