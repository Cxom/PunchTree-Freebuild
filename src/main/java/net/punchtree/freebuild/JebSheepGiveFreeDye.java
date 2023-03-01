package net.punchtree.freebuild;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class JebSheepGiveFreeDye implements Listener {

    private final Inventory FREE_DYE_INVENTORY;
    private final Component FREE_DYES_TITLE;

    public JebSheepGiveFreeDye() {
        FREE_DYES_TITLE = Component.text("Dyes!");
        FREE_DYE_INVENTORY = Bukkit.createInventory(null, 18, FREE_DYES_TITLE);
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.WHITE_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.LIGHT_GRAY_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.GRAY_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.BLACK_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.BROWN_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.RED_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.ORANGE_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.YELLOW_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.LIME_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.GREEN_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.CYAN_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.LIGHT_BLUE_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.BLUE_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.PURPLE_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.MAGENTA_DYE));
        FREE_DYE_INVENTORY.addItem(new ItemStack(Material.PINK_DYE));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClickSheep(PlayerInteractEntityEvent event) {
        if (!isJebSheep(event.getRightClicked())) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        event.getPlayer().openInventory(FREE_DYE_INVENTORY);
        event.setCancelled(true);
    }

    private boolean isJebSheep(Entity rightClicked) {
        return rightClicked.getType() == EntityType.SHEEP && rightClicked.getName().equals("jeb_");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if (!event.getView().title().equals(FREE_DYES_TITLE)) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null ||
                clickedInventory == null ||
                event.getClickedInventory() == event.getWhoClicked().getInventory() ||
                !clickedItem.getType().name().endsWith("DYE")) return;

        ItemStack itemToGive = new ItemStack(clickedItem.getType(), 64);
        clicker.getInventory().addItem(itemToGive);
    }

}
