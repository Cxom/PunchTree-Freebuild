package net.punchtree.freebuild.waterparks;

import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class SlideManager {

//    Map<UUID, SlideEditor> slideEditors = new HashMap<>();
//
//    public void startEditing(Player player, Block startBlock, BlockFace startDirection) {
//        if (slideEditors.containsKey(player.getUniqueId())) {
//            player.sendMessage("You are already editing a slide!");
//            return;
//        }
//        slideEditors.put(player.getUniqueId(), new SlideEditor(player, startBlock, startDirection));
//    }

    // slidetesting draw myslide

    private final Map<String, Slide> namesToSlides = new HashMap<>();

    private final BukkitTask drawTask;

    public SlideManager() {
        this.drawTask = new BukkitRunnable() {
            @Override
            public void run() {
                namesToSlides.values().forEach(Slide::draw);
            }
        }.runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), 0, 10);
    }

    public Slide startNewSlide(String name, Block startBlock, BlockFace startDirection) {
        Slide slide = new Slide();
        SlideSegment startSegment = SlideSegmentFactory.createStartSegment(new SlideSeam(startBlock, startDirection));
        slide.addSegment(startSegment);
        namesToSlides.put(name, slide);
        return slide;
    }

    public Slide getSlide(String slideName) {
        return namesToSlides.get(slideName);
    }

    public void onDisable() {
        if (drawTask != null) {
            drawTask.cancel();
        }
    }
}
