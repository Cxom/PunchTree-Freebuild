package net.punchtree.freebuild.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BlocksCommand implements CommandExecutor, Listener {
    private Inventory blockInventory;

    public BlocksCommand() {
        blockInventory = Bukkit.createInventory(null, 54, "Survival Block Depot");
        blockInventory.addItem(new ItemStack(Material.OAK_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.BIRCH_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.SPRUCE_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.JUNGLE_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.ACACIA_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.DARK_OAK_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.MANGROVE_LOG, 64));
        blockInventory.addItem(new ItemStack(Material.CRIMSON_STEM, 64));
        blockInventory.addItem(new ItemStack(Material.WARPED_STEM, 64));
        blockInventory.addItem(new ItemStack(Material.STONE, 64));
        blockInventory.addItem(new ItemStack(Material.GRANITE, 64));
        blockInventory.addItem(new ItemStack(Material.DIORITE, 64));
        blockInventory.addItem(new ItemStack(Material.ANDESITE, 64));
        blockInventory.addItem(new ItemStack(Material.COBBLED_DEEPSLATE, 64));
        blockInventory.addItem(new ItemStack(Material.BRICKS, 64));
        blockInventory.addItem(new ItemStack(Material.PACKED_MUD, 64));
        blockInventory.addItem(new ItemStack(Material.SANDSTONE, 64));
        blockInventory.addItem(new ItemStack(Material.RED_SANDSTONE, 64));
        blockInventory.addItem(new ItemStack(Material.SAND, 64));
//        blockInventory.addItem(new ItemStack(Material.RED_SAND, 64));
//        blockInventory.addItem(new ItemStack(Material.CLAY, 64));
//        blockInventory.addItem(new ItemStack(Material.GRAVEL, 64));
//        blockInventory.addItem(new ItemStack(Material.PRISMARINE, 64));
//        blockInventory.addItem(new ItemStack(Material.SEA_LANTERN, 64));
//        blockInventory.addItem(new ItemStack(Material.PRISMARINE_BRICKS, 64));
//        blockInventory.addItem(new ItemStack(Material.DARK_PRISMARINE, 64));
//        blockInventory.addItem(new ItemStack(Material.NETHER_BRICKS, 64));
//        blockInventory.addItem(new ItemStack(Material.RED_NETHER_BRICKS,, 64));
//        blockInventory.addItem(new ItemStack(Material.BASALT, 64));
//        blockInventory.addItem(new ItemStack(Material.BLACKSTONE, 64));
//        blockInventory.addItem(new ItemStack(Material.END_STONE, 64));
//        blockInventory.addItem(new ItemStack(Material.PURPUR_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.QUARTZ_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.WAXED_COPPER_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.WHITE_WOOL, 64));
//        blockInventory.addItem(new ItemStack(Material.WHITE_TERRACOTTA, 64));
//        blockInventory.addItem(new ItemStack(Material.GLASS, 64));
//        blockInventory.addItem(new ItemStack(Material.ICE, 64));
//        blockInventory.addItem(new ItemStack(Material.SNOW, 64));
//        blockInventory.addItem(new ItemStack(Material.GRASS_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.DIRT, 64));
//        blockInventory.addItem(new ItemStack(Material.CALCITE, 64));
//        blockInventory.addItem(new ItemStack(Material.TUFF, 64));
//        blockInventory.addItem(new ItemStack(Material.DRIPSTONE_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.SOUL_SAND, 64));
//        blockInventory.addItem(new ItemStack(Material.SOUL_SOIL, 64));
//        blockInventory.addItem(new ItemStack(Material.HAY_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.OCHRE_FROGLIGHT, 64));
//        blockInventory.addItem(new ItemStack(Material.VERDANT_FROGLIGHT, 64));
//        blockInventory.addItem(new ItemStack(Material.PEARLESCENT_FROGLIGHT, 64));
//        blockInventory.addItem(new ItemStack(Material.SHROOMLIGHT, 64));
//        blockInventory.addItem(new ItemStack(Material.SCAFFOLDING, 64));
//        blockInventory.addItem(new ItemStack(Material.HONEYCOMB, 64));
//        blockInventory.addItem(new ItemStack(Material.REDSTONE, 64));
//        blockInventory.addItem(new ItemStack(Material.HONEY_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.SLIME_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.IRON_BLOCK, 64));
//        blockInventory.addItem(new ItemStack(Material.WHITE_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.LIGHT_GRAY_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.GRAY_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.BLACK_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.BROWN_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.RED_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.ORANGE_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.YELLOW_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.LIME_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.GREEN_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.CYAN_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.LIGHT_BLUE_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.BLUE_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.PURPLE_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.MAGENTA_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.PINK_CONCRETE, 64));
//        blockInventory.addItem(new ItemStack(Material.FERN, 64));
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        player.openInventory(blockInventory);
        return true;
    }

    @EventHandler
    public void onBlockInventoryInteraction(InventoryClickEvent event) {
        if(blockInventory.equals(event.getInventory())) {
            event.setCancelled(true);
            if(!blockInventory.equals(event.getClickedInventory())) return;
            if(event.getCurrentItem() == null) return;

            event.getWhoClicked().getInventory().addItem(event.getCurrentItem().clone());
        }
    }
}
