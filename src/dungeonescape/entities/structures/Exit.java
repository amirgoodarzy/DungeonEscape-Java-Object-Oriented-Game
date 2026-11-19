package dungeonescape.entities.structures;

import dungeonescape.map.Position;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Represents the final dungeon extraction portal.
 * Stepping onto this tile triggers the VICTORY game state.
 */
public class Exit extends StaticEntity {
    private static final long serialVersionUID = 1L;

    public Exit(Position position) {
        super("Dungeon Exit", position, true); // Passable so player can enter
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Golden exit portal with concentric radiant rings
        g.setColor(new Color(255, 215, 0, 180));
        g.fillOval(px + 4, py + 4, tileSize - 8, tileSize - 8);

        g.setColor(new Color(255, 255, 200));
        g.fillOval(px + 12, py + 12, tileSize - 24, tileSize - 24);

        g.setColor(Color.WHITE);
        g.drawOval(px + 4, py + 4, tileSize - 8, tileSize - 8);
    }
}
