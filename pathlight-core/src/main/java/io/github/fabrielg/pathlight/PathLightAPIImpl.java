package io.github.fabrielg.pathlight;

import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.PathLightAPI;
import io.github.fabrielg.pathlight.api.Waypoint;
import io.github.fabrielg.pathlight.api.event.PathStartEvent;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of PathLightAPI.
 * Registered on startup, bridges the public API to internal systems.
 */
public class PathLightAPIImpl implements PathLightAPI {

	private final PathLightPlugin plugin;

	public PathLightAPIImpl(PathLightPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean startNavigation(Player player, String destinationName) {
		Optional<NavLocation> location = findLocation(destinationName);
		return location.filter(l -> startNavigationById(player, l.getId())).isPresent();
	}

	@Override
	public boolean startNavigationById(Player player, int locationId) {
		NavLocation destination = plugin.getNavigationGraph().getLocation(locationId);
		if (destination == null)
			return false;

		Waypoint anchor = plugin.getNavigationGraph().getWaypoint(destination.getAnchorWaypointId());
		if (anchor == null)
			return false;

		Waypoint closestToPlayer = plugin.getNavigationGraph().getClosestWaypoint(
				player.getWorld().getName(),
				player.getX(),
				player.getY(),
				player.getZ()
		);
		if (closestToPlayer == null)
			return false;

		List<Integer> path = plugin.getPathfinder().findPath(closestToPlayer.getId(), anchor.getId());
		if (path.isEmpty())
			return false;

		PathStartEvent event = new PathStartEvent(player, destination, path);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;

		plugin.getTrailManager().startTrail(player, path, anchor.getId());
		return true;
	}

	@Override
	public void stopNavigation(Player player) {
		plugin.getTrailManager().stopTrail(player);
	}

	@Override
	public boolean isNavigating(Player player) {
		return plugin.getTrailManager().hasTrail(player);
	}

	@Override
	public Collection<NavLocation> getLocations() {
		return plugin.getNavigationGraph().getAllLocations();
	}

	@Override
	public Optional<NavLocation> findLocation(String name) {
		return plugin.getDataManager().getLocations().values().stream()
				.filter(l -> l.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	@Override
	public Optional<NavLocation> findLocationById(int id) {
		return Optional.ofNullable(plugin.getDataManager().getLocations().get(id));
	}

	@Override
	public Collection<Waypoint> getWaypoints() {
		return plugin.getDataManager().getWaypoints().values();
	}

	@Override
	public List<Integer> calculatePath(int fromWaypointId, int toWaypointId) {
		return plugin.getPathfinder().findPath(fromWaypointId, toWaypointId);
	}
}
