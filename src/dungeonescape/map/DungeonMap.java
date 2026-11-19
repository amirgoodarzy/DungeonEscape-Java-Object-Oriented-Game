package dungeonescape.map;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.enemies.Ghost;
import dungeonescape.entities.enemies.Zombie;
import dungeonescape.entities.npc.NPC;
import dungeonescape.entities.structures.Door;
import dungeonescape.entities.structures.Exit;
import dungeonescape.entities.structures.Trap;
import dungeonescape.entities.structures.Wall;
import dungeonescape.items.AttackPotion;
import dungeonescape.items.Coin;
import dungeonescape.items.HealthPotion;
import dungeonescape.items.Key;
import dungeonescape.items.Treasure;
import dungeonescape.items.Weapon;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the interconnected graph of dungeon rooms and coordinates active zone transitions.
 * Demonstrates:
 * - Composition (DungeonMap HAS-A Map<String, Room>)
 * - Modularity and world graph architecture
 * - Factory method pattern for dungeon initialization
 */
public class DungeonMap implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Room> rooms;
    private String currentRoomId;
    private final String startingRoomId;

    public DungeonMap(String startingRoomId) {
        this.rooms = new HashMap<>();
        this.startingRoomId = startingRoomId;
        this.currentRoomId = startingRoomId;
    }

    public void addRoom(Room room) {
        if (room != null) {
            rooms.put(room.getId(), room);
        }
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room getCurrentRoom() {
        return rooms.get(currentRoomId);
    }

    public void setCurrentRoomId(String roomId) {
        if (rooms.containsKey(roomId)) {
            this.currentRoomId = roomId;
        } else {
            throw new IllegalArgumentException("Room ID not found in dungeon map: " + roomId);
        }
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public String getStartingRoomId() {
        return startingRoomId;
    }

    public Map<String, Room> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    /**
     * Factory method that constructs the default 4-room connected dungeon graph.
     * Contains NPCs, multiple enemy types (Zombie, Ghost, Boss), items, locked doors, and secrets.
     */
    public static DungeonMap createCampaignDungeon(long seed) {
        return CampaignFactory.create(seed);
    }

    public static DungeonMap createDefaultDungeon() {
        DungeonMap dungeon = new DungeonMap("ROOM_START");

        // ----------------------------------------------------
        // ROOM 1: Entrance Hall (Safe haven & orientation)
        // ----------------------------------------------------
        Room startRoom = new Room("ROOM_START", "Entrance Hall");
        // Interior decorative pillars (Walls)
        startRoom.addEntity(new Wall(new Position(4, 4)));
        startRoom.addEntity(new Wall(new Position(4, 7)));

        // Peaceful NPC offering hints and lore
        NPC hermit = new NPC("Elder Cedric", new Position(3, 3), Arrays.asList(
                "Greetings, brave traveler. You have entered the Forsaken Labyrinth.",
                "To escape, seek the Golden Key hidden inside the haunted Treasury to the north.",
                "The door to the Overlord's lair is sealed tight. Only the Golden Key can unlock it.",
                "Beware the spectral Ghost... weapons of steel cannot easily pierce its ethereal veil!",
                "Remember: you can press 'R' anytime to define your own rules in the Rule Editor!"
        ));
        startRoom.addEntity(hermit);

        // Introductory items
        startRoom.addEntity(new Coin(new Position(7, 3), 10));
        startRoom.addEntity(new HealthPotion(new Position(8, 8), 30));

        // Door East to Corridor of Shadows (unlocked)
        Door doorToCorridor = new Door("Archway to Corridor", new Position(15, 6),
                "ROOM_CORRIDOR", new Position(1, 6), false, null);
        startRoom.addDoor(Direction.RIGHT, doorToCorridor);
        dungeon.addRoom(startRoom);

        // ----------------------------------------------------
        // ROOM 2: Corridor of Shadows (Hostile zone)
        // ----------------------------------------------------
        Room corridor = new Room("ROOM_CORRIDOR", "Corridor of Shadows");
        // Door West back to Entrance Hall (unlocked)
        Door doorToStart = new Door("Door to Entrance", new Position(0, 6),
                "ROOM_START", new Position(14, 6), false, null);
        corridor.addDoor(Direction.LEFT, doorToStart);

        // Door North to Treasury (unlocked)
        Door doorToTreasury = new Door("Vault Gate", new Position(8, 0),
                "ROOM_TREASURY", new Position(8, 10), false, null);
        corridor.addDoor(Direction.UP, doorToTreasury);

        // Door East to Boss Chamber (LOCKED with Golden Key!)
        Door bossDoor = new Door("Gilded Skull Door", new Position(15, 6),
                "ROOM_BOSS", new Position(1, 6), true, "Golden Key");
        corridor.addDoor(Direction.RIGHT, bossDoor);

        // Obstacle pillars and traps
        corridor.addEntity(new Wall(new Position(5, 3)));
        corridor.addEntity(new Wall(new Position(5, 8)));
        corridor.addEntity(new Wall(new Position(11, 3)));
        corridor.addEntity(new Wall(new Position(11, 8)));
        corridor.addEntity(new Trap(new Position(8, 5), 15));

        // Enemies: Relentless Zombies
        corridor.addEntity(new Zombie(new Position(10, 4)));
        corridor.addEntity(new Zombie(new Position(12, 7)));

        // Collectibles
        corridor.addEntity(new Coin(new Position(6, 6), 15));
        corridor.addEntity(new AttackPotion(new Position(2, 9), 4));
        dungeon.addRoom(corridor);

        // ----------------------------------------------------
        // ROOM 3: The Treasury (Guarded by Ghost)
        // ----------------------------------------------------
        Room treasury = new Room("ROOM_TREASURY", "The Ancient Treasury");
        // Door South back to Corridor
        Door doorBackCorridor = new Door("Corridor Hatch", new Position(8, 11),
                "ROOM_CORRIDOR", new Position(8, 1), false, null);
        treasury.addDoor(Direction.DOWN, doorBackCorridor);

        // Treasury interior vaults (interior wall labyrinth)
        for (int x = 3; x <= 6; x++) {
            treasury.addEntity(new Wall(new Position(x, 4)));
        }
        for (int x = 9; x <= 12; x++) {
            treasury.addEntity(new Wall(new Position(x, 7)));
        }

        // Hostile apparition: Ghost (phases through walls!)
        treasury.addEntity(new Ghost(new Position(12, 3)));

        // Prized rewards: The Golden Key!
        treasury.addEntity(new Key("Golden Key", new Position(13, 2), "Golden Key"));
        treasury.addEntity(new Treasure("Emperor's Crown", new Position(3, 2), 200));
        treasury.addEntity(new Weapon("Dragonfang Broadsword", new Position(4, 8), 12));
        treasury.addEntity(new Coin(new Position(11, 9), 25));
        treasury.addEntity(new HealthPotion(new Position(2, 9), 40));
        dungeon.addRoom(treasury);

        // ----------------------------------------------------
        // ROOM 4: The Boss Chamber (Final encounter & Exit)
        // ----------------------------------------------------
        Room bossRoom = new Room("ROOM_BOSS", "Sanctum of the Overlord");
        // Door West back to Corridor
        Door doorBackFromBoss = new Door("Heavy Iron Gate", new Position(0, 6),
                "ROOM_CORRIDOR", new Position(14, 6), false, null);
        bossRoom.addDoor(Direction.LEFT, doorBackFromBoss);

        // Boss arena pillars
        bossRoom.addEntity(new Wall(new Position(4, 3)));
        bossRoom.addEntity(new Wall(new Position(4, 8)));
        bossRoom.addEntity(new Wall(new Position(11, 3)));
        bossRoom.addEntity(new Wall(new Position(11, 8)));

        // Floor traps in arena
        bossRoom.addEntity(new Trap(new Position(7, 4), 12));
        bossRoom.addEntity(new Trap(new Position(7, 7), 12));

        // The Dungeon Boss
        bossRoom.addEntity(new Boss(new Position(12, 6)));

        // The Extraction Portal (Exit) at the back of the chamber
        bossRoom.addEntity(new Exit(new Position(14, 6)));

        // Emergency potion inside boss chamber
        bossRoom.addEntity(new HealthPotion(new Position(2, 2), 50));
        dungeon.addRoom(bossRoom);

        return dungeon;
    }
}
