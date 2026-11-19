package dungeonescape.entities;

import dungeonescape.behaviors.Attackable;
import dungeonescape.behaviors.DamageType;
import dungeonescape.behaviors.Direction;
import dungeonescape.behaviors.Movable;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.items.Armor;
import dungeonescape.items.Item;
import dungeonescape.items.Weapon;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

/**
 * The user-controlled hero.
 * Demonstrates:
 * - Inheritance from LivingEntity
 * - Multiple typing via Movable and Attackable
 * - Composition (HAS-A Inventory, HAS-A equipped Weapon)
 * - Encapsulation of score, coins, and equipment state
 * - Method Overloading in attack()
 */
public class Player extends LivingEntity implements Movable, Attackable {
    private static final long serialVersionUID = 1L;

    private int score;
    private int coins;
    private int relicsCollected;
    private final Inventory inventory;
    private Weapon equippedWeapon;
    private Direction facingDirection;
    private Armor equippedArmor;
    private int level = 1;
    private int experience = 0;
    private int energy = 100;
    private int maxEnergy = 100;
    private long energyRegenerationClock;
    private final Set<String> keyring = new HashSet<>();


    public Player(Position position) {
        super("Adventurer", position,
                Constants.PLAYER_MAX_HEALTH,
                Constants.PLAYER_BASE_ATTACK,
                Constants.PLAYER_BASE_DEFENSE);
        this.score = 0;
        this.coins = 0;
        this.inventory = new Inventory(Constants.PLAYER_INVENTORY_CAPACITY);
        this.equippedWeapon = null;
        this.facingDirection = Direction.DOWN;
    }

    public boolean hasKey(String name) { return name != null && keyring.contains(name); }
    public void storeKey(String keyId) { if (keyId != null) keyring.add(keyId); }
    public Set<String> getKeys() { return java.util.Collections.unmodifiableSet(keyring); }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExperienceRequired() { return 60 + (level - 1) * 45; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public Armor getEquippedArmor() { return equippedArmor; }
    public boolean spendEnergy(int cost) { if (energy < cost || cost < 0) return false; energy -= cost; return true; }
    public int restoreEnergy(int amount) { int gained = Math.min(maxEnergy - energy, Math.max(0, amount)); energy += gained; return gained; }
    /** XP levels award health, strength, armor and capacity, preserving progress after save. */
    public boolean gainExperience(int amount) {
        experience += Math.max(0, amount);
        boolean leveled = false;
        while (experience >= getExperienceRequired()) {
            experience -= getExperienceRequired();
            level++;
            setMaxHealth(getMaxHealth() + 15);
            heal(15);
            setAttackPower(getAttackPower() + 2);
            setDefense(getDefense() + 1);
            maxEnergy += 5;
            restoreEnergy(25);
            leveled = true;
        }
        return leveled;
    }
    public void equipArmor(Armor armor) {
        if (equippedArmor != null) { setDefense(getDefense() - equippedArmor.getDefenseBonus()); equippedArmor.setEquipped(false); }
        equippedArmor = armor;
        if (armor != null) { setDefense(getDefense() + armor.getDefenseBonus()); armor.setEquipped(true); }
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public int getCoins() {
        return coins;
    }

    public int getRelicsCollected() { return relicsCollected; }
    public void addRelic() { relicsCollected++; }
    public boolean spendCoins(int amount) {
        if (amount < 0 || amount > coins) return false;
        coins -= amount;
        return true;
    }
    public void addCoins(int amount) {
        if (amount > 0) {
            this.coins += amount;
        }
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        if (facingDirection != null) {
            this.facingDirection = facingDirection;
        }
    }

    /**
     * Calculates the player's total attack rating including equipped weapon bonuses.
     */
    public int getTotalAttackPower() {
        int total = getAttackPower();
        if (equippedWeapon != null) {
            total += equippedWeapon.getAttackBonus();
        }
        return total;
    }

    /**
     * Equips a weapon, managing inventory transitions.
     */
    public void equipWeapon(Weapon weapon) {
        if (this.equippedWeapon != null) {
            this.equippedWeapon.setEquipped(false);
        }
        this.equippedWeapon = weapon;
        if (weapon != null) {
            weapon.setEquipped(true);
        }
    }

    /**
     * Unequips the currently held weapon.
     */
    public void unequipWeapon() {
        if (this.equippedWeapon != null) {
            this.equippedWeapon.setEquipped(false);
            this.equippedWeapon = null;
        }
    }

    /**
     * Attempts to collect an item into inventory or apply immediate effects.
     */
    public String collect(Item item) throws InventoryFullException {
        if (item == null) return "";
        return item.onCollect(this);
    }

    @Override
    public void move(Direction direction) {
        if (direction != null) {
            this.facingDirection = direction;
            setPosition(getPosition().translate(direction));
        }
    }

    @Override
    public void attack(LivingEntity target) {
        attack(target, 0);
    }

    /**
     * Overloaded attack method delivering empowered damage.
     * Demonstrates Method Overloading.
     */
    @Override
    public void attack(LivingEntity target, int bonusDamage) {
        if (target == null || !target.isAlive()) return;

        int power = getTotalAttackPower() + bonusDamage;
        target.takeDamage(power, DamageType.PHYSICAL);
        AudioPlayer.playAttackSound();
    }

    @Override
    public void update(long deltaMs) {
        // Passive energy regeneration (2 points / 550ms), only called while actively playing.
        energyRegenerationClock += Math.max(0, deltaMs);
        while (energyRegenerationClock >= 550) {
            restoreEnergy(2);
            energyRegenerationClock -= 550;
        }
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Shadow
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(px + 8, py + tileSize - 10, tileSize - 16, 8);

        // Body tunic (Hero Blue)
        g.setColor(new Color(45, 120, 220));
        g.fillRoundRect(px + 10, py + 12, tileSize - 20, tileSize - 18, 8, 8);

        // Armor plate / belt
        g.setColor(new Color(180, 140, 50));
        g.fillRect(px + 10, py + 26, tileSize - 20, 5);

        // Head
        g.setColor(new Color(255, 218, 185)); // Peach skin
        g.fillOval(px + 14, py + 6, tileSize - 28, tileSize - 28);

        // Hair / Helmet
        g.setColor(new Color(100, 60, 20));
        g.fillArc(px + 13, py + 4, tileSize - 26, 16, 0, 180);

        // Eyes oriented toward facing direction
        g.setColor(Color.BLACK);
        int eyeX = px + tileSize / 2;
        int eyeY = py + 14;
        if (facingDirection == Direction.LEFT) {
            g.fillRect(eyeX - 6, eyeY, 2, 3);
            g.fillRect(eyeX - 1, eyeY, 2, 3);
        } else if (facingDirection == Direction.RIGHT) {
            g.fillRect(eyeX, eyeY, 2, 3);
            g.fillRect(eyeX + 5, eyeY, 2, 3);
        } else if (facingDirection == Direction.UP) {
            // Back of head visible, no eyes
        } else {
            // Facing DOWN
            g.fillRect(eyeX - 4, eyeY, 2, 3);
            g.fillRect(eyeX + 2, eyeY, 2, 3);
        }

        // Weapon indicator if equipped
        if (equippedWeapon != null) {
            g.setColor(new Color(230, 230, 255));
            g.fillRect(px + tileSize - 12, py + 18, 4, 14);
            g.setColor(Color.YELLOW);
            g.fillRect(px + tileSize - 14, py + 24, 8, 3);
        }

        // Mini health bar above player head
        renderHealthBar(g, px, py, tileSize);
    }

    private void renderHealthBar(Graphics2D g, int px, int py, int tileSize) {
        int barW = tileSize - 12;
        int barH = 4;
        int barX = px + 6;
        int barY = py + 1;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);

        int filledW = (int) Math.round(barW * getHealthRatio());
        g.setColor(getHealthRatio() > 0.3 ? new Color(50, 220, 80) : new Color(240, 50, 50));
        g.fillRect(barX, barY, filledW, barH);
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barW, barH);
    }
}
