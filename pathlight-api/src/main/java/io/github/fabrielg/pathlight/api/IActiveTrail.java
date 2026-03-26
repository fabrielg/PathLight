package io.github.fabrielg.pathlight.api;

import org.bukkit.Color;

import java.util.List;

/**
 * Represents an active navigation trail for a player.
 * Accessible via PathLightAPI.getActiveTrail(player).
 *
 * Example usage:
 * <pre>
 * {@code
 * IActiveTrail trail = PathLightAPI.getInstance().getActiveTrail(player);
 * if (trail != null) {
 *     // Change trail color dynamically
 *     trail.setTrailColor(Color.RED);
 *
 *     // Check destination
 *     player.sendMessage("Navigating to: " + trail.getDestination().getName());
 * }
 * }
 * </pre>
 */
public interface IActiveTrail {
	/**
	 * Returns the destination this trail is navigating toward.
	 */
	NavLocation getDestination();

	/**
	 * Returns the full calculated path as an ordered list of waypoint IDs.
	 */
	List<Integer> getPath();

	/**
	 * Returns the index of the current waypoint in the path.
	 * 0 = just started, path.size() - 1 = arrived.
	 */
	int getCurrentIndex();

	/**
	 * Returns true if the player has reached the destination.
	 */
	boolean isComplete();

	/**
	 * Returns the current trail particle color.
	 */
	Color getTrailColor();

	/**
	 * Sets the trail particle color in real-time.
	 * The change is visible on the next render tick.
	 *
	 * @param color the new color
	 */
	void setTrailColor(Color color);

}
