# Dungeon Escape: The Three Sigils — Expanded Edition

A complete, playable 2D dungeon-crawler campaign written in **Java 11+ / Swing / Java2D**, built by expanding the original DungeonEscape OOP coursework project. No external game libraries or downloaded textures are required.

## Launch the game

1. Install **Java 11 or newer** (`java -version`).
2. Start the program in one of the following ways:
   - **macOS:** double-click `RUN_MAC.command` (allow execution in System Settings if macOS blocks it), or open Terminal in this folder and run `java -jar DungeonEscape_Playable.jar`.
   - **Windows:** double-click `RUN_WINDOWS.bat`, or run `java -jar DungeonEscape_Playable.jar` in Command Prompt.
   - **Linux:** run `./run_linux.sh` or `java -jar DungeonEscape_Playable.jar`.
3. Choose **NEW CAMPAIGN** and a difficulty in the main menu. The **Classic Dungeon** option preserves the original 4-room adventure.

## Expanded gameplay

- **Three acts, 15 connected chambers** with visually distinct color themes and randomized enemy encounters/loot placement. The room graph is intentional and consistent (not a fully procedurally generated labyrinth).
- **Three gate guardians** with increasing health/attack, including a **two-phase boss** that enrages below 50% HP.
- **Five enemy archetypes**: Zombie, Ghost, Archer, Sorcerer and Boss. Their AI uses movement, phasing, line of sight or multi-phase attacks.
- **Three sigils** (Ember / Frost / Void) needed to unlock each chapter's boss gate; a guardian must fall before the next act opens.
- **XP and leveling**, gold, coins, equipment upgrades, armor and a mana-like **energy** resource.
- **Three skills**: Whirlwind (area attack), Firebolt (straight-line ranged attack), Second Wind (self-heal).
- **Item inventory, keyring and drop/retrieve function**, shrines, health/energy potions, traps, discoverable campaign map, and journal with three optional legendary relics.
- **Three difficulty modes** (Easy, Normal, Hard), **save/load**, and a player-authored **DSL rule engine** with a Swing editor.
- **Original 4-room mode** remains available for demonstrating the coursework's evolution.

## Controls

| Key | Action |
| --- | --- |
| `W A S D` / arrow keys | Move (walk into an enemy to melee attack) |
| `SPACE` | Strike in the facing direction |
| `E` | Talk, interact, pick up an item under your feet, cross a nearby doorway |
| `Q` | Whirlwind (34 energy; nearby enemies) |
| `F` | Firebolt (25 energy; directional ranged attack) |
| `H` | Second Wind (45 energy; restores health) |
| `1` to `8` | Use or equip backpack items |
| `X` | Choose an item to drop on the floor; press `E` to retrieve it |
| `M` | Campaign discovery map (pauses foes) |
| `I` | Inventory and quest journal (pauses foes) |
| `P` | Pause/resume game |
| `K` | Quick save (`dungeon_save.dat` in the working directory) |
| `R` | Custom rule editor |
| `ESC` | Return to main menu (or close map/journal) |

**Walkthrough:** Leave the camp via the east door. In each act, explore the northern side chamber for its **sigil**, then the southern chamber for **weapon/armor** upgrades. Unlock the eastern guardian door, defeat the guardian, and take the next exit. In the third act, defeat the Void Sovereign and enter the portal. The legendary treasures in each northern branch are optional.

**Tip:** If you've collected too many potions or old weapons, press **X** to drop something; the sigils always go into a separate keyring, so a full backpack can never block an essential key.

## Build from source

From this directory, with JDK 11 or newer installed:

```bash
mkdir -p bin
find src -name '*.java' > sources.txt
javac --release 11 -encoding UTF-8 -d bin @sources.txt
jar --create --file DungeonEscape_Playable.jar --main-class dungeonescape.Main -C bin .
java -jar DungeonEscape_Playable.jar
```

On Windows you can compile through VS Code, IntelliJ IDEA, Eclipse or NetBeans; the repository has no Maven/Gradle requirement.

## Automated regression tests

```bash
java -cp bin dungeonescape.tests.GameTestSuite
java -cp bin dungeonescape.tests.GameplayRegressionTest
java -cp bin dungeonescape.tests.ExpandedCampaignTest
java -Djava.awt.headless=true -cp bin dungeonescape.tests.UiSmokeTest .
```

The expanded test completes **all three acts using actual movement/attack APIs**, checks locked gates, room links, sigils, equipment, boss state, victory, visited rooms, save/load, difficulty scaling and inventory drop behavior. GUI smoke test renders game, map and inventory to PNG images.

## Project architecture

`src/dungeonescape` contains independent packages for entities, enemy AI, items, maps, managers, rules/DSL, events, UI, exceptions and automated tests. It demonstrates encapsulation, inheritance, interfaces, polymorphism, abstract classes, event publication, composition, enums, checked exceptions, serialization and domain-specific language parsing.

- `map/CampaignFactory.java`: campaign room graph, rewards and encounter setup.
- `managers/GameManager.java`: high-level game state and input coordination.
- `managers/CombatManager.java`: combat and XP rewards.
- `entities/Player.java`: skills resources, leveling, inventory and gear.
- `entities/enemies/`: specialized combat AI.
- `ui/GamePanel.java` and `ui/HUD.java`: Java2D game board / input / status.
- `rules/` and `rules/dsl/`: original editable rule engine.

The original OOP writeups are included for study; see **EXPANSION_DESIGN_AND_TESTS.md** for the additional work in this edition. The game is a desktop Java/Swing application, not an installer or browser game.
