package fr.aqua_tuor.pathfinder.tasks;

import fr.aqua_tuor.pathfinder.managers.PathManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import fr.aqua_tuor.pathfinder.node.Node;

public class GameCountdown extends BukkitRunnable {

    private final PathManager pathManager;

    public GameCountdown(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @Override
    public void run() {
        // Check if one player is editing mode
        if (pathManager.getPlayerManager().getPlayersEditing().size() > 0) {

            // Show nodes
            pathManager.getNodes().forEach(node -> {
                node.show(true);

                if (pathManager.getPlayerManager().getPlayersNodesSelected().values().contains(node)) node.active(true);
            });

            // Show selected block if player is editing and has the stick in hand
            pathManager.getPlayerManager().getPlayersEditing().keySet().forEach(playerName -> {
                Player player = pathManager.getPlugin().getServer().getPlayer(playerName);
                if (player != null && player.getItemInHand().getType().name().contains("STICK")) {
                    Node.showCurrent(player);
                }
            });
        } else {
            pathManager.getNodes().forEach(node -> {
                node.show(false);
            });
        }
    }
}
