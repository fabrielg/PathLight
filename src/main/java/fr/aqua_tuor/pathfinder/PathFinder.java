package fr.aqua_tuor.pathfinder;

import fr.aqua_tuor.pathfinder.commands.PathfinderCommand;
import fr.aqua_tuor.pathfinder.listeners.PlayerClickWithStick;
import fr.aqua_tuor.pathfinder.listeners.PlayerJoinListener;
import fr.aqua_tuor.pathfinder.managers.PathManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PathFinder extends JavaPlugin {

    private PathManager pathManager;

    @Override
    public void onEnable() {
        pathManager = new PathManager(this);
        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(pathManager), this);
        getServer().getPluginManager().registerEvents(new PlayerClickWithStick(pathManager), this);

        // Commands
        getCommand("edit").setExecutor(new PathfinderCommand(pathManager));

        getServer().getConsoleSender().sendMessage("§aPathFinder is enabled");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§cPathFinder is disabled");
    }
}
