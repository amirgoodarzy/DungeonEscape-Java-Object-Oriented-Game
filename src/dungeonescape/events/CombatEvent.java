package dungeonescape.events;

import dungeonescape.behaviors.DamageType;
import dungeonescape.entities.LivingEntity;

/**
 * Dispatched when an attack resolves and damage is dealt to a target entity.
 */
public class CombatEvent extends GameEvent {
    private static final long serialVersionUID = 1L;

    private final LivingEntity attacker;
    private final LivingEntity target;
    private final int damage;
    private final DamageType damageType;
    private final boolean targetDefeated;

    public CombatEvent(LivingEntity attacker, LivingEntity target, int damage, DamageType damageType, boolean targetDefeated) {
        super("COMBAT");
        this.attacker = attacker;
        this.target = target;
        this.damage = damage;
        this.damageType = damageType;
        this.targetDefeated = targetDefeated;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public int getDamage() {
        return damage;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public boolean isTargetDefeated() {
        return targetDefeated;
    }
}
