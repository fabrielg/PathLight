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
			sender.sendMessage("§cThis command can only be used by a player.");
			return true;
		}

		if (!player.hasPermission("pathlight.admin")) {
			player.sendMessage("§cYou don't have permission to use this command.");
			return true;
		}

		player.getInventory().addItem(plugin.getNavTool().createToolItem());
		player.sendMessage("§aNavigation Editor tool given!");
		player.sendMessage("§7Right click in air to cycle modes.");
		return true;
	}
}