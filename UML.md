# Dungeon Escape — Complete UML Class Diagrams

This document provides the complete UML Class Diagrams for the **Dungeon Escape** project, formatted using standard Mermaid syntax. The diagrams reflect the **exact codebase implementation** without approximations.

---

## 1. Core Entity & Behavioral Interface Hierarchy

```mermaid
classDiagram
    %% Behavioral Interfaces
    class Movable {
        <<interface>>
        +move(Direction direction) void
    }
    class Attackable {
        <<interface>>
        +attack(LivingEntity target) void
        +attack(LivingEntity target, int bonusDamage) void
    }
    class Interactable {
        <<interface>>
        +interact(Player player) String
    }
    class Collectible {
        <<interface>>
        +onCollect(Player player) String
    }
    class Usable {
        <<interface>>
        +use(Player player) String
    }

    %% Entity Root
    class Entity {
        <<abstract>>
        -String name
        -Position position
        -boolean passable
        -boolean visible
        +getName() String
        +getPosition() Position
        +setPosition(Position pos) void
        +isPassable() boolean
        +render(Graphics2D g, int tileSize)* void
        +update(long deltaMs)* void
    }

    %% Living Entities
    class LivingEntity {
        <<abstract>>
        -int health
        -int maxHealth
        -int attackPower
        -int defense
        +getHealth() int
        +getMaxHealth() int
        +isAlive() boolean
        +takeDamage(int amount) int
        +takeDamage(int amount, DamageType type) int
        +heal(int amount) int
        +getHealthRatio() double
    }

    class Player {
        -int score
        -int coins
        -Inventory inventory
        -Weapon equippedWeapon
        -Direction facingDirection
        +move(Direction direction) void
        +attack(LivingEntity target) void
        +attack(LivingEntity target, int bonusDamage) void
        +collect(Item item) String
        +equipWeapon(Weapon weapon) void
    }

    class NPC {
        -List~String~ dialogueLines
        -int dialogueIndex
        +interact(Player player) String
    }

    class Enemy {
        <<abstract>>
        -int detectionRange
        -int stepDelay
        -int stepCounter
        +canDetect(Player player) boolean
        +decideMove(Player player, Room room)* Direction
        +isReadyToStep() boolean
    }

    class Zombie {
        +decideMove(Player player, Room room) Direction
        +attack(LivingEntity target, int bonusDamage) void
    }

    class Ghost {
        -boolean phasesThroughWalls
        +decideMove(Player player, Room room) Direction
        +attack(LivingEntity target, int bonusDamage) void
        +isPhasesThroughWalls() boolean
    }

    class Boss {
        -boolean enraged
        -int phase
        +takeDamage(int amount, DamageType type) int
        +decideMove(Player player, Room room) Direction
        +attack(LivingEntity target, int bonusDamage) void
    }

    %% Static Entities
    class StaticEntity {
        <<abstract>>
    }

    class Wall {
        +render(Graphics2D g, int tileSize) void
    }

    class Door {
        -String targetRoomId
        -Position targetPlayerPosition
        -boolean locked
        -String requiredKeyId
        +interact(Player player) String
        +unlock(Key key) boolean
        +unlockDirectly() void
    }

    class Exit {
        +render(Graphics2D g, int tileSize) void
    }

    class Trap {
        -int trapDamage
        -boolean sprung
        +trigger(Player player) void
    }

    %% Inheritances
    Entity <|-- LivingEntity
    Entity <|-- StaticEntity
    LivingEntity <|-- Player
    LivingEntity <|-- NPC
    LivingEntity <|-- Enemy
    Enemy <|-- Zombie
    Enemy <|-- Ghost
    Enemy <|-- Boss
    StaticEntity <|-- Wall
    StaticEntity <|-- Door
    StaticEntity <|-- Exit
    StaticEntity <|-- Trap

    %% Interface Realizations
    Movable <|.. Player
    Attackable <|.. Player
    Interactable <|.. NPC
    Movable <|.. Enemy
    Attackable <|.. Enemy
    Interactable <|.. Door

    %% Composition
    Player "1" *-- "1" Inventory : has-a
```

---

## 2. Item & Equipment Hierarchy

```mermaid
classDiagram
    class Entity {
        <<abstract>>
    }
    class Collectible {
        <<interface>>
        +onCollect(Player player) String
    }
    class Usable {
        <<interface>>
        +use(Player player) String
    }

    class Item {
        <<abstract>>
        -int value
        -String description
        +getValue() int
        +getDescription() String
        +onCollect(Player player)* String
    }

    class Key {
        -String keyId
        +getKeyId() String
        +onCollect(Player player) String
    }

    class Coin {
        +onCollect(Player player) String
    }

    class Treasure {
        +onCollect(Player player) String
    }

    class Consumable {
        <<abstract>>
        +onCollect(Player player) String
        +use(Player player)* String
    }

    class HealthPotion {
        -int healAmount
        +use(Player player) String
    }

    class AttackPotion {
        -int attackBoost
        +use(Player player) String
    }

    class Equipment {
        <<abstract>>
        -boolean equipped
        +isEquipped() boolean
    }

    class Weapon {
        -int attackBonus
        +getAttackBonus() int
        +use(Player player) String
    }

    Entity <|-- Item
    Collectible <|.. Item
    Item <|-- Key
    Item <|-- Coin
    Item <|-- Treasure
    Item <|-- Consumable
    Item <|-- Equipment
    Usable <|.. Consumable
    Usable <|.. Equipment
    Consumable <|-- HealthPotion
    Consumable <|-- AttackPotion
    Equipment <|-- Weapon
```

---

## 3. Player-Defined Rule Engine (DSL & AST)

```mermaid
classDiagram
    class RuleEngine {
        -List~Rule~ rules
        -int evaluationCycles
        +registerRuleFromSource(String sourceText) Rule
        +evaluateAll(RuleContext context) int
        +attachToEventBus(EventBus bus, RuleContext context) void
    }

    class Rule {
        -String name
        -Expression~Boolean~ condition
        -Action action
        -String sourceText
        -boolean enabled
        -int executionCount
        +evaluateAndExecute(RuleContext context) boolean
    }

    class RuleContext {
        -GameManager gameManager
        +getVariable(String identifier) Object
        +setVariable(String identifier, Object value) boolean
        +callFunction(String name, List args) Object
        +spawnEntity(String type, int x, int y) boolean
        +unlockDoor(String doorName) boolean
    }

    class Expression~T~ {
        <<interface>>
        +evaluate(RuleContext context) T
    }

    class LiteralExpression~T~ {
        -T value
        +evaluate(RuleContext context) T
    }

    class VariableExpression {
        -String identifier
        +evaluate(RuleContext context) Object
    }

    class BinaryArithmeticExpression {
        -Expression left
        -Operator operator
        -Expression right
        +evaluate(RuleContext context) Double
    }

    class ComparisonExpression {
        -Expression left
        -Operator operator
        -Expression right
        +evaluate(RuleContext context) Boolean
    }

    class LogicalExpression {
        -Expression~Boolean~ left
        -Operator operator
        -Expression~Boolean~ right
        +evaluate(RuleContext context) Boolean
    }

    class FunctionCallExpression {
        -String functionName
        -List~Expression~ arguments
        +evaluate(RuleContext context) Object
    }

    class Action {
        <<interface>>
        +execute(RuleContext context) void
    }

    class AssignmentAction {
        -String targetIdentifier
        -AssignType assignType
        -Expression expression
        +execute(RuleContext context) void
    }

    class SpawnAction {
        -String entityType
        -Expression xExpr
        -Expression yExpr
        +execute(RuleContext context) void
    }

    class UnlockAction {
        -String doorName
        +execute(RuleContext context) void
    }

    class MessageAction {
        -String messageText
        +execute(RuleContext context) void
    }

    class CompositeAction {
        -List~Action~ actions
        +execute(RuleContext context) void
    }

    RuleEngine "1" *-- "*" Rule : manages
    Rule "1" *-- "1" Expression : condition
    Rule "1" *-- "1" Action : action

    Expression <|.. LiteralExpression
    Expression <|.. VariableExpression
    Expression <|.. BinaryArithmeticExpression
    Expression <|.. ComparisonExpression
    Expression <|.. LogicalExpression
    Expression <|.. FunctionCallExpression

    Action <|.. AssignmentAction
    Action <|.. SpawnAction
    Action <|.. UnlockAction
    Action <|.. MessageAction
    Action <|.. CompositeAction
    CompositeAction "1" *-- "*" Action : composite
```

---

## 4. World Graph, Map & Management

```mermaid
classDiagram
    class GameManager {
        -Player player
        -DungeonMap dungeonMap
        -GameState gameState
        -CollisionManager collisionManager
        -CombatManager combatManager
        -RuleEngine ruleEngine
        -EventBus eventBus
        +startNewGame() void
        +handlePlayerMove(Direction direction) void
        +handlePlayerAttack() void
        +handlePlayerInteract() void
        +handleUseInventoryItem(int slot) void
        +update(long deltaMs) void
    }

    class DungeonMap {
        -Map~String, Room~ rooms
        -String currentRoomId
        -String startingRoomId
        +getCurrentRoom() Room
        +setCurrentRoomId(String id) void
        +createDefaultDungeon()$ DungeonMap
    }

    class Room {
        -String id
        -String name
        -int width
        -int height
        -TileType[][] tiles
        -List~Entity~ entities
        -Map~Direction, Door~ doors
        +isWalkable(int x, int y) boolean
        +addEntity(Entity entity) void
        +removeEntity(Entity entity) boolean
        +getEntityAt(Position pos) Optional~Entity~
        +render(Graphics2D g, int tileSize) void
    }

    class Position {
        <<final>>
        -int x
        -int y
        +translate(Direction dir) Position
        +manhattanDistanceTo(Position other) int
        +isAdjacentTo(Position other) boolean
    }

    class CollisionManager {
        +validateMove(Entity e, Position target, Direction d, Room r) void
        +canMoveTo(Entity e, Position target, Room r) boolean
    }

    class CombatManager {
        -EventBus eventBus
        +resolveAttack(Attackable attacker, LivingEntity target, Room room, Player player) boolean
        +resolveAttack(Attackable attacker, LivingEntity target, int bonus, Room room, Player player) boolean
    }

    class SaveLoadManager {
        +saveGame(File f, GameSnapshot s)$ void
        +loadGame(File f)$ GameSnapshot
        +saveRulesScript(File f, String s)$ void
        +loadRulesScript(File f)$ String
    }

    GameManager "1" *-- "1" DungeonMap : has-a
    GameManager "1" *-- "1" CollisionManager : has-a
    GameManager "1" *-- "1" CombatManager : has-a
    GameManager "1" *-- "1" RuleEngine : has-a
    GameManager "1" *-- "1" EventBus : has-a
    DungeonMap "1" *-- "*" Room : contains
    Room "1" *-- "*" Entity : contains
    Entity "1" *-- "1" Position : located-at
```

---

## 5. Custom Checked Exception Hierarchy

```mermaid
classDiagram
    class Exception {
        <<java.lang>>
    }

    class GameException {
        -long timestamp
        +getTimestamp() long
    }

    class InvalidMoveException {
        -int attemptedX
        -int attemptedY
        -Direction direction
        +getAttemptedX() int
        +getAttemptedY() int
        +getDirection() Direction
    }

    class LockedDoorException {
        -String doorName
        -String requiredKeyId
        +getDoorName() String
        +getRequiredKeyId() String
    }

    class InventoryFullException {
        -String itemName
        -int capacity
        +getItemName() String
        +getCapacity() int
    }

    class ItemNotFoundException {
        -String itemName
        +getItemName() String
    }

    class SaveLoadException {
        -String filePath
        +getFilePath() String
    }

    class RuleParseException {
        -int line
        -int column
        -String snippet
        +getLine() int
        +getColumn() int
        +getSnippet() String
    }

    class RuleValidationException {
        -String ruleName
        -String invalidIdentifier
        +getRuleName() String
        +getInvalidIdentifier() String
    }

    Exception <|-- GameException
    GameException <|-- InvalidMoveException
    GameException <|-- LockedDoorException
    GameException <|-- InventoryFullException
    GameException <|-- ItemNotFoundException
    GameException <|-- SaveLoadException
    GameException <|-- RuleParseException
    GameException <|-- RuleValidationException
```
