package io.github.fabrielg.pathlight;

import io.github.fabrielg.pathlight.data.DataManager;
import io.github.fabrielg.pathlight.graph.AStarPathfinder;
import io.github.fabrielg.pathlight.graph.NavigationGraph;
import io.github.fabrielg.pathlight.rendering.TrailManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PathLightPlugin extends JavaPlugin {

	private static PathLightPlugin instance;
	private DataManager dataManager;
	private NavigationGraph navigationGraph;
	private AStarPathfinder pathfinder;
	private TrailManager trailManager;

	@Override
	public void onEnable()
	{
		instance = this;

		getLogger().info("╔═══════════════════════════╗");
		getLogger().info("║   PathLight is starting   ║");
		getLogger().info("╚═══════════════════════════╝");

		this.dataManager = new DataManager(this);
		dataManager.load();

		this.navigationGraph = new NavigationGraph(dataManager);
		navigationGraph.build();

		this.pathfinder = new AStarPathfinder(navigationGraph);

		this.trailManager = new TrailManager(this, navigationGraph);

		getLogger().info("PathLight enabled successfully.");
	}

	@Override
	public void onDisable()
	{
		if (dataManager != null)
			dataManager.save();
		getLogger().info("PathLight disabled.");
	}

	public static PathLightPlugin getInstance()	{ return instance; }
	public DataManager getDataManager()			{ return dataManager; }
	public NavigationGraph getNavigationGraph()	{ return navigationGraph; }
	public AStarPathfinder getPathfinder()		{ return pathfinder; }
	public TrailManager getTrailManager()		{ return trailManager; }

}
