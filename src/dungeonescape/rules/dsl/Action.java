package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;
import java.io.Serializable;

/**
 * Common abstraction for all actionable effects triggered by player-defined rules.
 * Demonstrates:
 * - Abstraction
 * - Command pattern
 * - Separation of condition evaluation from effect execution
 */
public interface Action extends Serializable {
    /**
     * Executes this rule action against the game world context.
     * 
     * @param context Active game world context.
     */
    void execute(RuleContext context);
}
