package io.github.fabrielg.pathlight.api.event;

import io.github.fabrielg.pathlight.api.NavLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Called when a player's path is recalculated because they went off-route.
 *
 * Example usage:
 * <pre>
 * {@code
 * @EventHandler
 * public void onPathRecalculate(PathRecalculateEvent event) {
 *     event.getPlayer().sendMessage("Recalculating...");
 * }
 * }
 * </pre>
 */
public class PathRecalculateEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final NavLocation destination;
	private final List<Integer> oldPath;
	private final List<Integer> newPath;

	public PathRecalculateEvent(Player player, NavLocation destination,
								List<Integer> oldPath, List<Integer> newPath) {
		this.player      = player;
		this.destination = destination;
		this.oldPath     = oldPath;
		this.newPath     = newPath;
	}

	public Player getPlayer()           { return player; }
	public NavLocation getDestination() { return destination; }

	/** The path before recalculation. */
	public List<Integer> getOldPath()   { return oldPath; }

	/** The newly calculated path. */
	public List<Integer> getNewPath()   { return newPath; }

	@Override
	public HandlerList getHandlers()    { return HANDLERS; }

	public static HandlerList getHandlerList() { return HANDLERS; }

}
