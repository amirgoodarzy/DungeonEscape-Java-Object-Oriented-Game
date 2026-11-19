package dungeonescape.tests;

import dungeonescape.managers.GameManager;
import dungeonescape.ui.GamePanel;
import dungeonescape.utils.Constants;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
/** GUI smoke test: paints main gameplay + both journal/map overlays without a native screen. */
public final class UiSmokeTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                GameManager game = new GameManager();
                game.startNewGame();
                GamePanel panel = new GamePanel(game, () -> {});
                panel.setSize(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT);
                String dir = args.length>0 ? args[0] : System.getProperty("java.io.tmpdir");
                draw(panel, new File(dir, "expanded_gameplay.png"));
                panel.getActionMap().get("map").actionPerformed(new ActionEvent(panel, 0, "map"));
                draw(panel, new File(dir, "expanded_map.png"));
                panel.getActionMap().get("inventory").actionPerformed(new ActionEvent(panel, 0, "inventory"));
                draw(panel, new File(dir, "expanded_inventory.png"));
                System.out.println("[PASS] Rendered game screen, map and inventory overlays");
            } catch (Exception exception) { throw new RuntimeException(exception); }
        });
        System.exit(0);
    }
    private static void draw(GamePanel panel, File output) throws Exception {
        BufferedImage image = new BufferedImage(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();
        try { panel.paint(g); } finally { g.dispose(); }
        ImageIO.write(image,"png",output);
    }
}
