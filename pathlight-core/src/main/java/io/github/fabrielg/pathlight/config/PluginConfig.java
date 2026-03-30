package io.github.fabrielg.pathlight.config;

import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.rendering.TrailStyle;
import org.bukkit.Color;

/**
 * Central access point for all PathLight configuration values.
 * Reads from config.yml and exposes typed getters.
 * Call reload() to re-read the file without restarting.
 */
public class PluginConfig {

	private final PathLightPlugin plugin;

	// Particles
	private double		particleSpacing;
	private double		heightOffset;
	private Color		trailColor;
	private Color		playerLineColor;
	private float		particleSize;
	private TrailStyle	trailStyle;
	private double		catmullTension;
	private int			catmullSamples;

	// Navigation
	private long	refreshInterval;
	private double	offPathThreshold;

	// Editor
	private double	waypointClickRadius;
	private double	waypointSnapRadius;
	private double	edgeParticleSpacing;
	private long	visualizationInterval;
	private Color editorWaypointColor;
	private Color editorWaypointAnchorColor;
	private Color editorEdgeColor;
	private Color editorPlaceholderNewColor;
	private Color editorPlaceholderSnapColor;
	private Color editorSnapIndicatorColor;

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

		particleSpacing       = getDouble("particles.spacing",           0.4);

		heightOffset          = getDouble("particles.height-offset",      0.1);

		trailColor            = parseColor("particles.trail-color",       "255,140,0");

		playerLineColor       = parseColor("particles.player-line-color", "255,140,0");

		particleSize          = (float) getDouble("particles.size",       1.4);

		trailStyle			  = parseTrailStyle("particles.trail-style", "CATMULL_ROM");

		catmullTension 		  = getDouble("particles.catmull-tension", 0.5);

		catmullSamples  	  = (int) getDouble("particles.catmull-samples-per-segment", 12);


		refreshInterval       = (long) getDouble("navigation.refresh-interval",  5);

		offPathThreshold      = getDouble("navigation.off-path-threshold",        8.0);


		waypointClickRadius   = getDouble("editor.waypoint-click-radius",         1.0);

		waypointSnapRadius    = getDouble("editor.waypoint-snap-radius",          1.5);

		edgeParticleSpacing   = getDouble("editor.edge-particle-spacing",         0.1);

		visualizationInterval = (long) getDouble("editor.visualization-interval", 5);

		editorWaypointColor        = parseColor("editor.colors.waypoint",          "255,215,0");

		editorWaypointAnchorColor  = parseColor("editor.colors.waypoint-anchor",   "255,0,255");

		editorEdgeColor            = parseColor("editor.colors.edge",              "255,255,255");

		editorPlaceholderNewColor  = parseColor("editor.colors.placeholder-new",   "160,160,160");

		editorPlaceholderSnapColor = parseColor("editor.colors.placeholder-snap",  "0,255,255");

		editorSnapIndicatorColor   = parseColor("editor.colors.snap-indicator",    "0,255,0");


		plugin.getLogger().info("Configuration loaded successfully.");
	}

	public double getParticleSpacing()       { return particleSpacing; }
	public double getHeightOffset()          { return heightOffset; }
	public Color  getTrailColor()            { return trailColor; }
	public Color  getPlayerLineColor()       { return playerLineColor; }
	public float  getParticleSize()          { return particleSize; }
	public TrailStyle getTrailStyle()		 { return trailStyle; }
	public double getCatmullTension()		 { return catmullTension; }

	public int getCatmullSamples()			 { return catmullSamples; }

	public long   getRefreshInterval()       { return refreshInterval; }
	public double getOffPathThreshold()      { return offPathThreshold; }

	public double getWaypointClickRadius()   { return waypointClickRadius; }
	public double getWaypointSnapRadius()    { return waypointSnapRadius; }
	public double getEdgeParticleSpacing()   { return edgeParticleSpacing; }
	public long   getVisualizationInterval() { return visualizationInterval; }
	public Color getEditorWaypointColor()       { return editorWaypointColor; }
	public Color getEditorWaypointAnchorColor() { return editorWaypointAnchorColor; }
	public Color getEditorEdgeColor()           { return editorEdgeColor; }
	public Color getEditorPlaceholderNewColor() { return editorPlaceholderNewColor; }
	public Color getEditorPlaceholderSnapColor(){ return editorPlaceholderSnapColor; }
	public Color getEditorSnapIndicatorColor()  { return editorSnapIndicatorColor; }

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

	private TrailStyle parseTrailStyle(String path, String defaultValue) {
		String raw = plugin.getConfig().getString(path, defaultValue);
		try {
			return TrailStyle.valueOf(raw.toUpperCase());
		} catch (IllegalArgumentException e) {
			plugin.getLogger().warning("Invalid trail-style: \"" + raw
					+ "\". Valid values: LINEAR, CATMULL_ROM. Using default: " + defaultValue);
			return TrailStyle.valueOf(defaultValue);
		}
	}

}
