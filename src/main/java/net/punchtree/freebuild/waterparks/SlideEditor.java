package net.punchtree.freebuild.waterparks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class SlideEditor {

    // The entry point is a command

    private final Player player;

    private Slide slide;


    public SlideEditor(Player player, Block startBlock, BlockFace startDirection) {
        this.player = player;
    }


}
