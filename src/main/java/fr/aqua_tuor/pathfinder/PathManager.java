package fr.aqua_tuor.pathfinder;

public class PathManager {

    private final PathFinder plugin;

    public PathManager(PathFinder plugin) {
        this.plugin = plugin;
    }

    public PathFinder getPlugin() {
        return plugin;
    }

}
