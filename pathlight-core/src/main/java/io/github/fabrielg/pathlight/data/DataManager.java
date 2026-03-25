package io.github.fabrielg.pathlight.data;

import com.google.gson.*;
import io.github.fabrielg.pathlight.PathLightPlugin;
import io.github.fabrielg.pathlight.api.Edge;
import io.github.fabrielg.pathlight.api.NavLocation;
import io.github.fabrielg.pathlight.api.Waypoint;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

	private final PathLightPlugin plugin;
	private final Gson gson;
	private final File dataFile;
	private final String FILE_NAME = "graph.json";

	private Map<Integer, Waypoint> waypoints = new HashMap<>();
	private Map<Integer, NavLocation> locations = new HashMap<>();
	private List<Edge> edges = new ArrayList<>();

	public DataManager(PathLightPlugin plugin) {
		this.plugin = plugin;
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.dataFile = new File(plugin.getDataFolder(), FILE_NAME);
	}

	/**
	 * Loads the graph data from graph.json.
	 * If the file doesn't exist, creates an empty one.
	 */
	public void load() {
		if (!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdirs();

		if (!dataFile.exists()) {
			plugin.getLogger().info("No " + FILE_NAME + " found, creating empty file.");
			saveEmptyFile();
			return;
		}

		if (dataFile.length() == 0) {
			plugin.getLogger().warning(FILE_NAME + " is empty, resetting to default.");
			saveEmptyFile();
			return;
		}

		try (Reader reader = new FileReader(dataFile)) {
			JsonElement parsed = JsonParser.parseReader(reader);

			if (!parsed.isJsonObject()) {
				plugin.getLogger().severe(FILE_NAME + " is not a valid JSON object. "
						+ "PathLight will start with an empty graph. "
						+ "Your file has been backed up as " + FILE_NAME + ".bak");
				backupCorruptedFile();
				saveEmptyFile();
				return;
			}

			JsonObject root = parsed.getAsJsonObject();

			waypoints.clear();
			edges.clear();
			locations.clear();

			loadWaypoints(root);
			loadEdges(root);
			loadLocations(root);

			plugin.getLogger().info("Graph loaded: "
					+ waypoints.size() + " waypoints, "
					+ edges.size() + " edges, "
					+ locations.size() + " locations.");

		} catch (Exception e) {
			plugin.getLogger().severe("Failed to load " + FILE_NAME + ": " + e.getMessage()
					+ ". Starting with empty graph. Backup saved as " + FILE_NAME + ".bak");
			backupCorruptedFile();
			saveEmptyFile();
		}
	}

	private void loadWaypoints(JsonObject root) {
		if (!root.has("waypoints") || !root.get("waypoints").isJsonArray()) return;

		for (JsonElement el : root.getAsJsonArray("waypoints")) {
			try {
				JsonObject obj = el.getAsJsonObject();

				if (!obj.has("id") || !obj.has("world")
						|| !obj.has("x") || !obj.has("y") || !obj.has("z")) {
					plugin.getLogger().warning("Skipping malformed waypoint entry: " + obj);
					continue;
				}

				int id       = obj.get("id").getAsInt();
				String world = obj.get("world").getAsString();
				double x     = obj.get("x").getAsDouble();
				double y     = obj.get("y").getAsDouble();
				double z     = obj.get("z").getAsDouble();

				if (waypoints.containsKey(id)) {
					plugin.getLogger().warning("Duplicate waypoint ID #" + id + " skipped.");
					continue;
				}

				waypoints.put(id, new Waypoint(id, world, x, y, z));

			} catch (Exception e) {
				plugin.getLogger().warning("Skipping invalid waypoint entry: " + e.getMessage());
			}
		}
	}

	private void loadEdges(JsonObject root) {
		if (!root.has("edges") || !root.get("edges").isJsonArray()) return;

		for (JsonElement el : root.getAsJsonArray("edges")) {
			try {
				JsonObject obj = el.getAsJsonObject();

				if (!obj.has("from") || !obj.has("to")) {
					plugin.getLogger().warning("Skipping malformed edge entry: " + obj);
					continue;
				}

				edges.add(new Edge(obj.get("from").getAsInt(), obj.get("to").getAsInt()));

			} catch (Exception e) {
				plugin.getLogger().warning("Skipping invalid edge entry: " + e.getMessage());
			}
		}
	}

	private void loadLocations(JsonObject root) {
		if (!root.has("locations") || !root.get("locations").isJsonArray()) return;

		for (JsonElement el : root.getAsJsonArray("locations")) {
			try {
				JsonObject obj = el.getAsJsonObject();

				if (!obj.has("id") || !obj.has("name") || !obj.has("anchor")) {
					plugin.getLogger().warning("Skipping malformed location entry: " + obj);
					continue;
				}

				int id      = obj.get("id").getAsInt();
				String name = obj.get("name").getAsString().trim();
				int anchor  = obj.get("anchor").getAsInt();

				if (name.isEmpty()) {
					plugin.getLogger().warning("Skipping location with empty name (id=" + id + ")");
					continue;
				}

				if (locations.containsKey(id)) {
					plugin.getLogger().warning("Duplicate location ID #" + id + " skipped.");
					continue;
				}

				locations.put(id, new NavLocation(id, name, anchor));

			} catch (Exception e) {
				plugin.getLogger().warning("Skipping invalid location entry: " + e.getMessage());
			}
		}
	}

	/**
	 * Backs up a corrupted graph.json before overwriting it.
	 */
	private void backupCorruptedFile() {
		File backup = new File(plugin.getDataFolder(), FILE_NAME + ".bak");
		if (dataFile.exists()) {
			dataFile.renameTo(backup);
		}
	}

	/**
	 * Saves the current in-memory graph to graph.json.
	 */
	public void save() {
		JsonObject root = new JsonObject();

		JsonArray waypointArr = new JsonArray();
		for (Waypoint w : waypoints.values()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id",    w.getId());
			obj.addProperty("world", w.getWorld());
			obj.addProperty("x",     w.getX());
			obj.addProperty("y",     w.getY());
			obj.addProperty("z",     w.getZ());
			waypointArr.add(obj);
		}
		root.add("waypoints", waypointArr);

		JsonArray edgeArr = new JsonArray();
		for (Edge e : edges) {
			JsonObject obj = new JsonObject();
			obj.addProperty("from", e.getFromId());
			obj.addProperty("to",   e.getToId());
			edgeArr.add(obj);
		}
		root.add("edges", edgeArr);

		JsonArray locationArr = new JsonArray();
		for (NavLocation l : locations.values()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id",     l.getId());
			obj.addProperty("name",   l.getName());
			obj.addProperty("anchor", l.getAnchorWaypointId());
			locationArr.add(obj);
		}
		root.add("locations", locationArr);

		try (Writer writer = new FileWriter(dataFile)) {
			gson.toJson(root, writer);
		} catch (IOException e) {
			plugin.getLogger().severe("Failed to save " + FILE_NAME + ": " + e.getMessage());
		}
	}

	private void saveEmptyFile() {
		JsonObject root = new JsonObject();
		root.add("waypoints", new JsonArray());
		root.add("edges",     new JsonArray());
		root.add("locations", new JsonArray());

		try (Writer writer = new FileWriter(dataFile)) {
			gson.toJson(root, writer);
		} catch (IOException e) {
			plugin.getLogger().severe("Failed to create empty " + FILE_NAME + ": " + e.getMessage());
		}
	}

	/**
	 * Generates a new unique waypoint ID (next available integer).
	 */
	public int nextWaypointId() {
		return waypoints.keySet().stream().mapToInt(i -> i).max().orElse(0) + 1;
	}

	/**
	 * Generates a new unique location ID.
	 */
	public int nextLocationId() {
		return locations.keySet().stream().mapToInt(i -> i).max().orElse(0) + 1;
	}

	public String getFileName() { return FILE_NAME; }

	public Map<Integer, Waypoint>    getWaypoints() { return waypoints; }
	public Map<Integer, NavLocation> getLocations() { return locations; }
	public List<Edge>                getEdges()     { return edges; }

	public void addWaypoint(Waypoint w)    { waypoints.put(w.getId(), w); }
	public void removeWaypoint(int id)     { waypoints.remove(id); }

	public void addLocation(NavLocation l) { locations.put(l.getId(), l); }
	public void removeLocation(int id)     { locations.remove(id); }

	public void addEdge(Edge e)            { edges.add(e); }
	public void removeEdge(int from, int to) {
		edges.removeIf(e ->
				(e.getFromId() == from && e.getToId() == to) ||
						(e.getFromId() == to   && e.getToId() == from)
		);
	}
}
