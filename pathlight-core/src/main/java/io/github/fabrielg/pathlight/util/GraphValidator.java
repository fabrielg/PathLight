package io.github.fabrielg.pathlight.util;

import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.Waypoint;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Validates the integrity of the navigation graph after loading.
 * Detects and removes orphaned edges and invalid locations.
 * Logs warnings for every issue found.
 */
public class GraphValidator {

	private final DataManager dataManager;
	private final Logger logger;

	public GraphValidator(DataManager dataManager, Logger logger) {
		this.dataManager = dataManager;
		this.logger = logger;
	}

	/**
	 * Runs all validation checks and auto-fixes what can be fixed.
	 * Returns a list of all issues found (even the ones auto-fixed).
	 */
	public List<ValidationResult> validate() {
		List<ValidationResult> issues = new ArrayList<>();

		issues.addAll(validateEdges());
		issues.addAll(validateLocations());
		issues.addAll(validateWaypoints());

		if (issues.isEmpty()) {
			logger.info("Graph validation passed with no issues.");
		} else {
			logger.warning("Graph validation found " + issues.size() + " issue(s) :");
			for (ValidationResult issue : issues) {
				logger.warning("  → " + issue.getErrorMessage());
			}
		}

		return issues;
	}

	/**
	 * Removes edges that reference non-existent waypoints.
	 */
	private List<ValidationResult> validateEdges() {
		List<ValidationResult> issues = new ArrayList<>();
		List<Edge> toRemove = new ArrayList<>();

		for (Edge edge : dataManager.getEdges()) {
			boolean fromExists = dataManager.getWaypoints().containsKey(edge.getFromId());
			boolean toExists   = dataManager.getWaypoints().containsKey(edge.getToId());

			if (!fromExists || !toExists) {
				String msg = "Orphaned edge (" + edge.getFromId() + " → " + edge.getToId() + ")"
						+ " references missing waypoint(s)."
						+ (!fromExists ? " Missing: #" + edge.getFromId() : "")
						+ (!toExists   ? " Missing: #" + edge.getToId()   : "")
						+ " → auto-removed.";

				issues.add(ValidationResult.error(msg));
				toRemove.add(edge);
			}
		}

		dataManager.getEdges().removeAll(toRemove);
		return issues;
	}

	/**
	 * Removes locations whose anchor waypoint no longer exists.
	 */
	private List<ValidationResult> validateLocations() {
		List<ValidationResult> issues = new ArrayList<>();
		List<Integer> toRemove = new ArrayList<>();

		for (NavLocation location : dataManager.getLocations().values()) {
			if (!dataManager.getWaypoints().containsKey(location.getAnchorWaypointId())) {
				String msg = "Location \"" + location.getName() + "\" (id=" + location.getId() + ")"
						+ " is anchored on missing waypoint #" + location.getAnchorWaypointId()
						+ " → auto-removed.";

				issues.add(ValidationResult.error(msg));
				toRemove.add(location.getId());
			}
		}

		toRemove.forEach(dataManager::removeLocation);
		return issues;
	}

	/**
	 * Warns about waypoints with no edges (isolated nodes).
	 * These are not removed since they might be intentional,
	 * but they will never be reachable by pathfinding.
	 */
	private List<ValidationResult> validateWaypoints() {
		List<ValidationResult> issues = new ArrayList<>();

		for (Waypoint wp : dataManager.getWaypoints().values()) {
			boolean hasEdge = dataManager.getEdges().stream().anyMatch(e ->
					e.getFromId() == wp.getId() || e.getToId() == wp.getId()
			);

			if (!hasEdge) {
				issues.add(ValidationResult.error(
						"Waypoint #" + wp.getId() + " at (" +
								String.format("%.1f", wp.getX()) + ", " +
								String.format("%.1f", wp.getY()) + ", " +
								String.format("%.1f", wp.getZ()) + ")" +
								" has no edges and will never be reachable."
				));
			}
		}

		return issues;
	}

}
