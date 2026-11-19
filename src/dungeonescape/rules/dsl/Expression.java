package dungeonescape.rules.dsl;

import dungeonescape.rules.RuleContext;
import java.io.Serializable;

/**
 * Generic root interface for Abstract Syntax Tree (AST) expression nodes.
 * Demonstrates:
 * - Parametric Polymorphism via Java Generics (Expression<T>)
 * - Abstraction of language grammar nodes
 * - Interpreter pattern
 * 
 * @param <T> The evaluated return type (Boolean, Double, String, Object).
 */
public interface Expression<T> extends Serializable {
    /**
     * Evaluates this AST node against the provided execution context.
     * 
     * @param context Game world state and entity access bridge.
     * @return Evaluated result of type T.
     */
    T evaluate(RuleContext context);
}
