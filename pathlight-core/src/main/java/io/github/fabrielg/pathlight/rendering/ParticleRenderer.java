package io.github.fabrielg.pathlight.rendering;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Waypoint;
import io.github.fabrielg.pathlight.config.PluginConfig;
import io.github.fabrielg.pathlight.graph.NavigationGraph;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handles rendering particles along a calculated path.
 * Particles are sent only to the concerned player (client-side only).
 */
public class ParticleRenderer {

	private final NavigationGraph graph;
	private final PluginConfig config;

	public ParticleRenderer(NavigationGraph graph) {
		this.graph = graph;
		this.config = PathLightPlugin.getInstance().getPluginConfig();
	}

	/**
	 * Renders particles along the path starting from a given waypoint index.
	 * Called repeatedly in real-time as the player moves.
	 *
	 * @param player    the player to show particles to
	 * @param path      the full ordered list of waypoint IDs
	 * @param fromIndex the index in the path from which to start rendering
	 */
	public void render(Player player, List<Integer> path, int fromIndex) {
		render(player, path, fromIndex, config.getTrailColor());
	}

	/**
	 * Same as render() but with a custom color.
	 */
	public void render(Player player, List<Integer> path, int fromIndex, Color color) {
		if (path == null || path.isEmpty()) return;
		if (fromIndex >= path.size() - 1) return;

		for (int i = fromIndex; i < path.size() - 1; i++) {
			Waypoint from = graph.getWaypoint(path.get(i));
			Waypoint to   = graph.getWaypoint(path.get(i + 1));

			if (from == null || to == null) continue;

			renderSegment(player, from, to, color);
		}
	}

	/**
	 * Renders particles along a straight line between two waypoints.
	 * Uses linear interpolation to place particles at regular intervals.
	 */
	private void renderSegment(Player player, Waypoint from, Waypoint to, Color color) {
		double distance = from.distanceTo(to);
		if (distance == 0) return;

		int count = (int) Math.ceil(distance / config.getParticleSpacing());

		for (int i = 0; i <= count; i++) {
			double t = (double) i / count;

			double x = from.getX() + t * (to.getX() - from.getX());
			double y = from.getY() + t * (to.getY() - from.getY()) + config.getHeightOffset();
			double z = from.getZ() + t * (to.getZ() - from.getZ());

			spawnParticle(player, from.getWorld(), x, y, z, color);
		}
	}

	/**
	 * Spawns a single colored DUST particle at the given position.
	 * Only visible to the specified player.
	 */
	private void spawnParticle(Player player, String worldName, double x, double y, double z, Color color) {
		World world = player.getServer().getWorld(worldName);
		if (world == null) return;

		Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);

		player.spawnParticle(
				Particle.DUST,
				new Location(world, x, y, z),
				(int)config.getParticleSize(),
				0, 0, 0,
				dust
		);
	}

	/**
	 * Renders a real-time line from the player's current position
	 * to the next waypoint on the path.
	 * This shows the player where to go even if they are far from the path.
	 */
	public void renderPlayerToPath(Player player, List<Integer> path, int fromIndex, Color color) {
		if (path == null || path.isEmpty()) return;
		if (fromIndex >= path.size()) return;

		Waypoint nextOnPath = graph.getWaypoint(path.get(fromIndex));
		if (nextOnPath == null) return;

		World world = player.getServer().getWorld(nextOnPath.getWorld());
		if (world == null) return;

		double px = player.getLocation().getX();
		double py = player.getLocation().getY() + 0.1;
		double pz = player.getLocation().getZ();

		double tx = nextOnPath.getX();
		double ty = nextOnPath.getY() + 0.1;
		double tz = nextOnPath.getZ();

		double dx = tx - px;
		double dy = ty - py;
		double dz = tz - pz;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if (distance == 0) return;

		int count = (int) Math.ceil(distance / config.getParticleSpacing());
		Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);

		for (int i = 0; i <= count; i++) {
			double t = (double) i / count;
			player.spawnParticle(
					Particle.DUST,
					new Location(world, px + t * dx, py + t * dy, pz + t * dz),
					(int)config.getParticleSize(),
					0, 0, 0, dust
			);
		}
	}
}