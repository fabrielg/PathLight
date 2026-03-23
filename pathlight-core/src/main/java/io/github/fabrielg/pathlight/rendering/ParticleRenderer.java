package io.github.fabrielg.pathlight.rendering;

import io.github.fabrielg.pathlight.api.Waypoint;
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

	private static final double PARTICLE_SPACING = 0.5;
	private static final double HEIGHT_OFFSET = 0.1;
	private static final Color DEFAULT_COLOR = Color.fromRGB(255, 223, 0);

	private final NavigationGraph graph;

	public ParticleRenderer(NavigationGraph graph) {
		this.graph = graph;
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
		render(player, path, fromIndex, DEFAULT_COLOR);
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

		int count = (int) Math.ceil(distance / PARTICLE_SPACING);

		for (int i = 0; i <= count; i++) {
			double t = (double) i / count;

			double x = from.getX() + t * (to.getX() - from.getX());
			double y = from.getY() + t * (to.getY() - from.getY()) + HEIGHT_OFFSET;
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
				1,
				0, 0, 0,
				dust
		);
	}
}