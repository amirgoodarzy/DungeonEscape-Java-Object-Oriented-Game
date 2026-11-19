package dungeonescape.managers;

import dungeonescape.entities.Player;
import dungeonescape.exceptions.SaveLoadException;
import dungeonescape.map.DungeonMap;
import dungeonescape.rules.RuleEngine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles persistence of game state snapshots and text-based player-defined rules.
 * Demonstrates:
 * - Java standard library serialization (no external libraries or databases)
 * - Try-with-resources for deterministic stream closure
 * - Exception Translation pattern: wrapping low-level IOException / ClassNotFoundException
 *   into meaningful checked domain SaveLoadException instances
 */
public class SaveLoadManager {

    /**
     * Data Transfer Object (DTO) capturing a complete snapshot of the game world.
     */
    public static class GameSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Player player;
        private final DungeonMap dungeonMap;
        private final RuleEngine ruleEngine;
        private final GameState gameState;
        private final int turnCount;
        private final Difficulty difficulty;
        private final java.util.Set<String> visitedRooms;

        public GameSnapshot(Player player, DungeonMap dungeonMap, RuleEngine ruleEngine, GameState gameState) {
            this(player, dungeonMap, ruleEngine, gameState, 0, Difficulty.NORMAL);
        }

        public GameSnapshot(Player player, DungeonMap dungeonMap, RuleEngine ruleEngine,
                            GameState gameState, int turnCount) {
            this(player, dungeonMap, ruleEngine, gameState, turnCount, Difficulty.NORMAL, java.util.Collections.emptySet());
        }

        public GameSnapshot(Player player, DungeonMap dungeonMap, RuleEngine ruleEngine,
                            GameState gameState, int turnCount, Difficulty difficulty) {
            this(player, dungeonMap, ruleEngine, gameState, turnCount, difficulty, java.util.Collections.emptySet());
        }
        public GameSnapshot(Player player, DungeonMap dungeonMap, RuleEngine ruleEngine,
            GameState gameState, int turnCount, Difficulty difficulty, java.util.Set<String> visitedRooms) {
            this.visitedRooms = new java.util.HashSet<>(visitedRooms);
            this.difficulty = difficulty;
            this.player = player;
            this.dungeonMap = dungeonMap;
            this.ruleEngine = ruleEngine;
            this.gameState = gameState;
            this.turnCount = turnCount;
        }

        public Player getPlayer() {
            return player;
        }

        public DungeonMap getDungeonMap() {
            return dungeonMap;
        }

        public RuleEngine getRuleEngine() {
            return ruleEngine;
        }

        public GameState getGameState() {
            return gameState;
        }

        public java.util.Set<String> getVisitedRooms() { return visitedRooms == null ? java.util.Collections.emptySet() : visitedRooms; }
        public Difficulty getDifficulty() { return difficulty == null ? Difficulty.NORMAL : difficulty; }
        public int getTurnCount() {
            return turnCount;
        }
    }

    /**
     * Serializes the game snapshot to a binary disk file.
     * 
     * @param file Target file location.
     * @param snapshot Game state container.
     * @throws SaveLoadException if I/O fails during serialization.
     */
    public static void saveGame(File file, GameSnapshot snapshot) throws SaveLoadException {
        if (file == null || snapshot == null) {
            throw new SaveLoadException("Cannot save null snapshot or to null file", "null");
        }

        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(snapshot);
            oos.flush();
        } catch (IOException e) {
            throw new SaveLoadException("Failed to serialize game state to disk", file.getAbsolutePath(), e);
        }
    }

    /**
     * Restores a game snapshot from a binary disk file.
     * 
     * @param file Source file location.
     * @return Deserialized GameSnapshot.
     * @throws SaveLoadException if the file cannot be read or class definition has changed.
     */
    public static GameSnapshot loadGame(File file) throws SaveLoadException {
        if (file == null || !file.exists()) {
            throw new SaveLoadException("Save file does not exist", file != null ? file.getAbsolutePath() : "null");
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (GameSnapshot) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SaveLoadException("Failed to deserialize game snapshot from disk", file.getAbsolutePath(), e);
        }
    }

    /**
     * Saves player-authored rules to a plain text DSL script file.
     */
    public static void saveRulesScript(File file, String script) throws SaveLoadException {
        if (file == null) {
            throw new SaveLoadException("Target rule script file cannot be null", "null");
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(script != null ? script : "");
            writer.flush();
        } catch (IOException e) {
            throw new SaveLoadException("Failed to write rule script file", file.getAbsolutePath(), e);
        }
    }

    /**
     * Reads a plain text DSL script file containing player-authored rules.
     */
    public static String loadRulesScript(File file) throws SaveLoadException {
        if (file == null || !file.exists()) {
            throw new SaveLoadException("Rules script file not found", file != null ? file.getAbsolutePath() : "null");
        }

        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new SaveLoadException("Failed to read rules script file", file.getAbsolutePath(), e);
        }
    }
}
