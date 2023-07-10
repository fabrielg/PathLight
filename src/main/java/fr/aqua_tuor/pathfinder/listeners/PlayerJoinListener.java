package fr.aqua_tuor.pathfinder.listeners;

import fr.aqua_tuor.pathfinder.managers.PathManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private PathManager pathManager;

    public PlayerJoinListener(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pathManager.getPlayerManager().removePlayer(event.getPlayer());
    }

}
