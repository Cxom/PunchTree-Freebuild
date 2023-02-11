package net.punchtree.freebuild.towerdefense;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Minion {

    private final Path path;

    private PathPosition pathPosition;
    private final double speed;

    public Minion(Path path, double speed) {
        this.path = path;
        this.speed = speed;
        this.pathPosition = new PathPosition(path, 0, 0);
    }

    public void tick() {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(255, 50, 50));
        ParticleShapes.spawnParticle(getLocation());

        pathPosition.advance(speed);
    }

    private Location getLocation() {
        return path.getLocation(pathPosition);
    }
}
