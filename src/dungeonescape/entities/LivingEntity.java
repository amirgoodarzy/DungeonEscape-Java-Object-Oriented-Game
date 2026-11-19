package dungeonescape.entities;

import dungeonescape.behaviors.DamageType;
import dungeonescape.map.Position;

/**
 * Common abstract superclass for all mortal, animate beings in the dungeon.
 * Demonstrates:
 * - Encapsulation (private health with validated mutators enforcing invariants)
 * - Reuse across Player, Enemy, and NPC
 * - Method Overloading (takeDamage(int) vs takeDamage(int, DamageType))
 * - Coercion in percentage-based defense mitigations
 */
public abstract class LivingEntity extends Entity {
    private static final long serialVersionUID = 1L;

    private int health;
    private int maxHealth;
    private int attackPower;
    private int defense;

    public LivingEntity(String name, Position position, int maxHealth, int attackPower, int defense) {
        super(name, position, false); // Living entities block movement by default
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be strictly positive: " + maxHealth);
        }
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attackPower = Math.max(0, attackPower);
        this.defense = Math.max(0, defense);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        this.maxHealth = maxHealth;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = Math.max(0, attackPower);
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = Math.max(0, defense);
    }

    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Standard damage application without damage type specialization.
     * Delegates to overloaded takeDamage with PHYSICAL damage type.
     * 
     * @param amount Raw incoming damage.
     * @return Actual damage sustained after defense mitigation.
     */
    public int takeDamage(int amount) {
        return takeDamage(amount, DamageType.PHYSICAL);
    }

    /**
     * Overloaded damage application factoring in damage type and defensive mitigation.
     * Demonstrates Method Overloading, Coercion (widening int to double for armor mitigation),
     * and Encapsulation invariants (health never drops below 0).
     * 
     * @param amount Raw incoming damage.
     * @param type The classification of damage being inflicted.
     * @return Net damage subtracted from current health.
     */
    public int takeDamage(int amount, DamageType type) {
        if (amount <= 0 || !isAlive()) {
            return 0;
        }

        int netDamage;
        switch (type) {
            case PHYSICAL:
                // Coercion: Integer values promoted to double for percentage calculation
                double reduction = (double) defense / (defense + 20.0);
                netDamage = (int) Math.round(amount * (1.0 - reduction));
                netDamage = Math.max(1, netDamage); // At least 1 damage on physical hit
                break;
            case SPECTRAL:
                // Spectral attacks partially ignore physical armor
                netDamage = Math.max(1, amount - (defense / 3));
                break;
            case TRAP:
            case TRUE_DAMAGE:
            default:
                netDamage = amount; // Pure damage ignores armor
                break;
        }

        this.health = Math.max(0, this.health - netDamage);
        return netDamage;
    }

    /**
     * Restores health points, strictly clamped to maxHealth.
     * Demonstrates Encapsulation of class invariants.
     * 
     * @param amount Points of health to restore.
     * @return Effective amount of health restored.
     */
    public int heal(int amount) {
        if (amount <= 0 || !isAlive()) {
            return 0;
        }
        int before = this.health;
        this.health = Math.min(this.maxHealth, this.health + amount);
        return this.health - before;
    }

    /**
     * Calculates current health ratio as a floating point value between 0.0 and 1.0.
     * Demonstrates primitive numeric widening coercion.
     */
    public double getHealthRatio() {
        return (double) this.health / this.maxHealth;
    }
}
