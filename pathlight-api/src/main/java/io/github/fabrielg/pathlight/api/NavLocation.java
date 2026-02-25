package io.github.fabrielg.pathlight.api;

public class NavLocation {

	private final int id;
	private final String name;
	private final int anchorWaypointId;

	public NavLocation(int id, String name, int anchorWaypointId) {
		this.id = id;
		this.name = name;
		this.anchorWaypointId = anchorWaypointId;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getAnchorWaypointId() {
		return anchorWaypointId;
	}

	@Override
	public String toString() {
		return "NavLocation{id=" + id + ", name=" + name + ", anchor=" + anchorWaypointId + "}";
	}

}
