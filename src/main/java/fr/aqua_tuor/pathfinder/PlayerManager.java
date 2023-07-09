package fr.aqua_tuor.pathfinder;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerManager {

    private PathManager pathManager;

    HashMap<Player, Inventory> playersEditing = new HashMap<>();

    public PlayerManager(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    public void giveEditorTools(Player player) {
        player.getInventory().clear();

        ItemStack stickEditor = new ItemStack(Material.STICK);
        stickEditor.getItemMeta().setDisplayName("§aStick Editor");
        stickEditor.getItemMeta().addEnchant(Enchantment.DURABILITY, 1, true);
        stickEditor.getItemMeta().addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        stickEditor.getItemMeta().setUnbreakable(true);
        player.getInventory().setItem(0, stickEditor);
    }

    public boolean isEditing(Player player) {
        return playersEditing.containsValue(player);
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
