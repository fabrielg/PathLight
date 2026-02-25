package io.github.fabrielg.pathlight;

import io.github.fabrielg.pathlight.data.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PathLightPlugin extends JavaPlugin {

	private static PathLightPlugin instance;
	private DataManager dataManager;

	@Override
	public void onEnable()
	{
		instance = this;

		getLogger().info("╔═══════════════════════════╗");
		getLogger().info("║   PathLight is starting   ║");
		getLogger().info("╚═══════════════════════════╝");

		this.dataManager = new DataManager(this);
		dataManager.load();

		getLogger().info("PathLight enabled successfully.");
	}

	@Override
	public void onDisable()
	{
		if (dataManager != null)
			dataManager.save();
		getLogger().info("PathLight disabled.");
	}

	public static PathLightPlugin getInstance()
	{
		return instance;
	}

	public DataManager getDataManager()
	{
		return dataManager;
	}

}
