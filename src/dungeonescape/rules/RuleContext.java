package dungeonescape.rules;

import dungeonescape.entities.Player;
import dungeonescape.entities.enemies.Boss;
import dungeonescape.entities.enemies.Enemy;
import dungeonescape.entities.enemies.Ghost;
import dungeonescape.entities.enemies.Zombie;
import dungeonescape.entities.structures.Door;
import dungeonescape.managers.GameManager;
import dungeonescape.map.Position;
import dungeonescape.map.Room;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Sandboxed evaluation context passed to the Rule AST at runtime.
 * Bridges player-defined DSL rules with concrete game entities without exposing
 * unsafe Java reflection or system commands.
 * 
 * Demonstrates:
 * - Method Overloading in variable resolution:
 *   getVariable(String) vs getVariable(String, String)
 * - Coercion between numeric DSL types (Double) and game domain integers
 * - Encapsulated state access
 */
public class RuleContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private final GameManager gameManager;
    private final List<String> notifications;

    public RuleContext(GameManager gameManager) {
        this.gameManager = gameManager;
        this.notifications = new ArrayList<>();
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public Player getPlayer() {
        return gameManager != null ? gameManager.getPlayer() : null;
    }

    public Room getCurrentRoom() {
        return (gameManager != null && gameManager.getDungeonMap() != null)
                ? gameManager.getDungeonMap().getCurrentRoom()
                : null;
    }

    /**
     * Resolves a compound variable identifier (e.g. "player.health", "boss.health", "enemies.count").
     * 
     * @param identifier Full dotted identifier.
     * @return Evaluated value (Double, String, or Boolean).
     */
    public Object getVariable(String identifier) {
        if (identifier == null) return null;
        String[] parts = identifier.split("\\.", 2);
        if (parts.length == 2) {
            return getVariable(parts[0].trim(), parts[1].trim());
        }
        return getVariable("global", identifier.trim());
    }

    /**
     * Overloaded variable resolver splitting target entity scope from property name.
     * Demonstrates Method Overloading and type coercion.
     */
    public Object getVariable(String scope, String property) {
        Player player = getPlayer();
        Room room = getCurrentRoom();

        if ("player".equalsIgnoreCase(scope) && player != null) {
            switch (property.toLowerCase()) {
                case "health": return (double) player.getHealth();
                case "maxhealth": return (double) player.getMaxHealth();
                case "attack": return (double) player.getTotalAttackPower();
                case "baseattack": return (double) player.getAttackPower();
                case "defense": return (double) player.getDefense();
                case "score": return (double) player.getScore();
                case "coins": return (double) player.getCoins();
                case "alive": return player.isAlive();
                case "room": return room != null ? room.getName() : "";
                case "roomid": return room != null ? room.getId() : "";
                default: return null;
            }
        }

        if ("room".equalsIgnoreCase(scope) && room != null) {
            switch (property.toLowerCase()) {
                case "name": return room.getName();
                case "id": return room.getId();
                case "enemies": return (double) room.getEntities().stream().filter(e -> e instanceof Enemy).count();
                default: return null;
            }
        }

        if ("boss".equalsIgnoreCase(scope) && room != null) {
            Optional<Boss> bossOpt = room.getEntities().stream()
                    .filter(e -> e instanceof Boss)
                    .map(e -> (Boss) e)
                    .findFirst();
            if (bossOpt.isPresent()) {
                Boss boss = bossOpt.get();
                switch (property.toLowerCase()) {
                    case "health": return (double) boss.getHealth();
                    case "maxhealth": return (double) boss.getMaxHealth();
                    case "alive": return boss.isAlive();
                    case "enraged": return boss.isEnraged();
                    case "attack": return (double) boss.getAttackPower();
                    default: return null;
                }
            } else {
                return 0.0;
            }
        }

        if ("enemies".equalsIgnoreCase(scope) && room != null) {
            if ("count".equalsIgnoreCase(property)) {
                return (double) room.getEntities().stream().filter(e -> e instanceof Enemy).count();
            }
        }

        return null;
    }

    /**
     * Mutates an attribute on the target entity.
     * Demonstrates Coercion: converting generic numeric Double back to domain integer.
     */
    public boolean setVariable(String identifier, Object value) {
        if (identifier == null) return false;
        String[] parts = identifier.split("\\.", 2);
        if (parts.length != 2) return false;

        String scope = parts[0].trim();
        String property = parts[1].trim();

        Player player = getPlayer();
        if ("player".equalsIgnoreCase(scope) && player != null) {
            double numVal = (value instanceof Number) ? ((Number) value).doubleValue() : 0.0;
            int intVal = (int) Math.round(numVal);

            switch (property.toLowerCase()) {
                case "health":
                    if (intVal > player.getHealth()) {
                        player.heal(intVal - player.getHealth());
                    } else {
                        player.takeDamage(player.getHealth() - intVal);
                    }
                    return true;
                case "attack":
                case "baseattack":
                    player.setAttackPower(Math.max(1, intVal));
                    return true;
                case "defense":
                    player.setDefense(Math.max(0, intVal));
                    return true;
                case "score":
                    player.setScore(Math.max(0, intVal));
                    return true;
                case "coins":
                    player.setCoins(Math.max(0, intVal));
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }

    /**
     * Executes safe built-in functions requested in conditions (e.g. player.hasItem("Golden Key")).
     */
    public Object callFunction(String functionName, List<Object> args) {
        Player player = getPlayer();
        Room room = getCurrentRoom();

        if ("player.hasItem".equalsIgnoreCase(functionName) || "hasItem".equalsIgnoreCase(functionName)) {
            if (player != null && !args.isEmpty()) {
                String itemName = String.valueOf(args.get(0));
                return player.getInventory().hasItem(itemName);
            }
            return false;
        }

        if ("player.inRoom".equalsIgnoreCase(functionName) || "inRoom".equalsIgnoreCase(functionName)) {
            if (room != null && !args.isEmpty()) {
                String targetRoom = String.valueOf(args.get(0));
                return room.getId().equalsIgnoreCase(targetRoom) || room.getName().equalsIgnoreCase(targetRoom);
            }
            return false;
        }

        if ("enemies.nearby".equalsIgnoreCase(functionName) || "enemyNearby".equalsIgnoreCase(functionName)) {
            if (player != null && room != null && !args.isEmpty()) {
                int dist = ((Number) args.get(0)).intValue();
                return room.getEntities().stream()
                        .filter(e -> e instanceof Enemy)
                        .anyMatch(e -> e.getPosition().manhattanDistanceTo(player.getPosition()) <= dist);
            }
            return false;
        }

        return false;
    }

    /**
     * Spawns an enemy dynamically into the current room.
     */
    public boolean spawnEntity(String entityType, int x, int y) {
        Room room = getCurrentRoom();
        if (room == null) return false;

        Position pos = new Position(x, y);
        if (!room.isWalkable(x, y) || room.hasSolidEntityAt(pos) ||
                (getPlayer() != null && getPlayer().getPosition().equals(pos))) return false;

        if ("Ghost".equalsIgnoreCase(entityType)) {
            room.addEntity(new Ghost(pos));
            addNotification("A spectral Ghost materialized at " + pos + "!");
            return true;
        } else if ("Zombie".equalsIgnoreCase(entityType)) {
            room.addEntity(new Zombie(pos));
            addNotification("A decomposing Zombie emerged at " + pos + "!");
            return true;
        }
        return false;
    }

    /**
     * Unlocks a door by name in the active room.
     */
    public boolean unlockDoor(String doorName) {
        Room room = getCurrentRoom();
        if (room == null) return false;

        for (Door door : room.getEntities().stream()
                .filter(e -> e instanceof Door)
                .map(e -> (Door) e)
                .toArray(Door[]::new)) {
            if (door.getName().equalsIgnoreCase(doorName) || doorName.equalsIgnoreCase("all")) {
                door.unlockDirectly();
                addNotification("Door '" + door.getName() + "' was magically unlocked by custom rule!");
                return true;
            }
        }
        return false;
    }

    public void addNotification(String message) {
        if (message != null && !message.trim().isEmpty()) {
            notifications.add(message);
            if (gameManager != null) {
                gameManager.setMessage(message);
            }
        }
    }

    public List<String> drainNotifications() {
        List<String> copy = new ArrayList<>(notifications);
        notifications.clear();
        return copy;
    }
}
