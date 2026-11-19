# DungeonEscape Expanded Edition — Player's Guide

## Getting started

Install Java 11+ and launch `DungeonEscape_Playable.jar` (or use the provided platform starter scripts). Choose **NEW CAMPAIGN** and a difficulty. **Explorer** reduces monster strength, **Adventurer** is the intended normal setting, and **Nightmare** increases monster HP and damage. You can instead choose **Classic Dungeon** to play the corrected original 4-room adventure.

## Controls

Move with WASD or arrow keys. Walking toward an enemy standing immediately in front of you attacks it instead of stepping into its occupied tile. Space also attacks in the direction you last faced. `E` interacts with the object you're facing (doors, the elder, healing shrine) or a loose item at your feet. Walk over most items to pick them up automatically.

Use the inventory hotkeys **1–8** to drink potions, equip weapons and equip armor. Open **I** to review items, equipment, XP, level, keys and optional relics. If your 8-slot backpack fills, hit **X** and choose something to drop; **E** will collect an item at your feet again when you have room. Your sigils are automatically stored separately in your keyring.

Use **Q** (Whirlwind, 34 energy) to damage enemies touching you, **F** (Firebolt, 25 energy) to shoot in your facing direction, and **H** (Second Wind, 45 energy) for an emergency heal. Energy regenerates during active play. Healing/energy potions can be collected; the sanctuary shrine provides recovery in exchange for 30 gold. Defeating enemies yields XP, score, gold and some energy.

Other keys: **M** map, **R** DSL rule editor, **P** pause, **K** quick-save, **Esc** close overlay / return to menu, **Enter** replay after winning or losing. Maps and journal screens pause enemy movement. The rule editor also pauses combat.

## Main story: The Three Sigils

| Act | Camp | Junction | Northern key room | Southern equipment room | Boss |
|---|---|---|---|---|---|
| I (Fallen) | Emberlight Haven | Fallen Crossroads | Sunken Crypt — **Ember Sigil** | Forgotten Armory — Saber + Vest | Bone Sentinel |
| II (Ashen) | Ashen Refuge | Molten Forge | Frozen Library — **Frost Sigil** | Poisoned Garden — Glacier Blade + Dragonhide | Ash Warden |
| III (Void) | Starlit Outpost | Whispering Catacombs | Dark Shroud — **Void Sigil** | Royal Reliquary — Starfall Greatsword + Celestial Plate | Void Sovereign |

**Navigation:** Exit the starting camp via the east door (right side) to its junction. At the junction, the northern door leads to the act's sigil, the southern door to new gear, and the eastern locked gate to the boss. After beating bosses I and II, leave their room by the eastern exit to begin the next act. After beating boss III, walk to the golden portal near the eastern wall to win. If a door is locked, obtain its sigil and walk toward it again to unlock/cross. `E` also works for unlocking/entering.

Optional: gather a **legendary relic** from each northern key room. Relics give score and count toward a 3-relic side quest in your journal. Coins, potions and randomly placed enemies differ between runs.

## Enemies and tactics

- **Zombie** — pursues you; best handled one-on-one.
- **Ghost** — phases through wall tiles; spectral hits reduce the usefulness of armor.
- **Bone Archer** — shoots along unobstructed rows or columns. Break line-of-sight behind walls.
- **Void Sorcerer** — shoots spectral attacks from range, penetrating some armor.
- **Guardian Bosses** — large health pools and an enraged phase at low health. Save healing potions and energy for them.

## Save/load

Press **K** to write `dungeon_save.dat` in the game's current working directory. From the main menu choose **Load Saved Game** and select the file. Progress, player statistics, enemy and door states, equipment, explored rooms and DSL rules are serialized together. Saves from significantly different versions of Java class structures might not be compatible.

## Programmable rules — important OOP feature

Press **R** to view and edit player-defined rules in the language defined by the project's Lexer, Parser, AST and Rule Engine. One example:

```
RULE "Emergency Boost":
WHEN player.health < 30
THEN player.attack = player.attack + 5; message("A burst of power!")
```

Rules trigger on the transition into a true condition rather than once each screen frame. This is a tool for experimentation with game logic; creating unbalanced rules can make the campaign easier.

## Troubleshooting

- `java: command not found`: install Java 11+ and verify `java -version`.
- Window doesn't open: launch the JAR from Terminal / Command Prompt to view the error message; a graphical desktop is required.
- macOS refuses to open `.command`: run `bash RUN_MAC.command` from Terminal (or enable that file in macOS security settings).
- Save file not found: start the game from the directory containing the JAR, or browse for your save using the main menu.
- Locked gate: go north to collect the matching sigil. A locked door unlocks on the first movement into it and opens to the next room on a subsequent movement (or interact with E).
