package io.github.fabrielg.pathlight.api;

public class Edge {

	private final int fromId;
	private final int toId;

	public Edge(int fromId, int toId) {
		this.fromId = fromId;
		this.toId = toId;
	}

	public int getFromId() {
		return fromId;
	}

	public int getToId() {
		return toId;
	}

	@Override
	public String toString() {
		return "Edge{" + fromId + " <-> " + toId + "}";
	}
}
