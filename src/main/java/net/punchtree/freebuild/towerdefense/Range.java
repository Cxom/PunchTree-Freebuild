package net.punchtree.freebuild.towerdefense;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Range {

    private final Location center;
    private final double range;
    private final transient Location[] rangeCorners;

    public Range(Location center, double range) {
        this.center = center;
        this.range = range;
        rangeCorners = new Location[]{
                center.clone().add(range, 2, range),
                center.clone().add(-range, 2, range),
                center.clone().add(range, 2, -range),
                center.clone().add(-range, 2, -range)
        };
    }

    public void draw() {
        ParticleShapes.drawQuad(rangeCorners[0], rangeCorners[1], rangeCorners[2], rangeCorners[3], (int) (range * 2 * 2));
    }

    public boolean contains(Location location) {
        return Math.abs(location.getX() - center.getX()) <= range
                && Math.abs(location.getZ() - center.getZ()) <= range;
    }

    public Location center() {
        return center;
    }
}
