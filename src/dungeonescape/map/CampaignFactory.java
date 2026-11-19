package dungeonescape.map;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.enemies.*;
import dungeonescape.entities.npc.NPC;
import dungeonescape.entities.structures.*;
import dungeonescape.items.*;
import java.util.Arrays;
import java.util.Random;

/** Reproducible, 15-room, three-act campaign factory with randomized enemy/loot placements. */
public final class CampaignFactory {
    private CampaignFactory() {}

    public static DungeonMap create(long seed) {
        Random random = new Random(seed);
        DungeonMap dungeon = new DungeonMap("F1_HAVEN");
        String[][] ids = {
            {"F1_HAVEN", "F1_CROSSROADS", "F1_CRYPT", "F1_ARMORY", "F1_GUARDIAN"},
            {"F2_CAMP", "F2_FORGE", "F2_LIBRARY", "F2_GARDEN", "F2_WARDEN"},
            {"F3_OUTPOST", "F3_CATACOMBS", "F3_SHROUD", "F3_RELIQUARY", "F3_THRONE"}
        };
        String[][] names = {
            {"Emberlight Haven", "Fallen Crossroads", "The Sunken Crypt", "Forgotten Armory", "Gate of the Fallen"},
            {"Ashen Refuge", "Molten Forge", "Frozen Library", "Poisoned Garden", "Citadel of Ash"},
            {"Starlit Outpost", "Whispering Catacombs", "The Dark Shroud", "Royal Reliquary", "The Eternal Throne"}
        };
        for (int act = 0; act < 3; act++) {
            for (int i = 0; i < 5; i++) {
                Room room = new Room(ids[act][i], names[act][i]);
                dungeon.addRoom(room);
                decorate(room, random, act, i);
            }
            link(dungeon, ids[act][0], Direction.RIGHT, ids[act][1], null);
            link(dungeon, ids[act][1], Direction.UP, ids[act][2], null);
            link(dungeon, ids[act][1], Direction.DOWN, ids[act][3], null);
            String key = new String[] {"Ember Sigil", "Frost Sigil", "Void Sigil"}[act];
            link(dungeon, ids[act][1], Direction.RIGHT, ids[act][4], key);
            Room hidden = dungeon.getRoom(ids[act][2]);
            hidden.addEntity(new Key(key, new Position(12, 3), key));
            hidden.addEntity(new Treasure(new String[] {"Ancient Relic", "Forgotten Manuscript", "Celestial Crown"}[act], new Position(3, 3), 100 + act * 100));
            Room weaponRoom = dungeon.getRoom(ids[act][3]);
            weaponRoom.addEntity(new Weapon(new String[] {"Forged Saber", "Glacier Blade", "Starfall Greatsword"}[act],
                    new Position(12, 8), 8 + act * 9));
            weaponRoom.addEntity(new Armor(new String[] {"Scout's Vest", "Dragonhide Armor", "Celestial Plate"}[act],
                    new Position(3, 8), 2 + act * 3));
            Room bossRoom = dungeon.getRoom(ids[act][4]);
            Boss boss = new Boss(new Position(11, 5),
                    new String[] {"The Bone Sentinel", "The Ash Warden", "The Void Sovereign"}[act],
                    105 + act * 65, 15 + act * 6, 5 + act * 3);
            bossRoom.addEntity(boss);
            bossRoom.addEntity(new HealthPotion(new Position(3, 8), 45 + act * 15));
            if (act == 2) {
                bossRoom.addEntity(new Exit(new Position(14, 6)));
            }
        }
        // Connect acts after all rooms exist (otherwise a target room may be null).
        link(dungeon, "F1_GUARDIAN", Direction.RIGHT, "F2_CAMP", null);
        link(dungeon, "F2_WARDEN", Direction.RIGHT, "F3_OUTPOST", null);
        dungeon.getRoom("F1_HAVEN").addEntity(new NPC("Elder Cedric", new Position(3, 3), Arrays.asList(
            "Find the Ember Sigil in the northern crypt. A guardian blocks the next act.",
            "WASD to move, SPACE to fight, Q for a spin attack, F for a firebolt, H to heal.",
            "Visit both side rooms for keys and gear. Press M to see your campaign map.",
            "F1, F2, F3 each have a sigil and a guardian. Slay all three to escape.")));
        dungeon.getRoom("F1_HAVEN").addEntity(new Shrine(new Position(11, 5)));
        dungeon.getRoom("F1_HAVEN").addEntity(new HealthPotion(new Position(10, 8), 45));
        dungeon.getRoom("F1_HAVEN").addEntity(new Coin(new Position(7, 4), 30));
        for (int act = 1; act < 3; act++) {
            Room camp = dungeon.getRoom(ids[act][0]);
            camp.addEntity(new Shrine(new Position(11, 5)));
            camp.addEntity(new HealthPotion(new Position(9, 8), 60));
            camp.addEntity(new EnergyPotion(new Position(12, 3), 65));
            camp.addEntity(new NPC("Wandering Sage", new Position(3, 3), Arrays.asList(
                    "Your skills consume energy. Energy regenerates even when you are not fighting.",
                    "Collect the sigil in the north chamber before challenging the gatekeeper.")));
        }
        return dungeon;
    }

    private static void decorate(Room room, Random rng, int act, int index) {
        // A handful of corners are varied; the central corridors, doors and key positions remain open.
        if (index != 0) {
            Position[] possible = {new Position(4, 4), new Position(11, 4), new Position(4, 9), new Position(11, 9)};
            for (Position pos : possible) if (rng.nextBoolean()) room.addEntity(new Wall(pos));
            if (index != 4) {
                room.addEntity(new Coin(new Position(6, 2), 12 + 12 * act + rng.nextInt(15)));
                room.addEntity(new HealthPotion(new Position(2, 9), 30 + act * 10));
                if (act > 0) room.addEntity(new EnergyPotion(new Position(13, 9), 30 + act * 10));
                int enemies = 1 + act + (index == 1 ? 1 : 0);
                Position[] spawns = {new Position(10, 4), new Position(12, 7), new Position(6, 8), new Position(7, 3)};
                for (int n = 0; n < enemies && n < spawns.length; n++) {
                    int kind = (act * 3 + index + n + rng.nextInt(3)) % 4;
                    room.addEntity(EnemyFactory.create(kind, spawns[n], act));
                }
                if (index == 3) room.addEntity(new Trap(new Position(8, 5), 10 + act * 5));
            } else {
                room.addEntity(new Trap(new Position(8, 8), 10 + act * 4));
                room.addEntity(new Trap(new Position(6, 4), 12 + act * 4));
            }
        }
    }

    private static void link(DungeonMap map, String a, Direction dir, String b, String forwardKey) {
        Direction back = dir.opposite();
        map.getRoom(a).addDoor(dir, new Door("Gateway to " + map.getRoom(b).getName(), edge(dir), b, inside(back), forwardKey != null, forwardKey));
        map.getRoom(b).addDoor(back, new Door("Gateway to " + map.getRoom(a).getName(), edge(back), a, inside(dir), false, null));
    }

    private static Position edge(Direction d) {
        switch (d) {
            case UP: return new Position(8, 0);
            case DOWN: return new Position(8, 11);
            case LEFT: return new Position(0, 6);
            default: return new Position(15, 6);
        }
    }
    private static Position inside(Direction entry) {
        switch (entry) {
            case UP: return new Position(8, 1);
            case DOWN: return new Position(8, 10);
            case LEFT: return new Position(1, 6);
            default: return new Position(14, 6);
        }
    }
}
