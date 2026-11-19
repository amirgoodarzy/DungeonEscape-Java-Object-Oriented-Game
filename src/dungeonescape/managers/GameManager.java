package dungeonescape.managers;

import dungeonescape.behaviors.DamageType;
import dungeonescape.behaviors.Direction;
import dungeonescape.behaviors.Interactable;
import dungeonescape.entities.Entity;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.entities.structures.Door;
import dungeonescape.entities.structures.Exit;
import dungeonescape.entities.structures.Trap;
import dungeonescape.events.EventBus;
import dungeonescape.events.ItemCollectedEvent;
import dungeonescape.events.PlayerMoveEvent;
import dungeonescape.events.RoomChangedEvent;
import dungeonescape.exceptions.GameException;
import dungeonescape.exceptions.InvalidMoveException;
import dungeonescape.exceptions.InventoryFullException;
import dungeonescape.exceptions.LockedDoorException;
import dungeonescape.exceptions.SaveLoadException;
import dungeonescape.items.Item;
import dungeonescape.behaviors.Usable;
import dungeonescape.map.DungeonMap;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.rules.Rule;
import dungeonescape.rules.RuleContext;
import dungeonescape.rules.RuleEngine;
import dungeonescape.utils.AudioPlayer;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main coordinator orchestrating the game loop, state transitions, and sub-managers.
 * 
 * Demonstrates:
 * - Composition: GameManager HAS-A Player, DungeonMap, CollisionManager, CombatManager, RuleEngine, EventBus
 * - Separation of Concerns: Delegates domain tasks rather than acting as a God Object
 * - Exception Handling: Catches domain-checked exceptions and translates them to user-visible feedback
 * - State Machine management via GameState enum
 */
public class GameManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private Player player;
    private DungeonMap dungeonMap;
    private GameState gameState;

    private final CollisionManager collisionManager;
    private final CombatManager combatManager;
    private RuleEngine ruleEngine;
    private final EventBus eventBus;
    private transient RuleContext ruleContext;

    private String statusMessage;
    private long messageTimer;
    private int turnCount;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean classicMode = false;
    private java.util.Set<String> visitedRooms = new java.util.HashSet<>();


    public GameManager() {
        this.eventBus = new EventBus();
        this.collisionManager = new CollisionManager();
        this.combatManager = new CombatManager(this.eventBus);
        this.ruleEngine = new RuleEngine();
        this.gameState = GameState.MAIN_MENU;
        this.statusMessage = "Welcome to Dungeon Escape!";
        this.messageTimer = 0;
        this.turnCount = 0;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) this.difficulty = difficulty;
    }
    public Difficulty getDifficulty() { return difficulty; }
    public void startClassicGame() { classicMode = true; startNewGame(); }

    public void startNewGame() {
        // Keep rules authored in the menu (or previous run), but reset their execution state.
        List<Rule> previousRules = new ArrayList<>(this.ruleEngine.getRules());
        this.dungeonMap = classicMode ? DungeonMap.createDefaultDungeon()
                : DungeonMap.createCampaignDungeon(System.nanoTime());
        if (!classicMode) {
            for (Room room : dungeonMap.getRooms().values()) {
                for (Entity entity : room.getEntities()) {
                    if (entity instanceof Enemy) {
                        Enemy monster = (Enemy) entity;
                        if (difficulty == Difficulty.EASY) {
                            monster.setMaxHealth(Math.max(8, monster.getMaxHealth() * 3 / 4));
                            monster.setAttackPower(Math.max(1, monster.getAttackPower() * 3 / 4));
                        } else if (difficulty == Difficulty.HARD) {
                            monster.setMaxHealth(monster.getMaxHealth() * 5 / 4);
                            monster.heal(monster.getMaxHealth());
                            monster.setAttackPower(monster.getAttackPower() * 5 / 4);
                        }
                    }
                }
            }
        }
        classicMode = false;
        this.player = new Player(new Position(2, 2));
        this.gameState = GameState.PLAYING;
        this.visitedRooms = new java.util.HashSet<>();
        this.visitedRooms.add(dungeonMap.getCurrentRoomId());
        this.statusMessage = dungeonMap.getCurrentRoomId().startsWith("ROOM_")
            ? "FIND THE GOLDEN KEY! Explore the Treasury, then defeat the Dungeon Overlord."
            : "FIND THE EMBER SIGIL! Explore side chambers; [M] Map, [Q] Whirlwind, [F] Firebolt, [H] Heal.";
        this.ruleEngine = new RuleEngine();
        this.eventBus.clear();
        this.ruleContext = new RuleContext(this);
        this.ruleEngine.attachToEventBus(this.eventBus, this.ruleContext);
        this.turnCount = 0;

        // Register starter examples, then recover authored rules with fresh execution state.
        registerStarterRules();
        for (Rule previous : previousRules) {
            if (previous.getSourceText().trim().isEmpty()) continue;
            try {
                Rule restored = ruleEngine.registerRuleFromSource(previous.getSourceText());
                restored.setEnabled(previous.isEnabled());
            } catch (GameException e) {
                setMessage("Could not restore rule: " + previous.getName());
            }
        }
    }

    /**
     * Pre-populates a couple of sample rules in the DSL so the player can see them immediately in the editor.
     */
    private void registerStarterRules() {
        try {
            // Starter Rule 1: Berserker Surge
            ruleEngine.registerRuleFromSource(
                    "RULE \"Adrenaline Surge\":\n" +
                    "WHEN player.health < 30\n" +
                    "THEN player.attack = player.attack + 10; message(\"Adrenaline surge! Attack boosted by +10!\")"
            );

            // Starter Rule 2: Wealthy Vitality
            ruleEngine.registerRuleFromSource(
                    "RULE \"Wealthy Vitality\":\n" +
                    "WHEN player.coins >= 40\n" +
                    "THEN player.health += 25; message(\"Gold invigorates your spirit! (+25 HP)\")"
            );
        } catch (GameException ignored) {
            // Default rules are verified at compile time
        }
    }

    public Player getPlayer() {
        return player;
    }

    public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    public RuleContext getRuleContext() {
        if (ruleContext == null) {
            ruleContext = new RuleContext(this);
        }
        return ruleContext;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setMessage(String message) {
        this.statusMessage = message;
        this.messageTimer = System.currentTimeMillis();
    }

    /** Dynamic quest tracker derived from persistent dungeon state, no duplicated mutable flags. */
    public boolean hasVisited(String roomId) { return visitedRooms.contains(roomId); }
    public java.util.Set<String> getVisitedRooms() { return java.util.Collections.unmodifiableSet(visitedRooms); }
    public String getQuest() {
        if (dungeonMap == null) return "Begin the expedition";
        if (gameState == GameState.VICTORY) return "ESCAPED THE DUNGEON!";
        String id = dungeonMap.getCurrentRoomId();
        if (id.startsWith("ROOM_")) return player.getInventory().hasItem("Golden Key") || player.hasKey("Golden Key")
                ? "Defeat the Overlord and reach the portal" : "Find the Golden Key in the Treasury";
        int act = id.startsWith("F3_") ? 3 : id.startsWith("F2_") ? 2 : 1;
        String key = act == 1 ? "Ember Sigil" : act == 2 ? "Frost Sigil" : "Void Sigil";
        if (!(player.hasKey(key) || player.getInventory().hasItem(key))) return "ACT " + act + ": Retrieve " + key + " (north branch)";
        String bossId = "F" + act + "_" + (act == 1 ? "GUARDIAN" : act == 2 ? "WARDEN" : "THRONE");
        Room bossRoom = dungeonMap.getRoom(bossId);
        boolean bossAlive = bossRoom != null && bossRoom.getEntities().stream().anyMatch(e -> e instanceof dungeonescape.entities.enemies.Boss && ((Enemy)e).isAlive());
        if (bossAlive) return "ACT " + act + ": Defeat the guardian (east branch)";
        return act == 3 ? "Enter the portal in the final chamber" : "ACT " + act + ": Enter the next area (east exit)";
    }

    public int getTurnCount() {
        return turnCount;
    }

    /** Area attack: damages up to four surrounding enemies once each, uses 34 energy. */
    public void handleWhirlwind() {
        if (gameState != GameState.PLAYING || player == null || !player.isAlive()) return;
        if (!player.spendEnergy(34)) { setMessage("Not enough energy for Whirlwind (34)"); return; }
        ruleEngine.beginAction();
        try {
            Room room = dungeonMap.getCurrentRoom();
            int hits = 0, kills = 0;
            for (Direction direction : Direction.values()) {
                Optional<Enemy> target = room.getEnemyAt(player.getPosition().translate(direction));
                if (target.isPresent()) {
                    hits++;
                    if (combatManager.resolveAttack(player, target.get(), 8 + player.getLevel(), room, player)) kills++;
                }
            }
            setMessage("WHIRLWIND! Hit " + hits + " enemies, defeated " + kills + ".");
            turnCount++;
            checkPlayerStatus();
        } finally { ruleEngine.requestEvaluation(getRuleContext()); ruleEngine.endAction(); }
    }

    /** Directional energy bolt (straight line, stops at a wall / first target). */
    public void handleFirebolt() {
        if (gameState != GameState.PLAYING || player == null || !player.isAlive()) return;
        if (!player.spendEnergy(25)) { setMessage("Not enough energy for Firebolt (25)"); return; }
        ruleEngine.beginAction();
        try {
            Room room = dungeonMap.getCurrentRoom();
            Position cursor = player.getPosition();
            boolean hit = false;
            for (int i = 0; i < 5; i++) {
                cursor = cursor.translate(player.getFacingDirection());
                if (!room.isWalkable(cursor.getX(), cursor.getY())) break;
                Optional<Enemy> target = room.getEnemyAt(cursor);
                if (target.isPresent()) {
                    boolean kill = combatManager.resolveAttack(player, target.get(), 15 + player.getLevel() * 2, room, player);
                    setMessage(kill ? "FIREBOLT defeated " + target.get().getName() : "FIREBOLT hit " + target.get().getName());
                    hit = true; break;
                }
                if (room.hasSolidEntityAt(cursor) && room.getDoorAt(cursor) == null) break;
            }
            if (!hit) setMessage("FIREBOLT missed. It cost 25 energy.");
            turnCount++;
        } finally { ruleEngine.requestEvaluation(getRuleContext()); ruleEngine.endAction(); }
    }

    /** Emergency self-heal, finite energy resource avoids invincible healing loops. */
    public void handleSecondWind() {
        if (gameState != GameState.PLAYING || player == null || !player.isAlive()) return;
        if (player.getHealth() == player.getMaxHealth()) { setMessage("Your health is already full"); return; }
        if (!player.spendEnergy(45)) { setMessage("Not enough energy to heal (45)"); return; }
        int amount = player.heal(25 + 5 * player.getLevel());
        setMessage("SECOND WIND restored " + amount + " HP!");
    }

    // ==========================================
    // Player Input & Action Coordination
    // ==========================================

    /**
     * Handles player directional movement attempts.
     * Demonstrates checked exception handling: InvalidMoveException is caught
     * and translated into HUD feedback rather than terminating execution.
     */
    public void handlePlayerMove(Direction direction) {
        if (gameState != GameState.PLAYING || player == null || !player.isAlive()) return;
        ruleEngine.beginAction();
        try {

        Room currentRoom = dungeonMap.getCurrentRoom();
        Position currentPos = player.getPosition();
        Position targetPos = currentPos.translate(direction);
        player.setFacingDirection(direction);

        // 1. Check if target is an enemy: initiate melee attack instead of walking onto it!
        Optional<Enemy> enemyOpt = currentRoom.getEnemyAt(targetPos);
        if (enemyOpt.isPresent()) {
            boolean defeated = combatManager.resolveAttack(player, enemyOpt.get(), currentRoom, player);
            if (defeated) {
                setMessage(String.format("You defeated the %s!", enemyOpt.get().getName()));
            } else {
                setMessage(String.format("Attacked %s! (Enemy HP: %d/%d)",
                        enemyOpt.get().getName(), enemyOpt.get().getHealth(), enemyOpt.get().getMaxHealth()));
            }
            postActionUpdate();
            return;
        }

        // 2. Validate standard movement collision
        try {
            collisionManager.validateMove(player, targetPos, direction, currentRoom);
            player.move(direction);
            turnCount++;

            // Publish move event
            eventBus.publish(new PlayerMoveEvent(player, currentPos, targetPos, direction));

            // Check if player stepped onto an item
            handleItemPickup(targetPos, currentRoom);

            // Check if player stepped on a trap
            handleTrapTrigger(targetPos, currentRoom);

            // Check if player stepped onto a Door
            handleDoorCrossing(targetPos, currentRoom);

            // Check if player stepped onto Exit
            handleExitReached(targetPos, currentRoom);


        } catch (InvalidMoveException e) {
            // Check if blocked by a locked door to attempt automatic key interaction
            Door door = currentRoom.getDoorAt(targetPos);
            if (door != null && door.isLocked()) {
                try {
                    String result = door.interact(player);
                    setMessage(result);
                    eventBus.publish(new PlayerMoveEvent(player, currentPos, currentPos, direction));
                } catch (LockedDoorException lde) {
                    setMessage(lde.getMessage());
                }
            } else {
                setMessage("Way blocked: " + e.getMessage());
                AudioPlayer.playErrorSound();
            }
        }

        checkPlayerStatus();
        } finally {
            ruleEngine.requestEvaluation(getRuleContext());
            ruleEngine.endAction();
        }
    }

    /**
     * Executes manual player attack in the facing direction.
     */
    public void handlePlayerAttack() {
        if (gameState != GameState.PLAYING || player == null || !player.isAlive()) return;
        ruleEngine.beginAction();
        try {

        Room currentRoom = dungeonMap.getCurrentRoom();
        Position targetPos = player.getPosition().translate(player.getFacingDirection());

        Optional<Enemy> enemyOpt = currentRoom.getEnemyAt(targetPos);
        if (enemyOpt.isPresent()) {
            Enemy enemy = enemyOpt.get();
            boolean defeated = combatManager.resolveAttack(player, enemy, currentRoom, player);
            if (defeated) {
                setMessage(String.format("Defeated %s with a direct strike!", enemy.getName()));
            } else {
                setMessage(String.format("Struck %s! Remaining HP: %d", enemy.getName(), enemy.getHealth()));
            }
        } else {
            setMessage("You swing your weapon into empty air.");
            AudioPlayer.playTone(280, 70, 0.3);
        }

        postActionUpdate();
        } finally {
            ruleEngine.requestEvaluation(getRuleContext());
            ruleEngine.endAction();
        }
    }

    /**
     * Interacts with an entity in facing direction or under feet (Doors, NPCs).
     * Demonstrates Inclusion Polymorphism with Interactable.
     */
    public void handlePlayerInteract() {
        if (gameState != GameState.PLAYING || player == null) return;
        ruleEngine.beginAction();
        try {

        Room room = dungeonMap.getCurrentRoom();
        Position facingPos = player.getPosition().translate(player.getFacingDirection());

        // Check facing position or current position
        Optional<Interactable> interactable = room.getInteractableAt(facingPos);
        if (!interactable.isPresent()) {
            interactable = room.getInteractableAt(player.getPosition());
        }

        if (interactable.isPresent()) {
            try {
                String feedback = interactable.get().interact(player);
                setMessage(feedback);
                if (interactable.get() instanceof Door) {
                    // E is also a valid way to CROSS a door, not merely inspect it.
                    Door door = (Door) interactable.get();
                    if (!door.isLocked()) handleDoorCrossing(door.getPosition(), room);
                }
            } catch (LockedDoorException lde) {
                setMessage(lde.getMessage());
            } catch (GameException ge) {
                setMessage(ge.getMessage());
            }
        } else {
            // Let the player retry an item at their feet after making inventory space.
            if (room.getItemAt(player.getPosition()).isPresent()) {
                handleItemPickup(player.getPosition(), room);
            } else {
                setMessage("Nothing nearby to interact with.");
            }
        }
        } finally {
            ruleEngine.requestEvaluation(getRuleContext());
            ruleEngine.endAction();
        }
    }

    /**
     * Uses or equips an item in the given inventory slot.
     */
    public void handleUseInventoryItem(int slotIndex) {
        if (gameState != GameState.PLAYING || player == null) return;
        ruleEngine.beginAction();
        try {

        Item item = player.getInventory().get(slotIndex);
        if (item == null) {
            setMessage("Empty inventory slot #" + (slotIndex + 1));
            return;
        }

        if (item instanceof Usable) {
            try {
                String feedback = ((Usable) item).use(player);
                setMessage(feedback);
                postActionUpdate();
            } catch (GameException e) {
                setMessage("Cannot use item: " + e.getMessage());
                AudioPlayer.playErrorSound();
            }
        } else {
            setMessage("'" + item.getName() + "' is not a usable item.");
        }
        } finally {
            ruleEngine.requestEvaluation(getRuleContext());
            ruleEngine.endAction();
        }
    }

    /** Discards one backpack item onto the current floor tile (recoverable via E).
     *  Equipped gear is unequipped first, preventing stale combat bonuses.
     */
    public void handleDropInventoryItem(int slot) {
        if ((gameState != GameState.PLAYING && gameState != GameState.PAUSED) || player == null) return;
        Item item = player.getInventory().get(slot);
        if (item == null) { setMessage("There is no item in that slot."); return; }
        if (item == player.getEquippedWeapon()) player.unequipWeapon();
        if (item == player.getEquippedArmor()) player.equipArmor(null);
        try {
            player.getInventory().removeItem(item);
            item.setPosition(player.getPosition());
            dungeonMap.getCurrentRoom().addEntity(item);
            setMessage("Dropped " + item.getName() + ". Press E to pick it up again.");
        } catch (dungeonescape.exceptions.ItemNotFoundException ex) {
            setMessage("Could not drop item: " + ex.getMessage());
        }
    }

    // ==========================================
    // Turn Update & Environmental Checks
    // ==========================================

    private void handleItemPickup(Position pos, Room room) {
        Optional<Item> itemOpt = room.getItemAt(pos);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            try {
                String collectMsg = player.collect(item);
                room.removeEntity(item);
                setMessage(collectMsg);
                eventBus.publish(new ItemCollectedEvent(player, item));
            } catch (InventoryFullException e) {
                setMessage(e.getMessage());
                AudioPlayer.playErrorSound();
            }
        }
    }

    private void handleTrapTrigger(Position pos, Room room) {
        for (Entity e : room.getEntitiesAt(pos)) {
            if (e instanceof Trap) {
                ((Trap) e).trigger(player);
                setMessage(String.format("Ouch! You stepped on a spike trap (-%d HP)!", ((Trap) e).getTrapDamage()));
            }
        }
    }

    private void handleDoorCrossing(Position pos, Room room) {
        Door door = room.getDoorAt(pos);
        if (door != null && !door.isLocked()) {
            if (room.getId().matches("F[12]_(GUARDIAN|WARDEN)")
                    && door.getTargetRoomId().matches("F[23]_.*")
                    && room.getEntities().stream().anyMatch(e -> e instanceof dungeonescape.entities.enemies.Boss && ((Enemy)e).isAlive())) {
                setMessage("THE GATE IS SEALED! Defeat the guardian first.");
                return;
            }
            String targetRoomId = door.getTargetRoomId();
            Room targetRoom = dungeonMap.getRoom(targetRoomId);
            if (targetRoom != null) {
                String oldRoomId = room.getId();
                dungeonMap.setCurrentRoomId(targetRoomId);
                visitedRooms.add(targetRoomId);
                player.setPosition(door.getTargetPlayerPosition());
                setMessage("Entered " + targetRoom.getName());
                eventBus.publish(new RoomChangedEvent(player, oldRoomId, targetRoom));
            }
        }
    }

    private void handleExitReached(Position pos, Room room) {
        Optional<Entity> exitOpt = room.getEntitiesAt(pos).stream()
                .filter(e -> e instanceof Exit)
                .findFirst();

        if (exitOpt.isPresent()) {
            boolean bossAlive = room.getEntities().stream().anyMatch(e ->
                    e instanceof dungeonescape.entities.enemies.Boss &&
                    ((dungeonescape.entities.enemies.Boss) e).isAlive());
            if (bossAlive) {
                setMessage("The portal is sealed! Defeat the Dungeon Overlord first.");
                return;
            }
            gameState = GameState.VICTORY;
            setMessage("VICTORY! You conquered the dungeon and escaped!");
            AudioPlayer.playVictoryFanfare();
        }
    }

    /**
     * Drives AI decision-making and attacks for all living enemies in the current chamber.
     * Demonstrates Inclusion Polymorphism: enemy.decideMove() and enemy.attack()
     * dynamically execute distinct algorithms for Zombie, Ghost, and Boss.
     */
    public void updateEnemies() {
        updateEnemies(dungeonescape.utils.Constants.TIMER_DELAY_MS);
    }

    /** Enemy turns happen in real elapsed game time, not after dozens of keypresses. */
    public void updateEnemies(long deltaMs) {
        if (gameState != GameState.PLAYING || dungeonMap == null || player == null || !player.isAlive()) return;
        Room currentRoom = dungeonMap.getCurrentRoom();
        if (currentRoom == null) return;

        List<Enemy> enemies = new ArrayList<>();
        for (Entity e : currentRoom.getEntities()) {
            if (e instanceof Enemy && ((Enemy) e).isAlive()) enemies.add((Enemy) e);
        }

        boolean playerWasHit = false;
        for (Enemy enemy : enemies) {
            enemy.update(deltaMs);
            if (!enemy.isReadyToStep()) continue; // Attack AND movement obey the same cooldown.

            int distance = enemy.getPosition().manhattanDistanceTo(player.getPosition());
            boolean inRange = distance == 1 ||
                    (enemy instanceof dungeonescape.entities.enemies.RangedEnemy
                      && ((dungeonescape.entities.enemies.RangedEnemy) enemy).canShoot(player, currentRoom)) ||
                    (enemy instanceof dungeonescape.entities.enemies.Boss &&
                    ((dungeonescape.entities.enemies.Boss) enemy).isEnraged() && distance == 2);

            if (inRange) {
                enemy.attack(player);
                playerWasHit = true;
                setMessage(String.format("%s attacked you! HP: %d/%d",
                        enemy.getName(), player.getHealth(), player.getMaxHealth()));
                if (!player.isAlive()) break;
            } else {
                Direction moveDir = enemy.decideMove(player, currentRoom);
                if (moveDir != null) {
                    Position newPos = enemy.getPosition().translate(moveDir);
                    // Never let an enemy overlap the hero or another solid actor.
                    if (!newPos.equals(player.getPosition()) &&
                            collisionManager.canMoveTo(enemy, newPos, currentRoom)) {
                        try { enemy.move(moveDir); } catch (InvalidMoveException ignored) { }
                    }
                }
            }
        }
        if (playerWasHit) ruleEngine.requestEvaluation(getRuleContext());
        checkPlayerStatus();
    }

    private void postActionUpdate() {
        checkPlayerStatus();
    }

    private void checkPlayerStatus() {
        if (player != null && !player.isAlive()) {
            gameState = GameState.GAME_OVER;
            setMessage("DEFEAT! Your adventurer has perished in the dungeon.");
            AudioPlayer.playTone(110, 500, 0.7);
        }
    }

    public void update(long deltaMs) {
        // State updates per tick
        if (gameState == GameState.PLAYING && player != null) {
            player.update(deltaMs);
            updateEnemies(deltaMs);
        }
    }

    // ==========================================
    // Save / Load Delegation
    // ==========================================

    public void saveGame(File file) throws SaveLoadException {
        if (player == null || dungeonMap == null) {
            throw new SaveLoadException("Start a game before saving", file != null ? file.toString() : "null");
        }
        SaveLoadManager.GameSnapshot snapshot = new SaveLoadManager.GameSnapshot(
                this.player, this.dungeonMap, this.ruleEngine, this.gameState, this.turnCount, this.difficulty, this.visitedRooms);
        SaveLoadManager.saveGame(file, snapshot);
        setMessage("Game progress successfully saved to " + file.getName());
    }

    public void loadGame(File file) throws SaveLoadException {
        SaveLoadManager.GameSnapshot snapshot = SaveLoadManager.loadGame(file);
        this.player = snapshot.getPlayer();
        this.dungeonMap = snapshot.getDungeonMap();
        this.gameState = snapshot.getGameState();
        this.turnCount = snapshot.getTurnCount();
        this.difficulty = snapshot.getDifficulty();
        this.visitedRooms = new java.util.HashSet<>(snapshot.getVisitedRooms());
        if (dungeonMap != null) visitedRooms.add(dungeonMap.getCurrentRoomId());
        this.ruleEngine = snapshot.getRuleEngine() != null ? snapshot.getRuleEngine() : new RuleEngine();
        this.eventBus.clear();
        this.ruleContext = new RuleContext(this);
        this.ruleEngine.attachToEventBus(this.eventBus, this.ruleContext);
        setMessage("Game progress loaded from " + file.getName());
    }
}
