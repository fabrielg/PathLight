package io.github.fabrielg.pathlight;

import org.bukkit.plugin.java.JavaPlugin;

public class PathLightPlugin extends JavaPlugin {

	private static PathLightPlugin instance;

	@Override
	public void onEnable()
	{
		instance = this;

		getLogger().info("╔═══════════════════════════╗");
		getLogger().info("║   PathLight is starting   ║");
		getLogger().info("╚═══════════════════════════╝");

		getLogger().info("PathLight enabled successfully.");
	}

	@Override
	public void onDisable()
	{
		getLogger().info("PathLight disabled.");
	}

	public static PathLightPlugin getInstance()
	{
		return instance;
	}

}
