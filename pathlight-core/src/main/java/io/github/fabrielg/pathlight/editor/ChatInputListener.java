package io.github.fabrielg.pathlight.editor;

import io.github.fabrielg.pathlight.PathLightPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * One-shot listener that captures the next chat message from a specific player.
 * Automatically unregisters itself after receiving the input.
 */
public class ChatInputListener implements Listener {

	private final UUID targetUUID;
	private final Consumer<String> callback;

	public ChatInputListener(org.bukkit.plugin.Plugin plugin, Player player, Consumer<String> callback) {
		this.targetUUID = player.getUniqueId();
		this.callback   = callback;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().getUniqueId().equals(targetUUID)) return;
		if (!PathLightPlugin.getInstance().getNavTool().isPrompting(event.getPlayer())) return ;

		event.setCancelled(true);

		String input = event.getMessage().trim();

		event.getPlayer().getServer().getScheduler().runTask(
				event.getPlayer().getServer().getPluginManager().getPlugin("PathLight"),
				() -> callback.accept(input)
		);

		HandlerList.unregisterAll(this);
	}
}