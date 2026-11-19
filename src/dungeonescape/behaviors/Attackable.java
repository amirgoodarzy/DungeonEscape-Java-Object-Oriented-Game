package dungeonescape.behaviors;

import dungeonescape.entities.LivingEntity;

/**
 * Behavioral interface for entities capable of initiating combat actions.
 * Demonstrates multiple typing and method overloading at the interface level.
 */
public interface Attackable {
    /**
     * Executes a standard attack against a living target.
     * 
     * @param target The living entity being attacked.
     */
    void attack(LivingEntity target);

    /**
     * Executes an empowered attack with bonus damage.
     * Demonstrates method overloading on the interface level.
     * 
     * @param target The living entity being attacked.
     * @param bonusDamage Extra damage added to the standard attack power.
     */
    void attack(LivingEntity target, int bonusDamage);
}
