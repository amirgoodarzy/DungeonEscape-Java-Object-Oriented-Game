package dungeonescape.managers;

import dungeonescape.behaviors.Attackable;
import dungeonescape.behaviors.DamageType;
import dungeonescape.entities.LivingEntity;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.events.CombatEvent;
import dungeonescape.events.EventBus;
import dungeonescape.map.Room;
import dungeonescape.utils.AudioPlayer;
import java.io.Serializable;

/**
 * Manages combat resolution between hostile and friendly living entities.
 * Demonstrates:
 * - Direct peer-to-peer entity interaction (OOP core guideline)
 * - Method Overloading: resolveAttack(Attackable, LivingEntity) vs resolveAttack(Attackable, LivingEntity, int)
 * - Inclusion Polymorphism: operates uniformly across any Attackable attacker and LivingEntity target
 * - Event dispatching via EventBus
 */
public class CombatManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private final EventBus eventBus;

    public CombatManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Standard attack resolution without bonus damage.
     * Delegates to overloaded resolveAttack with 0 bonus damage.
     */
    public boolean resolveAttack(Attackable attacker, LivingEntity target, Room room, Player player) {
        return resolveAttack(attacker, target, 0, room, player);
    }

    /**
     * Overloaded attack resolution factoring in bonus damage.
     * Demonstrates Method Overloading.
     */
    public boolean resolveAttack(Attackable attacker, LivingEntity target, int bonusDamage, Room room, Player player) {
        if (attacker == null || target == null || !target.isAlive()) {
            return false;
        }

        int healthBefore = target.getHealth();
        // Dynamic dispatch: executes attacker's specific attack implementation
        attacker.attack(target, bonusDamage);
        int damageDealt = healthBefore - target.getHealth();
        boolean defeated = !target.isAlive();

        // If target was an enemy and was defeated, remove from room and grant score
        if (defeated && target instanceof Enemy && room != null) {
            room.removeEntity(target);
            AudioPlayer.playTone(220, 200, 0.6); // Defeat rumble

            if (player != null) {
                int bounty = (target instanceof Boss) ? 500 : 50;
                player.addScore(bounty);
                player.addCoins(target instanceof Boss ? 60 : 6);
                player.gainExperience(target instanceof Boss ? 125 : Math.max(20, target.getMaxHealth() / 2));
                player.restoreEnergy(12);
            }
        }

        // Publish combat event to EventBus for rule engine and telemetry
        if (eventBus != null) {
            LivingEntity attackerEntity = (attacker instanceof LivingEntity) ? (LivingEntity) attacker : null;
            eventBus.publish(new CombatEvent(attackerEntity, target, damageDealt, DamageType.PHYSICAL, defeated));
        }

        return defeated;
    }
}
