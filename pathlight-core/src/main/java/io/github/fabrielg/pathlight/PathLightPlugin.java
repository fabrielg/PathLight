package io.github.fabrielg.pathlight;

import io.github.fabrielg.pathlight.api.PathLightAPIProvider;
import io.github.fabrielg.pathlight.commands.NavToolCommand;
import io.github.fabrielg.pathlight.commands.PathCommand;
import io.github.fabrielg.pathlight.commands.PathLightCommand;
import io.github.fabrielg.pathlight.config.PluginConfig;
import io.github.fabrielg.pathlight.data.DataManager;
import io.github.fabrielg.pathlight.editor.NavTool;
import io.github.fabrielg.pathlight.graph.AStarPathfinder;
import io.github.fabrielg.pathlight.graph.NavigationGraph;
import io.github.fabrielg.pathlight.rendering.TrailManager;
import io.github.fabrielg.pathlight.util.GraphValidator;
import io.github.fabrielg.pathlight.util.ValidationResult;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PathLightPlugin extends JavaPlugin {

	private static PathLightPlugin instance;
	private PluginConfig config;
	private DataManager dataManager;
	private NavigationGraph navigationGraph;
	private AStarPathfinder pathfinder;
	private TrailManager trailManager;
	private NavTool navTool;

	@Override
	public void onEnable()
	{
		instance = this;

		getLogger().info("╔═══════════════════════════╗");
		getLogger().info("║   PathLight is starting   ║");
		getLogger().info("╚═══════════════════════════╝");

		this.config = new PluginConfig(this);

		this.dataManager = new DataManager(this);
		dataManager.load();

		GraphValidator validator = new GraphValidator(dataManager, getLogger());
		List<ValidationResult> issues = validator.validate();

		if (!issues.isEmpty()) {
			dataManager.save();
			getLogger().info(dataManager.getFileName() + " has been cleaned and saved.");
		}

		this.navigationGraph = new NavigationGraph(dataManager);
		navigationGraph.build();

		this.pathfinder = new AStarPathfinder(navigationGraph);

		this.trailManager = new TrailManager(this, navigationGraph);
		getServer().getPluginManager().registerEvents(trailManager, this);

		this.navTool = new NavTool(this);
		getServer().getPluginManager().registerEvents(navTool, this);

		PathCommand pathCommand = new PathCommand(this);
		getCommand("path").setExecutor(pathCommand);
		getCommand("path").setTabCompleter(pathCommand);

		PathLightCommand pathLightCommand = new PathLightCommand(this);
		getCommand("pathlight").setExecutor(pathLightCommand);
		getCommand("pathlight").setTabCompleter(pathLightCommand);

		getCommand("pathtool").setExecutor(new NavToolCommand(this));

		PathLightAPIProvider.register(new PathLightAPIImpl(this));

		getLogger().info("PathLight enabled successfully.");
	}

	@Override
	public void onDisable()
	{
		if (dataManager != null)
			dataManager.save();

		PathLightAPIProvider.unregister();

		getLogger().info("PathLight disabled.");
	}

	public static PathLightPlugin getInstance()	{ return instance; }
	public PluginConfig getPluginConfig()		{ return config; }
	public DataManager getDataManager()			{ return dataManager; }
	public NavigationGraph getNavigationGraph()	{ return navigationGraph; }
	public AStarPathfinder getPathfinder()		{ return pathfinder; }
	public TrailManager getTrailManager()		{ return trailManager; }
	public NavTool getNavTool()					{ return navTool; }

}
