package io.github.fabrielg.pathlight.commands;

import io.github.fabrielg.pathlight.PathLightPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/**
 * Handles the /pathlight admin command.
 * Usage: /pathlight reload
 */
public class PathLightCommand implements CommandExecutor, TabCompleter {

	private final PathLightPlugin plugin;

	public PathLightCommand(PathLightPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!sender.hasPermission("pathlight.admin")) {
			sender.sendMessage("§cYou don't have permission to use this command.");
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage("§eUsage: /pathlight reload");
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			sender.sendMessage("§7Reloading PathLight data...");

			plugin.getPluginConfig().load();
			plugin.getDataManager().load();
			plugin.getNavigationGraph().build();

			sender.sendMessage("§aPathLight reloaded successfully.");
			sender.sendMessage("§7Waypoints: §f" + plugin.getDataManager().getWaypoints().size());
			sender.sendMessage("§7Edges: §f" + plugin.getDataManager().getEdges().size());
			sender.sendMessage("§7Locations: §f" + plugin.getDataManager().getLocations().size());
			return true;
		}

		sender.sendMessage("§cUnknown subcommand. Usage: /pathlight reload");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			return List.of("reload");
		}
		return List.of();
	}
}
