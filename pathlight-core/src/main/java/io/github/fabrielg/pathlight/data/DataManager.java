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
		
		try (Reader reader = new FileReader(dataFile)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

			waypoints.clear();
			if (root.has("waypoints")) {
				JsonArray arr = root.getAsJsonArray("waypoints");
				for (JsonElement el : arr) {
					JsonObject obj = el.getAsJsonObject();
					int id			= obj.get("id").getAsInt();
					String world	= obj.get("world").getAsString();
					double x		= obj.get("x").getAsDouble();
					double y		= obj.get("y").getAsDouble();
					double z		= obj.get("z").getAsDouble();
					waypoints.put(id, new Waypoint(id, world, x, y, z));
				}
			}

			edges.clear();
			if (root.has("edges")) {
				JsonArray arr = root.getAsJsonArray("edges");
				for (JsonElement el : arr) {
					JsonObject obj = el.getAsJsonObject();
					int from = obj.get("from").getAsInt();
					int to   = obj.get("to").getAsInt();
					edges.add(new Edge(from, to));
				}
			}

			locations.clear();
			if (root.has("locations")) {
				JsonArray arr = root.getAsJsonArray("locations");
				for (JsonElement el : arr) {
					JsonObject obj = el.getAsJsonObject();
					int id       = obj.get("id").getAsInt();
					String name  = obj.get("name").getAsString();
					int anchor   = obj.get("anchor").getAsInt();
					locations.put(id, new NavLocation(id, name, anchor));
				}
			}

			plugin.getLogger().info("Graph loaded: "
					+ waypoints.size() + " waypoints, "
					+ edges.size() + " edges, "
					+ locations.size() + " locations.");
		} catch (IOException e) {
			plugin.getLogger().severe("Failed to load " + FILE_NAME + ": " + e.getMessage());
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
