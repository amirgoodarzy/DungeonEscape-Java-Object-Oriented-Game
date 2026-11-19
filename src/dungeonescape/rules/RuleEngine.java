package dungeonescape.rules;

import dungeonescape.events.CombatEvent;
import dungeonescape.events.DoorUnlockedEvent;
import dungeonescape.events.EventBus;
import dungeonescape.events.ItemCollectedEvent;
import dungeonescape.events.PlayerMoveEvent;
import dungeonescape.events.RoomChangedEvent;
import dungeonescape.exceptions.GameException;
import dungeonescape.rules.dsl.Lexer;
import dungeonescape.rules.dsl.Parser;
import dungeonescape.rules.dsl.Token;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The runtime coordinator for all player-defined game rules.
 * 
 * Demonstrates:
 * - Composition (RuleEngine HAS-A List<Rule>)
 * - Dynamic dispatch and execution of user-authored expressions
 * - Integration with EventBus: triggers evaluation when game events fire
 * - Safe exception handling during compilation and execution
 */
public class RuleEngine implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Rule> rules;
    private int evaluationCycles;
    private transient int actionDepth;
    private transient boolean evaluationPending;
    private transient RuleContext pendingContext;

    public RuleEngine() {
        this.rules = new ArrayList<>();
        this.evaluationCycles = 0;
    }

    /**
     * Wires the RuleEngine to automatically evaluate active rules upon game events.
     */
    public void attachToEventBus(EventBus eventBus, RuleContext context) {
        if (eventBus == null || context == null) return;

        eventBus.subscribe(PlayerMoveEvent.class, e -> requestEvaluation(context));
        eventBus.subscribe(CombatEvent.class, e -> requestEvaluation(context));
        eventBus.subscribe(ItemCollectedEvent.class, e -> requestEvaluation(context));
        eventBus.subscribe(RoomChangedEvent.class, e -> requestEvaluation(context));
        eventBus.subscribe(DoorUnlockedEvent.class, e -> requestEvaluation(context));
    }

    /**
     * Groups game events into a single atomic action; a move + pickup + room change
     * must evaluate the custom rules only once, after the world reaches its final state.
     */
    public void beginAction() {
        actionDepth++;
    }

    public void requestEvaluation(RuleContext context) {
        if (context == null) return;
        if (actionDepth > 0) {
            evaluationPending = true;
            pendingContext = context;
        } else {
            evaluateAll(context);
        }
    }

    public void endAction() {
        if (actionDepth <= 0) throw new IllegalStateException("Unbalanced action batch");
        if (--actionDepth == 0 && evaluationPending) {
            evaluationPending = false;
            RuleContext context = pendingContext;
            pendingContext = null;
            evaluateAll(context);
        }
    }

    /**
     * Compiles, validates, and registers a new rule directly from player-defined DSL source text.
     * 
     * @param sourceText DSL string (e.g. RULE "Berserk": WHEN player.health < 30 THEN player.attack += 10)
     * @return The compiled and active Rule instance.
     * @throws GameException if syntax or semantic validation fails.
     */
    public Rule registerRuleFromSource(String sourceText) throws GameException {
        Lexer lexer = new Lexer(sourceText);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, sourceText);
        Rule rule = parser.parseRule();

        // Remove any existing rule with identical name to allow updates
        removeRule(rule.getName());
        rules.add(rule);
        return rule;
    }

    /**
     * Compiles and registers multiple rules from a multi-rule script text.
     */
    public List<Rule> registerRulesFromScript(String scriptText) throws GameException {
        Lexer lexer = new Lexer(scriptText);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, scriptText);
        List<Rule> parsedRules = parser.parseRules();

        for (Rule rule : parsedRules) {
            removeRule(rule.getName());
            rules.add(rule);
        }
        return parsedRules;
    }

    public void addRule(Rule rule) {
        if (rule != null) {
            removeRule(rule.getName());
            rules.add(rule);
        }
    }

    public boolean removeRule(String ruleName) {
        return rules.removeIf(r -> r.getName().equalsIgnoreCase(ruleName));
    }

    public Optional<Rule> getRule(String ruleName) {
        return rules.stream()
                .filter(r -> r.getName().equalsIgnoreCase(ruleName))
                .findFirst();
    }

    public boolean setRuleEnabled(String ruleName, boolean enabled) {
        Optional<Rule> rule = getRule(ruleName);
        rule.ifPresent(r -> r.setEnabled(enabled));
        return rule.isPresent();
    }

    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public void clearRules() {
        rules.clear();
    }

    /**
     * Evaluates all enabled rules against the provided context.
     * Returns the count of rules that triggered and executed this cycle.
     */
    public int evaluateAll(RuleContext context) {
        if (context == null) return 0;
        evaluationCycles++;
        int triggeredCount = 0;

        for (Rule rule : rules) {
            if (rule.isEnabled()) {
                boolean executed = rule.evaluateAndExecute(context);
                if (executed) {
                    triggeredCount++;
                }
            }
        }
        return triggeredCount;
    }

    public int getEvaluationCycles() {
        return evaluationCycles;
    }

    /**
     * Exports all registered rules into a readable DSL script string for file saving.
     */
    public String exportScript() {
        StringBuilder sb = new StringBuilder();
        for (Rule rule : rules) {
            if (!rule.getSourceText().trim().isEmpty()) {
                sb.append(rule.getSourceText().trim()).append("\n\n");
            } else {
                sb.append(String.format("RULE \"%s\":\nWHEN %s\nTHEN %s\n\n",
                        rule.getName(), rule.getCondition(), rule.getAction()));
            }
        }
        return sb.toString();
    }
}
