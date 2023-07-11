package fr.aqua_tuor.pathfinder.listeners;

import fr.aqua_tuor.pathfinder.managers.PathManager;
import fr.aqua_tuor.pathfinder.node.Node;
import fr.aqua_tuor.pathfinder.node.NodeType;
import fr.aqua_tuor.pathfinder.path.Path;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerClickWithStick implements Listener {

    private PathManager pathManager;

    public PlayerClickWithStick(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @EventHandler
    public void onPlayerClickWithStick(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (pathManager.getPlayerManager().isEditing(player)) {
            if (player.getInventory().getItemInMainHand().getType().name().contains("STICK")) {

                // Place a new node
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

                    EquipmentSlot e = event.getHand();
                    if (!e.equals(EquipmentSlot.OFF_HAND)) return;

                    event.setCancelled(true);
                    Block block = event.getClickedBlock();
                    // Get the middle of the block
                    double x = block.getX() + 0.5;
                    double y = block.getY() + 1.5;
                    double z = block.getZ() + 0.5;

                    // Check if the node already exists
                    if (pathManager.getNodeByCoords(x, y, z) != null) {
                        Node node = pathManager.getNodeByCoords(x, y, z);

                        // Check if the node is already selected
                        if (pathManager.getPlayerManager().getPlayersNodesSelected().containsKey(player.getName())) {
                            Node selectedNode = pathManager.getPlayerManager().getPlayersNodesSelected().get(player.getName());
                            if (selectedNode.getId() == node.getId()) {
                                // Remove the node from the selected nodes
                                pathManager.getPlayerManager().getPlayersNodesSelected().remove(player.getName());

                                // Remove the path from the selected paths
                                pathManager.getPlayerManager().getPlayersPathsSelected().remove(player.getName());

                                node.setType(NodeType.NORMAL);
                                String message = "§aThe node has been unselected";
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                            } else {
                                // So the node selected is the second node
                                pathManager.getPlayerManager().getPlayersNodesSelected().remove(player.getName());
                                selectedNode.setType(NodeType.NORMAL);
                                node.setType(NodeType.NORMAL);

                                // Get the path
                                Path path = pathManager.getPlayerManager().getPlayersPathsSelected().get(player.getName());
                                pathManager.getPaths().add(path);

                                String message = "§aThe path has been created";
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                            }
                        } else {
                            // Add the node to the selected nodes
                            pathManager.getPlayerManager().getPlayersNodesSelected().put(player.getName(), node);
                            node.setType(NodeType.SELECTED);
                            String message = "§aThe node has been selected";
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                        }

                        return;
                    }

                    // Add the node
                    int lastId = pathManager.getLastNodeId();

                    Node node = new Node(lastId + 1, x, y, z, player.getWorld());
                    pathManager.addNode(node);

                    String message = "§aA node has been added";
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    event.setCancelled(true);

                    // Check if the player has selected a node before
                    if (pathManager.getPlayerManager().getPlayersNodesSelected().containsKey(player.getName())) {
                        String message = "§cCancelling the addition of a path";
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));

                        // Remove the node from the selected nodes
                        Node node = pathManager.getPlayerManager().getPlayersNodesSelected().get(player.getName());
                        pathManager.getPlayerManager().getPlayersNodesSelected().remove(player.getName());
                        node.setType(NodeType.NORMAL);
                        return;
                    }

                    Block block = event.getClickedBlock();
                    // Get the middle of the block
                    double x = block.getX() + 0.5;
                    double y = block.getY() + 1.5;
                    double z = block.getZ() + 0.5;

                    // Check if the node exists
                    Node node = pathManager.getNodeByCoords(x, y, z);
                    if (node == null) {
                        String message = "§cThere is no node on this block";
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                        return;
                    }

                    // Remove the node
                    pathManager.removeNode(node.getId());

                    String message = "§aA node has been removed";
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                }
            }
        }
    }
}
