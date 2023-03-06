package net.punchtree.freebuild.waterparks.segment;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.util.particle.ParticleShapes;
import net.punchtree.freebuild.waterparks.SlideSeam;
import net.punchtree.freebuild.waterparks.SlideSegment;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

public class CurveSlideSegment implements SlideSegment {


    enum CurveSize {
        SMALL, LARGE
    }

    enum CurveDirection {
        LEFT, RIGHT
    }

    private static final int SMALL_CURVE_RADIUS = 4;
    private static final int LARGE_CURVE_RADIUS = 7;

    private final SlideSeam startSlideSeam;
    private final boolean turnsLeft;
    private final boolean isSmall;
    private final int radius;
    private final SlideSeam endSlideSeam;

    public CurveSlideSegment(SlideSeam startSlideSeam, boolean left, boolean small) {
        this.startSlideSeam = startSlideSeam;
        this.turnsLeft = left;
        this.isSmall = small;
        this.radius = isSmall ? SMALL_CURVE_RADIUS : LARGE_CURVE_RADIUS;

        // add one because the start seam is the end seam of the previous segment (sort of analogous to zero-indexing)
        BlockFace endFace = getEndFace();
        Block endSeamBlock = startSlideSeam.block()
                .getRelative(startSlideSeam.blockFace(), radius + 1)
                .getRelative(endFace, radius);
        endSlideSeam = new SlideSeam(endSeamBlock, endFace);
    }

    private static final List<BlockFace> CLOCKWISE_FACES = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
    private BlockFace getEndFace() {
        int index = CLOCKWISE_FACES.indexOf(startSlideSeam.blockFace());
        int newIndex = turnsLeft ? index - 1 : index + 1;
        if (newIndex < 0) newIndex = CLOCKWISE_FACES.size() - 1;
        if (newIndex >= CLOCKWISE_FACES.size()) newIndex = 0;
        return CLOCKWISE_FACES.get(newIndex);
    }

    @Override
    public SlideSeam getEndingSeam() {
        return endSlideSeam;
    }

    @Override
    public void draw() {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(Color.AQUA));
        Location circleCenter = startSlideSeam.getCenter().add(endSlideSeam.blockFace().getModX() * (radius + 0.5), 0, endSlideSeam.blockFace().getModZ() * (radius + 0.5));
        int start = CLOCKWISE_FACES.indexOf(startSlideSeam.blockFace());
        start += turnsLeft ? -1 : 2;
        ParticleShapes.drawArc(circleCenter, start * 0.25, (start + 1) * 0.25, radius + 0.5, (int) (5 * radius * .5 * Math.PI));
    }
}
