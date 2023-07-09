package fr.aqua_tuor.pathfinder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PathfinderCommand implements CommandExecutor {

    private final PathManager pathManager;

    public PathfinderCommand(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;

        if (command.getName().equalsIgnoreCase("edit")) {
            if (pathManager.getPlayerManager().isEditing(player)) pathManager.getPlayerManager().removePlayer(player);
            else pathManager.getPlayerManager().addPlayer(player);
        }

        return true;
    }
}
