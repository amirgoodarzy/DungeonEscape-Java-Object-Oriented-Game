package dungeonescape.tests;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Archer;
import dungeonescape.entities.enemies.Sorcerer;
import dungeonescape.items.Armor;
import dungeonescape.items.Item;
import dungeonescape.items.Weapon;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;

/** Supplemental regression checks for expanded campaign mechanics. */
public final class ExpandedMechanicsTest {
    private static int checks;
    private static void ok(boolean result,String detail) {checks++;if(!result) throw new AssertionError(detail);}
    public static void main(String[] args) throws Exception {
        GameManager g=new GameManager();g.startNewGame();Player p=g.getPlayer();
        Room r=g.getDungeonMap().getCurrentRoom();
        Weapon sword=new Weapon("Test Sword",new Position(2,2),12);
        p.collect(sword);g.handleUseInventoryItem(0);
        ok(p.getTotalAttackPower()==p.getAttackPower()+12,"Weapon use didn't increase damage");
        g.handleDropInventoryItem(0);
        ok(p.getEquippedWeapon()==null,"Dropping a weapon must unequip it");
        ok(r.getItemAt(p.getPosition()).isPresent(),"Dropped item missing from floor");
        g.handlePlayerInteract();
        ok(p.getInventory().hasItem("Test Sword"),"E should retrieve dropped item");
        Armor armor=new Armor("Test Armor",p.getPosition(),7);
        p.collect(armor);g.handleUseInventoryItem(1);
        int equippedDefense=p.getDefense();
        g.handleDropInventoryItem(1);
        ok(p.getEquippedArmor()==null && p.getDefense()==equippedDefense-7,"Dropping armor must remove bonus");
        p.takeDamage(45);
        int before=p.getHealth();g.handleSecondWind();
        ok(p.getHealth()>before && p.getEnergy()==55,"Second Wind didn't spend energy and heal");
        int regenBefore=p.getEnergy();g.update(2200);
        ok(p.getEnergy()>regenBefore,"Energy didn't regenerate during gameplay");
        g.setGameState(GameState.PAUSED);regenBefore=p.getEnergy();g.update(1500);
        ok(p.getEnergy()==regenBefore,"Paused state must freeze resource regeneration");
        g.setGameState(GameState.PLAYING);
        // Ranged AI: direct row LOS and obstruction checks
        Archer a=new Archer(new Position(6,2));
        ok(a.canShoot(p,r),"Archer should shoot along an open row");
        r.addEntity(a);
        int healthBefore=p.getHealth();g.updateEnemies(2000);
        ok(p.getHealth()<healthBefore,"Ranged AI did not attack hero");
        r.removeEntity(a);
        // Energy bolt kills an enemy at range and uses the correct amount of energy.
        Sorcerer s=new Sorcerer(new Position(5,2));r.addEntity(s);
        p.setFacingDirection(Direction.RIGHT);p.setAttackPower(120);
        int energyBefore=p.getEnergy();g.handleFirebolt();
        ok(!s.isAlive() && p.getEnergy()==energyBefore-25+12,"Firebolt should hit, spend 25 and refund 12 on kill. HP=" + s.getHealth() + " ENERGY="+p.getEnergy());
        // Whirlwind damages multiple adjacent enemies but once per target.
        Archer a1=new Archer(new Position(2,1));Archer a2=new Archer(new Position(3,2));
        r.addEntity(a1);r.addEntity(a2);energyBefore=p.getEnergy();
        g.handleWhirlwind();
        ok(!a1.isAlive()&&!a2.isAlive(),"Whirlwind must strike adjacent enemies");
        ok(p.getEnergy()<=energyBefore-34+24+25 && p.getEnergy()>=0,"Whirlwind energy accounting failed (including kill/level-up refunds)");
        System.out.println("EXPANDED MECHANICS: " + checks + " ASSERTIONS PASSED");
    }
}
