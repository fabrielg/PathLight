package io.github.fabrielg.pathlight.config;

import io.github.fabrielg.pathlight.PathLightPlugin;
import org.bukkit.Color;

/**
 * Central access point for all PathLight configuration values.
 * Reads from config.yml and exposes typed getters.
 * Call reload() to re-read the file without restarting.
 */
public class PluginConfig {

	private final PathLightPlugin plugin;

	// Particles
	private double	particleSpacing;
	private double	heightOffset;
	private Color	trailColor;
	private Color	playerLineColor;
	private float	particleSize;

	// Navigation
	private long	refreshInterval;
	private double	offPathThreshold;

	// Editor
	private double	waypointClickRadius;
	private double	edgeParticleSpacing;
	private long	visualizationInterval;

	public PluginConfig(PathLightPlugin plugin) {
		this.plugin = plugin;
		load();
	}

	/**
	 * Loads (or reloads) all values from config.yml.
	 * If a value is missing or invalid, a default is used and a warning is logged.
	 */
	public void load() {
		plugin.saveDefaultConfig();

		plugin.reloadConfig();

		particleSpacing       = getDouble("particles.spacing",           0.5);
		heightOffset          = getDouble("particles.height-offset",      0.1);
		trailColor            = parseColor("particles.trail-color",       "255,140,0");
		playerLineColor       = parseColor("particles.player-line-color", "255,200,50");
		particleSize          = (float) getDouble("particles.size",       1.0);

		refreshInterval       = (long) getDouble("navigation.refresh-interval",  10);
		offPathThreshold      = getDouble("navigation.off-path-threshold",        8.0);

		waypointClickRadius   = getDouble("editor.waypoint-click-radius",         3.0);
		edgeParticleSpacing   = getDouble("editor.edge-particle-spacing",         1.5);
		visualizationInterval = (long) getDouble("editor.visualization-interval", 10);

		plugin.getLogger().info("Configuration loaded successfully.");
	}

	public double getParticleSpacing()       { return particleSpacing; }
	public double getHeightOffset()          { return heightOffset; }
	public Color  getTrailColor()            { return trailColor; }
	public Color  getPlayerLineColor()       { return playerLineColor; }
	public float  getParticleSize()          { return particleSize; }

	public long   getRefreshInterval()       { return refreshInterval; }
	public double getOffPathThreshold()      { return offPathThreshold; }

	public double getWaypointClickRadius()   { return waypointClickRadius; }
	public double getEdgeParticleSpacing()   { return edgeParticleSpacing; }
	public long   getVisualizationInterval() { return visualizationInterval; }

	/**
	 * Reads a double from config with a fallback default.
	 * Logs a warning if the key is missing.
	 */
	private double getDouble(String path, double defaultValue) {
		if (!plugin.getConfig().contains(path)) {
			plugin.getLogger().warning("Missing config key: '" + path
					+ "', using default: " + defaultValue);
			return defaultValue;
		}
		return plugin.getConfig().getDouble(path, defaultValue);
	}

	/**
	 * Parses a "R,G,B" string from config into a Bukkit Color.
	 * Falls back to the default string if parsing fails.
	 */
	private Color parseColor(String path, String defaultRgb) {
		String raw = plugin.getConfig().getString(path, defaultRgb);

		try {
			String[] parts = raw.split(",");
			if (parts.length != 3) throw new IllegalArgumentException("Expected 3 components");

			int r = Integer.parseInt(parts[0].trim());
			int g = Integer.parseInt(parts[1].trim());
			int b = Integer.parseInt(parts[2].trim());

			if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
				throw new IllegalArgumentException("RGB values must be between 0 and 255");
			}

			return Color.fromRGB(r, g, b);

		} catch (Exception e) {
			plugin.getLogger().warning("Invalid color format for '" + path
					+ "': \"" + raw + "\". Expected format: R,G,B (ex: 255,140,0). "
					+ "Using default: " + defaultRgb);

			String[] parts = defaultRgb.split(",");
			return Color.fromRGB(
					Integer.parseInt(parts[0].trim()),
					Integer.parseInt(parts[1].trim()),
					Integer.parseInt(parts[2].trim())
			);
		}
	}

}
