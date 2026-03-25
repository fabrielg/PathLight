package io.github.fabrielg.pathlight.api.event;

import io.github.fabrielg.pathlight.api.NavLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Called when a player starts navigating toward a destination.
 * This event is Cancellable : if cancelled, the navigation will not start.
 *
 * Example usage:
 * <pre>
 * {@code
 * @EventHandler
 * public void onPathStart(PathStartEvent event) {
 *     event.getPlayer().sendTitle("Navigating!", "Follow the trail", 10, 60, 10);
 * }
 * }
 * </pre>
 */
public class PathStartEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final NavLocation destination;
	private final List<Integer> path;
	private boolean cancelled = false;

	public PathStartEvent(Player player, NavLocation destination, List<Integer> path)
	{
		this.player = player;
		this.destination = destination;
		this.path = path;
	}

	/** The player who started navigation. */
	public Player getPlayer()			{ return player; }

	/** The destination the player is navigating to. */
	public NavLocation getDestination()	{ return destination; }

	/** The calculated path as an ordered list of waypoint IDs. */
	public List<Integer> getPath()		{ return path; }

	@Override
	public boolean isCancelled()		{ return cancelled; }

	@Override
	public void setCancelled(boolean b)	{ this.cancelled = b; }

	@Override
	public HandlerList getHandlers()	{ return HANDLERS; }

	public static HandlerList getHandlerList() { return HANDLERS; }

}
