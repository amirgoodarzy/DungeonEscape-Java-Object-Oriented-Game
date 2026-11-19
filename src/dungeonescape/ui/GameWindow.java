package dungeonescape.ui;

import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;
import dungeonescape.utils.Constants;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Top-level application window (JFrame) coordinating panel switching via CardLayout.
 */
public class GameWindow extends JFrame {
    private static final String PANEL_MENU = "MENU";
    private static final String PANEL_GAME = "GAME";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private final GameManager gameManager;
    private final MainMenuPanel menuPanel;
    private final GamePanel gamePanel;

    public GameWindow() {
        super("Dungeon Escape — Expanded Edition | Three Sigils Campaign");

        this.gameManager = new GameManager();
        this.cardLayout = new CardLayout();
        this.mainContainer = new JPanel(cardLayout);

        // Instantiate view panels with state transition callbacks
        this.menuPanel = new MainMenuPanel(gameManager, this::showGame);
        this.gamePanel = new GamePanel(gameManager, this::showMenu);

        mainContainer.add(menuPanel, PANEL_MENU);
        mainContainer.add(gamePanel, PANEL_GAME);

        add(mainContainer);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Center on screen

        showMenu();
    }

    public void showGame() {
        cardLayout.show(mainContainer, PANEL_GAME);
        gamePanel.requestFocusInWindow();
    }

    public void showMenu() {
        gameManager.setGameState(GameState.MAIN_MENU);
        cardLayout.show(mainContainer, PANEL_MENU);
    }
}
