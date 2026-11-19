package dungeonescape.tests;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Entity;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.items.Item;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import dungeonescape.managers.Difficulty;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;
import dungeonescape.entities.structures.Door;
import java.io.File;
import java.util.*;

/** End-to-end deterministic game-play tests exercising real input handlers, not teleports. */
public final class CampaignIntegrationTest {
    private static int checks;
    private static final Direction[] MOVES = {Direction.UP,Direction.DOWN,Direction.LEFT,Direction.RIGHT};
    private static void yes(boolean p, String why) { checks++; if (!p) throw new AssertionError(why); }
    private static boolean traversable(Room room, Position p, Player player) {
        if (!room.isWalkable(p.getX(),p.getY())) return false;
        Door door=room.getDoorAt(p);
        if (door!=null && door.isLocked() && !(player.hasKey(door.getRequiredKeyId()) || player.getInventory().hasItem(door.getRequiredKeyId()))) return false;
        for (Entity e: room.getEntitiesAt(p)) if (!e.isPassable() && !(e instanceof Enemy) && !(e instanceof Door)) return false;
        return true;
    }
    private static List<Direction> route(Room r, Position start, Position end, Player p) {
        Map<Position,Position> prev=new HashMap<>();
        Map<Position,Direction> direction=new HashMap<>();
        ArrayDeque<Position> q=new ArrayDeque<>(); q.add(start);prev.put(start,start);
        while(!q.isEmpty()) {
            Position curr=q.remove(); if(curr.equals(end)) break;
            for(Direction d:MOVES) {
                Position next=curr.translate(d);
                if(!prev.containsKey(next) && traversable(r,next,p)) {prev.put(next,curr);direction.put(next,d);q.add(next);}
            }
        }
        if(!prev.containsKey(end)) throw new AssertionError("No path in " + r.getId() + " from " + start + " to " + end);
        List<Direction> steps=new ArrayList<>(); Position curr=end;
        while(!curr.equals(start)) {steps.add(direction.get(curr));curr=prev.get(curr);}
        Collections.reverse(steps);return steps;
    }
    private static void walkTo(GameManager g, Position target) {
        String fromRoom=g.getDungeonMap().getCurrentRoomId();
        int moves=0;
        while(fromRoom.equals(g.getDungeonMap().getCurrentRoomId()) && !target.equals(g.getPlayer().getPosition())) {
            if(++moves>250) throw new AssertionError("Stuck at " + g.getPlayer().getPosition()+ " in " + fromRoom + " toward " + target+": " + g.getStatusMessage());
            List<Direction> path=route(g.getDungeonMap().getCurrentRoom(),g.getPlayer().getPosition(),target,g.getPlayer());
            if(path.isEmpty()) break;
            int before=g.getTurnCount();g.handlePlayerMove(path.get(0));
            if(g.getGameState()==GameState.GAME_OVER) throw new AssertionError("Died en route to " + target);
            if(g.getTurnCount()==before && g.getDungeonMap().getCurrentRoomId().equals(fromRoom)) {
                // Opening a locked gate costs one input, but should not repeatedly fail.
                g.handlePlayerMove(path.get(0));
            }
        }
    }
    private static void enter(GameManager g, Position door, String expected) {
        walkTo(g,door);
        yes(expected.equals(g.getDungeonMap().getCurrentRoomId()), "Failed to enter "+expected+"; now in "+g.getDungeonMap().getCurrentRoomId()+": "+g.getStatusMessage());
        yes(g.hasVisited(expected),"Room missing from explored map " + expected);
    }
    private static void equip(GameManager g,String name) {
        for (int i=0;i<g.getPlayer().getInventory().getCapacity();i++) {
            Item item=g.getPlayer().getInventory().get(i);
            if(item!=null&&item.getName().equals(name)){g.handleUseInventoryItem(i);return;}
        }
        throw new AssertionError("Not found in inventory: "+name);
    }
    private static void defeatBoss(GameManager g) {
        Room r=g.getDungeonMap().getCurrentRoom();
        Boss boss=null;
        for(Entity e:r.getEntities()) if(e instanceof Boss) boss=(Boss)e;
        yes(boss!=null,"No boss in "+r.getId());
        Position target=boss.getPosition();
        // Approach any safely reachable cardinal-adjacent tile.
        Position attackFrom=null;
        for(Direction d:MOVES) {
            Position candidate=target.translate(d);
            if(!traversable(r,candidate,g.getPlayer())) continue;
            try {route(r,g.getPlayer().getPosition(),candidate,g.getPlayer());attackFrom=candidate;break;}
            catch(AssertionError ignored) { }
        }
        yes(attackFrom!=null,"Boss inaccessible in " + r.getId());
        walkTo(g,attackFrom);
        int steps=0;
        while(boss.isAlive()&&steps++<20) {
            for(Direction d:MOVES) if(g.getPlayer().getPosition().translate(d).equals(boss.getPosition())) {
                g.handlePlayerMove(d);break;
            }
        }
        yes(!boss.isAlive(),"Boss couldn't be defeated in " + r.getId());
    }
    private static void fullRun() throws Exception {
        GameManager game=new GameManager(); game.setDifficulty(Difficulty.NORMAL);game.startNewGame();
        Player p=game.getPlayer();
        // Strong tester attacks prevent this navigation test becoming a combat balance test.
        p.setAttackPower(200);
        p.setMaxHealth(500);p.heal(500);
        yes(game.getDungeonMap().getRooms().size()==15,"Campaign must have exactly 15 interconnected rooms");
        String[][] ids={{"F1_HAVEN","F1_CROSSROADS","F1_CRYPT","F1_ARMORY","F1_GUARDIAN"},
            {"F2_CAMP","F2_FORGE","F2_LIBRARY","F2_GARDEN","F2_WARDEN"},
            {"F3_OUTPOST","F3_CATACOMBS","F3_SHROUD","F3_RELIQUARY","F3_THRONE"}};
        String[] sigils={"Ember Sigil","Frost Sigil","Void Sigil"};
        String[] swords={"Forged Saber","Glacier Blade","Starfall Greatsword"};
        String[] armors={"Scout's Vest","Dragonhide Armor","Celestial Plate"};
        for(int a=0;a<3;a++) {
            yes(ids[a][0].equals(game.getDungeonMap().getCurrentRoomId()),"Act start mismatch");
            enter(game,new Position(15,6),ids[a][1]);
            // The gateway cannot be opened before finding its sigil.
            walkTo(game,new Position(14,6));
            game.handlePlayerMove(Direction.RIGHT);
            yes(ids[a][1].equals(game.getDungeonMap().getCurrentRoomId()),"Boss door bypassed without key");
            enter(game,new Position(8,0),ids[a][2]);
            walkTo(game,new Position(12,3));
            yes(p.hasKey(sigils[a]),"Sigil couldn't be picked up: " + sigils[a]);
            walkTo(game,new Position(3,3));
            enter(game,new Position(8,11),ids[a][1]);
            enter(game,new Position(8,11),ids[a][3]);
            walkTo(game,new Position(12,8));
            equip(game,swords[a]);
            walkTo(game,new Position(3,8));
            equip(game,armors[a]);
            enter(game,new Position(8,0),ids[a][1]);
            // Try unlocked gateway and cross into guardian room.
            enter(game,new Position(15,6),ids[a][4]);
            yes(game.getQuest().contains("Defeat"),"Quest should identify boss objective");
            // Guardian exit should be sealed while boss lives.
            if(a<2) {
                walkTo(game,new Position(14,6));
                game.handlePlayerMove(Direction.RIGHT);
                yes(ids[a][4].equals(game.getDungeonMap().getCurrentRoomId()),"Guardian barrier bypassed");
            }
            defeatBoss(game);
            yes(p.getLevel()>1,"Combat should award XP levels");
            if(a<2) {
                enter(game,new Position(15,6),ids[a+1][0]);
            } else {
                enter(game,new Position(14,6),"F3_THRONE");
                yes(game.getGameState()==GameState.VICTORY,"Victory portal must end campaign");
            }
        }
        yes(game.getVisitedRooms().size()==15,"Not every room was visited");
        yes(p.getRelicsCollected()==3,"Collect three optional relics");
        yes(p.getEquippedArmor()!=null && p.getEquippedArmor().getName().equals(armors[2]),"Highest tier armor not equipped");
        yes(p.getEquippedWeapon()!=null && p.getEquippedWeapon().getName().equals(swords[2]),"Highest tier weapon not equipped");
        System.out.println("[PASS] Full 15-room campaign, all 3 sigils, treasure/gear, 3 bosses, final victory");
    }
    private static void saveAndDifficulty() throws Exception {
        GameManager easy=new GameManager();easy.setDifficulty(Difficulty.EASY);easy.startNewGame();
        GameManager hard=new GameManager();hard.setDifficulty(Difficulty.HARD);hard.startNewGame();
        Boss e=(Boss)easy.getDungeonMap().getRoom("F1_GUARDIAN").getEntities().stream().filter(x->x instanceof Boss).findFirst().orElseThrow();
        Boss h=(Boss)hard.getDungeonMap().getRoom("F1_GUARDIAN").getEntities().stream().filter(x->x instanceof Boss).findFirst().orElseThrow();
        yes(h.getMaxHealth()>e.getMaxHealth() && h.getAttackPower()>e.getAttackPower(),"Difficulties should scale enemy strengths");
        easy.getPlayer().storeKey("Ember Sigil");easy.getPlayer().gainExperience(120);
        easy.getPlayer().spendEnergy(50);
        easy.handlePlayerMove(Direction.RIGHT);
        File temp=File.createTempFile("dungeonescape_campaign_", ".sav");
        try {
            easy.saveGame(temp);
            GameManager restored=new GameManager();restored.loadGame(temp);
            yes(restored.getDifficulty()==Difficulty.EASY,"Difficulty didn't survive load");
            yes(restored.getPlayer().hasKey("Ember Sigil"),"Keyring didn't survive load");
            yes(restored.getPlayer().getLevel()==easy.getPlayer().getLevel(),"XP didn't survive load");
            yes(restored.getPlayer().getEnergy()==easy.getPlayer().getEnergy(),"Energy didn't survive load");
            yes(restored.getVisitedRooms().equals(easy.getVisitedRooms()),"Exploration progress didn't survive load");
            yes(restored.getDungeonMap().getRooms().size()==15,"Wrong map after load");
        } finally {temp.delete();}
        System.out.println("[PASS] 3 difficulty tiers, persistent keys, level/energy and exploration in save/load");
    }
    public static void main(String[] args) throws Exception {
        fullRun();saveAndDifficulty();
        System.out.println("CAMPAIGN INTEGRATION TESTS: "+checks+" ASSERTIONS PASSED");
    }
}
