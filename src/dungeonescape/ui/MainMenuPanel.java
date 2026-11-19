package dungeonescape.ui;

import dungeonescape.exceptions.SaveLoadException;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;
import dungeonescape.managers.Difficulty;
import dungeonescape.utils.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Main Menu screen presenting game startup options, persistence access,
 * instruction manuals, and the player-defined Rule Editor.
 */
public class MainMenuPanel extends JPanel {
    private final GameManager gameManager;
    private final Runnable onStartGame;

    public MainMenuPanel(GameManager gameManager, Runnable onStartGame) {
        this.gameManager = gameManager;
        this.onStartGame = onStartGame;

        setBackground(Constants.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // --- Header Title ---
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Constants.COLOR_BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(60, 20, 30, 20));

        JLabel titleLabel = new JLabel("DUNGEON ESCAPE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 42));
        titleLabel.setForeground(new Color(240, 200, 80));

        JLabel subtitleLabel = new JLabel("EXPANDED EDITION  •  THE THREE SIGILS", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(160, 160, 180));

        JLabel ruleEngineLabel = new JLabel("Powered by Player-Defined Custom Rule Engine (DSL & AST)", SwingConstants.CENTER);
        ruleEngineLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        ruleEngineLabel.setForeground(new Color(80, 180, 240));

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.CENTER);
        titlePanel.add(ruleEngineLabel, BorderLayout.SOUTH);
        add(titlePanel, BorderLayout.NORTH);

        // --- Center Menu Buttons ---
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Constants.COLOR_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        JButton btnNewGame = createMenuButton("NEW CAMPAIGN  •  3 ACTS / 15 ROOMS");
        JButton btnClassic = createMenuButton("Classic Dungeon  •  4 ROOMS");
        JButton btnLoadGame = createMenuButton("Load Saved Game");
        JButton btnRuleEditor = createMenuButton("Custom Rule Editor (DSL)");
        JButton btnHelp = createMenuButton("Instructions & Controls");
        JButton btnExit = createMenuButton("Exit Game");

        btnNewGame.addActionListener(e -> {
            String[] options = {"Explorer (Easy)", "Adventurer (Normal)", "Nightmare (Hard)"};
            int selected = JOptionPane.showOptionDialog(this,
                    "Choose your adventure difficulty:", "NEW CAMPAIGN",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (selected < 0) return;
            gameManager.setDifficulty(Difficulty.values()[selected]);
            gameManager.startNewGame();
            if (onStartGame != null) onStartGame.run();
        });

        btnClassic.addActionListener(e -> {
            gameManager.setDifficulty(Difficulty.NORMAL);
            gameManager.startClassicGame();
            if (onStartGame != null) onStartGame.run();
        });
        btnLoadGame.addActionListener(e -> handleLoadGame());

        btnRuleEditor.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            RuleEditorDialog dialog = new RuleEditorDialog(parentFrame, gameManager);
            dialog.setVisible(true);
        });

        btnHelp.addActionListener(e -> showHelpDialog());

        btnExit.addActionListener(e -> System.exit(0));

        gbc.gridy = 0; buttonPanel.add(btnNewGame, gbc);
        gbc.gridy = 1; buttonPanel.add(btnClassic, gbc);
        gbc.gridy = 2; buttonPanel.add(btnLoadGame, gbc);
        gbc.gridy = 3; buttonPanel.add(btnRuleEditor, gbc);
        gbc.gridy = 4; buttonPanel.add(btnHelp, gbc);
        gbc.gridy = 5; buttonPanel.add(btnExit, gbc);

        add(buttonPanel, BorderLayout.CENTER);

        // --- Footer ---
        JLabel footerLabel = new JLabel("Pure Java SE 11+ • Swing & Java2D • Zero External Dependencies", SwingConstants.CENTER);
        footerLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(110, 110, 125));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        add(footerLabel, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(300, 42));
        button.setFocusPainted(false);
        button.setBackground(new Color(45, 48, 62));
        button.setForeground(Color.WHITE);
        return button;
    }

    private void handleLoadGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(Constants.DEFAULT_SAVE_FILE));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                gameManager.loadGame(chooser.getSelectedFile());
                if (onStartGame != null) onStartGame.run();
            } catch (SaveLoadException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load save file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showHelpDialog() {
        String helpText =
                "DUNGEON ESCAPE EXPANDED - CONTROLS\n\n" +
                "WASD / ARROWS: Move; SPACE: Melee attack; E: Interact\n" +
                "Q: Whirlwind (34 energy) - attack nearby enemies\n" +
                "F: Firebolt (25 energy) - 5-tile ranged attack\n" +
                "H: Second Wind (45 energy) - heal yourself\n" +
                "1-8: Use items / equip armor and weapons; X: Drop an item\n" +
                "M: Campaign map; I: Inventory & journal\n" +
                "P: Pause; K: Quick save; R: Custom rule editor; ESC: Menu\n\n" +
                "CAMPAIGN OBJECTIVE\n" +
                "Explore 3 acts, 15 chambers and 3 guardian fights.\n" +
                "Find each sigil in the northern branch, collect equipment\n" +
                "in the southern branch, then unlock the eastern boss gate.\n" +
                "Defeat the guardian to unlock the passage to the next act.\n" +
                "Reach the last portal after defeating the Void Sovereign.\n\n" +
                "Collect 3 ancient relics as an optional side quest.\n" +
                "Use sanctuary shrines (E) to recover for 30 gold.\n" +
                "Energy regenerates over time. XP increases your stats.\n" +
                "Quest keys use a dedicated keyring, not backpack slots.\n" +
                "Rules (R) change attributes through the original DSL.";

        JOptionPane.showMessageDialog(this, helpText, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }
}
