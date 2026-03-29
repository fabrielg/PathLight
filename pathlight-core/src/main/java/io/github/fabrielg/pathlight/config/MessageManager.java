package io.github.fabrielg.pathlight.config;

import io.github.fabrielg.pathlight.PathLightPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manages all plugin messages loaded from messages.yml.
 * Supports placeholders, prefix, and toggleable admin logs.
 */
public class MessageManager {

	private final PathLightPlugin plugin;
	private FileConfiguration messages;
	private String prefix;

	public MessageManager(PathLightPlugin plugin) {
		this.plugin = plugin;
		load();
	}

	public void load() {
		File file = new File(plugin.getDataFolder(), "messages.yml");

		if (!file.exists()) {
			plugin.saveResource("messages.yml", false);
		}

		messages = YamlConfiguration.loadConfiguration(file);

		InputStream defaultStream = plugin.getResource("messages.yml");
		if (defaultStream != null) {
			YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
					new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
			);
			messages.setDefaults(defaults);
		}

		prefix = messages.getString("prefix", "§6✦ [PathLight]§r ");
		plugin.getLogger().info("Messages loaded successfully.");
	}

	/**
	 * Sends a message to a player.
	 * If the message is empty or null, nothing is sent.
	 *
	 * @param player the recipient
	 * @param path   the message path in messages.yml (ex: "navigation.started")
	 * @param replacements pairs of placeholder/value (ex: "destination", "The Castle")
	 */
	public void send(Player player, String path, String... replacements) {
		String message = get(path, replacements);
		if (!message.isEmpty()) {
			player.sendMessage(message);
		}
	}

	/**
	 * Returns a formatted message string.
	 * Returns empty string if the message is disabled (set to "").
	 */
	public String get(String path, String... replacements) {
		String message = messages.getString(path, "");
		if (message == null || message.isEmpty()) return "";

		message = message.replace("{prefix}", prefix);

		for (int i = 0; i < replacements.length - 1; i += 2) {
			message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
		}

		return message;
	}

	/**
	 * Logs an admin message to the console if enabled in messages.yml.
	 *
	 * @param logPath    the log path (ex: "logs.navigation-start")
	 * @param replacements pairs of placeholder/value
	 */
	public void log(String logPath, String... replacements) {
		boolean enabled = messages.getBoolean(logPath + ".enabled", false);
		if (!enabled) return;

		String message = messages.getString(logPath + ".message", "");
		if (message == null || message.isEmpty()) return;

		for (int i = 0; i < replacements.length - 1; i += 2) {
			message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
		}

		plugin.getLogger().info(message);
	}

	public String getEdgeOnOff(boolean b) {
		String message = messages.getString(b ? "editor.auto-edge-on" : "editor.auto-edge-off", "");
		if (message == null || message.isEmpty()) return "";

		message = message.replace("{prefix}", "");

		return message;
	}

	public String getPrefix() { return prefix; }
}
