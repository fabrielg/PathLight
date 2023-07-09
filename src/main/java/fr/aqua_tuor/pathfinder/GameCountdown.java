package fr.aqua_tuor.pathfinder;

import org.bukkit.scheduler.BukkitRunnable;

public class GameCountdown extends BukkitRunnable {

    private final PathManager pathManager;

    public GameCountdown(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @Override
    public void run() {
        // Check if one player is editing mode
        if (pathManager.getPlayerManager().getPlayersEditing().size() > 0) {
            pathManager.getNodes().forEach(node -> {
                node.show(true);
            });
        } else {
            pathManager.getNodes().forEach(node -> {
                node.show(false);
            });
        }
    }
}
