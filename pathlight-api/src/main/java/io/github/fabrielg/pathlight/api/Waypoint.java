package io.github.fabrielg.pathlight.api;

public class Waypoint {

	private final int id;
	private final String world;
	private final double x;
	private final double y;
	private final double z;

	public Waypoint(int id, String world, double x, double y, double z) {
		this.id = id;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getId() {
		return id;
	}

	public String getWorld() {
		return world;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double distanceTo(Waypoint other) {
		double dx = this.x - other.x;
		double dy = this.y - other.y;
		double dz = this.z - other.z;

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public String toString() {
		return "Waypoint{id=" + id + ", world=" + world + ", x=" + x + ", y=" + y + ", z=" + z + "}";
	}
}
