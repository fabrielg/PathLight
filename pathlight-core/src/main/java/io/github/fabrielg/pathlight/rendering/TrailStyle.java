package io.github.fabrielg.pathlight.rendering;

/**
 * Defines the visual style used to render particle trails.
 */
public enum TrailStyle {

	/**
	 * Straight lines between each waypoint pair.
	 * Clean, precise, low CPU cost.
	 */
	LINEAR,

	/**
	 * Smooth curve passing through all waypoints using Catmull-Rom splines.
	 * Natural-looking, no manual configuration needed.
	 * Automatically adapts to the direction of the path.
	 */
	CATMULL_ROM

}
