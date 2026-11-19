package dungeonescape.entities.npc;

import dungeonescape.behaviors.Interactable;
import dungeonescape.entities.LivingEntity;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;
import dungeonescape.map.Position;
import dungeonescape.utils.AudioPlayer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Peaceful non-player character that provides hints, lore, and dialogue.
 * Demonstrates:
 * - Subtyping LivingEntity
 * - Implementing Interactable while deliberately NOT implementing Attackable
 *   (demonstrates Interface Segregation Principle: peaceful entities shouldn't have combat methods)
 * - Information Hiding and state progression
 */
public class NPC extends LivingEntity implements Interactable {
    private static final long serialVersionUID = 1L;

    private final List<String> dialogueLines;
    private int dialogueIndex;

    public NPC(String name, Position position, List<String> dialogueLines) {
        super(name, position, 50, 0, 10);
        this.dialogueLines = new ArrayList<>();
        if (dialogueLines != null) {
            this.dialogueLines.addAll(dialogueLines);
        }
        this.dialogueIndex = 0;
    }

    public List<String> getDialogueLines() {
        return Collections.unmodifiableList(dialogueLines);
    }

    public void addDialogue(String line) {
        if (line != null && !line.trim().isEmpty()) {
            this.dialogueLines.add(line);
        }
    }

    @Override
    public String interact(Player player) throws GameException {
        if (dialogueLines.isEmpty()) {
            return String.format("%s nods quietly in your direction.", getName());
        }

        String line = dialogueLines.get(dialogueIndex);
        dialogueIndex = (dialogueIndex + 1) % dialogueLines.size(); // Cycle dialogue
        AudioPlayer.playTone(400, 80, 0.4);
        return String.format("%s: \"%s\"", getName(), line);
    }

    @Override
    public void update(long deltaMs) {
        // NPCs are peaceful and stationary in this vertical slice
    }

    @Override
    public void render(Graphics2D g, int tileSize) {
        int px = getPosition().getX() * tileSize;
        int py = getPosition().getY() * tileSize;

        // Shadow
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(px + 10, py + tileSize - 8, tileSize - 20, 6);

        // Sage robe (Emerald green)
        g.setColor(new Color(34, 139, 34));
        g.fillRoundRect(px + 12, py + 14, tileSize - 24, tileSize - 18, 6, 6);

        // Head
        g.setColor(new Color(245, 222, 179));
        g.fillOval(px + 15, py + 8, tileSize - 30, tileSize - 30);

        // Beard (White wizard beard)
        g.setColor(new Color(240, 240, 250));
        g.fillArc(px + 16, py + 18, tileSize - 32, 14, 180, 180);

        // Pointy Wizard Hat (Purple)
        g.setColor(new Color(106, 90, 205));
        int[] hatX = {px + tileSize / 2, px + 8, px + tileSize - 8};
        int[] hatY = {py + 1, py + 10, py + 10};
        g.fillPolygon(hatX, hatY, 3);
    }
}
