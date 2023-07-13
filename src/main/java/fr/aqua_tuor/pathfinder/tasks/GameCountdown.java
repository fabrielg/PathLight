package fr.aqua_tuor.pathfinder.tasks;

import fr.aqua_tuor.pathfinder.managers.PathManager;
import fr.aqua_tuor.pathfinder.node.NodeType;
import fr.aqua_tuor.pathfinder.path.Path;
import fr.aqua_tuor.pathfinder.path.PathType;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.block.Block;
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

            });

            // Show selected block if player is editing and has the stick in hand
            pathManager.getPlayerManager().getPlayersEditing().keySet().forEach(playerName -> {
                Player player = pathManager.getPlugin().getServer().getPlayer(playerName);
                if (player != null && player.getItemInHand().getType().name().contains("STICK")) {

                    if (pathManager.getPlayerManager().getPlayersNodesSelected().get(playerName) != null) {

                        Block block = player.getTargetBlock(null, 5);
                        if (block == null || block.getType().name().contains("AIR")) return;

                        double xEnd = block.getX() + 0.5;
                        double yEnd = block.getY() + 1.5;
                        double zEnd = block.getZ() + 0.5;
                        World worldEnd = block.getWorld();

                        Node startNode = pathManager.getPlayerManager().getPlayersNodesSelected().get(playerName);
                        Node endNode;
                        if (pathManager.getNodeByCoords(xEnd, yEnd, zEnd, worldEnd) != null)
                            endNode = pathManager.getNodeByCoords(xEnd, yEnd, zEnd, worldEnd);
                        else
                            endNode = new Node(pathManager.getLastNodeId() + 1, xEnd, yEnd, zEnd, worldEnd);

                        Path path;
                        if (pathManager.getPlayerManager().getPlayersPathsSelected().get(playerName) == null)
                            path = new Path(pathManager.getLastPathId() + 1, startNode, endNode, PathType.LINE, Color.WHITE);
                        else {
                            path = pathManager.getPlayerManager().getPlayersPathsSelected().get(playerName);
                            path.setEnd(endNode);
                        }

                        // Check if the end location is a node
                        if (pathManager.getNodeByCoords(xEnd, yEnd, zEnd, worldEnd) != null) {
                            // Set YELLOW color to path
                            path.setColor(Color.YELLOW);

                            // Set SELECTED type to the end node
                            endNode.setType(NodeType.SELECTED);
                        } else {
                            // Set WHITE color to path
                            path.setColor(Color.WHITE);

                            // Set NORMAL type to the end node
                            endNode.setType(NodeType.NORMAL);
                        }

                        pathManager.getPlayerManager().getPlayersPathsSelected().put(playerName, path);
                        path.drawPath();
                    } else {
                        Node.showCurrent(player);
                    }
                }
            });


        } else {
            pathManager.getNodes().forEach(node -> {
                node.show(false);
            });
        }
    }
}
