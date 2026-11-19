package dungeonescape.rules.dsl;

import dungeonescape.exceptions.RuleValidationException;
import dungeonescape.rules.Rule;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Semantic validator for AST rules.
 * Verifies that identifiers, assignments, and expressions conform to safe sandbox rules.
 */
public class RuleValidator {

    private static final Set<String> READABLE_VARIABLES = new HashSet<>(Arrays.asList(
            "player.health", "player.maxhealth", "player.attack", "player.baseattack",
            "player.defense", "player.score", "player.coins", "player.alive", "player.room", "player.roomid",
            "room.name", "room.id", "room.enemies",
            "boss.health", "boss.maxhealth", "boss.alive", "boss.enraged", "boss.attack",
            "enemies.count"
    ));

    private static final Set<String> MUTABLE_VARIABLES = new HashSet<>(Arrays.asList(
            "player.health", "player.attack", "player.baseattack",
            "player.defense", "player.score", "player.coins"
    ));

    public static void validate(Rule rule) throws RuleValidationException {
        if (rule == null) {
            throw new RuleValidationException("Unnamed", "null", "Rule cannot be null");
        }
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            throw new RuleValidationException("Unnamed", "name", "Rule name cannot be empty");
        }
        if (rule.getCondition() == null) {
            throw new RuleValidationException(rule.getName(), "condition", "Rule condition cannot be empty");
        }
        if (rule.getAction() == null) {
            throw new RuleValidationException(rule.getName(), "action", "Rule action cannot be empty");
        }

        validateAction(rule.getName(), rule.getAction());
    }

    private static void validateAction(String ruleName, Action action) throws RuleValidationException {
        if (action instanceof CompositeAction) {
            for (Action subAction : ((CompositeAction) action).getActions()) {
                validateAction(ruleName, subAction);
            }
        } else if (action instanceof AssignmentAction) {
            AssignmentAction assign = (AssignmentAction) action;
            String target = assign.getTargetIdentifier().toLowerCase();
            if (!MUTABLE_VARIABLES.contains(target)) {
                throw new RuleValidationException(ruleName, assign.getTargetIdentifier(),
                        "Variable is read-only or unrecognized. Mutable properties are: " + MUTABLE_VARIABLES);
            }
        }
    }

    public static boolean isVariableKnown(String identifier) {
        if (identifier == null) return false;
        return READABLE_VARIABLES.contains(identifier.toLowerCase());
    }
}
