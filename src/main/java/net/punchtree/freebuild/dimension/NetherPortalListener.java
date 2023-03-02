package net.punchtree.freebuild.dimension;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NetherPortalListener implements Listener {

    // Lighting and breaking portals works as normal (because generation happens on going through, not on lighting)

    // Teleport to overworld - ONLY link to pre-existing portal - if one cannot be found,
    //                          BREAK THE PORTAL in the nether and send the player attempting the teleport a message
    // Teleport to nether - generate or link to portal in nether as normal

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR &&
            event.getWorld().getEnvironment() == World.Environment.NORMAL) {
            event.setCancelled(true);
            if (event.getEntity() == null) return;
            if (event.getEntity() instanceof Player player) {
                player.sendMessage(ChatColor.RED + "Could not find a portal in the overworld to link to! (overworld portal generation is disabled)");

                offerOneTimeTeleport(player, event.getBlocks());
            }
            Block block = event.getEntity().getLocation().getBlock();
            if (block.getType() == Material.NETHER_PORTAL) {
                block.breakNaturally();
            } else if (block.getRelative(BlockFace.NORTH).getType() == Material.NETHER_PORTAL) {
                block.getRelative(BlockFace.NORTH).breakNaturally();
            } else if (block.getRelative(BlockFace.SOUTH).getType() == Material.NETHER_PORTAL) {
                block.getRelative(BlockFace.SOUTH).breakNaturally();
            } else if (block.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL) {
                block.getRelative(BlockFace.EAST).breakNaturally();
            } else if (block.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL) {
                block.getRelative(BlockFace.WEST).breakNaturally();
            }
        }
    }

    private void offerOneTimeTeleport(Player player, @NotNull List<BlockState> blocks) {
        // TODO this requires a map and a custom command, because the command is always executed as if they player had typed it in
        Location roughOutputLocation = blocks.get(0).getBlock().getLocation().add(0.5, 0, 0.5);
        player.sendMessage(ChatColor.RED + "(The portal would be generated around " + roughOutputLocation.getBlockX() + ", " + roughOutputLocation.getBlockY() + ", " + roughOutputLocation.getBlockZ() + ")");

//        String teleportCommand = String.format("/minecraft:execute in minecraft:overworld run teleport %s %f %f %f", player.getUniqueId(), roughOutputLocation.getX(), roughOutputLocation.getY(), roughOutputLocation.getZ());
//        Component teleportOffer = Component.text("If you'd like to teleport to the rough output location of this portal, ").color(NamedTextColor.RED).append(
//                Component.text("Click Here").color(NamedTextColor.RED).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
//        );
//        player.sendMessage(teleportOffer);
    }

}
