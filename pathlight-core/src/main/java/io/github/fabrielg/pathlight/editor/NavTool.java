package io.github.fabrielg.pathlight.editor;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
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

public class NavTool implements Listener {

	private static final NamespacedKey TOOL_KEY = new NamespacedKey("pathlight", "nav_tool");
	private static final double WAYPOINT_CLICK_RADIUS = 3.0;

	private final PathLightPlugin plugin;
	private final Map<UUID, EditorSession> sessions = new HashMap<>();

	public NavTool(PathLightPlugin plugin) {
		this.plugin = plugin;
		startVisualizationLoop();
	}

	public ItemStack createToolItem() {
		ItemStack item = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("§6§lNavigation Editor"));
		meta.lore(List.of(
				Component.text("§7Right click          → change mode"),
				Component.text("§7Left click           → place / select"),
				Component.text("§7Shift + Left click   → delete")
		));
		meta.getPersistentDataContainer().set(TOOL_KEY, PersistentDataType.BYTE, (byte) 1);
		item.setItemMeta(meta);
		return item;
	}

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
		boolean isSneaking = player.isSneaking();

		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			cycleMode(player, session);
			return;
		}

		if (action == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Location clickedLoc = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
			Waypoint nearby = getNearbyWaypoint(clickedLoc, player.getWorld().getName());

			if (isSneaking) {
				handleDelete(player, session, nearby);
			} else {
				handleLeftClick(player, session, clickedLoc, nearby);
			}
		}

		if (action == Action.LEFT_CLICK_AIR && isSneaking) {
			session.clearSelection();
			player.sendMessage("§7Selection cleared.");
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
				showActionBar(player, getOrCreateSession(player));
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
					player.sendMessage("§cNo waypoint here. Click closer to an existing waypoint.");
					return;
				}
				handleEdgeClick(player, session, nearby);
			}
			case LOCATION -> {
				if (nearby == null) {
					player.sendMessage("§cNo waypoint here. Click on an existing waypoint.");
					return;
				}
				promptLocationName(player, nearby);
			}
		}
	}

	/**
	 * Shift + left click → delete base on active mode.
	 */
	private void handleDelete(Player player, EditorSession session, Waypoint nearby) {
		if (nearby == null) {
			player.sendMessage("§cNo waypoint nearby to delete.");
			return;
		}

		switch (session.getMode()) {
			case WAYPOINT  -> removeWaypoint(player, nearby);
			case EDGE      -> { removeAllEdges(player, nearby); session.clearSelection(); }
			case LOCATION  -> removeLocationOnWaypoint(player, nearby);
		}
	}

	private void placeWaypoint(Player player, Location loc) {
		int id = plugin.getDataManager().nextWaypointId();
		Waypoint wp = new Waypoint(id, loc.getWorld().getName(),
				loc.getX(), loc.getY(), loc.getZ());

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

		player.sendMessage("§cWaypoint §f#" + waypoint.getId() + " §cand its edges removed.");
	}

	private void handleEdgeClick(Player player, EditorSession session, Waypoint clicked) {
		if (!session.hasSelection()) {
			session.setSelectedWaypointId(clicked.getId());
			player.sendMessage("§bWaypoint §f#" + clicked.getId()
					+ " §bselected. Click a second waypoint to connect.");
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

		player.sendMessage("§aEdge created: §f#" + fromId + " §a↔ §f#" + toId);
	}

	private void removeAllEdges(Player player, Waypoint waypoint) {
		int id = waypoint.getId();
		long count = plugin.getDataManager().getEdges().stream()
				.filter(e -> e.getFromId() == id || e.getToId() == id).count();

		plugin.getDataManager().getEdges().removeIf(e ->
				e.getFromId() == id || e.getToId() == id
		);
		plugin.getNavigationGraph().removeWaypoint(id);
		plugin.getNavigationGraph().addWaypoint(waypoint);
		plugin.getDataManager().save();

		player.sendMessage("§cRemoved §f" + count + " §cedge(s) from waypoint §f#" + id);
	}

	private void promptLocationName(Player player, Waypoint anchor) {
		player.sendMessage("§dType the destination name in chat. Type §fcancel §dto abort.");
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
					player.sendMessage("§dLocation §f\"" + input
							+ "\" §dcreated on waypoint §f#" + anchor.getId());
				}),
				plugin
		);
	}

	private void removeLocationOnWaypoint(Player player, Waypoint waypoint) {
		Optional<NavLocation> found = plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getAnchorWaypointId() == waypoint.getId())
				.findFirst();

		if (found.isEmpty()) {
			player.sendMessage("§eNo location anchored on waypoint §f#" + waypoint.getId());
			return;
		}
		plugin.getDataManager().removeLocation(found.get().getId());
		plugin.getDataManager().save();
		player.sendMessage("§cLocation §f\"" + found.get().getName() + "\" §cremoved.");
	}

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
		player.sendMessage(session.getMode().getDisplayName()
				+ " §7— " + session.getMode().getHint());
	}

	private void showActionBar(Player player, EditorSession session) {
		String sel = session.hasSelection()
				? " §7| Selected: §f#" + session.getSelectedWaypointId() : "";
		player.sendActionBar(Component.text(
				session.getMode().getDisplayName() + sel
						+ " §7| §fShift+LClick §7to delete"
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
			if (dist < minDist) { minDist = dist; closest = wp; }
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