package dungeonescape.items;
import dungeonescape.entities.Player;
import dungeonescape.exceptions.GameException;
import dungeonescape.map.Position;
import java.awt.*;
/** Refill energy used by combat skills. */
public class EnergyPotion extends Consumable {
    private static final long serialVersionUID = 1L;
    private final int amount;
    public EnergyPotion(Position position,int amount) {
        super("Mana Elixir",position,30,"Restores "+amount+" energy");this.amount=amount;
    }
    @Override public String use(Player p) throws GameException {
        if (p.getEnergy() >= p.getMaxEnergy()) throw new GameException("Energy already full");
        int gain=p.restoreEnergy(amount);p.getInventory().removeItem(this);
        return "Drank Mana Elixir ("+gain+" energy restored)";
    }
    @Override public void render(Graphics2D g,int t) {
        int x=getPosition().getX()*t,y=getPosition().getY()*t;
        g.setColor(Color.LIGHT_GRAY);g.fillRect(x+21,y+9,7,9);
        g.setColor(new Color(72,193,255));g.fillOval(x+13,y+17,t-26,t-25);
        g.setColor(Color.WHITE);g.drawOval(x+13,y+17,t-26,t-25);
    }
}
