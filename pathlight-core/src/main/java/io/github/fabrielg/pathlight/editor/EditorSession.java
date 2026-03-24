package io.github.fabrielg.pathlight.editor;

/**
 * Holds the editing state for one admin player.
 */
public class EditorSession {

	private EditorMode mode = EditorMode.WAYPOINT;

	private Integer selectedWaypointId = null;

	public EditorMode getMode()						{ return mode; }
	public void setMode(EditorMode mode)			{ this.mode = mode; }

	public Integer getSelectedWaypointId()			{ return selectedWaypointId; }
	public void setSelectedWaypointId(Integer id)	{ this.selectedWaypointId = id; }
	public void clearSelection()					{ this.selectedWaypointId = null; }
	public boolean hasSelection()					{ return selectedWaypointId != null; }
}