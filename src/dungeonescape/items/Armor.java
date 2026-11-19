package dungeonescape.items;
import dungeonescape.entities.Player;
import dungeonescape.map.Position;
import java.awt.*;
/** Equippable armor; swapping cannot stack bonuses. */
public class Armor extends Equipment {
    private static final long serialVersionUID = 1L;
    private final int defenseBonus;
    public Armor(String name, Position position, int defenseBonus) {
        super(name, position, 75, "Armor: +" + defenseBonus + " Defense");
        this.defenseBonus = defenseBonus;
    }
    public int getDefenseBonus() { return defenseBonus; }
    @Override public String use(Player player) {
        player.equipArmor(this);
        return "Equipped " + getName() + " (Defense +" + defenseBonus + ")";
    }
    @Override public void render(Graphics2D g, int tileSize) {
        int x=getPosition().getX()*tileSize, y=getPosition().getY()*tileSize;
        g.setColor(new Color(61,172,221)); g.fillRoundRect(x+11,y+9,tileSize-22,tileSize-18,5,5);
        g.setColor(new Color(237,211,111)); g.drawLine(x+17,y+17,x+30,y+31);g.drawLine(x+30,y+17,x+17,y+31);
    }
}
