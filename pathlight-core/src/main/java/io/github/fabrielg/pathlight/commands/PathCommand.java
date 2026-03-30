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
			String playerOnly = plugin.getMessageManager().get("commands.player-only");
			if (!playerOnly.isEmpty())
				sender.sendMessage(playerOnly);
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
				plugin.getMessageManager().send(player, "navigation.cancelled");
			} else {
				plugin.getMessageManager().send(player, "navigation.no-trail-active");
			}
			return true;
		}

		// /path <destination name>
		String locationName = String.join(" ", args);

		NavLocation targetLocation = findLocationByName(locationName);

		if (targetLocation == null) {
			plugin.getMessageManager().send(player, "navigation.destination-not-found",
					"destination", locationName);
			return true;
		}

		Waypoint anchorWaypoint = plugin.getNavigationGraph().getWaypoint(targetLocation.getAnchorWaypointId());
		if (anchorWaypoint == null) {
			plugin.getMessageManager().send(player, "navigation.invalid-anchor");
			return true;
		}

		Waypoint closestToPlayer = plugin.getNavigationGraph().getClosestWaypoint(
				player.getWorld().getName(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
		);

		if (closestToPlayer == null) {
			plugin.getMessageManager().send(player, "navigation.no-waypoints-in-world");
			return true;
		}

		// A*: calcul path
		List<Integer> path = plugin.getPathfinder().findPath(
				closestToPlayer.getId(),
				anchorWaypoint.getId()
		);

		if (path.isEmpty()) {
			plugin.getMessageManager().send(player, "navigation.no-path-found",
					"destination", targetLocation.getName());
			return true;
		}

		plugin.getTrailManager().startTrail(player, path, targetLocation);
		plugin.getMessageManager().send(player, "navigation.started",
				"destination", targetLocation.getName());
		plugin.getMessageManager().send(player, "navigation.hint-cancel");

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

			String input = args[0].toLowerCase();
			for (NavLocation location : plugin.getNavigationGraph().getAllLocations()) {
				if (location.getName().toLowerCase().startsWith(input)) {
					suggestions.add(location.getName());
				}
			}
			suggestions.add("cancel");
		}

		return suggestions;
	}
}
