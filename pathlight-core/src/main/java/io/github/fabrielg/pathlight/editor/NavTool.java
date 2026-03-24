package io.github.fabrielg.pathlight.editor;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * The in-game navigation editor tool (custom stick).
 * Allows admins to place/remove waypoints, create/delete edges,
 * and define named locations directly in the world.
 */
public class NavTool implements Listener {

	private static final NamespacedKey TOOL_KEY = new NamespacedKey("pathlight", "nav_tool");

	private static final double WAYPOINT_CLICK_RADIUS = 3.0;

	private final PathLightPlugin plugin;

	private final Map<UUID, EditorSession> sessions = new HashMap<>();

	public NavTool(PathLightPlugin plugin) {
		this.plugin = plugin;
		startVisualizationLoop();
	}

	/**
	 * Creates and returns the NavTool item.
	 * Call this to give the tool to an admin.
	 */
	public ItemStack createToolItem() {
		ItemStack item = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = item.getItemMeta();

		meta.displayName(Component.text("§6§lNavigation Editor"));
		meta.lore(List.of(
				Component.text("§7Right click in air to change mode"),
				Component.text("§7Current mode shown in action bar")
		));

		meta.getPersistentDataContainer().set(TOOL_KEY, PersistentDataType.BYTE, (byte) 1);
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Returns true if the given item is the NavTool.
	 */
	public boolean isNavTool(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return false;
		return item.getItemMeta()
				.getPersistentDataContainer()
				.has(TOOL_KEY, PersistentDataType.BYTE);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (!isNavTool(item)) return;
		if (!player.hasPermission("pathlight.admin")) return;

		event.setCancelled(true);

		EditorSession session = getOrCreateSession(player);
		Action action = event.getAction();

		if (action == Action.RIGHT_CLICK_AIR) {
			cycleMode(player, session);
			return;
		}

		if (action == Action.RIGHT_CLICK_BLOCK) {
			Waypoint nearby = getNearbyWaypoint(player.getLocation(), player.getWorld().getName());

			if (nearby != null) {
				handleRightClickWaypoint(player, session, nearby);
			} else {
				cycleMode(player, session);
			}
			return;
		}

		if (action == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Location clickedLoc = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
			Waypoint nearby = getNearbyWaypoint(clickedLoc, player.getWorld().getName());

			handleLeftClick(player, session, clickedLoc, nearby);
		}
	}

	@EventHandler
	public void onItemHeld(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();

		new BukkitRunnable() {
			@Override
			public void run() {
				ItemStack item = player.getInventory().getItemInMainHand();
				if (!isNavTool(item)) return;
				if (!player.hasPermission("pathlight.admin")) return;

				EditorSession session = getOrCreateSession(player);
				showActionBar(player, session);
			}
		}.runTaskLater(plugin, 1L);
	}

	private void handleLeftClick(Player player, EditorSession session, Location loc, Waypoint nearby) {
		switch (session.getMode()) {

			case WAYPOINT -> {
				if (nearby != null) {
					player.sendMessage("§eWaypoint §f#" + nearby.getId() + " §ealready exists here.");
					return;
				}
				placeWaypoint(player, loc);
			}

			case EDGE -> {
				if (nearby == null) {
					player.sendMessage("§cNo waypoint found here. Click closer to a waypoint.");
					return;
				}
				handleEdgeClick(player, session, nearby);
			}

			case LOCATION -> {
				if (nearby == null) {
					player.sendMessage("§cNo waypoint found here. Click on an existing waypoint.");
					return;
				}
				promptLocationName(player, nearby);
			}
		}
	}

	private void handleRightClickWaypoint(Player player, EditorSession session, Waypoint waypoint) {
		switch (session.getMode()) {

			case WAYPOINT -> removeWaypoint(player, waypoint);

			case EDGE -> {
				removeAllEdges(player, waypoint);
				session.clearSelection();
			}

			case LOCATION -> {
				removeLocationOnWaypoint(player, waypoint);
			}
		}
	}

	private void placeWaypoint(Player player, Location loc) {
		int id = plugin.getDataManager().nextWaypointId();
		Waypoint wp = new Waypoint(id, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());

		plugin.getDataManager().addWaypoint(wp);
		plugin.getNavigationGraph().addWaypoint(wp);
		plugin.getDataManager().save();

		player.sendMessage("§aWaypoint §f#" + id + " §acreated at §f"
				+ formatCoords(loc.getX(), loc.getY(), loc.getZ()));

		loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.3, 0.3, 0.3, 0);
	}

	private void removeWaypoint(Player player, Waypoint waypoint) {
		plugin.getDataManager().getEdges().removeIf(e ->
				e.getFromId() == waypoint.getId() || e.getToId() == waypoint.getId()
		);
		plugin.getNavigationGraph().removeWaypoint(waypoint.getId());
		plugin.getDataManager().removeWaypoint(waypoint.getId());
		plugin.getDataManager().save();

		player.sendMessage("§cWaypoint §f#" + waypoint.getId() + " §cremoved.");
	}

	private void handleEdgeClick(Player player, EditorSession session, Waypoint clicked) {
		if (!session.hasSelection()) {
			session.setSelectedWaypointId(clicked.getId());
			player.sendMessage("§bWaypoint §f#" + clicked.getId() + " §bselected. Now click a second waypoint.");
			return;
		}

		int fromId = session.getSelectedWaypointId();
		int toId   = clicked.getId();
		session.clearSelection();

		if (fromId == toId) {
			player.sendMessage("§cCannot connect a waypoint to itself.");
			return;
		}

		boolean exists = plugin.getDataManager().getEdges().stream().anyMatch(e ->
				(e.getFromId() == fromId && e.getToId() == toId) ||
						(e.getFromId() == toId   && e.getToId() == fromId)
		);

		if (exists) {
			player.sendMessage("§eEdge between §f#" + fromId + " §eand §f#" + toId + " §ealready exists.");
			return;
		}

		Edge edge = new Edge(fromId, toId);
		plugin.getDataManager().addEdge(edge);
		plugin.getNavigationGraph().addEdge(edge);
		plugin.getDataManager().save();

		player.sendMessage("§aEdge created between §f#" + fromId + " §aand §f#" + toId);
	}

	private void removeAllEdges(Player player, Waypoint waypoint) {
		int id = waypoint.getId();
		long count = plugin.getDataManager().getEdges().stream()
				.filter(e -> e.getFromId() == id || e.getToId() == id)
				.count();

		plugin.getDataManager().getEdges().removeIf(e ->
				e.getFromId() == id || e.getToId() == id
		);
		plugin.getNavigationGraph().removeWaypoint(id);
		plugin.getNavigationGraph().addWaypoint(waypoint); // réajoute sans liaisons
		plugin.getDataManager().save();

		player.sendMessage("§cRemoved §f" + count + " §cedge(s) from waypoint §f#" + id);
	}

	private void promptLocationName(Player player, Waypoint anchor) {
		player.sendMessage("§dType the name of this destination in chat. Type §fcancel §dto abort.");

		plugin.getServer().getPluginManager().registerEvents(
				new ChatInputListener(plugin, player, input -> {
					if (input.equalsIgnoreCase("cancel")) {
						player.sendMessage("§7Cancelled.");
						return;
					}

					int id = plugin.getDataManager().nextLocationId();
					NavLocation location = new NavLocation(id, input, anchor.getId());

					plugin.getDataManager().addLocation(location);
					plugin.getDataManager().save();

					player.sendMessage("§dLocation §f\"" + input + "\" §dcreated on waypoint §f#" + anchor.getId());
				}),
				plugin
		);
	}

	private void removeLocationOnWaypoint(Player player, Waypoint waypoint) {
		Optional<NavLocation> found = plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getAnchorWaypointId() == waypoint.getId())
				.findFirst();

		if (found.isEmpty()) {
			player.sendMessage("§eNo location is anchored on waypoint §f#" + waypoint.getId());
			return;
		}

		plugin.getDataManager().removeLocation(found.get().getId());
		plugin.getDataManager().save();
		player.sendMessage("§cLocation §f\"" + found.get().getName() + "\" §cremoved.");
	}

	/**
	 * Repeating task that shows waypoints and edges as particles
	 * to any admin holding the NavTool.
	 */
	private void startVisualizationLoop() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : plugin.getServer().getOnlinePlayers()) {
					if (!isNavTool(player.getInventory().getItemInMainHand())) continue;
					if (!player.hasPermission("pathlight.admin")) continue;

					renderEditorParticles(player);
					showActionBar(player, getOrCreateSession(player));
				}
			}
		}.runTaskTimer(plugin, 0L, 10L);
	}

	private void renderEditorParticles(Player player) {
		String world = player.getWorld().getName();

		for (Waypoint wp : plugin.getDataManager().getWaypoints().values()) {
			if (!wp.getWorld().equals(world)) continue;

			Location loc = new Location(player.getWorld(), wp.getX(), wp.getY(), wp.getZ());
			player.spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1,
					new Particle.DustOptions(Color.YELLOW, 1.5f));

			boolean isAnchor = plugin.getDataManager().getLocations().values()
					.stream().anyMatch(l -> l.getAnchorWaypointId() == wp.getId());
			if (isAnchor) {
				player.spawnParticle(Particle.DUST,
						loc.clone().add(0, 0.5, 0), 3, 0.1, 0.1, 0.1,
						new Particle.DustOptions(Color.FUCHSIA, 1.5f));
			}
		}

		for (Edge edge : plugin.getDataManager().getEdges()) {
			Waypoint from = plugin.getDataManager().getWaypoints().get(edge.getFromId());
			Waypoint to   = plugin.getDataManager().getWaypoints().get(edge.getToId());
			if (from == null || to == null) continue;
			if (!from.getWorld().equals(world)) continue;

			renderEdgeLine(player, from, to);
		}
	}

	private void renderEdgeLine(Player player, Waypoint from, Waypoint to) {
		double distance = from.distanceTo(to);
		int steps = (int) Math.ceil(distance / 1.5);

		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			double x = from.getX() + t * (to.getX() - from.getX());
			double y = from.getY() + t * (to.getY() - from.getY()) + 0.1;
			double z = from.getZ() + t * (to.getZ() - from.getZ());

			player.spawnParticle(Particle.DUST,
					new Location(player.getWorld(), x, y, z), 1, 0, 0, 0,
					new Particle.DustOptions(Color.WHITE, 0.8f));
		}
	}

	private void cycleMode(Player player, EditorSession session) {
		session.setMode(session.getMode().next());
		session.clearSelection();
		showActionBar(player, session);
		player.sendMessage(session.getMode().getDisplayName() + " §7activated. " + session.getMode().getHint());
	}

	private void showActionBar(Player player, EditorSession session) {
		String selectionInfo = session.hasSelection()
				? " §7| Selected: §f#" + session.getSelectedWaypointId()
				: "";
		player.sendActionBar(Component.text(
				session.getMode().getDisplayName() + selectionInfo
		));
	}

	private Waypoint getNearbyWaypoint(Location loc, String world) {
		Waypoint closest = null;
		double minDist = WAYPOINT_CLICK_RADIUS;

		for (Waypoint wp : plugin.getDataManager().getWaypoints().values()) {
			if (!wp.getWorld().equals(world)) continue;

			double dx = wp.getX() - loc.getX();
			double dy = wp.getY() - loc.getY();
			double dz = wp.getZ() - loc.getZ();
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (dist < minDist) {
				minDist = dist;
				closest = wp;
			}
		}

		return closest;
	}

	private EditorSession getOrCreateSession(Player player) {
		return sessions.computeIfAbsent(player.getUniqueId(), k -> new EditorSession());
	}

	private String formatCoords(double x, double y, double z) {
		return String.format("%.1f, %.1f, %.1f", x, y, z);
	}
}