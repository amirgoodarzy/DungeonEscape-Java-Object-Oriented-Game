package dungeonescape.ui;

import dungeonescape.behaviors.Direction;
import dungeonescape.exceptions.SaveLoadException;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;
import dungeonescape.map.Room;
import dungeonescape.utils.Constants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Main game canvas component rendering the dungeon map, player, enemies, HUD,
 * and state overlays (Paused, Game Over, Victory).
 * 
 * Demonstrates:
 * - Java Swing & Java2D graphics pipeline
 * - Decoupled focus-safe input handling via Swing Key Bindings (ActionMap & InputMap)
 * - Timer-driven 60 FPS game loop conforming to Swing thread safety
 */
public class GamePanel extends JPanel {
    private final GameManager gameManager;
    private final HUD hud;
    private final Runnable onReturnToMenu;
    private Timer gameLoopTimer;
    private boolean showMap;
    private boolean showInventory;

    public GamePanel(GameManager gameManager, Runnable onReturnToMenu) {
        this.gameManager = gameManager;
        this.hud = new HUD();
        this.onReturnToMenu = onReturnToMenu;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setBackground(Constants.COLOR_BACKGROUND);
        setFocusable(true);

        setupKeyBindings();
        startGameLoop();
    }

    private void startGameLoop() {
        gameLoopTimer = new Timer(Constants.TIMER_DELAY_MS, e -> {
            if (gameManager.getGameState() == GameState.PLAYING && !showMap && !showInventory) {
                gameManager.update(Constants.TIMER_DELAY_MS);
            }
            repaint();
        });
        gameLoopTimer.start();
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Directional Movement: Arrows and WASD
        bindKey(im, am, "UP", "move_up", () -> gameManager.handlePlayerMove(Direction.UP));
        bindKey(im, am, "W", "move_w", () -> gameManager.handlePlayerMove(Direction.UP));
        bindKey(im, am, "DOWN", "move_down", () -> gameManager.handlePlayerMove(Direction.DOWN));
        bindKey(im, am, "S", "move_s", () -> gameManager.handlePlayerMove(Direction.DOWN));
        bindKey(im, am, "LEFT", "move_left", () -> gameManager.handlePlayerMove(Direction.LEFT));
        bindKey(im, am, "A", "move_a", () -> gameManager.handlePlayerMove(Direction.LEFT));
        bindKey(im, am, "RIGHT", "move_right", () -> gameManager.handlePlayerMove(Direction.RIGHT));
        bindKey(im, am, "D", "move_d", () -> gameManager.handlePlayerMove(Direction.RIGHT));

        // Action Keys
        bindKey(im, am, "SPACE", "attack", gameManager::handlePlayerAttack);
        bindKey(im, am, "E", "interact", gameManager::handlePlayerInteract);

        // Inventory Hotkeys 1 - 8
        for (int i = 1; i <= 8; i++) {
            final int slot = i - 1;
            bindKey(im, am, String.valueOf(i), "use_slot_" + i, () -> gameManager.handleUseInventoryItem(slot));
        }

        // Combat abilities and exploration overlays
        bindKey(im, am, "Q", "whirlwind", gameManager::handleWhirlwind);
        bindKey(im, am, "F", "firebolt", gameManager::handleFirebolt);
        bindKey(im, am, "H", "second_wind", gameManager::handleSecondWind);
        bindKey(im, am, "M", "map", () -> { showMap = !showMap; showInventory = false; });
        bindKey(im, am, "I", "inventory", () -> { showInventory = !showInventory; showMap = false; });
        bindKey(im, am, "X", "drop_item", this::showDropItemDialog);

        // Rule Editor Modal
        bindKey(im, am, "R", "rule_editor", this::openRuleEditor);

        // Pause Toggle
        bindKey(im, am, "P", "pause", this::togglePause);

        // Save progress shortcut
        bindKey(im, am, "K", "quick_save", this::quickSave);

        // Return to Menu / Escape
        bindKey(im, am, "ESCAPE", "menu", this::handleEscape);

        // Enter key for restart
        bindKey(im, am, "ENTER", "restart", this::handleRestart);
    }

    private void bindKey(InputMap im, ActionMap am, String key, String name, Runnable action) {
        im.put(KeyStroke.getKeyStroke(key), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean overlayToggle = name.equals("map") || name.equals("inventory") || name.equals("menu") || name.equals("pause") || name.equals("rule_editor") || name.equals("quick_save") || name.equals("drop_item");
                if (!showMap && !showInventory || overlayToggle) action.run();
                repaint();
            }
        });
    }

    private void openRuleEditor() {
        // A modal editor must not leave real-time enemy AI running in the background.
        boolean resumeAfterEditing = gameManager.getGameState() == GameState.PLAYING;
        if (resumeAfterEditing) gameManager.setGameState(GameState.PAUSED);
        try {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            RuleEditorDialog dialog = new RuleEditorDialog(parent, gameManager);
            dialog.setVisible(true);
        } finally {
            if (resumeAfterEditing && gameManager.getGameState() == GameState.PAUSED) {
                gameManager.setGameState(GameState.PLAYING);
            }
            requestFocusInWindow();
        }
    }

    /** Safe item-discard picker: enemy time freezes while the modal window is open. */
    private void showDropItemDialog() {
        dungeonescape.entities.Player player = gameManager.getPlayer();
        if (player == null || player.getInventory().isEmpty()) {
            gameManager.setMessage("Your backpack is empty.");
            return;
        }
        String[] options = new String[player.getInventory().size()];
        for (int i=0;i<options.length;i++) {
            options[i] = (i+1) + ". " + player.getInventory().get(i).getName();
        }
        boolean resume = gameManager.getGameState() == GameState.PLAYING;
        if (resume) gameManager.setGameState(GameState.PAUSED);
        try {
            Object selected = JOptionPane.showInputDialog(this, "Select an item to drop on the floor:",
                    "Manage backpack", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (selected != null) {
                for (int i=0;i<options.length;i++) if (options[i].equals(selected)) {
                    gameManager.handleDropInventoryItem(i);
                    break;
                }
            }
        } finally {
            if (resume && gameManager.getGameState() == GameState.PAUSED)
                gameManager.setGameState(GameState.PLAYING);
            requestFocusInWindow();
        }
    }

    private void togglePause() {
        if (gameManager.getGameState() == GameState.PLAYING) {
            gameManager.setGameState(GameState.PAUSED);
            gameManager.setMessage("Game PAUSED. Press 'P' to resume, 'K' to save.");
        } else if (gameManager.getGameState() == GameState.PAUSED) {
            gameManager.setGameState(GameState.PLAYING);
            gameManager.setMessage("Game resumed!");
        }
    }

    private void quickSave() {
        try {
            File saveFile = new File(Constants.DEFAULT_SAVE_FILE);
            gameManager.saveGame(saveFile);
        } catch (SaveLoadException e) {
            JOptionPane.showMessageDialog(this, "Save Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEscape() {
        if (showMap || showInventory) { showMap = false; showInventory = false; return; }
        if (onReturnToMenu != null) {
            onReturnToMenu.run();
        }
    }

    private void handleRestart() {
        showMap = false; showInventory = false;
        if (gameManager.getGameState() == GameState.GAME_OVER || gameManager.getGameState() == GameState.VICTORY) {
            if (gameManager.getDungeonMap() != null &&
                    gameManager.getDungeonMap().getCurrentRoomId().startsWith("ROOM_")) {
                gameManager.startClassicGame();
            } else {
                gameManager.startNewGame();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable crisp rendering hints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Room room = (gameManager.getDungeonMap() != null) ? gameManager.getDungeonMap().getCurrentRoom() : null;

        // 1. Render Current Room Tiles and Entities
        if (room != null) {
            room.render(g2, Constants.TILE_SIZE);
        }

        // 2. Render Player on top of floor/entities
        if (gameManager.getPlayer() != null && gameManager.getPlayer().isAlive()) {
            gameManager.getPlayer().render(g2, Constants.TILE_SIZE);
        }

        // 3. Render HUD Strip at the bottom
        hud.render(g2, gameManager, Constants.GAME_HEIGHT, Constants.WINDOW_WIDTH, Constants.HUD_HEIGHT);

        // Journal and map are visual overlays; the simulation pauses behind them.
        if (showMap) renderCampaignMap(g2);
        if (showInventory) renderInventory(g2);

        // 4. Render State Overlays (Paused, Game Over, Victory)
        GameState state = gameManager.getGameState();
        if (state == GameState.PAUSED) {
            renderPausedOverlay(g2);
        } else if (state == GameState.GAME_OVER) {
            renderGameOverOverlay(g2);
        } else if (state == GameState.VICTORY) {
            renderVictoryOverlay(g2);
        }
    }

    private void renderCampaignMap(Graphics2D g2) {
        g2.setColor(new Color(7, 11, 26, 239));
        g2.fillRoundRect(40, 35, 688, 502, 22, 22);
        g2.setColor(new Color(237, 199, 91));
        g2.setFont(new Font("SansSerif", Font.BOLD, 27));
        g2.drawString("CHRONICLES OF THE DUNGEON", 80, 79);
        if (gameManager.getDungeonMap()==null || gameManager.getDungeonMap().getRooms().size() < 15) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 19));
            g2.drawString("CLASSIC DUNGEON  /  4 ROOMS", 85, 156);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("Entrance  →  Corridor  →  Treasury  →  Boss", 85, 208);
            g2.drawString("Objective: " + gameManager.getQuest(), 85, 259);
            g2.drawString("Press M to close this map.", 85, 310);
            return;
        }
        String[][] ids = {{"F1_HAVEN", "F1_CROSSROADS", "F1_CRYPT", "F1_ARMORY", "F1_GUARDIAN"},
                         {"F2_CAMP", "F2_FORGE", "F2_LIBRARY", "F2_GARDEN", "F2_WARDEN"},
                         {"F3_OUTPOST", "F3_CATACOMBS", "F3_SHROUD", "F3_RELIQUARY", "F3_THRONE"}};
        String[] acts = {"ACT I  /  THE FALLEN", "ACT II  /  THE ASHEN", "ACT III  /  THE VOID"};
        for (int a = 0; a < 3; a++) {
            int y = 124 + a*120;
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(new Color(173, 197, 232));
            g2.drawString(acts[a], 85, y);
            for (int b = 0; b < 5; b++) {
                String id = ids[a][b];
                boolean here = gameManager.getDungeonMap()!=null && id.equals(gameManager.getDungeonMap().getCurrentRoomId());
                boolean visited = gameManager.hasVisited(id);
                int x = 83+b*127;
                g2.setColor(here ? new Color(221, 176, 76) : visited ? new Color(49, 116, 103) : new Color(44, 48, 69));
                g2.fillRoundRect(x,y+15,117,58,12,12);
                g2.setColor(here ? Color.BLACK : new Color(226, 237, 249));
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String name = gameManager.getDungeonMap() == null ? id : gameManager.getDungeonMap().getRoom(id).getName();
                if (!visited) name = "???";
                if (name.length()>17) name = name.substring(0,16)+"...";
                g2.drawString(name,x+7,y+39);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.drawString(here ? "CURRENT" : visited ? "DISCOVERED" : "UNEXPLORED",x+7,y+57);
            }
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Objective: " + gameManager.getQuest(), 84, 491);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Press M to close. Enemies pause while the map is open.", 85, 516);
    }

    private void renderInventory(Graphics2D g2) {
        g2.setColor(new Color(8, 11, 25, 241));
        g2.fillRoundRect(95, 28, 575, 527, 22, 22);
        g2.setColor(new Color(244, 213, 118));
        g2.setFont(new Font("SansSerif", Font.BOLD, 27));
        g2.drawString("ADVENTURER'S JOURNAL", 132, 77);
        dungeonescape.entities.Player p = gameManager.getPlayer();
        if (p == null) return;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString(String.format("LEVEL %d  |  XP %d / %d  |  GOLD %d",p.getLevel(), p.getExperience(), p.getExperienceRequired(),p.getCoins()), 135, 112);
        g2.drawString("Weapon: " + (p.getEquippedWeapon()==null ? "None" : p.getEquippedWeapon().getName()), 135, 140);
        g2.drawString("Armor: " + (p.getEquippedArmor()==null ? "None" : p.getEquippedArmor().getName()), 135, 163);
        g2.setColor(new Color(180, 222, 244));
        g2.drawString("BACKPACK", 135, 197);
        for (int i=0;i<p.getInventory().getCapacity();i++) {
            dungeonescape.items.Item item = p.getInventory().get(i);
            int y = 224 + i*26;
            g2.setColor(i%2==0 ? new Color(31, 37, 57) : new Color(24, 30, 47));
            g2.fillRoundRect(133, y-17, 493, 24, 5, 5);
            g2.setColor(Color.WHITE);
            g2.drawString((i+1)+". "+(item==null?"—":item.getName()), 147, y);
        }
        g2.setColor(new Color(255, 217, 109));
        g2.drawString("Quest keys: " + (p.getKeys().isEmpty() ? "None" : String.join(", ", p.getKeys())), 135, 457);
        g2.drawString("Optional side quest: Ancient relics " + p.getRelicsCollected() + "/3", 135, 476);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("1–8: use/equip    X: drop item    Q: Spin    F: Bolt    H: Heal", 135, 496);
        g2.drawString("Press I to close. Enemies pause while your journal is open.", 135, 519);
    }

    private void renderPausedOverlay(Graphics2D g2) {
        drawTranslucentShade(g2, new Color(10, 10, 20, 190));

        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2.setColor(new Color(255, 215, 0));
        drawCenteredString(g2, "GAME PAUSED", Constants.GAME_HEIGHT / 2 - 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        drawCenteredString(g2, "Press 'P' to Resume  |  'K' to Save  |  'R' for Rule Editor  |  'ESC' for Menu", Constants.GAME_HEIGHT / 2 + 20);
    }

    private void renderGameOverOverlay(Graphics2D g2) {
        drawTranslucentShade(g2, new Color(50, 10, 10, 210));

        g2.setFont(new Font("Serif", Font.BOLD, 44));
        g2.setColor(new Color(240, 50, 50));
        drawCenteredString(g2, "YOU DIED", Constants.GAME_HEIGHT / 2 - 40);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        String stats = String.format("Final Score: %d  |  Turns Taken: %d  |  Gold: %d",
                gameManager.getPlayer() != null ? gameManager.getPlayer().getScore() : 0,
                gameManager.getTurnCount(),
                gameManager.getPlayer() != null ? gameManager.getPlayer().getCoins() : 0);
        drawCenteredString(g2, stats, Constants.GAME_HEIGHT / 2 + 10);

        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(new Color(200, 200, 220));
        drawCenteredString(g2, "Press ENTER to Restart  |  Press ESC for Main Menu", Constants.GAME_HEIGHT / 2 + 50);
    }

    private void renderVictoryOverlay(Graphics2D g2) {
        drawTranslucentShade(g2, new Color(15, 35, 20, 210));

        g2.setFont(new Font("Serif", Font.BOLD, 46));
        g2.setColor(new Color(255, 215, 0));
        drawCenteredString(g2, "DUNGEON ESCAPED!", Constants.GAME_HEIGHT / 2 - 50);

        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(new Color(150, 240, 160));
        drawCenteredString(g2, "Victory! You conquered the dungeon and extracted alive!", Constants.GAME_HEIGHT / 2 - 10);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        String stats = String.format("Final Glory Score: %d  |  Total Gold: %d  |  Turns Taken: %d",
                gameManager.getPlayer() != null ? gameManager.getPlayer().getScore() : 0,
                gameManager.getPlayer() != null ? gameManager.getPlayer().getCoins() : 0,
                gameManager.getTurnCount());
        drawCenteredString(g2, stats, Constants.GAME_HEIGHT / 2 + 25);

        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(new Color(230, 230, 240));
        drawCenteredString(g2, "Press ENTER to Play Again  |  Press ESC for Main Menu", Constants.GAME_HEIGHT / 2 + 65);
    }

    private void drawTranslucentShade(Graphics2D g2, Color color) {
        g2.setColor(color);
        g2.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private void drawCenteredString(Graphics2D g2, String text, int y) {
        int stringWidth = g2.getFontMetrics().stringWidth(text);
        int x = (Constants.WINDOW_WIDTH - stringWidth) / 2;
        g2.drawString(text, x, y);
    }
}
