package fr.aqua_tuor.pathfinder;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerManager {

    private PathManager pathManager;

    HashMap<String, HashMap<Integer, ItemStack>> playersEditing = new HashMap<>();

    public PlayerManager(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    public void giveEditorTools(Player player) {
        player.getInventory().clear();

        ItemStack stickEditor = new ItemStack(Material.STICK);
        ItemMeta stickEditorMeta = stickEditor.getItemMeta();
        stickEditorMeta.setDisplayName("§aStick Editor");
        stickEditorMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        stickEditorMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        stickEditorMeta.setUnbreakable(true);
        stickEditor.setItemMeta(stickEditorMeta);
        player.getInventory().setItem(0, stickEditor);
    }

    public boolean isEditing(Player player) {
        return playersEditing.containsKey(player.getName());
    }

    private HashMap<Integer, ItemStack> getItems(Player player) {
        HashMap<Integer, ItemStack> items = new HashMap<>();
        Inventory inventory = player.getInventory();

        // Inventory contents
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null) {
                items.put(i, inventory.getItem(i));
            }
        }

        // Armor contents
        for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
            if (player.getInventory().getArmorContents()[i] != null) {
                items.put(i + 100, player.getInventory().getArmorContents()[i]);
            }
        }

        return items;
    }

    public void addPlayer(Player player) {
        if (!playersEditing.containsValue(player.getName())) {
            playersEditing.put(player.getName(), getItems(player));
            giveEditorTools(player);
        }
    }

    public void removePlayer(Player player) {
        if (playersEditing.containsKey(player.getName())) {
            player.getInventory().clear();

            playersEditing.get(player.getName()).forEach((slot, item) -> {
                if (slot < 100) {
                    player.getInventory().setItem(slot, item);
                } else {
                    player.getInventory().setArmorContents(new ItemStack[]{item});
                }
            });

            playersEditing.remove(player.getName());
        }
    }

    public HashMap<String, HashMap<Integer, ItemStack>> getPlayersEditing() {
        return playersEditing;
    }

}
