package io.github.fabrielg.pathlight.api.event;

import io.github.fabrielg.pathlight.api.NavLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player's navigation ends.
 * This happens either when the player reaches the destination,
 * manually cancels with /path cancel, or disconnects.
 *
 * Example usage:
 * <pre>
 * {@code
 * @EventHandler
 * public void onPathEnd(PathEndEvent event) {
 *     if (event.getReason() == PathEndEvent.Reason.REACHED) {
 *         event.getPlayer().sendMessage("Well done, you arrived!");
 *     }
 * }
 * }
 * </pre>
 */
public class PathEndEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	/**
	 * The reason why the navigation ended.
	 */
	public enum Reason {
		/** Player reached the destination. */
		REACHED,
		/** Player manually cancelled with /path cancel. */
		CANCELLED,
		/** Player disconnected while navigating. */
		DISCONNECTED
	}

	private final Player player;
	private final NavLocation destination;
	private final Reason reason;

	public PathEndEvent(Player player, NavLocation destination, Reason reason) {
		this.player = player;
		this.destination = destination;
		this.reason = reason;
	}

	public Player getPlayer()           { return player; }
	public NavLocation getDestination() { return destination; }
	public Reason getReason()           { return reason; }

	@Override
	public HandlerList getHandlers()    { return HANDLERS; }

	public static HandlerList getHandlerList() { return HANDLERS; }

}
