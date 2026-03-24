package io.github.fabrielg.pathlight.editor;

/**
 * Represents the current editing mode of the navigation tool.
 */
public enum EditorMode {

	WAYPOINT("§6Waypoint Mode", "§7Left click a block to place a waypoint. Right click a waypoint to remove it."),
	EDGE    ("§bEdge Mode",     "§7Left click two waypoints to connect them. Right click to remove all edges of a waypoint."),
	LOCATION("§dLocation Mode", "§7Left click a waypoint to set a destination. Right click to remove it.");

	private final String displayName;
	private final String hint;

	EditorMode(String displayName, String hint) {
		this.displayName = displayName;
		this.hint        = hint;
	}

	public String getDisplayName() { return displayName; }
	public String getHint()        { return hint; }

	/**
	 * Returns the next mode in the cycle.
	 */
	public EditorMode next() {
		EditorMode[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}

}
