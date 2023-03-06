package net.punchtree.freebuild.playingcards;

import net.kyori.adventure.text.Component;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class PlayingCardInteractListener implements Listener {

    // Playing cards work by being an item in an item frame
    // Right clicking on the frame takes a card from it into the hot bar if the currently selected slot is open
    // Right clicking on the frame again puts the card back into the frame
    // There are three to four stack items depending on the number of cards in the stack
    // Cards can be played on any new surface and will create a new stack object when placed
    // Right clicking will place them face up, shift right clicking will place them face down
    // Shift right-clicking three times in quick succession will shuffle a deck
    // Left clicking three times in quick succession will offer the option to deal a deck

    private static final NamespacedKey PLAYING_CARDS_KEY = new NamespacedKey(PunchTreeFreebuildPlugin.getInstance(), "playing_cards");

    private static final Material PLAYING_CARD_MATERIAL = Material.PAPER;
    private static final Material PLAYING_CARD_STACK_MATERIAL = Material.BUNDLE;

    private static final int PLAYING_CARD_MIN_CUSTOM_MODEL_DATA = 1001;
    private static final int PLAYING_CARD_MAX_CUSTOM_MODEL_DATA = 1052;

    private static final int PLAYING_CARD_STACK_MIN_CUSTOM_MODEL_DATA = 1001;
    private static final int PLAYING_CARD_STACK_MAX_CUSTOM_MODEL_DATA = 1052;

    @EventHandler
    public void onCardInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if ( ! (event.getRightClicked() instanceof ItemFrame itemFrame)) return;
        ItemStack itemInFrame = itemFrame.getItem();
        ItemStack itemInHand = player.getInventory().getItem(event.getHand());

        if (!isCardStack(itemInFrame) && !isFaceDownCardStack(itemInFrame)) return;

        if (player.isSneaking()) {
            if (isPlayingCard(itemInHand) || isCardStack(itemInHand) || isFaceDownCard(itemInHand) || isFaceDownCardStack(itemInHand)) {
                addCardOrCardStackToFrame(itemFrame, itemInHand);
                player.getInventory().setItem(event.getHand(), null);
            }
        } else {
            if (itemInHand.getType() == Material.AIR) {
                player.getInventory().setItem(event.getHand(), drawCard(itemFrame).getNewItem());
            } else {
                HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(peekCard(itemFrame).getNewItem());
                if (overflowItems.size() > 0) {
                    player.sendMessage(ChatColor.RED + "Your inventory is full!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                } else {
                    drawCard(itemFrame);
                }
            }
        }
    }

    private boolean isCardStack(ItemStack item) {
        return item != null
                && item.getType() == PLAYING_CARD_STACK_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() >= PLAYING_CARD_STACK_MIN_CUSTOM_MODEL_DATA
                && item.getItemMeta().getCustomModelData() <= PLAYING_CARD_STACK_MAX_CUSTOM_MODEL_DATA;
    }

    private boolean isPlayingCard(ItemStack item) {
        return item != null
                && item.getType() == PLAYING_CARD_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() >= PLAYING_CARD_MIN_CUSTOM_MODEL_DATA
                && item.getItemMeta().getCustomModelData() <= PLAYING_CARD_MAX_CUSTOM_MODEL_DATA;
    }

    private void addCardOrCardStackToFrame(ItemFrame itemFrame, ItemStack cardOrCardStack) {
        ItemStack itemStack = itemFrame.getItem();
        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        if (isPlayingCard(cardOrCardStack)) {
            List<ItemStack> items = Stream.concat(Stream.of(cardOrCardStack), bundleMeta.getItems().stream()).toList();
            bundleMeta.setItems(items);
        } else if (isCardStack(cardOrCardStack) || isFaceDownCardStack(cardOrCardStack)) {
            List<ItemStack> items = Stream.concat(((BundleMeta) cardOrCardStack.getItemMeta()).getItems().stream(), bundleMeta.getItems().stream()).toList();
            bundleMeta.setItems(items);
        } else if (isFaceDownCard(cardOrCardStack)) {
            List<ItemStack> items = Stream.concat(Stream.of(flipCardOrCardStack(cardOrCardStack)), bundleMeta.getItems().stream()).toList();
            bundleMeta.setItems(items);
        }
        if (!isFaceDownCardStack(itemStack)) {
            bundleMeta.setCustomModelData(bundleMeta.getItems().get(0).getItemMeta().getCustomModelData());
        }
        itemStack.setItemMeta(bundleMeta);
        itemFrame.setItem(itemStack);
        showCardCount(itemFrame);
    }

    private PlayingCard peekCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();
        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
        return PlayingCard.fromItem(drawnCard);
    }

    private PlayingCard drawCard(ItemFrame itemFrame) {
        ItemStack itemStack = itemFrame.getItem();
        BundleMeta bundleMeta = (BundleMeta) itemStack.getItemMeta();
        ItemStack drawnCard = bundleMeta.getItems().get(0);
        if (bundleMeta.getItems().size() == 1) {
            // If this is the last card, remove the item frame
            itemFrame.remove();
        } else {
            bundleMeta.setItems(bundleMeta.getItems().subList(1, bundleMeta.getItems().size()));

            if (!isFaceDownCardStack(itemStack)) {
                bundleMeta.setCustomModelData(bundleMeta.getItems().get(0).getItemMeta().getCustomModelData());
            }
            itemStack.setItemMeta(bundleMeta);
            itemFrame.setItem(itemStack);
            showCardCount(itemFrame);
        }
        return PlayingCard.fromItem(drawnCard);
    }

    private void showCardCount(ItemFrame frame) {
        ItemStack item = frame.getItem();
        if (!isCardStack(item) && !isFaceDownCardStack(item)) return;
        item.editMeta(meta -> {
            BundleMeta bundleMeta = (BundleMeta) meta;
            bundleMeta.displayName(Component.text(bundleMeta.getItems().size()));
        });
        frame.setItem(item);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (frame.isValid()) {
                    ItemStack item = frame.getItem();
                    item.editMeta(meta -> meta.displayName(null));
                    frame.setItem(item);
                }
            }
        }.runTaskLater(PunchTreeFreebuildPlugin.getInstance(), 20);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // On right click on block with a playing card in hand
        // if there's not an item frame there, create one and place a new card stack with the card in it
        ItemStack itemInHand = event.getItem();
        boolean isCardStack = isCardStack(itemInHand);
        if ( !isPlayingCard(itemInHand) && !isCardStack && !isFaceDownCard(itemInHand) && !isFaceDownCardStack(itemInHand)) return;

        // Cancel all events while holding cards or decks, even if they don't cause a card related action
        event.setCancelled(true);

        if ( event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        BlockFace clickedFace = event.getBlockFace();
        EquipmentSlot hand = event.getHand();

//        if (player.isSneaking()) {
        tryPlacingCardOrCardStack(hand, itemInHand, isCardStack, player, clickedBlock, clickedFace);
//        } else {
//            drawCard();
//            if (isHoldingDeck(player, hand)) {
//                addCardToHeldDeck();
//            }
//        }
    }

    private void tryPlacingCardOrCardStack(EquipmentSlot hand, ItemStack itemInHand, boolean isCardStack, Player player, Block clickedBlock, BlockFace clickedFace) {
        if (!canBlockFaceHaveCardPlacedOnIt(clickedBlock, clickedFace)) return;


        ItemFrame frame = clickedBlock.getWorld().spawn(clickedBlock.getRelative(clickedFace).getLocation(), ItemFrame.class, frameBeforeSpawn -> {
            frameBeforeSpawn.setFacingDirection(clickedFace, true);
            frameBeforeSpawn.setVisible(false);
            frameBeforeSpawn.setRotation(getRotationForYaw(player.getLocation().getYaw()));
            frameBeforeSpawn.setFixed(true);
            if (isCardStack && player.isSneaking()) {
                BundleMeta meta = (BundleMeta) itemInHand.getItemMeta();
                ItemStack itemToBeInFrame = PlayingCard.fromItem(meta.getItems().get(0)).getNewPileItem();
                itemToBeInFrame.editMeta(itemToBeInFrameMeta -> itemToBeInFrameMeta.displayName(null));
                frameBeforeSpawn.setItem(itemToBeInFrame);
                if (meta.getItems().size() == 1) {
                    player.getInventory().setItem(hand, null);
                } else if (meta.getItems().size() == 2) {
                    player.getInventory().setItem(hand, meta.getItems().get(1));
                } else {
                    meta.setItems(meta.getItems().subList(1, meta.getItems().size()));
                    meta.setCustomModelData(meta.getItems().get(0).getItemMeta().getCustomModelData());
                    itemInHand.setItemMeta(meta);
                }
            } else if (isFaceDownCardStack(itemInHand) && player.isSneaking()) {
                BundleMeta meta = (BundleMeta) itemInHand.getItemMeta();
                ItemStack itemToBeInFrame = flipCardOrCardStack(PlayingCard.fromItem(meta.getItems().get(0)).getNewPileItem());
                itemToBeInFrame.editMeta(itemToBeInFrameMeta -> itemToBeInFrameMeta.displayName(null));
                frameBeforeSpawn.setItem(itemToBeInFrame);
                if (meta.getItems().size() == 1) {
                    player.getInventory().setItem(hand, null);
                } else if (meta.getItems().size() == 2) {
                    player.getInventory().setItem(hand, flipCardOrCardStack(meta.getItems().get(1)));
                } else {
                    meta.setItems(meta.getItems().subList(1, meta.getItems().size()));
                    itemInHand.setItemMeta(meta);
                }
            } else if (isFaceDownCard(itemInHand)) {
                ItemStack itemToBeInFrame = PlayingCard.getNewFaceDownPileItem();
                itemToBeInFrame.editMeta(meta -> {
                    BundleMeta bundleMeta = (BundleMeta) meta;
                    meta.displayName(null);
                    bundleMeta.addItem(flipCardOrCardStack(itemInHand));
                });
                frameBeforeSpawn.setItem(itemToBeInFrame);
                player.getInventory().setItem(hand, null);
            } else {
                ItemStack itemToBeInFrame = (isCardStack || isFaceDownCardStack(itemInHand)) ? itemInHand : PlayingCard.fromItem(itemInHand).getNewPileItem();
                itemToBeInFrame.editMeta(meta -> meta.displayName(null));
                frameBeforeSpawn.setItem(itemToBeInFrame);
                player.getInventory().setItem(hand, null);
            }
        });
    }



    // TODO see if cards can skip the manual conversion from yaw to rotation
    private Rotation getRotationForYaw(double yaw) {
        if (Math.abs(yaw) > 135) return Rotation.NONE;
        else if (Math.abs(yaw) < 45) return Rotation.FLIPPED;
        else if (yaw < 0) return Rotation.CLOCKWISE;
        else return Rotation.COUNTER_CLOCKWISE;
    }

    private boolean canBlockFaceHaveCardPlacedOnIt(Block clickedBlock, BlockFace clickedFace) {
        if (!clickedBlock.getBlockData().isFaceSturdy(clickedFace, BlockSupport.CENTER)) return false;
        Material touchingClickedFace = clickedBlock.getRelative(clickedFace).getType();
        boolean isClickedFaceUnobstructed = touchingClickedFace == Material.AIR || touchingClickedFace.name().contains("DOOR") || touchingClickedFace.name().contains("PANE");
        return isClickedFaceUnobstructed && !blockFaceHasItemFrame(clickedBlock, clickedFace);
    }

    private boolean blockFaceHasItemFrame(Block clickedBlock, BlockFace clickedFace) {
        return clickedBlock.getLocation().getNearbyEntities(2, 2, 2).stream()
//                .peek(entity -> {
//                    Bukkit.getPlayer("Cxom").sendMessage("====================================");
//                    Bukkit.getPlayer("Cxom").sendMessage("Entity: " + entity.getType().name());
//                    Bukkit.getPlayer("Cxom").sendMessage("Entity location: " + entity.getLocation());
//                    Bukkit.getPlayer("Cxom").sendMessage("Relatv location: " + clickedBlock.getRelative(clickedFace).getLocation());
//                    if (entity instanceof ItemFrame itemFrame) {
//                        Bukkit.getPlayer("Cxom").sendMessage("Item frame facing: " + itemFrame.getFacing().name());
//                    }
//                })
                .filter(entity -> entity instanceof ItemFrame)
                .map(entity -> (ItemFrame) entity)
                .anyMatch(itemFrame -> {
                    Location location = itemFrame.getLocation();
                    Block newFrameBlock = clickedBlock.getRelative(clickedFace);
                    return location.getBlockX() == newFrameBlock.getX()
                            && location.getBlockY() == newFrameBlock.getY()
                            && location.getBlockZ() == newFrameBlock.getZ();
//                            && itemFrame.getFacing() == clickedFace;
                });
    }

    @EventHandler
    public void onHangBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) return;
        ItemStack item = itemFrame.getItem();
        if (!isPlayingCard(item) && !isCardStack(item) && !isFaceDownCard(item) && !isFaceDownCardStack(item)) return;

        event.setCancelled(true);

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof BundleMeta bundleMeta) {
            if (isFaceDownCardStack(item)) {
                // make this a face down card
                if (bundleMeta.getItems().size() == 1) {
                    itemFrame.getLocation().getWorld().dropItem(itemFrame.getLocation(), flipCardOrCardStack(bundleMeta.getItems().get(0)));
                    itemFrame.remove();
                    return;
                } else {
                    bundleMeta.displayName(PlayingCard.FACE_DOWN_CARD_PILE_NAME);
                }
            } else {
                if (bundleMeta.getItems().size() == 1) {
                    itemFrame.getLocation().getWorld().dropItem(itemFrame.getLocation(), bundleMeta.getItems().get(0));
                    itemFrame.remove();
                    return;
                } else {
                    bundleMeta.displayName(PlayingCard.fromItem(bundleMeta.getItems().get(0)).getName());
                }
            }
        } else {
            meta.displayName(PlayingCard.fromItem(item).getName());
        }
        item.setItemMeta(meta);

        itemFrame.getLocation().getWorld().dropItem(itemFrame.getLocation(), item);
        itemFrame.remove();
    }

    @EventHandler
    public void onFlipCard(PlayerSwapHandItemsEvent event) {
        ItemStack mainHandItem = event.getMainHandItem();
        if (isPlayingCard(mainHandItem) || isCardStack(mainHandItem) || isFaceDownCard(mainHandItem) || isFaceDownCardStack(mainHandItem)) {
            event.setCancelled(true);
            ItemStack flippedCard = flipCardOrCardStack(mainHandItem);
            event.getPlayer().getInventory().setItemInOffHand(flippedCard);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_TOAST_IN, 1, 1);
        }
        ItemStack offHandItem = event.getOffHandItem();
        if (isPlayingCard(offHandItem) || isCardStack(offHandItem) || isFaceDownCard(offHandItem) || isFaceDownCardStack(offHandItem)) {
            event.setCancelled(true);
            ItemStack flippedCard = flipCardOrCardStack(offHandItem);
            event.getPlayer().getInventory().setItemInMainHand(flippedCard);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_TOAST_IN, 1, 1);
        }
    }

    private ItemStack flipCardOrCardStack(ItemStack item) {
        if (isFaceDownCard(item)) {
            ItemMeta meta = item.getItemMeta();
            List<PlayingCard> cards = meta.getPersistentDataContainer().get(PLAYING_CARDS_KEY, new CardStackPersistentDataType());
            return PlayingCard.getItemForCardList(cards);
        } else if (isFaceDownCardStack(item)) {
            item.editMeta(meta -> {
                ItemStack topCard = ((BundleMeta) item.getItemMeta()).getItems().get(0);
                meta.setCustomModelData(topCard.getItemMeta().getCustomModelData());
                meta.displayName(PlayingCard.fromItem(topCard).getName());
            });
            return item;
        } else if (isPlayingCard(item)) {
            ItemStack faceDownCard = PlayingCard.getNewFaceDownCardItem();
            writeCardToPdc(faceDownCard, PlayingCard.fromItem(item));
            return faceDownCard;
        } else if (isCardStack(item)) {
            item.editMeta(meta -> {
                meta.setCustomModelData(1000);
                BundleMeta bundleMeta = (BundleMeta) meta;
                bundleMeta.displayName(PlayingCard.FACE_DOWN_CARD_PILE_NAME);
            });

//            ItemStack faceDownPile = PlayingCard.getNewFaceDownPileItem();
//            List<PlayingCard> cards = ((BundleMeta) item.getItemMeta()).getItems().stream().map(PlayingCard::fromItem).collect(Collectors.toList());
//            writeCardsToPdc(faceDownPile, cards);
            return item;
        } else {
            return item;
        }
    }

    private void writeCardToPdc(ItemStack faceDownCard, PlayingCard fromItem) {
        writeCardsToPdc(faceDownCard, List.of(fromItem));
    }

    private void writeCardsToPdc(ItemStack faceDownCard, List<PlayingCard> cards) {
        faceDownCard.editMeta(meta -> {
            meta.getPersistentDataContainer().set(PLAYING_CARDS_KEY, new CardStackPersistentDataType(), cards);
        });
    }

    private boolean isFaceDownCard(ItemStack item) {
        return item != null &&
                item.getType() == PLAYING_CARD_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 1000
                && item.getItemMeta().getPersistentDataContainer().has(PLAYING_CARDS_KEY);
    }

    private boolean isFaceDownCardStack(ItemStack item) {
        return item != null &&
                item.getType() == PLAYING_CARD_STACK_MATERIAL
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 1000;
    }

}
