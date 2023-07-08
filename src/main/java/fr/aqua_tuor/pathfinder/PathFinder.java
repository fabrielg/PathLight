package fr.aqua_tuor.pathfinder;

import org.bukkit.plugin.java.JavaPlugin;

public final class PathFinder extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        getServer().getConsoleSender().sendMessage("§aPathFinder is enabled");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§cPathFinder is disabled");
    }
}
