package net.punchtree.freebuild.waterparks.segment;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.waterparks.SlideSeam;
import net.punchtree.freebuild.waterparks.SlideSegment;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Color;
import org.bukkit.Particle;

public class StraightSlideSegment implements SlideSegment {

    private static final int STRAIGHT_SEGMENT_LENGTH = 3;

    private final SlideSeam startSlideSeam;
    private final SlideSeam endSlideSeam;

    public StraightSlideSegment(SlideSeam startSlideSeam) {
        this.startSlideSeam = startSlideSeam;
        this.endSlideSeam = new SlideSeam(startSlideSeam.block().getRelative(startSlideSeam.blockFace(), STRAIGHT_SEGMENT_LENGTH),
                                                    startSlideSeam.blockFace());
    }

    @Override
    public SlideSeam getEndingSeam() {
        return endSlideSeam;
    }

    @Override
    public void draw() {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(Color.AQUA));
        ParticleShapes.spawnParticleLine(startSlideSeam.getCenter(),
                                         endSlideSeam.getCenter(),
                                   5 * STRAIGHT_SEGMENT_LENGTH);
    }
}
