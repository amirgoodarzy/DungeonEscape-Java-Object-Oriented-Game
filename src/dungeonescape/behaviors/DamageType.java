package dungeonescape.behaviors;

import java.io.Serializable;

/**
 * Categorizes types of damage dealt in combat.
 * Used to demonstrate method overloading (takeDamage(int) vs takeDamage(int, DamageType))
 * and specialized resistances or behavioral reactions.
 */
public enum DamageType implements Serializable {
    PHYSICAL,
    SPECTRAL,
    TRAP,
    TRUE_DAMAGE
}
