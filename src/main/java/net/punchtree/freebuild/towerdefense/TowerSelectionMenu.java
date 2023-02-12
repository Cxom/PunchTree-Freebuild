package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TowerSelectionMenu implements Listener {

    private static final TextComponent MENU_TITLE = Component.text("Tower Selection");

    private final TowerDefensePlayerManager towerDefensePlayerManager;
    private final Inventory inventory;

    public TowerSelectionMenu(TowerDefensePlayerManager towerDefensePlayerManager) {
        this.towerDefensePlayerManager = towerDefensePlayerManager;
        this.inventory = Bukkit.createInventory(null, 9, MENU_TITLE);
        inventory.addItem(TowerType.BASIC.getIcon());
        Bukkit.getPluginManager().registerEvents(this, PunchTreeFreebuildPlugin.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if ( ! (event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != null && event.getView().title().equals(MENU_TITLE)) {
            event.setCancelled(true);

            if (event.getClickedInventory() != inventory) return;
            if (event.getCurrentItem() == null) return;

            // taking for granted that the player is a TowerDefensePlayer
            TowerDefensePlayer tdPlayer = towerDefensePlayerManager.getPlayer(player);

            if (event.getCurrentItem().getType() == TowerType.BASIC.iconMaterial) {
                tdPlayer.placeTower(TowerType.BASIC);
            }
        }
    }

    public void showTo(TowerDefensePlayer tdPlayer) {
        tdPlayer.getPlayer().sendMessage("Opening tower placement menu!");
        tdPlayer.getPlayer().openInventory(inventory);
    }
}
