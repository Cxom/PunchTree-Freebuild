package net.punchtree.freebuild.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlocksCommand implements CommandExecutor, Listener {
    private final Inventory TEMPLATED_INVENTORY;
    private final ItemStack SCROLL_LEFT_ITEM;
    private final ItemStack SCROLL_RIGHT_ITEM;
    private final ItemStack PLACEHOLDER_ITEM;
    private static final List<Material> MATERIALS;
    private static final List<ItemStack> CLICKABLE_ITEMS;
    private static final Component BLOCK_SHOP_TITLE;
    private static final int BLOCK_SHOP_SIZE;
    private static final int BLOCKS_PER_PAGE;
    private static final Component SCROLL_RIGHT_ITEM_NAME;
    private static final Component SCROLL_LEFT_ITEM_NAME;
    private static final Component PLACEHOLDER_ITEM_NAME;
    private static final List<Component> CLICKABLE_ITEM_LORE;

    static {
        MATERIALS = new ArrayList<>();
        CLICKABLE_ITEMS = new ArrayList<>();
        BLOCK_SHOP_TITLE = Component
                .text("Survival Block Depot")
                .color(NamedTextColor.DARK_GRAY);
        BLOCK_SHOP_SIZE = 45;
        BLOCKS_PER_PAGE = 35;
        SCROLL_RIGHT_ITEM_NAME = Component
                .text("Scroll Right")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD);
        SCROLL_LEFT_ITEM_NAME = Component
                .text("Scroll Left")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD);
        PLACEHOLDER_ITEM_NAME = Component.text("");
        CLICKABLE_ITEM_LORE = List.of(
                Component
                        .text("Click for a stack.")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, true),
                Component
                        .text("Shift-right click to fill your inventory.")
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, true));
        MATERIALS.addAll(List.of(
                Material.OAK_LOG,
                Material.BIRCH_LOG,
                Material.SPRUCE_LOG,
                Material.JUNGLE_LOG,
                Material.ACACIA_LOG,
                Material.DARK_OAK_LOG,
                Material.MANGROVE_LOG,
                Material.CRIMSON_STEM,
                Material.WARPED_STEM,
                Material.STONE,
                Material.GRANITE,
                Material.DIORITE,
                Material.ANDESITE,
                Material.COBBLED_DEEPSLATE,
                Material.BRICKS,
                Material.PACKED_MUD,
                Material.SANDSTONE,
                Material.RED_SANDSTONE,
                Material.SAND,
                Material.RED_SAND,
                Material.CLAY,
                Material.GRAVEL,
                Material.PRISMARINE,
                Material.SEA_LANTERN,
                Material.PRISMARINE_BRICKS,
                Material.DARK_PRISMARINE,
                Material.NETHER_BRICKS,
                Material.RED_NETHER_BRICKS,
                Material.BASALT,
                Material.BLACKSTONE,
                Material.END_STONE,
                Material.PURPUR_BLOCK,
                Material.QUARTZ_BLOCK,
                Material.WAXED_COPPER_BLOCK,
                Material.WHITE_WOOL,
                Material.WHITE_TERRACOTTA,
                Material.GLASS,
                Material.ICE,
                Material.SNOW,
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.CALCITE,
                Material.TUFF,
                Material.DRIPSTONE_BLOCK,
                Material.SOUL_SAND,
                Material.SOUL_SOIL,
                Material.HAY_BLOCK,
                Material.OCHRE_FROGLIGHT,
                Material.VERDANT_FROGLIGHT,
                Material.PEARLESCENT_FROGLIGHT,
                Material.SHROOMLIGHT,
                Material.SCAFFOLDING,
                Material.HONEYCOMB,
                Material.REDSTONE,
                Material.HONEY_BLOCK,
                Material.SLIME_BLOCK,
                Material.IRON_BLOCK,
                Material.WHITE_CONCRETE,
                Material.LIGHT_GRAY_CONCRETE,
                Material.GRAY_CONCRETE,
                Material.BLACK_CONCRETE,
                Material.BROWN_CONCRETE,
                Material.RED_CONCRETE,
                Material.ORANGE_CONCRETE,
                Material.YELLOW_CONCRETE,
                Material.LIME_CONCRETE,
                Material.GREEN_CONCRETE,
                Material.CYAN_CONCRETE,
                Material.LIGHT_BLUE_CONCRETE,
                Material.BLUE_CONCRETE,
                Material.PURPLE_CONCRETE,
                Material.MAGENTA_CONCRETE,
                Material.PINK_CONCRETE,
                Material.WHITE_CONCRETE_POWDER,
                Material.LIGHT_GRAY_CONCRETE_POWDER,
                Material.GRAY_CONCRETE_POWDER,
                Material.BLACK_CONCRETE_POWDER,
                Material.BROWN_CONCRETE_POWDER,
                Material.RED_CONCRETE_POWDER,
                Material.ORANGE_CONCRETE_POWDER,
                Material.YELLOW_CONCRETE_POWDER,
                Material.LIME_CONCRETE_POWDER,
                Material.GREEN_CONCRETE_POWDER,
                Material.CYAN_CONCRETE_POWDER,
                Material.LIGHT_BLUE_CONCRETE_POWDER,
                Material.BLUE_CONCRETE_POWDER,
                Material.PURPLE_CONCRETE_POWDER,
                Material.MAGENTA_CONCRETE_POWDER,
                Material.PINK_CONCRETE_POWDER,
                Material.FERN
        ));

        for(Material m : MATERIALS) {
            ItemStack clickableItem = new ItemStack(m);
            clickableItem.editMeta(meta -> {
                        meta.displayName(Component
                                .text(WordUtils.capitalizeFully(m.name().replace('_', ' ')))
                                .color(NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                        meta.lore(CLICKABLE_ITEM_LORE);
                    });
            CLICKABLE_ITEMS.add(clickableItem);
        }
    }

    public BlocksCommand() {
        //Below creates the template inventory used to generate the displayed inventory when a player runs /blocks
        TEMPLATED_INVENTORY = Bukkit.createInventory(null, BLOCK_SHOP_SIZE, BLOCK_SHOP_TITLE);
        SCROLL_LEFT_ITEM = new ItemStack(Material.AMETHYST_SHARD);
        SCROLL_RIGHT_ITEM = SCROLL_LEFT_ITEM.clone();
        PLACEHOLDER_ITEM = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        SCROLL_LEFT_ITEM.editMeta(meta -> {
            meta.displayName(SCROLL_LEFT_ITEM_NAME);
        });
        SCROLL_RIGHT_ITEM.editMeta(meta -> {
           meta.displayName(SCROLL_RIGHT_ITEM_NAME);
        });
        PLACEHOLDER_ITEM.editMeta(meta -> {
            meta.displayName(PLACEHOLDER_ITEM_NAME);
        });

        //Apply (placeholder & scroll items) to the left and right sides
        for(int i = 0; i < BLOCK_SHOP_SIZE; i = i+8) {
            TEMPLATED_INVENTORY.setItem(i, PLACEHOLDER_ITEM);
            if(i == 0 || i+1 >= BLOCK_SHOP_SIZE) {continue;}
            i++;
            TEMPLATED_INVENTORY.setItem(i, PLACEHOLDER_ITEM);
        }
        TEMPLATED_INVENTORY.setItem(18, SCROLL_LEFT_ITEM);
        TEMPLATED_INVENTORY.setItem(26, SCROLL_RIGHT_ITEM);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) return false;
        player.openInventory(prepareInventory());
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if(event.getView().title().equals(BLOCK_SHOP_TITLE)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if(clickedItem == null || clickedItem.equals(PLACEHOLDER_ITEM) || clickedInventory == null) {return;}

            if(clickedItem.equals(SCROLL_LEFT_ITEM)) {
                scrollInventory(-7, clickedInventory);
            }else if(clickedItem.equals(SCROLL_RIGHT_ITEM)) {
                scrollInventory(7, clickedInventory);
            }else {
                if(CLICKABLE_ITEMS.contains(clickedItem)) {
                    ItemStack itemToGive = new ItemStack(clickedItem.getType(), 64);
                    Inventory clickerInventory = clicker.getInventory();
                    if(event.isRightClick() && event.isShiftClick()) {
                        while(clickerInventory.firstEmpty() != -1) {
                            clickerInventory.addItem(itemToGive);
                        }
                        return;
                    }
                    clicker.getInventory().addItem(itemToGive);
                }
            }
        }
    }

    private Inventory prepareInventory() {
        Inventory blockInventory = Bukkit.createInventory(null, BLOCK_SHOP_SIZE, BLOCK_SHOP_TITLE);
        List<ItemStack> currentStock = CLICKABLE_ITEMS.subList(0, Math.min(BLOCKS_PER_PAGE, CLICKABLE_ITEMS.size()));
        blockInventory.setContents(TEMPLATED_INVENTORY.getContents());
        blockInventory.addItem(currentStock.toArray(new ItemStack[0]));
        return blockInventory;
    }
    private void scrollInventory(int amountToScroll, Inventory invToScroll) {
        
    }

    private void wipeClickableBlocks(Inventory currentInventory) {
        for(ItemStack item : currentInventory.getContents()) {
            if(item == null) {continue;}
            if(CLICKABLE_ITEMS.contains(item)) {
                currentInventory.remove(item);
            }
        }
    }

}
