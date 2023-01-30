package net.punchtree.freebuild.billiards;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static net.punchtree.freebuild.billiards.BilliardsItems.TABLE_MATERIAL;

public class BilliardsManager {

    private static final double NEAR_THRESHOLD = 7;

    private Set<BilliardTable> billiardTables = new HashSet<>();

    public Set<BilliardTable> getNearbyTables(Location location) {
        return billiardTables.stream()
                .filter(table -> table.getCenter().distanceSquared(location) < NEAR_THRESHOLD * NEAR_THRESHOLD)
                .collect(Collectors.toSet());
    }

    public void attemptToRegisterTable(Player player) {
        Block searchStartBlock = player.getLocation().subtract(0, .5, 0).getBlock();
        if (searchStartBlock.getType() != TABLE_MATERIAL) {
            player.sendMessage(ChatColor.RED + "It doesn't appear you're standing on a pool table!");
            return;
        }

        final int MAX_SEARCH_DISTANCE = 9;
        int xMinOffset = 0;
        int xMaxOffset = 0;
        int zMinOffset = 0;
        int zMaxOffset = 0;
        for (int i = 0; i < MAX_SEARCH_DISTANCE; ++i) {
            if (searchStartBlock.getRelative(-i, 0, 0).getType() == TABLE_MATERIAL) {
                xMinOffset = -i;
            } else break;
        }
        for (int i = 0; i < MAX_SEARCH_DISTANCE; ++i) {
            if (searchStartBlock.getRelative(i, 0, 0).getType() == TABLE_MATERIAL) {
                xMaxOffset = +i;
            } else break;
        }
        for (int i = 0; i < MAX_SEARCH_DISTANCE; ++i) {
            if (searchStartBlock.getRelative(0, 0, -i).getType() == TABLE_MATERIAL) {
                zMinOffset = -i;
            } else break;
        }
        for (int i = 0; i < MAX_SEARCH_DISTANCE; ++i) {
            if (searchStartBlock.getRelative(0, 0, i).getType() == TABLE_MATERIAL) {
                zMaxOffset = +i;
            } else break;
        }

        if (!isDimensionsRight(xMaxOffset - xMinOffset + 1, zMaxOffset - zMinOffset + 1)) {
            player.sendMessage(ChatColor.RED + "It does not appear the dimensions of this pool table are right! It should be 4x8 blocks.");
            return;
        }

        for (int xi = xMinOffset; xi <= xMaxOffset; ++xi) {
            for (int zi = zMinOffset; zi <= zMaxOffset; ++zi) {
                if (searchStartBlock.getRelative(xi, 0, zi).getType() != TABLE_MATERIAL) {
                    player.sendMessage(ChatColor.RED + "It doesn't appear you're standing on a pool table (right dimensions, but not filled in)!");
                    return;
                }
            }
        }

        BilliardTable newTable = new BilliardTable(
            player.getWorld(),
            searchStartBlock.getY() + 1,
            searchStartBlock.getX() + xMinOffset,
            searchStartBlock.getZ() + zMinOffset,
            searchStartBlock.getX() + xMaxOffset + 1,
            searchStartBlock.getZ() + zMaxOffset + 1
        );
        billiardTables.add(newTable);

        player.sendMessage(ChatColor.AQUA + "Registered a new pool table!");
        newTable.highlight();
        player.sendMessage("Debug: " + newTable);
    }

    private boolean isDimensionsRight(int xSize, int zSize) {
        return (xSize == 8 && zSize == 16) || (xSize == 16 & zSize == 8);
    }

}
