package io.github.fabrielg.pathlight.editor;

/**
 * Holds the editing state for one admin player.
 */
public class EditorSession {

	private EditorMode mode = EditorMode.WAYPOINT;
	private Integer lastWaypointId = null;
	private boolean autoEdgeEnabled = true;

	public EditorMode getMode()						{ return mode; }
	public void setMode(EditorMode mode) {
		if (mode == EditorMode.WAYPOINT)
		{
			lastWaypointId = null;
			autoEdgeEnabled = true;
		}
		this.mode = mode;
	}

	public Integer getLastWaypointId()            { return lastWaypointId; }
	public void setLastWaypointId(Integer id)     { this.lastWaypointId = id; }
	public void clearLastWaypoint()               { this.lastWaypointId = null; }
	public boolean hasLastWaypoint()              { return lastWaypointId != null; }

	public boolean isAutoEdgeEnabled()            { return autoEdgeEnabled; }
	public void toggleAutoEdge()                  { this.autoEdgeEnabled = !autoEdgeEnabled; }
}