package dungeonescape.entities.structures;

import dungeonescape.map.Position;
import dungeonescape.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Solid impassable wall segment.
 * Demonstrates inheritance and concrete rendering specialization.
 */
public class Wall extends StaticEntity {
    private static final long serialVersionUID = 1L;

    public Wall(Position position) {
        super("Wall", position, false); // impassable
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Base block
        g.setColor(Constants.COLOR_WALL);
        g.fillRect(px, py, tileSize, tileSize);

        // Brick mortar highlights
        g.setColor(new Color(50, 50, 60));
        g.drawRect(px, py, tileSize, tileSize);
        g.drawLine(px, py + tileSize / 2, px + tileSize, py + tileSize / 2);
        g.drawLine(px + tileSize / 2, py, px + tileSize / 2, py + tileSize / 2);
        g.drawLine(px + tileSize / 4, py + tileSize / 2, px + tileSize / 4, py + tileSize);
        g.drawLine(px + (3 * tileSize) / 4, py + tileSize / 2, px + (3 * tileSize) / 4, py + tileSize);
    }
}
