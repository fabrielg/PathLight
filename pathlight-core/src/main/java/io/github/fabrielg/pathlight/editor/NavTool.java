package io.github.fabrielg.pathlight.editor;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class NavTool implements Listener {

	private static final NamespacedKey TOOL_KEY = new NamespacedKey("pathlight", "nav_tool");

	private final double WAYPOINT_CLICK_RADIUS;

	private static final double SNAP_RADIUS = 2.0;

	private static final int MAX_TARGET_DISTANCE = 20;

	private final PathLightPlugin plugin;
	private final Map<UUID, EditorSession> sessions = new HashMap<>();
	private final List<UUID> prompting = new ArrayList<>();

	public NavTool(PathLightPlugin plugin) {
		this.plugin = plugin;
		this.WAYPOINT_CLICK_RADIUS = plugin.getPluginConfig().getWaypointClickRadius();
		startVisualizationLoop();
	}

	public ItemStack createToolItem() {
		ItemStack item = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text("§6§lNavigation Editor"));
		meta.lore(List.of(
				Component.text("§7Right click           → change mode"),
				Component.text("§7Shift + Right click  → toggle auto-edge (waypoint mode)"),
				Component.text("§7Left click            → place / connect"),
				Component.text("§7Shift + Left click   → cancel selection / delete")
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

		if (event.getHand() == EquipmentSlot.OFF_HAND)
			return;
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (!isNavTool(item)) return;
		if (!player.hasPermission("pathlight.admin")) return;

		event.setCancelled(true);

		EditorSession session = getOrCreateSession(player);
		Action action         = event.getAction();
		boolean isSneaking    = player.isSneaking();

		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			if (isSneaking && session.getMode() == EditorMode.WAYPOINT) {
				session.toggleAutoEdge();
				session.clearLastWaypoint();
				String state = session.isAutoEdgeEnabled() ? "§aON" : "§cOFF";
				player.sendMessage("§7Auto-edge: " + state);
				showActionBar(player, session);
			} else {
				cycleMode(player, session);
			}
			return;
		}

		if (action == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Location clickedLoc = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
			Waypoint nearby     = getNearbyWaypoint(clickedLoc, player.getWorld().getName());

			if (isSneaking) {
				handleShiftLeftClick(player, session, nearby);
			} else {
				handleLeftClick(player, session, clickedLoc, nearby);
			}
		}

		if (action == Action.LEFT_CLICK_AIR && isSneaking) {
			handleShiftLeftClick(player, session, null);
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

	private void handleShiftLeftClick(Player player, EditorSession session, Waypoint nearby) {
		if (session.getMode() == EditorMode.WAYPOINT) {
			if (session.hasLastWaypoint()) {
				session.clearLastWaypoint();
				player.sendMessage("§7Selection cancelled.");
				return;
			}
			if (nearby == null) {
				player.sendMessage("§cNo waypoint nearby to delete.");
				return;
			}
			removeWaypoint(player, nearby);
			return;
		}

		if (session.getMode() == EditorMode.LOCATION) {
			if (nearby == null) {
				player.sendMessage("§cNo waypoint nearby to remove location from.");
				return;
			}
			removeLocationOnWaypoint(player, nearby);
		}
	}

	private void handleLeftClick(Player player, EditorSession session, Location clickedLoc, Waypoint nearby) {
		switch (session.getMode()) {
			case WAYPOINT -> handleWaypointClick(player, session, clickedLoc, nearby);
			case LOCATION -> {
				if (nearby == null) {
					player.sendMessage("§cNo waypoint here. Click on an existing waypoint.");
					return;
				}
				promptLocationName(player, nearby);
			}
		}
	}

	private void handleWaypointClick(Player player, EditorSession session, Location clickedLoc, Waypoint nearby) {
		if (nearby != null) {
			if (!session.hasLastWaypoint()) {
				session.setLastWaypointId(nearby.getId());
				player.sendMessage("§eWaypoint §f#" + nearby.getId()
						+ " §eselected as start.");
				return;
			}

			if (session.isAutoEdgeEnabled()) {
				createEdgeIfNotExists(player, session.getLastWaypointId(), nearby.getId());
			}
			session.setLastWaypointId(nearby.getId());
			return;
		}

		Waypoint newWaypoint = placeWaypoint(player, clickedLoc);
		if (newWaypoint == null) return;

		if (session.isAutoEdgeEnabled() && session.hasLastWaypoint()) {
			createEdgeIfNotExists(player, session.getLastWaypointId(), newWaypoint.getId());
		}

		session.setLastWaypointId(newWaypoint.getId());
	}

	/**
	 * Creates and returns the waypoint, or null if creation failed
	 */
	private Waypoint placeWaypoint(Player player, Location loc) {
		int id = plugin.getDataManager().nextWaypointId();
		Waypoint wp = new Waypoint(id, loc.getWorld().getName(),
				loc.getX(), loc.getY(), loc.getZ());

		plugin.getDataManager().addWaypoint(wp);
		plugin.getNavigationGraph().addWaypoint(wp);
		plugin.getDataManager().save();

		player.sendMessage("§aWaypoint §f#" + id + " §acreated at §f"
				+ formatCoords(loc.getX(), loc.getY(), loc.getZ()));

		loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.3, 0.3, 0.3, 0);
		return wp;
	}

	private void removeWaypoint(Player player, Waypoint waypoint) {
		plugin.getDataManager().getEdges().removeIf(e ->
				e.getFromId() == waypoint.getId() || e.getToId() == waypoint.getId()
		);
		plugin.getNavigationGraph().removeWaypoint(waypoint.getId());
		plugin.getDataManager().removeWaypoint(waypoint.getId());
		plugin.getDataManager().save();

		player.sendMessage("§cWaypoint §f#" + waypoint.getId()
				+ " §cand its edges removed.");
	}

	private void createEdgeIfNotExists(Player player, int fromId, int toId) {
		if (fromId == toId) return;

		boolean exists = plugin.getDataManager().getEdges().stream().anyMatch(e ->
				(e.getFromId() == fromId && e.getToId() == toId) ||
						(e.getFromId() == toId   && e.getToId() == fromId)
		);

		if (exists) {
			player.sendMessage("§eEdge between §f#" + fromId
					+ " §eand §f#" + toId + " §ealready exists.");
			return;
		}

		Edge edge = new Edge(fromId, toId);
		plugin.getDataManager().addEdge(edge);
		plugin.getNavigationGraph().addEdge(edge);
		plugin.getDataManager().save();

		player.sendMessage("§aEdge created: §f#" + fromId + " §a↔ §f#" + toId);
	}

	public boolean isPrompting(Player player)
	{
		return prompting.contains(player.getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (isPrompting(player))
			prompting.remove(player.getUniqueId());
	}

	private void promptLocationName(Player player, Waypoint anchor) {
		if (isPrompting(player))
			return;
		prompting.add(player.getUniqueId());
		player.sendMessage("§dType the destination name in chat. Type §fcancel §dto abort.");

		plugin.getServer().getPluginManager().registerEvents(
				new ChatInputListener(plugin, player, input -> {
					if (!isPrompting(player))
						return;
					if (input.equalsIgnoreCase("cancel")) {
						player.sendMessage("§7Cancelled.");
						prompting.remove(player.getUniqueId());
						return;
					}
					if (plugin.getDataManager().getLocations().values().stream().anyMatch(n -> n.getName().equals(input)))
					{
						player.sendMessage("§cLocation §f\"" + input
								+ "\" §calready exists !");
						prompting.remove(player.getUniqueId());
						return;
					}
					int id = plugin.getDataManager().nextLocationId();
					NavLocation location = new NavLocation(id, input, anchor.getId());
					plugin.getDataManager().addLocation(location);
					plugin.getDataManager().save();
					player.sendMessage("§dLocation §f\"" + input
							+ "\" §dcreated on waypoint §f#" + anchor.getId());
					prompting.remove(player.getUniqueId());
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

					EditorSession session = getOrCreateSession(player);
					renderEditorParticles(player);
					renderPlaceholder(player, session);
					showActionBar(player, session);
				}
			}
		}.runTaskTimer(plugin, 0L,
				plugin.getPluginConfig().getVisualizationInterval());
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
			renderEdgeLine(player, from, to, Color.WHITE, 1.5);
		}
	}

	private void renderPlaceholder(Player player, EditorSession session) {
		if (session.getMode() != EditorMode.WAYPOINT) return;
		if (!session.isAutoEdgeEnabled()) return;
		if (!session.hasLastWaypoint()) return;

		Waypoint lastWp = plugin.getDataManager()
				.getWaypoints().get(session.getLastWaypointId());
		if (lastWp == null) return;

		Block targetBlock = player.getTargetBlockExact(MAX_TARGET_DISTANCE);
		if (targetBlock == null) return;

		Location targetLoc   = targetBlock.getLocation().add(0.5, 1.0, 0.5);
		String worldName     = player.getWorld().getName();

		Waypoint snapTarget = getNearbyWaypointExcluding(
				targetLoc, worldName, lastWp.getId()
		);

		if (snapTarget != null) {
			Location snapLoc = new Location(player.getWorld(),
					snapTarget.getX(), snapTarget.getY(), snapTarget.getZ());

			renderSnapIndicator(player, snapLoc);

			renderPlaceholderLine(player, lastWp, snapTarget.getX(),
					snapTarget.getY(), snapTarget.getZ(), Color.LIME);
		} else {
			renderPlaceholderLine(player, lastWp,
					targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(),
					Color.fromRGB(160, 160, 160));
		}
	}

	private void renderPlaceholderLine(Player player, Waypoint from, double tx, double ty, double tz, Color color) {
		double fx = from.getX();
		double fy = from.getY() + 0.1;
		double fz = from.getZ();

		double dx = tx - fx;
		double dy = (ty + 0.1) - fy;
		double dz = tz - fz;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance == 0) return;

		double spacing = 0.8;
		int count = (int) Math.ceil(distance / spacing);

		Particle.DustOptions dust = new Particle.DustOptions(color, 0.8f);
		for (int i = 0; i <= count; i++) {
			double t = (double) i / count;
			player.spawnParticle(Particle.DUST,
					new Location(player.getWorld(), fx + t * dx, fy + t * dy, fz + t * dz),
					1, 0, 0, 0, dust);
		}
	}

	private void renderSnapIndicator(Player player, Location center) {
		int points = 8;
		double radius = 0.5;
		Particle.DustOptions dust = new Particle.DustOptions(Color.LIME, 1.2f);

		for (int i = 0; i < points; i++) {
			double angle = (2 * Math.PI / points) * i;
			double x = center.getX() + radius * Math.cos(angle);
			double z = center.getZ() + radius * Math.sin(angle);
			player.spawnParticle(Particle.DUST,
					new Location(center.getWorld(), x, center.getY() + 0.5, z),
					1, 0, 0, 0, dust);
		}
	}

	private void renderEdgeLine(Player player, Waypoint from, Waypoint to, Color color, double spacing) {
		double distance = from.distanceTo(to);
		int steps = (int) Math.ceil(distance / spacing);

		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			double x = from.getX() + t * (to.getX() - from.getX());
			double y = from.getY() + t * (to.getY() - from.getY()) + 0.1;
			double z = from.getZ() + t * (to.getZ() - from.getZ());

			player.spawnParticle(Particle.DUST,
					new Location(player.getWorld(), x, y, z), 1, 0, 0, 0,
					new Particle.DustOptions(color, 0.8f));
		}
	}

	private void cycleMode(Player player, EditorSession session) {
		session.setMode(session.getMode().next());
		showActionBar(player, session);
		player.sendMessage(session.getMode().getDisplayName()
				+ " §7— " + session.getMode().getHint());
	}

	private void showActionBar(Player player, EditorSession session) {
		String autoEdgeInfo = session.getMode() == EditorMode.WAYPOINT
				? (session.isAutoEdgeEnabled() ? " §7| Auto-edge: §aON" : " §7| Auto-edge: §cOFF")
				: "";

		String lastWpInfo = session.hasLastWaypoint()
				? " §7| Last: §f#" + session.getLastWaypointId()
				: "";

		player.sendActionBar(Component.text(
				session.getMode().getDisplayName() + autoEdgeInfo + lastWpInfo
		));
	}

	private Waypoint getNearbyWaypoint(Location loc, String world) {
		return getNearbyWaypointExcluding(loc, world, -1);
	}

	/**
	 * Find the nearest waypoint within the detection radius,
	 * optionally excluding a specific waypoint (to avoid snapping to the lastWaypoint).
	 */
	private Waypoint getNearbyWaypointExcluding(Location loc, String world, int excludeId) {
		Waypoint closest = null;
		double minDist   = SNAP_RADIUS;

		for (Waypoint wp : plugin.getDataManager().getWaypoints().values()) {
			if (!wp.getWorld().equals(world)) continue;
			if (wp.getId() == excludeId) continue;

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