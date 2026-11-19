package dungeonescape.ui;
import dungeonescape.entities.Player;
import dungeonescape.items.Item;
import dungeonescape.managers.GameManager;
import dungeonescape.map.Room;
import dungeonescape.utils.Constants;
import java.awt.*;
/** Three-resource HUD + objectives, equipment, hotbar and messages. */
public class HUD {
    private static final Font LABEL = new Font("SansSerif", Font.BOLD, 12);
    private static final Font SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public void render(Graphics2D g, GameManager game, int y, int width, int height) {
        g.setColor(new Color(12,15,25));g.fillRect(0,y,width,height);
        Player p=game.getPlayer();if(p==null)return;
        bar(g,"HEALTH",p.getHealth(),p.getMaxHealth(),new Color(81,202,100),14,y+10);
        bar(g,"ENERGY",p.getEnergy(),p.getMaxEnergy(),new Color(68,166,233),14,y+34);
        bar(g,"XP",p.getExperience(),p.getExperienceRequired(),new Color(239,183,67),14,y+58);
        g.setColor(Color.WHITE); g.setFont(LABEL);
        g.drawString("LVL "+p.getLevel()+"   ATK "+p.getTotalAttackPower()+"   DEF "+p.getDefense()+"   GOLD "+p.getCoins()+"   SCORE "+p.getScore(),211,y+18);
        Room r=game.getDungeonMap()==null?null:game.getDungeonMap().getCurrentRoom();
        g.setColor(new Color(163,206,244));
        g.drawString((r==null?"Dungeon":r.getName())+"    /    "+game.getDifficulty(),211,y+38);
        g.setColor(new Color(236,216,150)); g.setFont(SMALL);
        g.drawString(game.getQuest(),211,y+56);
        for(int i=0;i<8;i++) {
            int x=211+i*67;
            Item item=p.getInventory().get(i);
            g.setColor(item!=null?new Color(50,58,77):new Color(24,27,42));
            g.fillRoundRect(x,y+66,62,29,5,5);
            g.setColor(new Color(96,108,133));g.drawRoundRect(x,y+66,62,29,5,5);
            g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,10));
            g.drawString(""+(i+1),x+4,y+77);
            if(item!=null) {
                String name=item.getName(); if(name.length()>8)name=name.substring(0,7)+"…";
                g.drawString(name,x+4,y+90);
            }
        }
        g.setColor(new Color(167,184,205)); g.setFont(SMALL);
        g.drawString("WASD move   SPACE attack   Q spin   F bolt   H heal   M map   I bag   X drop   R rules   K save",14,y+114);
        String msg=game.getStatusMessage();if(msg==null)msg="";
        g.setColor(new Color(255,233,154));
        if(g.getFontMetrics().stringWidth(msg)>width-28) {
            while (msg.length()>3 && g.getFontMetrics().stringWidth(msg+"...")>width-28)msg=msg.substring(0,msg.length()-1);
            msg+="...";
        }
        g.drawString(msg,14,y+135);
    }
    private void bar(Graphics2D g,String label,int amount,int max,Color color,int x,int y) {
        g.setColor(new Color(184,195,211));g.setFont(SMALL);
        g.drawString(label,x,y+10);
        int bx=x+62;g.setColor(new Color(39,42,58));g.fillRoundRect(bx,y,123,13,5,5);
        g.setColor(color);g.fillRoundRect(bx,y,(int)(123.0*Math.max(0,amount)/Math.max(1,max)),13,5,5);
        g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,10));
        g.drawString(amount+"/"+max,bx+39,y+10);
    }
}
