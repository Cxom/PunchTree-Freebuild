package net.punchtree.freebuild.billiards;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.util.particle.ParticleShapes;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

public class BilliardTable {

    static final int TABLE_SHORT_SIZE = 8;
    static final int TABLE_LONG_SIZE = TABLE_SHORT_SIZE * 2;
    private final World world;
    private final int y, xMin, zMin, xMax, zMax;
    private final boolean isXMainAxis;

    private BilliardBall cueBall;

    public BilliardTable(World world, int y, int xMin, int zMin, int xMax, int zMax) {
        this.world = world;
        this.xMin = xMin;
        this.zMin = zMin;
        this.y = y;
        this.xMax = xMax;
        this.zMax = zMax;
        this.isXMainAxis = (xMax - xMin) >= (zMax - zMin);
    }

    public Location getCenter() {
        return new Location(world, (xMin + xMax) * .5, y, (zMin + zMax) * .5);
    }
    private Location getHeadCenter() {
        if (isXMainAxis) {
            return new Location(world, xMin + .25 * TABLE_LONG_SIZE, y, zMin + .5 * TABLE_SHORT_SIZE);
        } else {
            return new Location(world, xMin + .5 * TABLE_SHORT_SIZE, y, zMin + .25 * TABLE_LONG_SIZE);
        }
    }

    public void highlight() {
        Location aa = new Location(world, xMin, y, zMin);
        Location ab = new Location(world, xMax, y, zMin);
        Location bb = new Location(world, xMax, y, zMax);
        Location ba = new Location(world, xMin, y, zMax);
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(Color.GREEN));
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                ParticleShapes.drawQuad(aa, ab, ba, bb, 10);
                ++i;
                if (i > 10) {
                    this.cancel();
                }
            }
        }.runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), 0, 5);
    }

    @Override
    public String toString() {
        return String.format("world:%s y:%d min:(%d,%d) max:(%d,%d)", world.getName(), y, xMin, zMin, xMax, zMax);
    }

    public void spawnCueBall() {
        if (cueBall != null) {
            cueBall.remove();
        }
        cueBall = new BilliardBall(this, getHeadCenter());
    }

    public void clear() {
        cueBall.remove();
    }

}
