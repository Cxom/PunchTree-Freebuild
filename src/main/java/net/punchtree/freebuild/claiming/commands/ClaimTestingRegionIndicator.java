package net.punchtree.freebuild.claiming.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimTestingRegionIndicator implements Listener {

    private final RegionContainer regionContainer;

    public ClaimTestingRegionIndicator() {
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    private final List<UUID> testingPlayers = new ArrayList<>();

    public void toggle(Player player) {
        if (testingPlayers.contains(player.getUniqueId())) {
            testingPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Claim testing indicator mode disabled");
        } else {
            testingPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Claim testing indicator mode enabled");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if ( ! testingPlayers.contains(player.getUniqueId())) return;

        /* Info to display
         * - Chunk index
         * - Unclaimed/Claimed by other (in red)/Claimed by self (in green)
         * - parent region, if claimed
         */

        Chunk chunk = event.getTo().getChunk();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(chunk.getWorld()));

        String chunkRegionName = String.format("claim_%d_%d", chunk.getX(), chunk.getZ());

        String chunkIndexInfo = String.format("(%d, %d)", chunk.getX(), chunk.getZ());

        String chunkClaimStatusInfo = ChatColor.YELLOW + "Unclaimed";
        if (regionManager.hasRegion(chunkRegionName)) {
            ProtectedRegion chunkRegion = regionManager.getRegion(chunkRegionName);
            ProtectedRegion parentRegion = chunkRegion.getParent();

            if (parentRegion == null) {
                chunkClaimStatusInfo = ChatColor.RED + "Broken. Please report this to staff...";
            } else {
                // TODO - this raises an important point, the mvp uses uuid named regions, but there is also the worldguard idea of owners
                // WE NEED TO MAKE SURE that we are treating this very clearly - there needs to be one source of truth

                ChatColor chunkClaimStatusColor = parentRegion.getId().startsWith(player.getUniqueId().toString()) ? ChatColor.GREEN : ChatColor.RED;
                // TODO use one-source-of-truth owners! see above!
                String creatorUniqueId = parentRegion.getId().substring(0, parentRegion.getId().lastIndexOf('-'));
                Player creator = Bukkit.getPlayer(UUID.fromString(creatorUniqueId));
                String creatorName = creator == null ? null : creator.getName();
                chunkClaimStatusInfo = String.format("%s%s%s", chunkClaimStatusColor, "Claimed by ", creatorName);
            }
        }

        String totalChunkInfo = String.format("%s %s", chunkIndexInfo, chunkClaimStatusInfo);
        player.sendActionBar(totalChunkInfo);
    }
}
