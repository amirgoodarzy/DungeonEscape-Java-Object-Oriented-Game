package dungeonescape.tests;

import dungeonescape.behaviors.Direction;
import dungeonescape.entities.Entity;
import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.entities.enemies.RangedEnemy;
import dungeonescape.entities.structures.Door;
import dungeonescape.items.Armor;
import dungeonescape.items.Weapon;
import dungeonescape.managers.CollisionManager;
import dungeonescape.managers.Difficulty;
import dungeonescape.managers.GameManager;
import dungeonescape.managers.GameState;
import dungeonescape.map.DungeonMap;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/** Real-input integration playthrough of the expanded 3-act / 15-room campaign. */
public final class ExpandedCampaignTest {
    private static int assertions = 0;
    private static void ok(boolean condition,String reason) {
        assertions++;
        if (!condition) throw new AssertionError(reason);
    }
    private static final String[][] IDS = {
      {"F1_HAVEN","F1_CROSSROADS","F1_CRYPT","F1_ARMORY","F1_GUARDIAN"},
      {"F2_CAMP","F2_FORGE","F2_LIBRARY","F2_GARDEN","F2_WARDEN"},
      {"F3_OUTPOST","F3_CATACOMBS","F3_SHROUD","F3_RELIQUARY","F3_THRONE"}
    };
    public static void main(String[] args) throws Exception {
        GameManager game = new GameManager();
        game.startNewGame();
        DungeonMap world = game.getDungeonMap();
        ok(world.getRooms().size()==15,"Not 15 rooms");
        ok("F1_HAVEN".equals(world.getCurrentRoomId()),"Wrong campaign start");
        int ranged = 0;
        for (Room room : world.getRooms().values()) {
            for (Direction dir : Direction.values()) {
                Door door = room.getDoor(dir);
                if (door==null) continue;
                Position pos=door.getPosition();
                ok(room.isWalkable(pos.getX(),pos.getY()),"Door placed on a wall: "+room.getId());
                ok(world.getRoom(door.getTargetRoomId())!=null,"Unconnected door: "+room.getId());
                Position dest=door.getTargetPlayerPosition();
                Room destRoom=world.getRoom(door.getTargetRoomId());
                ok(destRoom.isWalkable(dest.getX(),dest.getY()),"Door exit in wall: "+destRoom.getId());
                ok(!destRoom.hasSolidEntityAt(dest),"Door exit collides with another entity: "+destRoom.getId());
            }
            for (Entity e : room.getEntities()) if (e instanceof RangedEnemy) ranged++;
        }
        ok(ranged>0,"No ranged enemy AI present");
        System.out.println("[PASS] All 15 rooms, door graph, spawns and ranged archetypes validated");
        for (int act=0;act<3;act++) {
            String[] ids=IDS[act];
            String keyName=new String[]{"Ember Sigil","Frost Sigil","Void Sigil"}[act];
            ok(ids[0].equals(world.getCurrentRoomId()),"Cannot start act "+(act+1));
            walkTo(game,new Position(15,6)); // camp -> crossroads
            ok(ids[1].equals(world.getCurrentRoomId()),"Crossroads inaccessible");
            Door gate=world.getCurrentRoom().getDoor(Direction.RIGHT);
            ok(gate!=null && gate.isLocked(),"Guardian gate must be locked initially");
            // Up branch for sigil & relic
            walkTo(game,new Position(8,0));
            ok(ids[2].equals(world.getCurrentRoomId()),"Northern branch inaccessible");
            walkTo(game,new Position(12,3));
            ok(game.getPlayer().hasKey(keyName),"Key cannot be collected in act "+(act+1));
            walkTo(game,new Position(3,3));
            walkTo(game,new Position(8,11));
            ok(ids[1].equals(world.getCurrentRoomId()),"Cannot return from north");
            // Down branch for gear
            walkTo(game,new Position(8,11));
            ok(ids[3].equals(world.getCurrentRoomId()),"Southern branch inaccessible");
            walkTo(game,new Position(12,8));
            walkTo(game,new Position(3,8));
            int armorSlot=-1, weaponSlot=-1;
            for(int i=0;i<game.getPlayer().getInventory().size();i++) {
                if(game.getPlayer().getInventory().get(i) instanceof Armor) armorSlot=i;
                if(game.getPlayer().getInventory().get(i) instanceof Weapon) weaponSlot=i;
            }
            ok(armorSlot>=0 && weaponSlot>=0,"Act gear inaccessible or bag full");
            game.handleUseInventoryItem(armorSlot);
            game.handleUseInventoryItem(weaponSlot);
            walkTo(game,new Position(8,0));
            ok(ids[1].equals(world.getCurrentRoomId()),"Cannot return from armory");
            walkTo(game,new Position(14,6));
            // Unlock with the real game input, then cross.
            game.handlePlayerMove(Direction.RIGHT);
            ok(!gate.isLocked(),"Collected sigil failed to open gate");
            game.handlePlayerMove(Direction.RIGHT);
            ok(ids[4].equals(world.getCurrentRoomId()),"Guardian room is unreachable");
            // Egress/portal must be boss-gated even if player runs past guardian.
            if (act==2) {
                walkTo(game,new Position(14,6));
                ok(game.getGameState()==GameState.PLAYING,"Final portal wrongly unlocked before boss");
            } else {
                walkTo(game,new Position(15,6));
                ok(ids[4].equals(world.getCurrentRoomId()),"Guard gate must not bypass living boss");
            }
            // Player-facing attack loop uses actual game combat API.
            walkTo(game,new Position(10,5));
            game.getPlayer().setFacingDirection(Direction.RIGHT);
            Boss boss=null;
            for(Entity e : world.getCurrentRoom().getEntities()) if(e instanceof Boss)boss=(Boss)e;
            ok(boss!=null,"Boss missing in act "+(act+1));
            for(int strike=0;strike<50 && boss.isAlive();strike++)game.handlePlayerAttack();
            ok(!boss.isAlive(),"Cannot kill guardian in act "+(act+1));
            if(act<2) {
                // If previously on sealed gateway, entering it must now work.
                walkTo(game,new Position(15,6));
                ok(IDS[act+1][0].equals(world.getCurrentRoomId()),"Cannot enter next act");
            } else {
                walkTo(game,new Position(14,6));
                ok(game.getGameState()==GameState.VICTORY,"Final victory portal unreachable");
            }
            System.out.println("[PASS] Act "+(act+1)+": sigil, relic, equipment, locked gate, boss, transition");
        }
        ok(game.getPlayer().getLevel()>1,"No XP progression");
        ok(game.getPlayer().getRelicsCollected()==3,"Relic journal not tracking campaign");
        ok(game.getVisitedRooms().size()==15,"Discovery map did not record all 15 chambers");
        System.out.println("[PASS] Victory, all 15 rooms visited, three relics, level progression");
        GameManager saved=new GameManager(); saved.setDifficulty(Difficulty.HARD);saved.startNewGame();
        saved.handlePlayerMove(Direction.RIGHT);
        File file=File.createTempFile("three_sigils_test_", ".dat");file.deleteOnExit();
        saved.saveGame(file);
        GameManager loaded=new GameManager(); loaded.loadGame(file);
        ok(loaded.getDifficulty()==Difficulty.HARD,"Difficulty not retained after load");
        ok(loaded.getVisitedRooms().equals(saved.getVisitedRooms()),"Discovery progress not restored");
        ok(loaded.getDungeonMap().getRooms().size()==15,"Campaign truncated after save");
        ok(loaded.getRuleEngine().getRules().size()==saved.getRuleEngine().getRules().size(),"Custom rules lost after load");
        System.out.println("[PASS] Three-sigil save/load: rules, difficulty and explored locations");
        GameManager easy = new GameManager();easy.setDifficulty(Difficulty.EASY);easy.startNewGame();
        GameManager hard = new GameManager();hard.setDifficulty(Difficulty.HARD);hard.startNewGame();
        ok(sumMonsterHealth(hard)>sumMonsterHealth(easy),"Difficulty setting does not influence enemies");
        System.out.println("[PASS] Explorer/Adventurer/Nightmare enemy stat scaling");
        GameManager drops = new GameManager(); drops.startNewGame();
        Weapon sample = new Weapon("Old Sword", new Position(4,4), 7);
        drops.getPlayer().getInventory().addItem(sample);
        drops.handleUseInventoryItem(0);
        ok(drops.getPlayer().getEquippedWeapon()==sample,"Weapon could not be equipped");
        drops.handleDropInventoryItem(0);
        ok(drops.getPlayer().getInventory().size()==0,"Drop did not free backpack space");
        ok(drops.getPlayer().getEquippedWeapon()==null,"Dropped weapon remained equipped");
        ok(drops.getDungeonMap().getCurrentRoom().getItemAt(drops.getPlayer().getPosition()).isPresent(),"Dropped item did not return to the map");
        drops.handlePlayerInteract();
        ok(drops.getPlayer().getInventory().size()==1,"Dropped item could not be retrieved");
        Armor testArmor = new Armor("Old Armor", new Position(4,4), 5);
        drops.getPlayer().getInventory().addItem(testArmor);
        int defense=drops.getPlayer().getDefense();
        drops.handleUseInventoryItem(1);
        ok(drops.getPlayer().getDefense()==defense+5,"Armor equip broken");
        drops.handleDropInventoryItem(1);
        ok(drops.getPlayer().getDefense()==defense,"Dropping armor left stale defense buff");
        System.out.println("[PASS] Backpack: drop/recover gear, un-equip weapon/armor without ghost bonuses");
        System.out.println("EXPANDED CAMPAIGN: "+assertions+" assertions passed, 0 failed");
    }
    private static int sumMonsterHealth(GameManager game) {
        int total=0;
        for(Room room:game.getDungeonMap().getRooms().values())
            for(Entity e:room.getEntities())if(e instanceof Enemy)total+=((Enemy)e).getMaxHealth();
        return total;
    }
    private static void walkTo(GameManager game, Position target) {
        Room room=game.getDungeonMap().getCurrentRoom();
        Position origin=game.getPlayer().getPosition();
        ArrayDeque<Position> queue=new ArrayDeque<>();
        Map<Position,Position> previous=new HashMap<>();
        Map<Position,Direction> used=new HashMap<>();
        CollisionManager manager=new CollisionManager();
        queue.add(origin);previous.put(origin,origin);
        while(!queue.isEmpty() && !previous.containsKey(target)) {
            Position current=queue.removeFirst();
            for(Direction direction:Direction.values()) {
                Position next=current.translate(direction);
                if(previous.containsKey(next)|| !manager.canMoveTo(game.getPlayer(),next,room))continue;
                previous.put(next,current);used.put(next,direction);queue.addLast(next);
            }
        }
        ok(previous.containsKey(target),"No path to "+target+" in "+room.getId());
        ArrayDeque<Direction> path=new ArrayDeque<>();
        for(Position pos=target;!pos.equals(origin);pos=previous.get(pos))path.addFirst(used.get(pos));
        while(!path.isEmpty())game.handlePlayerMove(path.removeFirst());
    }
}
