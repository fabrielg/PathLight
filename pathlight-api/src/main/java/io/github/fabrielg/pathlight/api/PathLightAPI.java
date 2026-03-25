package io.github.fabrielg.pathlight.api;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * PathLight public API.
 * Use PathLightAPI.getInstance() to access the implementation.
 *
 * Example usage from another plugin:
 * <pre>
 * {@code
 * PathLightAPI api = PathLightAPI.getInstance();
 *
 * // Start navigation
 * api.startNavigation(player, "Castle");
 *
 * // Stop navigation
 * api.stopNavigation(player);
 *
 * // Check if navigating
 * if (api.isNavigating(player)) { ... }
 * }
 * </pre>
 */
public interface PathLightAPI {

	/**
	 * Returns the PathLight API instance.
	 * Returns null if PathLight is not loaded.
	 */
	static PathLightAPI getInstance() {
		return PathLightAPIProvider.instance;
	}

	/**
	 * Starts navigation for a player toward the named destination.
	 * Fires a PathStartEvent (cancellable).
	 *
	 * @param player          the player to navigate
	 * @param destinationName the name of the destination (case-insensitive)
	 * @return true if navigation started, false if destination not found or no path exists
	 */
	boolean startNavigation(Player player, String destinationName);

	/**
	 * Starts navigation for a player toward a destination by its ID.
	 *
	 * @param player        the player to navigate
	 * @param locationId    the ID of the NavLocation
	 * @return true if navigation started successfully
	 */
	boolean startNavigationById(Player player, int locationId);

	/**
	 * Stops the active navigation for a player.
	 * Fires a PathEndEvent with reason CANCELLED.
	 * Does nothing if the player has no active navigation.
	 */
	void stopNavigation(Player player);

	/**
	 * Returns true if the player currently has an active navigation.
	 */
	boolean isNavigating(Player player);

	/**
	 * Returns all registered destinations.
	 */
	Collection<NavLocation> getLocations();

	/**
	 * Finds a destination by its exact name (case-insensitive).
	 */
	Optional<NavLocation> findLocation(String name);

	/**
	 * Finds a destination by its ID.
	 */
	Optional<NavLocation> findLocationById(int id);

	/**
	 * Returns all registered waypoints.
	 */
	Collection<Waypoint> getWaypoints();

	/**
	 * Calculates a path between two waypoints and returns the ordered list of IDs.
	 * Returns an empty list if no path exists.
	 */
	List<Integer> calculatePath(int fromWaypointId, int toWaypointId);
}
