package net.punchtree.freebuild.waterparks;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public record SlideSeam(Block block, BlockFace blockFace) {

    // east towards positive X
    // south towards positive Z

    public Location getCenter() {
        double xOffset = blockFace == BlockFace.EAST ? 1 : blockFace == BlockFace.WEST ? 0 : 0.5;
        double zOffset = blockFace == BlockFace.SOUTH ? 1 : blockFace == BlockFace.NORTH ? 0 : 0.5;
        return block.getLocation().clone().add(xOffset, 0.5, zOffset);
    }
}
