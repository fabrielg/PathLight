package fr.aqua_tuor.pathfinder;

import org.bukkit.plugin.java.JavaPlugin;

public final class PathFinder extends JavaPlugin {

    private PathManager pathManager;

    @Override
    public void onEnable() {
        pathManager = new PathManager(this);
        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(pathManager), this);

        getServer().getConsoleSender().sendMessage("§aPathFinder is enabled");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§cPathFinder is disabled");
    }
}
