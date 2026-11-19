package dungeonescape.tests;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.structures.Door;
import dungeonescape.exceptions.GameException;
import dungeonescape.items.Key;
import dungeonescape.items.Weapon;
import dungeonescape.managers.CollisionManager;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;
import dungeonescape.map.DungeonMap;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.rules.Rule;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/** Integration tests: real movement, doors, inventory, AI, rule events, persistence and victory. */
public final class GameplayRegressionTest {
    private static int passed;

    public static void main(String[] args) throws Exception {
        testAllDoorsTraversable();
        testRuleAtomicityAndReset();
        testSaveLoadGameRulesAndTurns();
        testEnemyCooldownAndPause();
        testCompleteCampaign();
        System.out.println("GAMEPLAY REGRESSIONS: " + passed + " PASSED, 0 FAILED");
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void passed(String name) {
        passed++;
        System.out.println("[PASS] " + name);
    }

    private static void testAllDoorsTraversable() {
        DungeonMap dungeon = DungeonMap.createDefaultDungeon();
        CollisionManager collisions = new CollisionManager();
        for (Room room : dungeon.getRooms().values()) {
            for (Direction direction : Direction.values()) {
                Door door = room.getDoor(direction);
                if (door == null) continue;
                Position p = door.getPosition();
                check(room.isWalkable(p.getX(), p.getY()), "Door embedded in wall: " + door.getName());
                check(dungeon.getRoom(door.getTargetRoomId()) != null, "Door leads nowhere: " + door.getName());
                check(!door.getTargetPlayerPosition().equals(p) || !door.getTargetRoomId().equals(room.getId()),
                        "Door transition doesn't go anywhere");
                Player player = new Player(p.translate(direction.opposite()));
                if (!door.isLocked()) check(collisions.canMoveTo(player, p, room),
                        "Unlocked door blocked by collision: " + door.getName());
                else check(!collisions.canMoveTo(player, p, room), "Locked door unexpectedly walkable");
            }
        }
        passed("All perimeter doors, targets and lock collisions");
    }

    private static void testRuleAtomicityAndReset() throws GameException {
        GameManager gm = new GameManager();
        // Editor is usable in main menu; authored rules must survive New Game.
        gm.getRuleEngine().registerRuleFromSource(
                "RULE \"Quick Start\": WHEN player.coins >= 0 THEN player.attack += 4");
        gm.startClassicGame();
        Rule rule = gm.getRuleEngine().getRule("Quick Start").orElseThrow(AssertionError::new);
        int originalAttack = gm.getPlayer().getAttackPower();
        gm.handlePlayerMove(Direction.UP);
        check(gm.getPlayer().getAttackPower() == originalAttack + 4, "Rule didn't execute on movement");
        check(rule.getExecutionCount() == 1, "One move generated duplicated rule executions");
        gm.handlePlayerMove(Direction.DOWN);
        check(rule.getExecutionCount() == 1, "Always-true rule stacked its buff every move");
        gm.startClassicGame();
        check(gm.getRuleEngine().getRule("Quick Start").isPresent(), "Starting a new game erased custom rule");
        check(gm.getRuleEngine().getRule("Quick Start").get().getExecutionCount() == 0,
                "Restart kept stale rule execution counts");
        check(gm.getRuleEngine().getRules().size() == 3, "Restart duplicated default rules");
        passed("Rule events coalesced, no buff stacking, editor rules persist on restart");
    }

    private static void testSaveLoadGameRulesAndTurns() throws Exception {
        GameManager game = new GameManager();
        game.startClassicGame();
        game.getRuleEngine().registerRuleFromSource(
                "RULE \"Save Me\": WHEN player.coins >= 0 THEN player.defense += 7");
        game.handlePlayerMove(Direction.UP);
        int defense = game.getPlayer().getDefense();
        int turns = game.getTurnCount();
        check(turns == 1, "Turn counter did not advance");

        File file = File.createTempFile("dungeon_progress_regression_", ".dat");
        file.deleteOnExit();
        game.saveGame(file);
        GameManager restored = new GameManager();
        restored.loadGame(file);
        check(restored.getTurnCount() == turns, "Save/load lost turn count");
        check(restored.getRuleEngine().getRule("Save Me").isPresent(), "Save/load lost custom rules");
        check(restored.getPlayer().getDefense() == defense, "Save/load lost player defense");
        restored.handlePlayerMove(Direction.DOWN);
        check(restored.getPlayer().getDefense() == defense, "Save/load re-executed a spent rule");
        check(restored.getRuleEngine().getRule("Save Me").get().getExecutionCount() == 1,
                "Save/load lost execution history");
        passed("Save/load restores rules, states and turn count");
    }

    private static void testEnemyCooldownAndPause() {
        GameManager game = new GameManager();
        game.startClassicGame();
        game.getDungeonMap().setCurrentRoomId("ROOM_CORRIDOR");
        game.getPlayer().setPosition(new Position(10, 3)); // Zombie is adjacent at 10,4.
        int hp = game.getPlayer().getHealth();
        game.update(16);
        check(game.getPlayer().getHealth() == hp, "Enemy attacked without waiting for cooldown");
        game.update(600);
        int hpAfterAttack = game.getPlayer().getHealth();
        check(hpAfterAttack < hp, "Enemy never attacked after enough elapsed time");
        game.update(16);
        check(game.getPlayer().getHealth() == hpAfterAttack, "Enemy attacked on every frame");
        game.setGameState(GameState.PAUSED);
        game.update(3000);
        check(game.getPlayer().getHealth() == hpAfterAttack, "Enemy attacked while paused");
        passed("Enemy real-time cooldown and pause freeze");
    }

    /** Uses only public game input, so we test the same movement/combat the player sees. */
    private static void testCompleteCampaign() {
        GameManager game = new GameManager();
        game.startClassicGame();
        walkTo(game, new Position(15, 6));
        check("ROOM_CORRIDOR".equals(game.getDungeonMap().getCurrentRoomId()), "Cannot leave entrance");
        walkTo(game, new Position(8, 0));
        check("ROOM_TREASURY".equals(game.getDungeonMap().getCurrentRoomId()), "Cannot enter treasury");

        walkTo(game, new Position(13, 2));
        check(game.getPlayer().hasKey("Golden Key"), "Golden Key cannot be collected");
        walkTo(game, new Position(4, 8));
        int weaponSlot = -1;
        for (int i = 0; i < game.getPlayer().getInventory().size(); i++) {
            if (game.getPlayer().getInventory().get(i) instanceof Weapon) weaponSlot = i;
        }
        check(weaponSlot >= 0, "Cannot collect the sword");
        game.handleUseInventoryItem(weaponSlot);
        check(game.getPlayer().getEquippedWeapon() != null, "Cannot equip collected sword");

        walkTo(game, new Position(8, 11));
        check("ROOM_CORRIDOR".equals(game.getDungeonMap().getCurrentRoomId()), "Cannot return from treasury");
        walkTo(game, new Position(14, 6));
        Door door = game.getDungeonMap().getCurrentRoom().getDoor(Direction.RIGHT);
        check(door.isLocked(), "Boss door should initially require a key");
        game.handlePlayerMove(Direction.RIGHT); // First attempt uses key and unlocks.
        check(!door.isLocked(), "Cannot unlock boss door with Golden Key");
        game.handlePlayerMove(Direction.RIGHT); // Second attempt enters room.
        check("ROOM_BOSS".equals(game.getDungeonMap().getCurrentRoomId()), "Cannot enter boss chamber");

        walkTo(game, new Position(14, 6)); // Stepping on portal before killing boss must not win.
        check(game.getGameState() == GameState.PLAYING, "Portal awarded victory before boss defeat");
        walkTo(game, new Position(11, 6));
        game.getPlayer().setFacingDirection(Direction.RIGHT);
        Boss boss = null;
        for (dungeonescape.entities.Entity e : game.getDungeonMap().getCurrentRoom().getEntities()) {
            if (e instanceof Boss) boss = (Boss) e;
        }
        check(boss != null, "Boss missing");
        for (int i = 0; i < 30 && boss.isAlive(); i++) game.handlePlayerAttack();
        check(!boss.isAlive(), "Cannot defeat boss");
        walkTo(game, new Position(14, 6));
        check(game.getGameState() == GameState.VICTORY, "Winning after defeating boss is broken");
        passed("Full dungeon playthrough: 4 rooms, Golden Key, sword, boss, victory");
    }

    /** Breadth-first search through collision-legal squares, replayed with actual WASD game API. */
    private static void walkTo(GameManager game, Position target) {
        Room room = game.getDungeonMap().getCurrentRoom();
        Position origin = game.getPlayer().getPosition();
        ArrayDeque<Position> open = new ArrayDeque<>();
        Map<Position, Position> prior = new HashMap<>();
        Map<Position, Direction> step = new HashMap<>();
        CollisionManager collisions = new CollisionManager();
        open.add(origin);
        prior.put(origin, origin);
        while (!open.isEmpty() && !prior.containsKey(target)) {
            Position current = open.removeFirst();
            for (Direction direction : Direction.values()) {
                Position next = current.translate(direction);
                if (prior.containsKey(next) || !collisions.canMoveTo(game.getPlayer(), next, room)) continue;
                prior.put(next, current);
                step.put(next, direction);
                open.addLast(next);
            }
        }
        check(prior.containsKey(target), "No playable path to " + target + " in " + room.getName());
        ArrayDeque<Direction> path = new ArrayDeque<>();
        Position position = target;
        while (!position.equals(origin)) {
            path.addFirst(step.get(position));
            position = prior.get(position);
        }
        while (!path.isEmpty()) game.handlePlayerMove(path.removeFirst());
    }
}
