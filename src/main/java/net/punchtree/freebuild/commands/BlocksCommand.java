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
    private static final ItemStack SCROLL_UP_ITEM;
    private static final ItemStack SCROLL_DOWN_ITEM;
    private static final ItemStack PLACEHOLDER_ITEM;
    private static final List<Material> MATERIALS;
    private static final List<ItemStack> CLICKABLE_ITEMS;
    private static final Component BLOCK_SHOP_TITLE;
    private static final int BLOCK_SHOP_SIZE = 45;
    private static final int BLOCKS_PER_PAGE = 40;
    private static final Component SCROLL_UP_ITEM_NAME;
    private static final Component SCROLL_DOWN_ITEM_NAME;
    private static final Component PLACEHOLDER_ITEM_NAME;
    private static final List<Component> CLICKABLE_ITEM_LORE;

    static {
        CLICKABLE_ITEMS = new ArrayList<>();
        BLOCK_SHOP_TITLE = Component
                .text("Survival Block Depot")
                .color(NamedTextColor.DARK_GRAY);
        SCROLL_UP_ITEM_NAME = Component
                .text("Scroll Up")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD);
        SCROLL_DOWN_ITEM_NAME = Component
                .text("Scroll Down")
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
        MATERIALS = List.of(
                Material.OAK_LOG,
                Material.SPRUCE_LOG,
                Material.BIRCH_LOG,
                Material.JUNGLE_LOG,
                Material.ACACIA_LOG,
                Material.DARK_OAK_LOG,
                Material.MANGROVE_LOG,
                Material.CRIMSON_STEM,
                Material.WARPED_STEM,
                Material.STRIPPED_OAK_LOG,
                Material.STRIPPED_SPRUCE_LOG,
                Material.STRIPPED_BIRCH_LOG,
                Material.STRIPPED_JUNGLE_LOG,
                Material.STRIPPED_ACACIA_LOG,
                Material.STRIPPED_DARK_OAK_LOG,
                Material.STRIPPED_MANGROVE_LOG,
                Material.STRIPPED_CRIMSON_STEM,
                Material.STRIPPED_WARPED_STEM,
                Material.STONE,
                Material.GRANITE,
                Material.DIORITE,
                Material.ANDESITE,
                Material.COBBLED_DEEPSLATE,
                Material.DEEPSLATE,
                Material.BRICKS,
                Material.PACKED_MUD,
                Material.SANDSTONE,
                Material.SMOOTH_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.SMOOTH_RED_SANDSTONE,
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
                Material.SMOOTH_BASALT,
                Material.BLACKSTONE,
                Material.END_STONE,
                Material.PURPUR_BLOCK,
                Material.QUARTZ_BLOCK,
                Material.SMOOTH_QUARTZ,
                Material.WAXED_COPPER_BLOCK,
                Material.WHITE_WOOL,
                Material.TERRACOTTA,
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
                Material.TUBE_CORAL_BLOCK,
                Material.BRAIN_CORAL_BLOCK,
                Material.BUBBLE_CORAL_BLOCK,
                Material.FIRE_CORAL_BLOCK,
                Material.HORN_CORAL_BLOCK,
                Material.DEAD_TUBE_CORAL_BLOCK,
                Material.DEAD_BRAIN_CORAL_BLOCK,
                Material.DEAD_BUBBLE_CORAL_BLOCK,
                Material.DEAD_FIRE_CORAL_BLOCK,
                Material.DEAD_HORN_CORAL_BLOCK,
                Material.TUBE_CORAL,
                Material.BRAIN_CORAL,
                Material.BUBBLE_CORAL,
                Material.FIRE_CORAL,
                Material.HORN_CORAL,
                Material.TUBE_CORAL_FAN,
                Material.BRAIN_CORAL_FAN,
                Material.BUBBLE_CORAL_FAN,
                Material.FIRE_CORAL_FAN,
                Material.HORN_CORAL_FAN,
                Material.LILY_PAD,
                Material.WHITE_DYE,
                Material.LIGHT_GRAY_DYE,
                Material.GRAY_DYE,
                Material.BLACK_DYE,
                Material.BROWN_DYE,
                Material.RED_DYE,
                Material.ORANGE_DYE,
                Material.YELLOW_DYE,
                Material.LIME_DYE,
                Material.GREEN_DYE,
                Material.CYAN_DYE,
                Material.LIGHT_BLUE_DYE,
                Material.BLUE_DYE,
                Material.PURPLE_DYE,
                Material.MAGENTA_DYE,
                Material.PINK_DYE,
                Material.OAK_LEAVES,
                Material.SPRUCE_LEAVES,
                Material.BIRCH_LEAVES,
                Material.JUNGLE_LEAVES,
                Material.ACACIA_LEAVES,
                Material.DARK_OAK_LEAVES,
                Material.MANGROVE_LEAVES,
                Material.AZALEA_LEAVES,
                Material.FLOWERING_AZALEA_LEAVES,
                Material.MANGROVE_ROOTS,
                Material.MOSS_BLOCK,
                Material.PODZOL,
                Material.COARSE_DIRT,
                Material.ROOTED_DIRT,
                Material.MUD,
                Material.MYCELIUM,
                Material.AZALEA,
                Material.FLOWERING_AZALEA,
                Material.CACTUS,
                Material.SUGAR_CANE,
                Material.MUSHROOM_STEM,
                Material.BROWN_MUSHROOM_BLOCK,
                Material.RED_MUSHROOM_BLOCK,
                Material.WARPED_WART_BLOCK,
                Material.NETHER_WART_BLOCK,
                Material.DRIED_KELP_BLOCK,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.OXEYE_DAISY,
                Material.CORNFLOWER,
                Material.LILY_OF_THE_VALLEY,
                Material.PEONY,
                Material.ROSE_BUSH,
                Material.LILAC,
                Material.SUNFLOWER,
                Material.TALL_GRASS,
                Material.LARGE_FERN,
                Material.BAMBOO,
                Material.SWEET_BERRIES,
                Material.GLOW_BERRIES,
                Material.HANGING_ROOTS,
                Material.GRASS,
                Material.FERN
        );

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

        SCROLL_DOWN_ITEM = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        SCROLL_UP_ITEM = SCROLL_DOWN_ITEM.clone();
        PLACEHOLDER_ITEM = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        SCROLL_UP_ITEM.editMeta(meta -> {
            meta.displayName(SCROLL_UP_ITEM_NAME);
        });
        SCROLL_DOWN_ITEM.editMeta(meta -> {
            meta.displayName(SCROLL_DOWN_ITEM_NAME);
        });
        PLACEHOLDER_ITEM.editMeta(meta -> {
            meta.displayName(PLACEHOLDER_ITEM_NAME);
        });
    }

    public BlocksCommand() {
        //Below creates the template inventory used to generate the displayed inventory when a player runs /blocks
        TEMPLATED_INVENTORY = Bukkit.createInventory(null, BLOCK_SHOP_SIZE, BLOCK_SHOP_TITLE);

        //Apply (placeholder & scroll items) to the left and right sides
        for(int row = 8; row < BLOCK_SHOP_SIZE; row += 9) {
            TEMPLATED_INVENTORY.setItem(row, PLACEHOLDER_ITEM);
        }
        TEMPLATED_INVENTORY.setItem(17, SCROLL_UP_ITEM);
        TEMPLATED_INVENTORY.setItem(35, SCROLL_DOWN_ITEM);
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
        if (!event.getView().title().equals(BLOCK_SHOP_TITLE)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.equals(PLACEHOLDER_ITEM) || clickedInventory == null) {return;}

        if(clickedItem.equals(SCROLL_UP_ITEM)) {
            scrollInventory(-8, clickedInventory);
        }else if(clickedItem.equals(SCROLL_DOWN_ITEM)) {
            scrollInventory(8, clickedInventory);
        }else {
            if(CLICKABLE_ITEMS.contains(clickedItem)) {
                ItemStack itemToGive = new ItemStack(clickedItem.getType(), 64);
                Inventory clickerInventory = clicker.getInventory();
                if(event.isRightClick() && event.isShiftClick()) {
                    while(clickerInventory.firstEmpty() != -1) {
                        clickerInventory.addItem(itemToGive);
                    }
                } else {
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
        int clickableItemIndex = CLICKABLE_ITEMS.indexOf(invToScroll.getItem(0));

        if (clickableItemIndex == 0 && amountToScroll <= 0) return;
        if (clickableItemIndex + BLOCKS_PER_PAGE >= CLICKABLE_ITEMS.size() && amountToScroll >= 0) return;

        invToScroll.setContents(TEMPLATED_INVENTORY.getContents());

        int sublistStart = clickableItemIndex + amountToScroll;
        List<ItemStack> itemsOnThisPage = CLICKABLE_ITEMS.subList(sublistStart, Math.min(sublistStart + BLOCKS_PER_PAGE, CLICKABLE_ITEMS.size()));
        itemsOnThisPage.forEach(invToScroll::addItem);
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
