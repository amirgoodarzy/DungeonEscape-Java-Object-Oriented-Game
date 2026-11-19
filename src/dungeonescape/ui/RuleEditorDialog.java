package dungeonescape.ui;

import dungeonescape.exceptions.GameException;
import dungeonescape.exceptions.SaveLoadException;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.SaveLoadManager;
import dungeonescape.rules.Rule;
import dungeonescape.rules.RuleEngine;
import dungeonescape.rules.dsl.Lexer;
import dungeonescape.rules.dsl.Parser;
import dungeonescape.rules.dsl.Token;
import dungeonescape.utils.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Interactive Swing GUI modal allowing players to author, test, validate,
 * enable/disable, and save genuinely new game rules using the custom DSL.
 * 
 * Demonstrates:
 * - GUI event-driven programming with Swing
 * - Real-time parsing and validation using RuleEngine
 * - Checked exception presentation to the user
 */
public class RuleEditorDialog extends JDialog {
    private final GameManager gameManager;
    private final RuleEngine ruleEngine;

    private DefaultListModel<String> listModel;
    private JList<String> rulesList;
    private JTextField nameField;
    private JTextArea ruleCodeArea;
    private JLabel statusLabel;
    private JComboBox<String> templateCombo;

    public RuleEditorDialog(Frame parent, GameManager gameManager) {
        super(parent, "Dungeon Rule Engine — Player-Defined Rules Editor", true);
        this.gameManager = gameManager;
        this.ruleEngine = gameManager.getRuleEngine();

        setSize(820, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        initComponents();
        refreshRuleList();
    }

    private void initComponents() {
        // --- Left Panel: Rules List and Management ---
        JPanel leftPanel = new JPanel(new BorderLayout(6, 6));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Active Custom Rules"));
        leftPanel.setPreferredSize(new Dimension(260, 400));

        listModel = new DefaultListModel<>();
        rulesList = new JList<>(listModel);
        rulesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        rulesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRule();
        });

        leftPanel.add(new JScrollPane(rulesList), BorderLayout.CENTER);

        JPanel listButtons = new JPanel(new GridLayout(2, 2, 4, 4));
        JButton btnNew = new JButton("New Rule");
        JButton btnToggle = new JButton("Toggle Active");
        JButton btnDelete = new JButton("Delete");
        JButton btnExport = new JButton("Save Script");

        btnNew.addActionListener(e -> clearEditor());
        btnToggle.addActionListener(e -> toggleSelectedRule());
        btnDelete.addActionListener(e -> deleteSelectedRule());
        btnExport.addActionListener(e -> exportRulesToFile());

        listButtons.add(btnNew);
        listButtons.add(btnToggle);
        listButtons.add(btnDelete);
        listButtons.add(btnExport);
        leftPanel.add(listButtons, BorderLayout.SOUTH);

        // --- Right Panel: Rule Source Editor ---
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(BorderFactory.createTitledBorder("DSL Rule Editor"));

        // Header: Rule name & template selector
        JPanel headerPanel = new JPanel(new BorderLayout(6, 6));
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        namePanel.add(new JLabel("Rule Name:"));
        nameField = new JTextField(18);
        namePanel.add(nameField);

        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        templatePanel.add(new JLabel("Template:"));
        templateCombo = new JComboBox<>(new String[]{
                "-- Select Template --",
                "Berserker Rage (Low HP -> ATK+)",
                "Greed Bonus (Coins -> Score*)",
                "Ghost Ambush (Room + HP -> Spawn)",
                "Master Key (Item -> Unlock Door)"
        });
        templateCombo.addActionListener(e -> applyTemplate());
        templatePanel.add(templateCombo);

        headerPanel.add(namePanel, BorderLayout.WEST);
        headerPanel.add(templatePanel, BorderLayout.EAST);
        rightPanel.add(headerPanel, BorderLayout.NORTH);

        // Code Editor Text Area
        ruleCodeArea = new JTextArea();
        ruleCodeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ruleCodeArea.setBackground(new Color(28, 28, 36));
        ruleCodeArea.setForeground(new Color(220, 230, 240));
        ruleCodeArea.setCaretColor(Color.WHITE);
        ruleCodeArea.setText("RULE \"My Custom Rule\":\nWHEN player.health < 40\nTHEN player.attack = player.attack + 10; message(\"Power surging!\")");
        rightPanel.add(new JScrollPane(ruleCodeArea), BorderLayout.CENTER);

        // Footer: Validation status and action buttons
        JPanel footerPanel = new JPanel(new BorderLayout(4, 4));
        statusLabel = new JLabel("Status: Ready to edit or compile rules.");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(100, 180, 255));
        footerPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnValidate = new JButton("Validate Syntax");
        JButton btnSave = new JButton("Save & Activate Rule");
        JButton btnClose = new JButton("Close Editor");

        btnValidate.addActionListener(e -> validateRuleText());
        btnSave.addActionListener(e -> saveAndActivateRule());
        btnClose.addActionListener(e -> dispose());

        actionButtons.add(btnValidate);
        actionButtons.add(btnSave);
        actionButtons.add(btnClose);
        footerPanel.add(actionButtons, BorderLayout.SOUTH);

        rightPanel.add(footerPanel, BorderLayout.SOUTH);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(260);
        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshRuleList() {
        listModel.clear();
        for (Rule rule : ruleEngine.getRules()) {
            String status = rule.isEnabled() ? "[ON] " : "[OFF] ";
            listModel.addElement(status + rule.getName());
        }
    }

    private void loadSelectedRule() {
        int index = rulesList.getSelectedIndex();
        if (index >= 0 && index < ruleEngine.getRules().size()) {
            Rule rule = ruleEngine.getRules().get(index);
            nameField.setText(rule.getName());
            ruleCodeArea.setText(rule.getSourceText());
            statusLabel.setText("Loaded: " + rule.getName() + " (Executed " + rule.getExecutionCount() + " times)");
            statusLabel.setForeground(new Color(100, 200, 255));
        }
    }

    private void clearEditor() {
        rulesList.clearSelection();
        nameField.setText("");
        ruleCodeArea.setText("RULE \"New Rule\":\nWHEN \nTHEN ");
        statusLabel.setText("Enter your custom condition and action, then click 'Save & Activate'.");
        statusLabel.setForeground(Color.LIGHT_GRAY);
    }

    private void toggleSelectedRule() {
        int index = rulesList.getSelectedIndex();
        if (index >= 0 && index < ruleEngine.getRules().size()) {
            Rule rule = ruleEngine.getRules().get(index);
            rule.setEnabled(!rule.isEnabled());
            refreshRuleList();
            rulesList.setSelectedIndex(index);
        }
    }

    private void deleteSelectedRule() {
        int index = rulesList.getSelectedIndex();
        if (index >= 0 && index < ruleEngine.getRules().size()) {
            Rule rule = ruleEngine.getRules().get(index);
            ruleEngine.removeRule(rule.getName());
            refreshRuleList();
            clearEditor();
            statusLabel.setText("Deleted rule: " + rule.getName());
            statusLabel.setForeground(Color.ORANGE);
        }
    }

    private boolean validateRuleText() {
        String code = ruleCodeArea.getText().trim();
        if (code.isEmpty()) {
            statusLabel.setText("Error: Rule code cannot be empty!");
            statusLabel.setForeground(Color.RED);
            return false;
        }

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens, code);
            Rule rule = parser.parseRule();

            statusLabel.setText("Validation SUCCESS: Rule '" + rule.getName() + "' is syntactically & semantically valid!");
            statusLabel.setForeground(new Color(50, 205, 50));
            return true;
        } catch (GameException e) {
            statusLabel.setText("Compile Error: " + e.getMessage());
            statusLabel.setForeground(new Color(255, 70, 70));
            return false;
        }
    }

    private void saveAndActivateRule() {
        if (!validateRuleText()) {
            return;
        }

        String code = ruleCodeArea.getText().trim();
        try {
            Rule rule = ruleEngine.registerRuleFromSource(code);
            refreshRuleList();
            statusLabel.setText("ACTIVE: Rule '" + rule.getName() + "' successfully compiled and added to engine!");
            statusLabel.setForeground(new Color(50, 220, 100));
            gameManager.setMessage("Custom rule activated: " + rule.getName());
        } catch (GameException e) {
            statusLabel.setText("Registration Error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void exportRulesToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(Constants.DEFAULT_RULES_FILE));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                SaveLoadManager.saveRulesScript(file, ruleEngine.exportScript());
                JOptionPane.showMessageDialog(this, "Rules successfully saved to " + file.getName());
            } catch (SaveLoadException e) {
                JOptionPane.showMessageDialog(this, "Failed to save rules: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyTemplate() {
        int selected = templateCombo.getSelectedIndex();
        switch (selected) {
            case 1:
                nameField.setText("Berserker Rage");
                ruleCodeArea.setText("RULE \"Berserker Rage\":\nWHEN player.health < 25\nTHEN player.attack = player.attack + 15; message(\"Berserker rage activated!\")");
                break;
            case 2:
                nameField.setText("Greed Multiplier");
                ruleCodeArea.setText("RULE \"Greed Multiplier\":\nWHEN player.coins >= 50\nTHEN player.score = player.score * 2; message(\"Greed rewarded! Score doubled!\")");
                break;
            case 3:
                nameField.setText("Ghost Ambush");
                ruleCodeArea.setText("RULE \"Ghost Ambush\":\nWHEN player.room == \"The Ancient Treasury\" AND player.health < 50\nTHEN spawn(\"Ghost\", 4, 3); message(\"A vengeful spirit awakens!\")");
                break;
            case 4:
                nameField.setText("Master Key");
                ruleCodeArea.setText("RULE \"Master Key\":\nWHEN player.hasItem(\"Golden Key\")\nTHEN unlock(\"Gilded Skull Door\"); message(\"The Golden Key unlocked the portal!\")");
                break;
        }
    }
}
