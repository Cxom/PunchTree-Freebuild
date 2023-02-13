package net.punchtree.freebuild.towerdefense;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Range {

    private final Location center;
    private final int range;
    private final transient Location[] rangeCorners;

    public Range(Location center, int range) {
        this.center = center;
        this.range = range;
        rangeCorners = new Location[]{
                center.clone().add((range + 0.5), 2, (range + 0.5)),
                center.clone().add(-(range + 0.5), 2, (range + 0.5)),
                center.clone().add((range + 0.5), 2, -(range + 0.5)),
                center.clone().add(-(range + 0.5), 2, -(range + 0.5))
        };
    }

    void draw() {
        ParticleShapes.drawQuad(rangeCorners[0], rangeCorners[1], rangeCorners[2], rangeCorners[3], ((range * 2) + 1) * 2);
    }

    public boolean contains(Location location) {
        return Math.abs(location.getX() - center.getX()) <= range + 0.5
                && Math.abs(location.getZ() - center.getZ()) <= range + 0.5;
    }

    public Location center() {
        return center;
    }
}
