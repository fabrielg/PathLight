package fr.aqua_tuor.pathfinder.listeners;

import fr.aqua_tuor.pathfinder.managers.PathManager;
import fr.aqua_tuor.pathfinder.node.Node;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
                    Block block = event.getClickedBlock();
                    // Get the middle of the block
                    double x = block.getX() + 0.5;
                    double y = block.getY() + 0.5;
                    double z = block.getZ() + 0.5;

                    // Check if the node already exists
                    if (pathManager.getNodeByCoords(x, y, z) != null) {
                        String message = "§cA node already exists on this block";
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                        return;
                    }

                    // Add the node
                    int id = 0;
                    for (Node node : pathManager.getNodes()) {
                        if (node.getId() > id) {
                            id = node.getId();
                        }
                    }
                    Node node = new Node(id + 1, x, y, z, player.getWorld());
                    pathManager.addNode(node);

                    String message = "§aA node added";
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));

                }

            }
        }
    }

}
