package io.github.fabrielg.pathlight.commands;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /path command.
 * Usage: /path <location name>
 *        /path cancel
 */
public class PathCommand implements CommandExecutor, TabCompleter {

	private  final PathLightPlugin plugin;

	public PathCommand(PathLightPlugin plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("§cThis command can only be used by a player.");
			return true;
		}

		if (args.length == 0) {
			player.sendMessage("§eUsage: /path <destination> | /path cancel");
			return true;
		}

		// /path cancel
		if (args[0].equalsIgnoreCase("cancel")) {
			if (plugin.getTrailManager().hasTrail(player)) {
				plugin.getTrailManager().stopTrail(player);
				player.sendMessage("§cNavigation cancelled.");
			} else {
				player.sendMessage("§eYou have no active navigation.");
			}
			return true;
		}

		// /path <nom de destination>
		String locationName = String.join(" ", args);

		NavLocation targetLocation = findLocationByName(locationName);

		if (targetLocation == null) {
			player.sendMessage("§cDestination §e\"" + locationName + "\" §cnot found.");
			player.sendMessage("§7Use /path with a valid destination name.");
			return true;
		}

		Waypoint anchorWaypoint = plugin.getNavigationGraph().getWaypoint(targetLocation.getAnchorWaypointId());
		if (anchorWaypoint == null) {
			player.sendMessage("§cThis destination has no valid anchor waypoint. Contact an admin.");
			return true;
		}

		Waypoint closestToPlayer = plugin.getNavigationGraph().getClosestWaypoint(
				player.getWorld().getName(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
		);

		if (closestToPlayer == null) {
			player.sendMessage("§cNo waypoints found in this world. Contact an admin.");
			return true;
		}

		// A*: calcul path
		List<Integer> path = plugin.getPathfinder().findPath(
				closestToPlayer.getId(),
				anchorWaypoint.getId()
		);

		if (path.isEmpty()) {
			player.sendMessage("§cNo path found to §e" + targetLocation.getName() + "§c.");
			return true;
		}

		plugin.getTrailManager().startTrail(player, path, anchorWaypoint.getId());
		player.sendMessage("§aNavigating to §e" + targetLocation.getName() + "§a. Follow the trail!");
		player.sendMessage("§7Type §f/path cancel §7to stop navigation.");

		return true;
	}

	private NavLocation findLocationByName(String name) {
		for (NavLocation location : plugin.getNavigationGraph().getAllLocations()) {
			if (location.getName().equalsIgnoreCase(name)) {
				return location;
			}
		}
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.add("cancel");

			String input = args[0].toLowerCase();
			for (NavLocation location : plugin.getNavigationGraph().getAllLocations()) {
				if (location.getName().toLowerCase().startsWith(input)) {
					suggestions.add(location.getName());
				}
			}
		}

		return suggestions;
	}
}
