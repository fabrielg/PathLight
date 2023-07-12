package fr.aqua_tuor.pathfinder.managers;

import fr.aqua_tuor.pathfinder.path.PathType;
import fr.aqua_tuor.pathfinder.tasks.GameCountdown;
import fr.aqua_tuor.pathfinder.node.Node;
import fr.aqua_tuor.pathfinder.path.Path;
import fr.aqua_tuor.pathfinder.PathFinder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;

import java.util.ArrayList;

public class PathManager {

    private final PathFinder plugin;
    private PlayerManager playerManager;

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private GameCountdown countdown;

    public PathManager(PathFinder plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(this);

        paths = new ArrayList<>();
        loadNodesConfig();

        paths = new ArrayList<>();
        loadPathsConfig();

        // Start countdown
        countdown = new GameCountdown(this);
        countdown.runTaskTimer(plugin, 0, 10);
    }

    public PathFinder getPlugin() {
        return plugin;
    }

    public void loadPathsConfig() {
        paths.clear();
        plugin.getConfig().getConfigurationSection("paths").getKeys(false).forEach(key -> {
            int id = Integer.parseInt(key);
            int startId = plugin.getConfig().getInt("paths." + key + ".start");
            int endId = plugin.getConfig().getInt("paths." + key + ".end");
            PathType type = PathType.valueOf(plugin.getConfig().getString("paths." + key + ".type"));
            Color color = getColorByString(plugin.getConfig().getString("paths." + key + ".color"));

            Path path = new Path(id, getNodeById(startId), getNodeById(endId), type, color);
            paths.add(path);
        });
    }

    public void loadNodesConfig() {
        nodes.clear();
        plugin.getConfig().getConfigurationSection("nodes").getKeys(false).forEach(key -> {
            int id = Integer.parseInt(key);
            double x = plugin.getConfig().getDouble("nodes." + key + ".x");
            double y = plugin.getConfig().getDouble("nodes." + key + ".y");
            double z = plugin.getConfig().getDouble("nodes." + key + ".z");
            String worldName = plugin.getConfig().getString("nodes." + key + ".world");
            Node node = new Node(id, x, y, z, Bukkit.getWorld(worldName));
            nodes.add(node);
        });
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public Node getNodeById(int id) {
        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public Node getNodeByCoords(double x, double y, double z, World world) {
        for (Node node : nodes) {
            if (node.getX() == x && node.getY() == y && node.getZ() == z && node.getWorld().equals(world)) {
                return node;
            }
        }
        return null;
    }

    public void addNode(Node node) {
        nodes.add(node);
        plugin.getConfig().set("nodes." + node.getId() + ".x", node.getX());
        plugin.getConfig().set("nodes." + node.getId() + ".y", node.getY());
        plugin.getConfig().set("nodes." + node.getId() + ".z", node.getZ());
        plugin.getConfig().set("nodes." + node.getId() + ".world", node.getWorld().getName());
        plugin.saveConfig();
    }

    public void removeNode(int id) {
        nodes.removeIf(node -> node.getId() == id);
        plugin.getConfig().set("nodes." + id, null);
        plugin.saveConfig();
    }

    public void addPath(Path path) {
        paths.add(path);
        plugin.getConfig().set("paths." + path.getId() + ".start", path.getStart().getId());
        plugin.getConfig().set("paths." + path.getId() + ".end", path.getEnd().getId());
        plugin.getConfig().set("paths." + path.getId() + ".type", path.getType().toString());
        plugin.getConfig().set("paths." + path.getId() + ".color", getColorString(path.getColor()));
        plugin.saveConfig();
    }

    public void removePath(int id) {
        paths.removeIf(path -> path.getId() == id);
        plugin.getConfig().set("paths." + id, null);
        plugin.saveConfig();
    }

    public int getLastPathId() {
        int lastId = 0;
        for (Path path : paths) {
            if (path.getId() > lastId) {
                lastId = path.getId();
            }
        }
        return lastId;
    }

    public int getLastNodeId() {
        int lastId = 0;
        for (Node node : nodes) {
            if (node.getId() > lastId) {
                lastId = node.getId();
            }
        }
        return lastId;
    }

    public Color getColorByString(String colorString) {
        switch (colorString) {
            case "SILVER":
                return Color.SILVER;
            case "GRAY":
                return Color.GRAY;
            case "BLACK":
                return Color.BLACK;
            case "RED":
                return Color.RED;
            case "MAROON":
                return Color.MAROON;
            case "YELLOW":
                return Color.YELLOW;
            case "OLIVE":
                return Color.OLIVE;
            case "LIME":
                return Color.LIME;
            case "GREEN":
                return Color.GREEN;
            case "AQUA":
                return Color.AQUA;
            case "TEAL":
                return Color.TEAL;
            case "BLUE":
                return Color.BLUE;
            case "NAVY":
                return Color.NAVY;
            case "FUCHSIA":
                return Color.FUCHSIA;
            case "PURPLE":
                return Color.PURPLE;
            case "ORANGE":
                return Color.ORANGE;
            default:
                return Color.WHITE;
        }
    }

    public String getColorString(Color color) {
        if (color.equals(Color.SILVER)) {
            return "SILVER";
        } else if (color.equals(Color.GRAY)) {
            return "GRAY";
        } else if (color.equals(Color.BLACK)) {
            return "BLACK";
        } else if (color.equals(Color.RED)) {
            return "RED";
        } else if (color.equals(Color.MAROON)) {
            return "MAROON";
        } else if (color.equals(Color.YELLOW)) {
            return "YELLOW";
        } else if (color.equals(Color.OLIVE)) {
            return "OLIVE";
        } else if (color.equals(Color.LIME)) {
            return "LIME";
        } else if (color.equals(Color.GREEN)) {
            return "GREEN";
        } else if (color.equals(Color.AQUA)) {
            return "AQUA";
        } else if (color.equals(Color.TEAL)) {
            return "TEAL";
        } else if (color.equals(Color.BLUE)) {
            return "BLUE";
        } else if (color.equals(Color.NAVY)) {
            return "NAVY";
        } else if (color.equals(Color.FUCHSIA)) {
            return "FUCHSIA";
        } else if (color.equals(Color.PURPLE)) {
            return "PURPLE";
        } else if (color.equals(Color.ORANGE)) {
            return "ORANGE";
        } else {
            return "WHITE";
        }
    }

}
