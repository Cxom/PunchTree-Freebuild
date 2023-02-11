package net.punchtree.freebuild.towerdefense;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class Path {

    private final Location[] points;

    public Path(Location[] points) {
        this.points = points;
    }

    public void playTestAnimation() {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(255, 50, 50));
        for (int i = 0; i < points.length - 1; ++i) {
            ParticleShapes.spawnParticleLine(points[i], points[i + 1], (int) points[i].distance(points[i + 1]) * 4);
        }
    }

    public Location getLocation(PathPosition pathPosition) {
        if (pathPosition.segmentIndex() == points.length - 1) {
            return points[points.length - 1];
        }
        if (pathPosition.segmentIndex() > points.length - 1) {
            throw new IllegalArgumentException("Path segment index is out of bounds: " + pathPosition.segmentIndex());
        }
        Location pathSegmentStart = points[pathPosition.segmentIndex()];
        Location pathSegmentEnd = points[pathPosition.segmentIndex() + 1];
        Vector directionToNextPoint = pathSegmentEnd.clone().subtract(pathSegmentStart).toVector().normalize();
        return pathSegmentStart.clone().add(directionToNextPoint.multiply(pathPosition.segmentPosition()));
    }

    public double getSegmentLength(int pathSegmentIndex) {
        return points[pathSegmentIndex].distance(points[pathSegmentIndex + 1]);
    }

    public boolean isAtEnd(PathPosition pathPosition) {
        return pathPosition.segmentIndex() >= points.length - 1;
    }

}
