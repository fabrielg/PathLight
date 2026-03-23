package io.github.fabrielg.pathlight.rendering;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Waypoint;
import io.github.fabrielg.pathlight.graph.NavigationGraph;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages active trails for all players.
 * Refreshes particle rendering in real-time and advances
 * the trail start as the player moves forward.
 */
public class TrailManager {
	private static final long REFRESH_INTERVAL_TICKS = 10L; // 10 ticks
	private static final double WAYPOINT_REACHED_DISTANCE = 4.0;

	private final PathLightPlugin plugin;
	private final NavigationGraph graph;
	private final ParticleRenderer renderer;

	private final Map<UUID, ActiveTrail> activeTrails = new HashMap<>();

	public TrailManager(PathLightPlugin plugin, NavigationGraph graph) {
		this.plugin		= plugin;
		this.graph		= graph;
		this.renderer	= new ParticleRenderer(graph);

		startRenderLoop();
	}

	/* API */

	/**
	 * Starts a trail for the given player toward the given path.
	 * Replaces any existing trail for that player.
	 */
	public void startTrail(Player player, List<Integer> path) {
		activeTrails.put(player.getUniqueId(), new ActiveTrail(path));
	}

	/**
	 * Cancels and removes the active trail for the given player.
	 */
	public void stopTrail(Player player) {
		activeTrails.remove(player.getUniqueId());
	}

	/**
	 * Returns true if the player currently has an active trail.
	 */
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
					UUID uuid  = entry.getKey();
					ActiveTrail trail = entry.getValue();

					Player player = plugin.getServer().getPlayer(uuid);

					if (player == null || !player.isOnline()) {
						activeTrails.remove(uuid);
						continue;
					}

					updateTrailIndex(player, trail);

					if (trail.isComplete()) {
						activeTrails.remove(uuid);
						player.sendMessage("§aYou have reached your destination!");
						continue;
					}

					renderer.render(player, trail.getPath(), trail.getCurrentIndex());
				}
			}
		}.runTaskTimer(plugin, 0L, REFRESH_INTERVAL_TICKS);
	}

	/**
	 * Advances the trail's start index based on the player's current position.
	 * When the player is close enough to the next waypoint, we move forward.
	 */
	private void updateTrailIndex(Player player, ActiveTrail trail) {
		List<Integer> path = trail.getPath();
		int currentIndex   = trail.getCurrentIndex();

		while (currentIndex < path.size() - 1) {
			Waypoint next = graph.getWaypoint(path.get(currentIndex + 1));
			if (next == null) break;

			double dx = player.getLocation().getX() - next.getX();
			double dy = player.getLocation().getY() - next.getY();
			double dz = player.getLocation().getZ() - next.getZ();
			double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (distance <= WAYPOINT_REACHED_DISTANCE) {
				currentIndex++;
				trail.setCurrentIndex(currentIndex);
			} else {
				break;
			}
		}
	}

	/**
	 * Represents the state of an active trail for one player.
	 */
	private static class ActiveTrail {
		private final List<Integer> path;
		private int currentIndex = 0;

		ActiveTrail(List<Integer> path) {
			this.path = path;
		}

		public List<Integer> getPath()        { return path; }
		public int getCurrentIndex()          { return currentIndex; }
		public void setCurrentIndex(int idx)  { this.currentIndex = idx; }
		public boolean isComplete()           { return currentIndex >= path.size() - 1; }
	}

}
