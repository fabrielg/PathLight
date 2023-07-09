package fr.aqua_tuor.pathfinder;

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

        // Start coundown
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
            int x = plugin.getConfig().getInt("nodes." + key + ".x");
            int y = plugin.getConfig().getInt("nodes." + key + ".y");
            int z = plugin.getConfig().getInt("nodes." + key + ".z");
            String worldName = plugin.getConfig().getString("nodes." + key + ".world");
            Bukkit.broadcastMessage(id + " " + x + " " + y + " " + z + " " + worldName);
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

}
