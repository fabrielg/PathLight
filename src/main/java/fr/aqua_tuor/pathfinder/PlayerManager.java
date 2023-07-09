package fr.aqua_tuor.pathfinder;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerManager {

    private PathManager pathManager;

    HashMap<Player, Inventory> playersEditing = new HashMap<>();

    public PlayerManager(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    public void giveEditorTools(Player player) {
    }

    public void addPlayer(Player player) {
        if (!playersEditing.containsValue(player)) {
            playersEditing.put(player, player.getInventory());
            giveEditorTools(player);
        }
    }

    public void removePlayer(Player player) {
        if (playersEditing.containsValue(player)) {
            player.getInventory().clear();
            player.getInventory().setContents(playersEditing.get(player).getContents());
            playersEditing.remove(player);
        }
    }

}
