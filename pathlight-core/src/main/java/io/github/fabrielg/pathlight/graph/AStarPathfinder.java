package io.github.fabrielg.pathlight.graph;

import io.github.fabrielg.pathlight.api.Waypoint;

import java.util.*;

public class AStarPathfinder {

	public final NavigationGraph graph;

	public AStarPathfinder(NavigationGraph graph) {
		this.graph = graph;
	}

	/**
	 * Finds the shortest path from startId to goalId.
	 * Returns an ordered list of waypoint IDs from start to goal,
	 * or an empty list if no path exists.
	 */
	public List<Integer> findPath(int startId, int goalId) {
		int iterations = 0;
		final int MAX_ITERATIONS = 10_000;

		if (startId == goalId)
			return List.of(startId);

		Waypoint goal = graph.getWaypoint(goalId);
		if (goal == null || !graph.hasWaypoint(startId))
			return Collections.emptyList();

		// ── A* Data Structures ──────────────────────────────

		// Priority queue: always explore the node with the smallest f(n)
		PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));

		// Nodes already fully explored
		Set<Integer> closedSet = new HashSet<>();

		// g(n): best known distance since departure
		Map<Integer, Double> gScore = new HashMap<>();

		// To reconstruct the path: where did we come from to arrive at this node ?
		Map<Integer, Integer> cameFrom = new HashMap<>();

		// Initialisation with the starting node
		gScore.put(startId, 0.0);
		openSet.add(new Node(startId, heuristic(startId, goalId)));

		// ── Main loop ─────────────────────────────────────

		while (!openSet.isEmpty()) {

			// Takes the most promising node (smallest f)
			Node current = openSet.poll();
			int currentId = current.waypointId;

			// We have reached our destination!
			if (currentId == goalId) {
				return reconstructPath(cameFrom, currentId);
			}

			// This node is now fully explored
			closedSet.add(currentId);

			// Explore the neighbours
			for (int neighborId : graph.getNeighbors(currentId)) {

				// Already explored, we skip it
				if (closedSet.contains(neighborId)) continue;

				Waypoint currentWp  = graph.getWaypoint(currentId);
				Waypoint neighborWp = graph.getWaypoint(neighborId);
				if (neighborWp == null) continue;

				// Calculates the new g(n) using currentId
				double tentativeG = gScore.getOrDefault(currentId, Double.MAX_VALUE)
						+ currentWp.distanceTo(neighborWp);

				// If this path to the neighbour is better than what we knew
				if (tentativeG < gScore.getOrDefault(neighborId, Double.MAX_VALUE)) {
					cameFrom.put(neighborId, currentId);
					gScore.put(neighborId, tentativeG);

					double f = tentativeG + heuristic(neighborId, goalId);
					openSet.add(new Node(neighborId, f));
				}
			}
			if (++iterations > MAX_ITERATIONS) {
				return Collections.emptyList();
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Estimates the remaining distance from waypointId to the goal.
	 * Uses straight-line (euclidean) distance — always an underestimate,
	 * which guarantees A* finds the optimal path.
	 */
	private double heuristic(int waypointId, Waypoint goal) {
		Waypoint wp = graph.getWaypoint(waypointId);
		if (wp == null) return Double.MAX_VALUE;
		return wp.distanceTo(goal);
	}

	private double heuristic(int waypointId, int goalId) {
		Waypoint goal = graph.getWaypoint(goalId);
		if (goal == null) return Double.MAX_VALUE;
		return heuristic(waypointId, goal);
	}

	/**
	 * Walks back through the cameFrom map to reconstruct the full path.
	 * Returns it in order from start to goal.
	 */
	private List<Integer> reconstructPath(Map<Integer, Integer> cameFrom, int current) {
		LinkedList<Integer> path = new LinkedList<>();
		path.addFirst(current);

		while (cameFrom.containsKey(current)) {
			current = cameFrom.get(current);
			path.addFirst(current);
		}

		return new ArrayList<>(path);
	}

	/**
	 * Internal node used by the priority queue.
	 * Holds the waypoint ID and its f score.
	 */
	private static class Node {
		final int waypointId;
		final double f;

		Node(int waypointId, double f) {
			this.waypointId = waypointId;
			this.f = f;
		}
	}
}
