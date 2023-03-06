package net.punchtree.freebuild.waterparks;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SlideTestingCommand implements CommandExecutor {

    private static final String SLIDE_MAKING_PERMISSION = "ptfb.slide.make";

    private final SlideManager slideManager;

    public SlideTestingCommand(SlideManager slideManager) {
        this.slideManager = slideManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if ( ! (sender instanceof Player player)) return false;
        if ( ! player.hasPermission(SLIDE_MAKING_PERMISSION)) return true;

        if (args.length < 2) return false;

        String subcommand = args[0];
        String slideName = args[1];

        switch (subcommand) {
            case "start" -> {
                BlockFace startDirection = player.getFacing();
                Block startBlock = player.getLocation().getBlock().getRelative(startDirection.getOppositeFace(), 3);

                slideManager.startNewSlide(slideName, startBlock, startDirection);
                player.sendMessage("Created a new slide named " + slideName);
            }
            case "draw" -> {
                Optional.of(slideManager.getSlide(slideName)).ifPresent(Slide::draw);
                player.sendMessage(ChatColor.GREEN + "Drew slide:" + slideName + " :)");
            }
            case "addstraight" -> {
                Optional.of(slideManager.getSlide(slideName)).ifPresent(slide -> slide.addSegment(SlideSegmentType.STRAIGHT));
                player.sendMessage(ChatColor.GREEN + "Added a straight segment to slide:" + slideName + " :)");
            }
            case "addleft" -> {
                Optional.of(slideManager.getSlide(slideName)).ifPresent(slide -> slide.addSegment(SlideSegmentType.CURVE_LEFT));
                player.sendMessage(ChatColor.GREEN + "Added a curve left to slide:" + slideName + " :)");
            }
            case "addright" -> {
                Optional.of(slideManager.getSlide(slideName)).ifPresent(slide -> slide.addSegment(SlideSegmentType.CURVE_RIGHT));
                player.sendMessage(ChatColor.GREEN + "Added a curve right to slide:" + slideName + " :)");
            }
            default -> {
                return false;
            }
        }

        return true;
    }

}
