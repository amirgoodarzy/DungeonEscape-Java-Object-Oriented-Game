package dungeonescape.tests;

import dungeonescape.behaviors.DamageType;
import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Inventory;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.entities.enemies.Ghost;
import dungeonescape.entities.enemies.Zombie;
import dungeonescape.entities.npc.NPC;
import dungeonescape.entities.structures.Door;
import dungeonescape.entities.structures.Wall;
import dungeonescape.events.EventBus;
import dungeonescape.exceptions.GameException;
import dungeonescape.exceptions.InvalidMoveException;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.exceptions.ItemNotFoundException;
import dungeonescape.exceptions.LockedDoorException;
import dungeonescape.exceptions.RuleParseException;
import dungeonescape.exceptions.RuleValidationException;
import dungeonescape.items.Coin;
import dungeonescape.items.HealthPotion;
import dungeonescape.items.Key;
import dungeonescape.items.Weapon;
import dungeonescape.managers.CollisionManager;
import dungeonescape.managers.CombatManager;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.SaveLoadManager;
import dungeonescape.map.DungeonMap;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.rules.Rule;
import dungeonescape.rules.RuleContext;
import dungeonescape.rules.RuleEngine;
import dungeonescape.rules.dsl.Lexer;
import dungeonescape.rules.dsl.Parser;
import dungeonescape.rules.dsl.Token;
import dungeonescape.utils.EntityRepository;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Self-contained, zero-dependency automated test suite for Dungeon Escape.
 * Executable on any standard Java SE 11+ environment without external JUnit JARs.
 * 
 * Tests:
 * 1. Player Movement & Boundaries (InvalidMoveException)
 * 2. Collision Manager & Ghost Wall-Phasing
 * 3. Enemy Polymorphism (Zombie, Ghost, Boss)
 * 4. Combat Resolution, Damage Types & Overloading
 * 5. Inventory Encapsulation & Capacity (InventoryFullException)
 * 6. Door Locking, Keys & Unlocking (LockedDoorException)
 * 7. NPC Dialogue & Interface Segregation
 * 8. Rule DSL Lexer Tokenization
 * 9. Rule DSL Parser & AST Generation
 * 10. Rule Engine Dynamic Execution & Attribute Mutation
 * 11. Complex DSL Logic (AND, OR, NOT, Arithmetic)
 * 12. Rule Semantic Validator & Sandbox Security
 * 13. Persistence: Save & Load Roundtrip Serialization
 * 14. Java Coercion & Widening Conversion
 * 15. Parametric Polymorphism (EntityRepository<T>)
 */
public class GameTestSuite {
    private static int passedCount = 0;
    private static int failedCount = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  DUNGEON ESCAPE — COMPREHENSIVE OOP AUTOMATED TEST SUITE");
        System.out.println("  University of Messina • OOP Course • Prof. Salvatore Distefano");
        System.out.println("======================================================================\n");

        runTest("Player Movement & Invariants", GameTestSuite::testPlayerMovement);
        runTest("Collision Manager & Boundary Constraints", GameTestSuite::testCollisionConstraints);
        runTest("Ghost Subtyping & Wall Phasing Capability", GameTestSuite::testGhostWallPhasing);
        runTest("Enemy Polymorphism & Multi-Phase Boss State", GameTestSuite::testEnemyPolymorphism);
        runTest("Combat Manager & Method Overloading", GameTestSuite::testCombatAndOverloading);
        runTest("Inventory Information Hiding & Capacity Limits", GameTestSuite::testInventoryCapacity);
        runTest("Door Interaction & Key Matching Exceptions", GameTestSuite::testDoorAndKeyInteraction);
        runTest("NPC Interaction & Interface Segregation (No Attack)", GameTestSuite::testNPCInterfaceSegregation);
        runTest("Rule DSL Lexer Tokenization", GameTestSuite::testRuleLexer);
        runTest("Rule DSL Parser & AST Hierarchy", GameTestSuite::testRuleParser);
        runTest("Player-Defined Rule Execution & Attribute Mutation", GameTestSuite::testRuleExecution);
        runTest("Complex DSL Logical & Arithmetic Precedence", GameTestSuite::testComplexRuleLogic);
        runTest("Rule Sandbox Validation & Security Exceptions", GameTestSuite::testRuleValidator);
        runTest("Save/Load Serialization Roundtrip", GameTestSuite::testSaveLoadSerialization);
        runTest("Numeric Coercion Demonstrations", GameTestSuite::testNumericCoercion);
        runTest("Parametric Polymorphism (Generic EntityRepository<T>)", GameTestSuite::testGenericRepository);

        System.out.println("\n----------------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d PASSED, %d FAILED (TOTAL %d)%n",
                passedCount, failedCount, (passedCount + failedCount));
        System.out.println("----------------------------------------------------------------------");

        if (failedCount > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestCase testCase) {
        try {
            testCase.run();
            System.out.printf("[PASS] %s%n", testName);
            passedCount++;
        } catch (Throwable t) {
            System.out.printf("[FAIL] %s: %s%n", testName, t.getMessage());
            t.printStackTrace(System.out);
            failedCount++;
        }
    }

    @FunctionalInterface
    interface TestCase {
        void run() throws Throwable;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion Failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(String.format("Assertion Failed (%s): Expected [%s] but found [%s]", message, expected, actual));
        }
    }

    // ==========================================
    // 1. Movement & Invariants
    // ==========================================
    private static void testPlayerMovement() {
        Player player = new Player(new Position(2, 2));
        assertEquals(new Position(2, 2), player.getPosition(), "Initial position mismatch");

        player.move(Direction.RIGHT);
        assertEquals(new Position(3, 2), player.getPosition(), "Position after right step mismatch");
        assertEquals(Direction.RIGHT, player.getFacingDirection(), "Facing direction mismatch");

        // Health encapsulation test
        assertEquals(100, player.getHealth(), "Initial health should be max (100)");
        player.takeDamage(40);
        assertTrue(player.getHealth() < 100, "Health must decrease on damage");
        player.heal(200);
        assertEquals(player.getMaxHealth(), player.getHealth(), "Heal must clamp to maxHealth invariant");
    }

    // ==========================================
    // 2. Collision Constraints
    // ==========================================
    private static void testCollisionConstraints() {
        CollisionManager cm = new CollisionManager();
        Room room = new Room("TEST_ROOM", "Test Chamber", 10, 10);
        Player player = new Player(new Position(1, 1));

        // Attempt move into perimeter wall at (0, 1) heading LEFT
        boolean exceptionThrown = false;
        try {
            cm.validateMove(player, new Position(0, 1), Direction.LEFT, room);
        } catch (InvalidMoveException e) {
            exceptionThrown = true;
            assertEquals(Direction.LEFT, e.getDirection(), "Exception direction mismatch");
            assertEquals(0, e.getAttemptedX(), "Exception attempted X mismatch");
        }
        assertTrue(exceptionThrown, "Moving into wall must throw InvalidMoveException");
    }

    // ==========================================
    // 3. Ghost Wall Phasing
    // ==========================================
    private static void testGhostWallPhasing() {
        CollisionManager cm = new CollisionManager();
        Room room = new Room("TEST_ROOM", "Test Chamber", 10, 10);
        Ghost ghost = new Ghost(new Position(1, 1));

        // Ghost can phase through wall at (0, 1)
        assertTrue(ghost.isPhasesThroughWalls(), "Ghost must have wall-phasing capability");
        assertTrue(cm.canMoveTo(ghost, new Position(0, 1), room),
                "CollisionManager must allow Ghost to move through wall tiles");
    }

    // ==========================================
    // 4. Enemy Polymorphism & Boss Phases
    // ==========================================
    private static void testEnemyPolymorphism() {
        Player player = new Player(new Position(5, 5));
        Room room = new Room("R", "Arena", 16, 12);

        // Polymorphic list of enemies
        List<Enemy> enemies = Arrays.asList(
                new Zombie(new Position(5, 2)),
                new Ghost(new Position(2, 5)),
                new Boss(new Position(8, 8))
        );

        for (Enemy enemy : enemies) {
            assertTrue(enemy.isAlive(), "Enemy should be initially alive");
            // Polymorphic method call
            Direction dir = enemy.decideMove(player, room);
            // Verify each subtype executes without error
            assertTrue(dir != null || enemy.getPosition().isAdjacentTo(player.getPosition()),
                    "Enemy decideMove must return valid direction or null if adjacent");
        }

        // Multi-phase Boss testing
        Boss boss = new Boss(new Position(8, 8));
        assertEquals(1, boss.getPhase(), "Boss should begin in Phase 1");
        boss.takeDamage(65, DamageType.TRUE_DAMAGE);
        assertEquals(2, boss.getPhase(), "Boss should transition to Phase 2 below 50% HP");
        assertTrue(boss.isEnraged(), "Boss should be enraged in Phase 2");
    }

    // ==========================================
    // 5. Combat Resolution & Overloading
    // ==========================================
    private static void testCombatAndOverloading() {
        EventBus bus = new EventBus();
        CombatManager cm = new CombatManager(bus);
        Player player = new Player(new Position(2, 2));
        Zombie zombie = new Zombie(new Position(2, 3));
        Room room = new Room("R", "Room");
        room.addEntity(zombie);

        int initialHealth = zombie.getHealth();
        // Overload 1: standard attack
        cm.resolveAttack(player, zombie, room, player);
        assertTrue(zombie.getHealth() < initialHealth, "Target must sustain damage");

        // Overload 2: attack with bonus damage
        int healthAfterFirst = zombie.getHealth();
        cm.resolveAttack(player, zombie, 20, room, player);
        assertTrue(zombie.getHealth() < healthAfterFirst, "Empowered attack must deal extra damage");
    }

    // ==========================================
    // 6. Inventory Encapsulation & Exceptions
    // ==========================================
    private static void testInventoryCapacity() throws GameException {
        Inventory inv = new Inventory(2);
        assertEquals(2, inv.getCapacity(), "Capacity mismatch");

        Key key1 = new Key("Silver Key", new Position(0, 0), "Silver");
        Key key2 = new Key("Gold Key", new Position(0, 0), "Gold");
        Key key3 = new Key("Bronze Key", new Position(0, 0), "Bronze");

        inv.addItem(key1);
        inv.addItem(key2);
        assertTrue(inv.isFull(), "Inventory should be full after adding 2 items");

        boolean fullThrown = false;
        try {
            inv.addItem(key3);
        } catch (InventoryFullException e) {
            fullThrown = true;
            assertEquals("Bronze Key", e.getItemName(), "Exception should carry rejected item name");
        }
        assertTrue(fullThrown, "Exceeding capacity must throw InventoryFullException");

        inv.removeItem(key1);
        assertEquals(1, inv.size(), "Size should be 1 after removing key1");

        boolean notFoundThrown = false;
        try {
            inv.removeItem(key1); // key1 was already removed
        } catch (ItemNotFoundException e) {
            notFoundThrown = true;
        }
        assertTrue(notFoundThrown, "Removing missing item must throw ItemNotFoundException");
    }

    // ==========================================
    // 7. Door Locking & Keys
    // ==========================================
    private static void testDoorAndKeyInteraction() {
        Position pos = new Position(5, 5);
        Door door = new Door("Vault Gate", pos, "ROOM_VAULT", new Position(1, 1), true, "Golden Key");
        Player player = new Player(new Position(4, 5));

        // Locked door without key
        boolean lockedThrown = false;
        try {
            door.interact(player);
        } catch (LockedDoorException e) {
            lockedThrown = true;
            assertEquals("Golden Key", e.getRequiredKeyId(), "Required key id mismatch");
        }
        assertTrue(lockedThrown, "Interacting with locked door without key must throw LockedDoorException");

        // Player acquires key
        try {
            player.getInventory().addItem(new Key("Golden Key", new Position(0, 0), "Golden Key"));
            String result = door.interact(player);
            assertTrue(!door.isLocked(), "Door should be unlocked after interaction with key");
            assertTrue(result.contains("unlocked"), "Result message should confirm unlocking");
        } catch (GameException e) {
            throw new AssertionError("Failed to unlock door: " + e.getMessage());
        }
    }

    // ==========================================
    // 8. NPC & Interface Segregation
    // ==========================================
    private static void testNPCInterfaceSegregation() throws GameException {
        NPC npc = new NPC("Wise Hermit", new Position(3, 3), Arrays.asList("Beware the dark dungeon."));
        Player player = new Player(new Position(3, 4));

        String dialogue = npc.interact(player);
        assertTrue(dialogue.contains("Beware the dark dungeon"), "NPC dialogue response mismatch");

        // Interface Segregation Principle verification:
        // NPC is a LivingEntity, but it does NOT implement Attackable!
        assertTrue(npc instanceof dungeonescape.behaviors.Interactable, "NPC must implement Interactable");
        assertTrue(!(npc instanceof dungeonescape.behaviors.Attackable), "NPC must NOT implement Attackable");
    }

    // ==========================================
    // 9. Rule DSL Lexer
    // ==========================================
    private static void testRuleLexer() throws RuleParseException {
        String code = "RULE \"Test Rule\": WHEN player.health < 30 AND player.score >= 100 THEN player.attack += 15";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.size() > 10, "Lexer should produce multiple tokens");
        assertEquals(dungeonescape.rules.dsl.TokenType.RULE, tokens.get(0).getType(), "First token must be RULE");
        assertEquals("Test Rule", tokens.get(1).getLiteral(), "Second token must be literal string rule name");
    }

    // ==========================================
    // 10. Rule DSL Parser & AST
    // ==========================================
    private static void testRuleParser() throws GameException {
        String code = "RULE \"Berserk\": WHEN player.health <= 25 THEN player.attack = player.attack + 20";
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer.tokenize(), code);
        Rule rule = parser.parseRule();

        assertEquals("Berserk", rule.getName(), "Parsed rule name mismatch");
        assertTrue(rule.getCondition() != null, "Rule condition AST must not be null");
        assertTrue(rule.getAction() != null, "Rule action AST must not be null");
    }

    // ==========================================
    // 11. Rule Execution & Dynamic Mutation
    // ==========================================
    private static void testRuleExecution() throws GameException {
        GameManager gm = new GameManager();
        gm.startNewGame();
        Player player = gm.getPlayer();
        RuleEngine engine = gm.getRuleEngine();
        RuleContext context = gm.getRuleContext();

        // Register a genuinely brand-new player rule not pre-coded in Java!
        String ruleScript = "RULE \"Divine Wrath\": WHEN player.health < 50 THEN player.attack = player.attack + 25";
        engine.registerRuleFromSource(ruleScript);

        int initialAtk = player.getTotalAttackPower();

        // With full health (100), condition is FALSE: rule should NOT trigger
        int triggered = engine.evaluateAll(context);
        assertEquals(0, triggered, "Rule should not trigger when health >= 50");
        assertEquals(initialAtk, player.getTotalAttackPower(), "Attack power should remain unchanged");

        // Damage player to 40 (< 50): condition is now TRUE!
        player.takeDamage(60, DamageType.TRUE_DAMAGE);
        triggered = engine.evaluateAll(context);
        assertEquals(1, triggered, "Rule should trigger when health < 50");
        assertEquals(initialAtk + 25, player.getTotalAttackPower(), "Player attack should be incremented by 25");
    }

    // ==========================================
    // 12. Complex DSL Logic (AND, OR, NOT, Arithmetic)
    // ==========================================
    private static void testComplexRuleLogic() throws GameException {
        GameManager gm = new GameManager();
        gm.startNewGame();
        Player player = gm.getPlayer();
        RuleEngine engine = gm.getRuleEngine();
        RuleContext context = gm.getRuleContext();

        // Complex boolean logic: (health < 40 OR coins >= 10) AND NOT (defense > 10)
        String complexRule = "RULE \"Shadow Surge\": WHEN (player.health < 40 OR player.coins >= 10) AND NOT player.defense > 10 THEN player.defense = player.defense + 3";
        engine.registerRuleFromSource(complexRule);

        player.setCoins(15); // Satisfies (player.coins >= 10)
        int initialDef = player.getDefense();

        int count = engine.evaluateAll(context);
        assertEquals(1, count, "Complex rule should trigger successfully");
        assertEquals(initialDef + 3, player.getDefense(), "Player defense should have increased by 3");
    }

    // ==========================================
    // 13. Rule Validator Security
    // ==========================================
    private static void testRuleValidator() {
        // Attempt to assign an illegal or unknown system variable
        String maliciousRule = "RULE \"Hack\": WHEN true THEN system.formatDisk = true";
        boolean caught = false;
        try {
            Lexer lexer = new Lexer(maliciousRule);
            Parser parser = new Parser(lexer.tokenize(), maliciousRule);
            parser.parseRule();
        } catch (RuleValidationException e) {
            caught = true;
            assertTrue(e.getMessage().contains("read-only or unrecognized"), "Validator should reject unknown target variable");
        } catch (GameException e) {
            caught = true;
        }
        assertTrue(caught, "RuleValidator must reject unauthorized/malicious variable mutation");
    }

    // ==========================================
    // 14. Save / Load Serialization
    // ==========================================
    private static void testSaveLoadSerialization() throws Exception {
        File tempSave = File.createTempFile("dungeon_test_save", ".dat");
        tempSave.deleteOnExit();

        GameManager gm1 = new GameManager();
        gm1.startNewGame();
        gm1.getPlayer().addCoins(77);
        gm1.getPlayer().addScore(350);

        // Save
        gm1.saveGame(tempSave);
        assertTrue(tempSave.exists() && tempSave.length() > 0, "Save file must be written to disk");

        // Load into fresh manager
        GameManager gm2 = new GameManager();
        gm2.loadGame(tempSave);

        assertEquals(77, gm2.getPlayer().getCoins(), "Restored coins mismatch");
        assertEquals(350, gm2.getPlayer().getScore(), "Restored score mismatch");
    }

    // ==========================================
    // 15. Java Numeric Coercion
    // ==========================================
    private static void testNumericCoercion() {
        int health = 75;
        int maxHealth = 100;
        // Explicit demonstration of widening coercion in Java: int / int integer division -> double
        double ratio = (double) health / maxHealth;
        assertEquals(0.75, ratio, "Widening conversion should produce exact decimal ratio");

        // String coercion
        String message = "Player health is " + health;
        assertEquals("Player health is 75", message, "Integer should coerce into string concatenation");
    }

    // ==========================================
    // 16. Parametric Polymorphism (Generic Repository)
    // ==========================================
    private static void testGenericRepository() {
        EntityRepository<Enemy> enemyRepo = new EntityRepository<>();
        Zombie z = new Zombie(new Position(1, 1));
        Ghost g = new Ghost(new Position(2, 2));

        enemyRepo.add(z);
        enemyRepo.add(g);

        assertEquals(2, enemyRepo.size(), "Repository size mismatch");
        assertTrue(enemyRepo.findAt(new Position(1, 1)).isPresent(), "Should find entity at (1, 1)");
        assertEquals("Zombie", enemyRepo.findAt(new Position(1, 1)).get().getName(), "Entity name mismatch");

        // Filter using generics
        List<Enemy> filtered = enemyRepo.filter(e -> e.getHealth() > 30);
        assertEquals(1, filtered.size(), "Filtered size mismatch (only Zombie has health > 30)");
        assertEquals("Zombie", filtered.get(0).getName(), "Filtered entity mismatch");
    }
}
