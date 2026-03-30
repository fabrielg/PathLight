package io.github.fabrielg.pathlight.commands;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handles all /pathlight admin subcommands.
 *
 * Usage:
 *   /pathlight reload
 *   /pathlight stats
 *   /pathlight list locations
 *   /pathlight list waypoints
 *   /pathlight info waypoint <id>
 *   /pathlight info location <name>
 *   /pathlight delete waypoint <id>
 *   /pathlight delete location <id>
 *   /pathlight tp <name>
 */
public class PathLightCommand implements CommandExecutor, TabCompleter {

	private final PathLightPlugin plugin;

	public PathLightCommand(PathLightPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("pathlight.admin")) {
			if (sender instanceof Player player)
				plugin.getMessageManager().send(player, "commands.no-permission");
			return true;
		}

		if (args.length == 0) {
			sendHelp(sender);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "reload"  -> handleReload(sender);
			case "stats"   -> handleStats(sender);
			case "list"    -> handleList(sender, args);
			case "info"    -> handleInfo(sender, args);
			case "delete"  -> handleDelete(sender, args);
			case "tp", "teleport"	   -> handleTeleport(sender, args);
			default        -> sendHelp(sender);
		}

		return true;
	}

	// ─────────────────────────────────────────
	//  RELOAD
	// ─────────────────────────────────────────
	private void handleReload(CommandSender sender) {
		sender.sendMessage("§7Reloading PathLight...");

		plugin.getPluginConfig().load();
		plugin.getMessageManager().load();
		plugin.getDataManager().load();
		plugin.getNavigationGraph().build();
		plugin.getNavTool().load();
		plugin.getTrailManager().load();

		sender.sendMessage("§aPathLight reloaded successfully.");
		sender.sendMessage("§7Waypoints : §f" + plugin.getDataManager().getWaypoints().size());
		sender.sendMessage("§7Edges     : §f" + plugin.getDataManager().getEdges().size());
		sender.sendMessage("§7Locations : §f" + plugin.getDataManager().getLocations().size());
	}

	// ─────────────────────────────────────────
	//  STATS
	// ─────────────────────────────────────────
	private void handleStats(CommandSender sender) {
		int waypointCount = plugin.getDataManager().getWaypoints().size();
		int edgeCount     = plugin.getDataManager().getEdges().size();
		int locationCount = plugin.getDataManager().getLocations().size();

		long isolatedCount = plugin.getDataManager().getWaypoints().values().stream()
				.filter(wp -> plugin.getDataManager().getEdges().stream()
						.noneMatch(e -> e.getFromId() == wp.getId() || e.getToId() == wp.getId()))
				.count();

		long navigatingCount = plugin.getServer().getOnlinePlayers().stream()
				.filter(p -> plugin.getTrailManager().hasTrail(p))
				.count();

		sender.sendMessage("§6§l--- PathLight Stats ---");
		sender.sendMessage("§7Waypoints  : §f" + waypointCount);
		sender.sendMessage("§7Edges      : §f" + edgeCount);
		sender.sendMessage("§7Locations  : §f" + locationCount);
		sender.sendMessage("§7Isolated waypoints (no edges) : §e" + isolatedCount);
		sender.sendMessage("§7Players navigating : §f" + navigatingCount);
	}

	// ─────────────────────────────────────────
	//  LIST
	// ─────────────────────────────────────────
	private void handleList(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("§eUsage: /pathlight list <locations|waypoints>");
			return;
		}

		switch (args[1].toLowerCase()) {
			case "locations"  -> listLocations(sender);
			case "waypoints"  -> listWaypoints(sender);
			default -> sender.sendMessage("§eUsage: /pathlight list <locations|waypoints>");
		}
	}

	private void listLocations(CommandSender sender) {
		var locations = plugin.getDataManager().getLocations().values();

		if (locations.isEmpty()) {
			sender.sendMessage("§7No locations registered yet.");
			return;
		}

		sender.sendMessage("§6§l--- Locations (" + locations.size() + ") ---");
		for (NavLocation loc : locations) {
			Waypoint anchor = plugin.getDataManager().getWaypoints().get(loc.getAnchorWaypointId());
			String anchorInfo = anchor != null
					? "wp #" + anchor.getId() + " (" + formatCoords(anchor) + ")"
					: "§cmissing anchor wp #" + loc.getAnchorWaypointId();

			sender.sendMessage("§f[" + loc.getId() + "] §e" + loc.getName()
					+ " §7→ anchored on " + anchorInfo);
		}
	}

	private void listWaypoints(CommandSender sender) {
		var waypoints = plugin.getDataManager().getWaypoints().values();

		if (waypoints.isEmpty()) {
			sender.sendMessage("§7No waypoints registered yet.");
			return;
		}

		sender.sendMessage("§6§l--- Waypoints (" + waypoints.size() + ") ---");
		var edges = plugin.getDataManager().getEdges();
		var sortedWaypoints = waypoints.stream()
				.sorted(Comparator.comparingInt((Waypoint wp) -> {
					return (int) edges.stream()
							.filter(e -> e.getFromId() == wp.getId() || e.getToId() == wp.getId())
							.count();
				}).reversed()).toList();
		for (Waypoint wp : sortedWaypoints) {
			int edgeCount = (int) edges.stream()
					.filter(e -> e.getFromId() == wp.getId() || e.getToId() == wp.getId())
					.count();

			boolean isAnchor = plugin.getDataManager().getLocations().values().stream()
					.anyMatch(l -> l.getAnchorWaypointId() == wp.getId());

			String anchorTag = isAnchor ? " §d[LOCATION]" : "";
			String edgeTag   = edgeCount == 0
					? " §c[ISOLATED]"
					: " §7(" + edgeCount + " edge(s))";

			sender.sendMessage("§f#" + wp.getId()
					+ " §7" + wp.getWorld()
					+ " " + formatCoords(wp)
					+ edgeTag + anchorTag);
		}
	}

	// ─────────────────────────────────────────
	//  INFO
	// ─────────────────────────────────────────
	private void handleInfo(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage("§eUsage: /pathlight info <waypoint <id> | location <name>>");
			return;
		}

		switch (args[1].toLowerCase()) {
			case "waypoint" -> infoWaypoint(sender, args);
			case "location" -> infoLocation(sender, args);
			default -> sender.sendMessage("§eUsage: /pathlight info <waypoint|location> ...");
		}
	}

	private void infoWaypoint(CommandSender sender, String[] args) {
		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			sender.sendMessage("§cInvalid waypoint ID: §f" + args[2]);
			return;
		}

		Waypoint wp = plugin.getDataManager().getWaypoints().get(id);
		if (wp == null) {
			sender.sendMessage("§cWaypoint §f#" + id + " §cnot found.");
			return;
		}

		List<String> neighbors = new ArrayList<>();
		for (Edge edge : plugin.getDataManager().getEdges()) {
			if (edge.getFromId() == id) neighbors.add("#" + edge.getToId());
			else if (edge.getToId() == id) neighbors.add("#" + edge.getFromId());
		}

		NavLocation anchoredLocation = plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getAnchorWaypointId() == id)
				.findFirst().orElse(null);

		sender.sendMessage("§6§l--- Waypoint #" + id + " ---");
		sender.sendMessage("§7World  : §f" + wp.getWorld());
		sender.sendMessage("§7Coords : §f" + formatCoords(wp));
		sender.sendMessage("§7Edges  : §f" + (neighbors.isEmpty()
				? "§cnone (isolated)" : String.join(", ", neighbors)));
		if (anchoredLocation != null) {
			sender.sendMessage("§7Location anchor for : §d" + anchoredLocation.getName()
					+ " §7(id=" + anchoredLocation.getId() + ")");
		}
	}

	private void infoLocation(CommandSender sender, String[] args) {
		String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

		NavLocation location = plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getName().equalsIgnoreCase(name))
				.findFirst().orElse(null);

		if (location == null) {
			sender.sendMessage("§cLocation §f\"" + name + "\" §cnot found.");
			return;
		}

		Waypoint anchor = plugin.getDataManager().getWaypoints().get(location.getAnchorWaypointId());

		sender.sendMessage("§6§l--- Location: " + location.getName() + " ---");
		sender.sendMessage("§7ID     : §f" + location.getId());
		sender.sendMessage("§7Anchor : §f#" + location.getAnchorWaypointId()
				+ (anchor != null ? " at " + formatCoords(anchor) : " §c(missing!)"));
	}

	// ─────────────────────────────────────────
	//  DELETE
	// ─────────────────────────────────────────
	private void handleDelete(CommandSender sender, String[] args) {
		if (args.length < 3) {
			sender.sendMessage("§eUsage: /pathlight delete <waypoint <id> | location <id>>");
			return;
		}

		switch (args[1].toLowerCase()) {
			case "waypoint" -> deleteWaypoint(sender, args);
			case "location" -> deleteLocation(sender, args);
			default -> sender.sendMessage("§eUsage: /pathlight delete <waypoint|location> <id>");
		}
	}

	private void deleteWaypoint(CommandSender sender, String[] args) {
		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			sender.sendMessage("§cInvalid waypoint ID: §f" + args[2]);
			return;
		}

		if (!plugin.getDataManager().getWaypoints().containsKey(id)) {
			sender.sendMessage("§cWaypoint §f#" + id + " §cnot found.");
			return;
		}

		int edgesBefore = plugin.getDataManager().getEdges().size();
		plugin.getDataManager().getEdges().removeIf(e ->
				e.getFromId() == id || e.getToId() == id
		);
		int edgesRemoved = edgesBefore - plugin.getDataManager().getEdges().size();

		List<String> removedLocations = new ArrayList<>();
		plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getAnchorWaypointId() == id)
				.forEach(l -> removedLocations.add(l.getName()));
		removedLocations.forEach(name ->
				plugin.getDataManager().getLocations().values().stream()
						.filter(l -> l.getName().equals(name))
						.findFirst()
						.ifPresent(l -> plugin.getDataManager().removeLocation(l.getId()))
		);

		plugin.getNavigationGraph().removeWaypoint(id);
		plugin.getDataManager().removeWaypoint(id);
		plugin.getDataManager().save();

		sender.sendMessage("§cWaypoint §f#" + id + " §cdeleted.");
		sender.sendMessage("§7Edges removed : §f" + edgesRemoved);
		if (!removedLocations.isEmpty()) {
			sender.sendMessage("§7Locations removed : §c" + String.join(", ", removedLocations));
		}
	}

	private void deleteLocation(CommandSender sender, String[] args) {
		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			sender.sendMessage("§cInvalid location ID: §f" + args[2]);
			return;
		}

		NavLocation location = plugin.getDataManager().getLocations().get(id);
		if (location == null) {
			sender.sendMessage("§cLocation §f#" + id + " §cnot found.");
			return;
		}

		plugin.getDataManager().removeLocation(id);
		plugin.getDataManager().save();

		sender.sendMessage("§cLocation §f\"" + location.getName() + "\" §cdeleted.");
		sender.sendMessage("§7The anchor waypoint §f#" + location.getAnchorWaypointId()
				+ " §7was kept.");
	}

	// ─────────────────────────────────────────
	//  TELEPORT
	// ─────────────────────────────────────────
	private void handleTeleport(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("§cYou must be a player to execute this command !");
			return;
		}

		if (args.length < 2) {
			sender.sendMessage("§eUsage: /pathlight tp <location name>");
			return;
		}

		String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
		NavLocation dest = plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getName().equalsIgnoreCase(name))
				.findFirst().orElse(null);

		if (dest == null) {
			sender.sendMessage("§cLocation §f\"" + name + "\" §cnot found.");
			return;
		}

		Waypoint anchor = plugin.getDataManager().getWaypoints().get(dest.getAnchorWaypointId());
		Location location = new Location(plugin.getServer().getWorld(anchor.getWorld()), anchor.getX(), anchor.getY(), anchor.getZ());
		player.teleport(location);
		plugin.getMessageManager().send(player, "commands.teleported",
			"destination", name,
				"x", String.format("%.1f", anchor.getX()),
				"y", String.format("%.1f", anchor.getY()),
				"z", String.format("%.1f", anchor.getZ())
		);
	}

    // ─────────────────────────────────────────
	//  TAB COMPLETION
	// ─────────────────────────────────────────

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
									  String label, String[] args) {
		if (!sender.hasPermission("pathlight.admin")) return List.of();

		if (args.length == 1) {
			return filter(List.of("reload", "stats", "list", "info", "delete", "teleport"), args[0]);
		}

		if (args.length == 2) {
			return switch (args[0].toLowerCase()) {
				case "list"   -> filter(List.of("locations", "waypoints"), args[1]);
				case "info"   -> filter(List.of("waypoint", "location"), args[1]);
				case "delete" -> filter(List.of("waypoint", "location"), args[1]);
				case "tp", "teleport" -> filter(
						plugin.getDataManager().getLocations().values()
								.stream().map(l -> String.valueOf(l.getName())).toList(),
						args[1]
				);
				default -> List.of();
			};
		}

		if (args.length == 3) {
			return switch (args[0].toLowerCase()) {
				case "info", "delete" -> switch (args[1].toLowerCase()) {
					case "waypoint" -> filter(
							plugin.getDataManager().getWaypoints().keySet()
									.stream().map(String::valueOf).toList(),
							args[2]
					);
					case "location" -> filter(
							plugin.getDataManager().getLocations().values()
									.stream().map(l -> String.valueOf(l.getName())).toList(),
							args[2]
					);
					default -> List.of();
				};
				default -> List.of();
			};
		}

		return List.of();
	}

	// ─────────────────────────────────────────
	//  HELPERS
	// ─────────────────────────────────────────
	private void sendHelp(CommandSender sender) {
		sender.sendMessage("§6§l--- PathLight Admin Commands ---");
		sender.sendMessage("§f/pathlight reload §7— Reload config and graph");
		sender.sendMessage("§f/pathlight stats §7— Show graph statistics");
		sender.sendMessage("§f/pathlight list <locations|waypoints> §7— List all entries");
		sender.sendMessage("§f/pathlight info waypoint <id> §7— Waypoint details");
		sender.sendMessage("§f/pathlight info location <name> §7— Location details");
		sender.sendMessage("§f/pathlight delete waypoint <id> §7— Delete a waypoint");
		sender.sendMessage("§f/pathlight delete location <id> §7— Delete a location");
		sender.sendMessage("§f/pathlight tp <location name> §7— Teleport to a location");
	}

	private String formatCoords(Waypoint wp) {
		return String.format("(%.1f, %.1f, %.1f)", wp.getX(), wp.getY(), wp.getZ());
	}

	private List<String> filter(List<String> options, String input) {
		return options.stream()
				.filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
				.toList();
	}
}
