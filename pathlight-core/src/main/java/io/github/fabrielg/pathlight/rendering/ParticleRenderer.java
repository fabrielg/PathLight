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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static io.github.fabrielg.pathlight.rendering.TrailStyle.*;

/**
 * Handles rendering particles along a calculated path.
 * Supports LINEAR and CATMULL_ROM trail styles.
 * Particles are sent only to the concerned player.
 */
public class ParticleRenderer {

	private final NavigationGraph graph;
	private final PluginConfig config;

	public ParticleRenderer(NavigationGraph graph) {
		this.graph = graph;
		this.config = PathLightPlugin.getInstance().getPluginConfig();
	}

	/**
	 * Renders the trail from fromIndex to the end of the path.
	 * Automatically uses the configured trail style.
	 */
	public void render(Player player, List<Integer> path, int fromIndex, Color color) {
		if (path == null || path.isEmpty()) return;
		if (fromIndex >= path.size()) return;

		List<Integer> subPath = path.subList(fromIndex, path.size());

		switch (config.getTrailStyle()) {
			case LINEAR      -> renderLinear(player, subPath, color);
			case CATMULL_ROM -> renderCatmullRom(player, subPath, color);
		}
	}

	// ─────────────────────────────────────────
	//  STYLE : LINEAR
	// ─────────────────────────────────────────

	/**
	 * Renders straight lines between each pair of consecutive waypoints.
	 */
	private void renderLinear(Player player, List<Integer> path, Color color) {
		for (int i = 0; i < path.size() - 1; i++) {
			Waypoint from = graph.getWaypoint(path.get(i));
			Waypoint to   = graph.getWaypoint(path.get(i + 1));
			if (from == null || to == null) continue;

			renderSegmentLinear(player, from, to, color);
		}
	}

	private void renderSegmentLinear(Player player, Waypoint from, Waypoint to, Color color) {
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

	// ─────────────────────────────────────────
	//  STYLE : CATMULL-ROM
	// ─────────────────────────────────────────

	/**
	 * Renders a smooth Catmull-Rom spline through all waypoints in the path.
	 */
	private void renderCatmullRom(Player player, List<Integer> path, Color color) {
		List<Vector> controlPoints = new ArrayList<>();
		String worldName = null;

		for (int id : path) {
			Waypoint wp = graph.getWaypoint(id);
			if (wp == null) continue;
			controlPoints.add(new Vector(wp.getX(), wp.getY() + config.getHeightOffset(), wp.getZ()));
			if (worldName == null) worldName = wp.getWorld();
		}

		if (controlPoints.size() < 2 || worldName == null) return;

		CatmullRomSpline spline = new CatmullRomSpline(
				config.getCatmullTension(),
				config.getCatmullSamples()
		);
		List<Vector> splinePoints = spline.generate(controlPoints);

		renderSplinePoints(player, splinePoints, worldName, color);
	}

	/**
	 * Places particles at regular intervals along the spline points.
	 * Uses distance-based spacing rather than index-based for uniform density.
	 */
	private void renderSplinePoints(Player player, List<Vector> points,
									String worldName, Color color) {
		double accumulated = 0.0;
		double spacing     = config.getParticleSpacing();

		spawnParticle(player, worldName,
				points.get(0).getX(),
				points.get(0).getY(),
				points.get(0).getZ(), color);

		for (int i = 1; i < points.size(); i++) {
			Vector prev = points.get(i - 1);
			Vector curr = points.get(i);

			double segmentLength = prev.distance(curr);
			accumulated += segmentLength;

			if (accumulated >= spacing) {
				spawnParticle(player, worldName, curr.getX(), curr.getY(), curr.getZ(), color);
				accumulated = 0.0;
			}
		}
	}

	// ─────────────────────────────────────────
	//  PLAYER LINE → NEXT WAYPOINT
	// ─────────────────────────────────────────

	/**
	 * Renders a real-time line from the player's position
	 * to the next waypoint on the path.
	 * Always uses LINEAR style regardless of config
	 * (a curved line toward the player would look odd).
	 */
	public void renderPlayerToPath(Player player, List<Integer> path,
								   int fromIndex, Color color) {
		if (path == null || path.isEmpty()) return;
		if (fromIndex >= path.size()) return;

		Waypoint next = graph.getWaypoint(path.get(fromIndex));
		if (next == null) return;

		World world = player.getServer().getWorld(next.getWorld());
		if (world == null) return;

		double px = player.getLocation().getX();
		double py = player.getLocation().getY() + config.getHeightOffset();
		double pz = player.getLocation().getZ();
		double tx = next.getX();
		double ty = next.getY() + config.getHeightOffset();
		double tz = next.getZ();

		double dx = tx - px;
		double dy = ty - py;
		double dz = tz - pz;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance == 0) return;

		int count = (int) Math.ceil(distance / config.getParticleSpacing());
		Particle.DustOptions dust = new Particle.DustOptions(color, config.getParticleSize());

		for (int i = 0; i <= count; i++) {
			double t = (double) i / count;
			player.spawnParticle(Particle.DUST,
					new Location(world, px + t * dx, py + t * dy, pz + t * dz),
					1, 0, 0, 0, dust);
		}
	}

	// ─────────────────────────────────────────
	//  HELPER
	// ─────────────────────────────────────────

	private void spawnParticle(Player player, String worldName,
							   double x, double y, double z, Color color) {
		World world = player.getServer().getWorld(worldName);
		if (world == null) return;

		Particle.DustOptions dust = new Particle.DustOptions(color, config.getParticleSize());
		player.spawnParticle(Particle.DUST,
				new Location(world, x, y, z),
				1, 0, 0, 0, dust);
	}
}