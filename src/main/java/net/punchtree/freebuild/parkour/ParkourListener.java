package net.punchtree.freebuild.parkour;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ParkourListener implements Listener {

    // When you trigger the threshold, it will give you a "parkour" item
    // - Left clicking will teleport you back to the start
    // - Right clicking will teleport you back to the respawn point
    // - Shift right clicking will set a new respawn point

    private static final ItemStack PARKOUR_ITEM = new ItemStack(Material.SUNFLOWER);
    public static final String PARKOUR_ITEM_PERMISSION = "ptfb.parkour-item";

    static {
        PARKOUR_ITEM.editMeta(meta -> {
            meta.displayName(Component.text("Parkour Item").color(NamedTextColor.GOLD));
            meta.lore(Arrays.asList(Component.text("Left click to teleport to the start"), Component.text("Right click to teleport to the respawn point"), Component.text("Shift right click to set a new respawn point")));
            meta.setCustomModelData(300);
        });
    }

    private record ParkourPlayer(UUID uuid, Location start, Location respawn) {}

    private final Map<UUID, ParkourPlayer> parkourPlayers = new HashMap<>();

    @EventHandler
    public void onParkourThresholdTrigger(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PARKOUR_ITEM_PERMISSION)) return;
        if (!isParkourThreshold(event.getTo().clone().subtract(0, .1, 0).getBlock())) return;
        if (parkourPlayers.containsKey(player.getUniqueId())) return;
        // TODO check the player's inventory to see if they already have the item
        if (player.getInventory().addItem(PARKOUR_ITEM).isEmpty()) {
            parkourPlayers.put(player.getUniqueId(), new ParkourPlayer(player.getUniqueId(), event.getTo(), event.getTo()));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Objects.equals(event.getItem(), PARKOUR_ITEM)) return;
        Player player = event.getPlayer();
        ParkourPlayer parkourPlayer = parkourPlayers.get(player.getUniqueId());
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> player.teleport(parkourPlayer.start());
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (player.isSneaking()) {
                    parkourPlayers.put(player.getUniqueId(), new ParkourPlayer(player.getUniqueId(), parkourPlayer.start(), player.getLocation()));
                    player.sendActionBar(Component.text("Respawn point set!").color(NamedTextColor.RED));
                } else {
                    player.teleport(parkourPlayer.respawn());
                }
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!event.getItemDrop().getItemStack().equals(PARKOUR_ITEM)) return;
        parkourPlayers.remove(event.getPlayer().getUniqueId());
        event.getPlayer().sendActionBar(Component.text("Left Parkour").color(TextColor.color(0, 200, 255)));
        event.getItemDrop().remove();
    }

    private boolean isParkourThreshold(Block block) {
        return block.getType() == Material.JIGSAW;
    }

}
