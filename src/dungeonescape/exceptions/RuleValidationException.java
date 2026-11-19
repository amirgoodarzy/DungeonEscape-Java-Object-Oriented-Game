package dungeonescape.exceptions;

/**
 * Thrown by the Rule Validator when a syntactically correct rule violates semantic invariants
 * (e.g. referencing an unknown variable, assigning invalid types, or invoking non-existent functions).
 */
public class RuleValidationException extends GameException {
    private final String ruleName;
    private final String invalidIdentifier;

    public RuleValidationException(String ruleName, String invalidIdentifier, String reason) {
        super(String.format("Validation Error in Rule '%s': Identifier '%s' is invalid. %s", ruleName, invalidIdentifier, reason));
        this.ruleName = ruleName;
        this.invalidIdentifier = invalidIdentifier;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getInvalidIdentifier() {
        return invalidIdentifier;
    }
}
