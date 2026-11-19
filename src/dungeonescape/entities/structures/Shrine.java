package dungeonescape.entities.structures;

import dungeonescape.behaviors.Interactable;
import dungeonescape.entities.Player;
import dungeonescape.map.Position;
import java.awt.*;
/** Interactive shop/healing shrine: converts earned gold into recovery, without infinite free healing. */
public final class Shrine extends StaticEntity implements Interactable {
    private static final long serialVersionUID = 1L;
    public static final int PRICE = 30;
    public Shrine(Position position) { super("Sanctuary Shrine", position, false); }
    @Override public String interact(Player p) {
        if (p.getHealth() >= p.getMaxHealth() && p.getEnergy() >= p.getMaxEnergy())
            return "Shrine: You are already fully rested. Keep your gold.";
        if (!p.spendCoins(PRICE)) return "Shrine: Recovery costs 30 gold. You have " + p.getCoins() + ".";
        int healed = p.heal(55), energy = p.restoreEnergy(50);
        return "Shrine blessing: +"+healed+" HP, +"+energy+" energy (-30 gold)";
    }
    @Override public void render(Graphics2D g,int t) {
        int x=getPosition().getX()*t,y=getPosition().getY()*t;
        g.setColor(new Color(59, 198, 179, 85));g.fillOval(x+4,y+4,t-8,t-8);
        g.setColor(new Color(137,216,211));g.fillRoundRect(x+16,y+9,t-32,t-17,4,4);
        g.setColor(new Color(232,217,146));g.fillRect(x+10,y+18,t-20,7);
        g.setColor(Color.WHITE);g.drawOval(x+4,y+4,t-8,t-8);
    }
}
