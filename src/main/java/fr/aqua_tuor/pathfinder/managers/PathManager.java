package fr.aqua_tuor.pathfinder.managers;

import fr.aqua_tuor.pathfinder.tasks.GameCountdown;
import fr.aqua_tuor.pathfinder.node.Node;
import fr.aqua_tuor.pathfinder.path.Path;
import fr.aqua_tuor.pathfinder.PathFinder;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class PathManager {

    private final PathFinder plugin;
    private PlayerManager playerManager;

    private ArrayList<Path> paths;
    private ArrayList<Node> nodes = new ArrayList<>();
    private GameCountdown countdown;

    public PathManager(PathFinder plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(this);
        paths = new ArrayList<>();
        loadNodesConfig();

        // Start countdown
        countdown = new GameCountdown(this);
        countdown.runTaskTimer(plugin, 0, 10);
    }

    public PathFinder getPlugin() {
        return plugin;
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

    public Node getNodeByCoords(double x, double y, double z) {
        for (Node node : nodes) {
            if (node.getX() == x && node.getY() == y && node.getZ() == z) {
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

}
