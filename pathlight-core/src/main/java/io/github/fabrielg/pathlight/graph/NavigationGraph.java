package io.github.fabrielg.pathlight.graph;

import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;
import io.github.fabrielg.pathlight.data.DataManager;

import java.util.*;

/**
 * In-memory navigation graph built from DataManager data.
 * Organizes waypoints and edges into an adjacency list
 * for efficient pathfinding.
 */
public class NavigationGraph {

	private final DataManager dataManager;

	private final Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

	public NavigationGraph(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * Builds the adjacency list from the data currently loaded in DataManager.
	 * Must be called after DataManager.load(), and again after any structural change.
	 */
	public void build() {
		adjacencyList.clear();

		for (int id : dataManager.getWaypoints().keySet()) {
			adjacencyList.put(id, new ArrayList<>());
		}

		for (Edge edge : dataManager.getEdges()) {
			int from = edge.getFromId();
			int to   = edge.getToId();

			if (!adjacencyList.containsKey(from) || !adjacencyList.containsKey(to)) {
				continue;
			}

			adjacencyList.get(from).add(to);
			adjacencyList.get(to).add(from);
		}
	}

	/**
	 * Registers a new waypoint in the graph (without full rebuild).
	 */
	public void addWaypoint(Waypoint waypoint) {
		adjacencyList.put(waypoint.getId(), new ArrayList<>());
	}

	/**
	 * Removes a waypoint and all its connections from the graph.
	 */
	public void removeWaypoint(int waypointId) {
		adjacencyList.remove(waypointId);
		for (List<Integer> neighbors : adjacencyList.values()) {
			neighbors.remove(Integer.valueOf(waypointId));
		}
	}

	/**
	 * Registers a new edge in the graph (without full rebuild).
	 */
	public void addEdge(Edge edge) {
		adjacencyList.computeIfAbsent(edge.getFromId(), k -> new ArrayList<>()).add(edge.getToId());
		adjacencyList.computeIfAbsent(edge.getToId(),   k -> new ArrayList<>()).add(edge.getFromId());
	}

	/**
	 * Removes an edge from the graph.
	 */
	public void removeEdge(int fromId, int toId) {
		if (adjacencyList.containsKey(fromId))
			adjacencyList.get(fromId).remove(Integer.valueOf(toId));

		if (adjacencyList.containsKey(toId))
			adjacencyList.get(toId).remove(Integer.valueOf(fromId));
	}

	/**
	 * Returns the neighbors of a given waypoint.
	 */
	public List<Integer> getNeighbors(int waypointId) {
		return adjacencyList.getOrDefault(waypointId, Collections.emptyList());
	}

	/**
	 * Returns the waypoint object by its ID.
	 */
	public Waypoint getWaypoint(int id) {
		return dataManager.getWaypoints().get(id);
	}

	/**
	 * Finds the closest waypoint to a given position in the world.
	 * Used to determine the player's entry point in the graph.
	 */
	public Waypoint getClosestWaypoint(String world, double x, double y, double z) {
		Waypoint closest = null;
		double minDistance = Double.MAX_VALUE;

		for (Waypoint waypoint : dataManager.getWaypoints().values()) {
			if (!waypoint.getWorld().equals(world)) continue;

			double dx = waypoint.getX() - x;
			double dy = waypoint.getY() - y;
			double dz = waypoint.getZ() - z;
			double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (distance < minDistance) {
				minDistance = distance;
				closest = waypoint;
			}
		}

		return closest;
	}

	/**
	 * Finds the closest waypoint to the player that is part of a given path.
	 * Used in real-time to advance the display along the calculated path.
	 */
	public Waypoint getClosestWaypointOnPath(String world, double x, double y, double z, List<Integer> path) {
		Waypoint closest = null;
		double minDistance = Double.MAX_VALUE;

		for (int waypointId : path) {
			Waypoint waypoint = getWaypoint(waypointId);
			if (waypoint == null) continue;
			if (!waypoint.getWorld().equals(world)) continue;

			double dx = waypoint.getX() - x;
			double dy = waypoint.getY() - y;
			double dz = waypoint.getZ() - z;
			double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (distance < minDistance) {
				minDistance = distance;
				closest = waypoint;
			}
		}

		return closest;
	}

	/**
	 * Returns the NavLocation by its ID.
	 */
	public NavLocation getLocation(int id) {
		return dataManager.getLocations().get(id);
	}

	/**
	 * Returns all registered NavLocations.
	 */
	public Collection<NavLocation> getAllLocations() {
		return dataManager.getLocations().values();
	}

	/**
	 * Returns whether the graph contains a given waypoint.
	 */
	public boolean hasWaypoint(int id) {
		return adjacencyList.containsKey(id);
	}
}
