package net.punchtree.freebuild.util.particle;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Duplicated from punchtree-util - a proper dependency should be set up
 */
@Deprecated
public class ParticleShapes {

    private static ParticleBuilder particleBuilder = new ParticleBuilder(Particle.REDSTONE).color(Color.WHITE);

    public static void setParticleBuilder(ParticleBuilder particleBuilder) {
        ParticleShapes.particleBuilder = particleBuilder;
    }

    public static ParticleBuilder getParticleBuilder() {
        return particleBuilder;
    }

    public static void drawQuad(Location aa, Location ab, Location ba, Location bb, int steps) {
        drawLine(aa, ab, steps);
        drawLine(ab, bb, steps);
        drawLine(aa, ba, steps);
        drawLine(ba, bb, steps);
    }

    public static void drawLine(Location a, Location b, int steps) {
        Vector difference = b.clone().toVector().subtract(a.toVector());
        difference.multiply(1d/(steps-1));
        // change <= vs < for endpoint
        for ( int i = 0; i < steps; ++i ) {
            Location l = a.clone().add(difference.clone().multiply(i));
            spawnParticle(l);
        }
    }

    public static void spawnParticle(Location location) {
        particleBuilder.location(location).spawn();
    }
}
