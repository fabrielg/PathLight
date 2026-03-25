package io.github.fabrielg.pathlight.rendering;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import io.github.fabrielg.pathlight.api.event.PathEndEvent;
import io.github.fabrielg.pathlight.api.event.PathRecalculateEvent;
import io.github.fabrielg.pathlight.graph.NavigationGraph;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TrailManager implements Listener {

	private final PathLightPlugin plugin;
	private final NavigationGraph graph;
	private final ParticleRenderer renderer;

	private final Map<UUID, ActiveTrail> activeTrails = new HashMap<>();

	public TrailManager(PathLightPlugin plugin, NavigationGraph graph) {
		this.plugin   = plugin;
		this.graph    = graph;
		this.renderer = new ParticleRenderer(graph);
		startRenderLoop();
	}

	/* API */
	public void startTrail(Player player, List<Integer> path, int destinationId) {
		activeTrails.put(player.getUniqueId(), new ActiveTrail(path, destinationId));
	}

	public void stopTrail(Player player) {
		ActiveTrail trail = activeTrails.remove(player.getUniqueId());
		if (trail != null) {
			NavLocation dest = plugin.getNavigationGraph().getLocation(trail.getDestinationWaypointId());
			PathEndEvent event = new PathEndEvent(player, dest, PathEndEvent.Reason.CANCELLED);
			plugin.getServer().getPluginManager().callEvent(event);
		}
	}

	public boolean hasTrail(Player player) {
		return activeTrails.containsKey(player.getUniqueId());
	}

	/* Rendering loop */

	/**
	 * Starts the repeating task that renders and updates all active trails.
	 */
	private void startRenderLoop() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Map.Entry<UUID, ActiveTrail> entry : Map.copyOf(activeTrails).entrySet()) {
					UUID uuid        = entry.getKey();
					ActiveTrail trail = entry.getValue();
					Player player    = plugin.getServer().getPlayer(uuid);

					if (player == null || !player.isOnline()) {
						activeTrails.remove(uuid);
						continue;
					}

					updateTrailIndex(player, trail);

					if (isOffPath(player, trail)) {
						boolean recalculated = recalculatePath(player, trail);
						if (!recalculated) {
							player.sendMessage("§cCannot find a path from your position.");
							activeTrails.remove(uuid);
							continue;
						}
					}

					if (trail.isComplete()) {
						activeTrails.remove(uuid);

						NavLocation dest = plugin.getNavigationGraph().getLocation(trail.getDestinationWaypointId());
						PathEndEvent event = new PathEndEvent(player, dest, PathEndEvent.Reason.REACHED);
						plugin.getServer().getPluginManager().callEvent(event);

						player.sendMessage("§aYou have reached your destination!");
						continue;
					}

					renderer.render(player, trail.getPath(), trail.getCurrentIndex(),
							Color.fromRGB(255, 140, 0));

					renderer.renderPlayerToPath(player, trail.getPath(),
							trail.getCurrentIndex(), plugin.getPluginConfig().getPlayerLineColor());
				}
			}
		}.runTaskTimer(plugin, 0L, plugin.getPluginConfig().getRefreshInterval());
	}

	/**
	 * Finds the closest waypoint on the path to the player
	 * and jumps directly to that index.
	 * This handles shortcuts naturally : if the player skips
	 * ahead, the index advances automatically.
	 */
	private void updateTrailIndex(Player player, ActiveTrail trail) {
		List<Integer> path = trail.getPath();
		int currentIndex   = trail.getCurrentIndex();

		double minDist     = Double.MAX_VALUE;
		int bestIndex      = currentIndex;

		for (int i = currentIndex; i < path.size(); i++) {
			Waypoint wp = graph.getWaypoint(path.get(i));
			if (wp == null) continue;

			double dist = distanceTo(player, wp);
			if (dist < minDist) {
				minDist   = dist;
				bestIndex = i;
			}
		}

		trail.setCurrentIndex(bestIndex);
	}

	/**
	 * Returns true if the player's closest waypoint in the entire graph
	 * is not on the current path (player went off-route).
	 */
	private boolean isOffPath(Player player, ActiveTrail trail) {
		Waypoint closestInGraph = graph.getClosestWaypoint(
				player.getWorld().getName(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
		);

		if (closestInGraph == null) return false;

		if (trail.getPath().contains(closestInGraph.getId())) return false;

		double dist = distanceTo(player, closestInGraph);
		return dist < plugin.getPluginConfig().getOffPathThreshold();
	}

	/**
	 * Recalculates the path from the player's current position
	 * to the original destination.
	 * Returns true if a new path was found.
	 */
	private boolean recalculatePath(Player player, ActiveTrail trail) {
		Waypoint closestToPlayer = graph.getClosestWaypoint(
				player.getWorld().getName(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
		);

		if (closestToPlayer == null) return false;

		List<Integer> newPath = plugin.getPathfinder().findPath(
				closestToPlayer.getId(),
				trail.getDestinationWaypointId()
		);

		if (newPath.isEmpty()) return false;

		NavLocation dest = plugin.getNavigationGraph().getLocation(trail.getDestinationWaypointId());
		PathRecalculateEvent event = new PathRecalculateEvent(player, dest, trail.getPath(), newPath);
		plugin.getServer().getPluginManager().callEvent(event);

		trail.setPath(newPath);
		trail.setCurrentIndex(0);
		return true;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		ActiveTrail trail = activeTrails.remove(player.getUniqueId());

		if (trail != null) {
			NavLocation dest = plugin.getNavigationGraph()
					.getLocation(trail.getDestinationWaypointId());
			PathEndEvent endEvent = new PathEndEvent(
					player, dest, PathEndEvent.Reason.DISCONNECTED
			);
			plugin.getServer().getPluginManager().callEvent(endEvent);
		}
	}

	private double distanceTo(Player player, Waypoint wp) {
		double dx = player.getLocation().getX() - wp.getX();
		double dy = player.getLocation().getY() - wp.getY();
		double dz = player.getLocation().getZ() - wp.getZ();
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private static class ActiveTrail {
		private List<Integer> path;
		private int currentIndex = 0;
		private final int destinationWaypointId;

		ActiveTrail(List<Integer> path, int destinationWaypointId) {
			this.path                  = path;
			this.destinationWaypointId = destinationWaypointId;
		}

		public List<Integer> getPath()                   { return path; }
		public void setPath(List<Integer> path)          { this.path = path; }
		public int getCurrentIndex()                     { return currentIndex; }
		public void setCurrentIndex(int idx)             { this.currentIndex = idx; }
		public int getDestinationWaypointId()            { return destinationWaypointId; }
		public boolean isComplete()                      { return currentIndex >= path.size() - 1; }
	}
}