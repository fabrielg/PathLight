package io.github.fabrielg.pathlight.commands;

import io.github.fabrielg.pathlight.PathLightPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Gives the NavTool item to the admin who runs /pathtool.
 */
public class NavToolCommand implements CommandExecutor {

	private final PathLightPlugin plugin;

	public NavToolCommand(PathLightPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			String playerOnly = plugin.getMessageManager().get("commands.player-only");
			if (!playerOnly.isEmpty())
				sender.sendMessage(playerOnly);
			return true;
		}

		if (!player.hasPermission("pathlight.admin")) {
			plugin.getMessageManager().send(player, "commands.no-permission");
			return true;
		}

		player.getInventory().addItem(plugin.getNavTool().createToolItem());
		return true;
	}
}