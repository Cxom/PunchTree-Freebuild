package net.punchtree.freebuild.billiards;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.util.particle.ParticleShapes;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BilliardTable {

    static final double CUE_BALL_SPEED = .02;

    static final int TABLE_SHORT_SIZE = 8;
    static final int TABLE_LONG_SIZE = TABLE_SHORT_SIZE * 2;
    private final World world;
    private final int y, xMin, zMin, xMax, zMax;
    private final boolean isXShortAxis;

    private BilliardBall cueBall;

    private List<BilliardBall> balls = new ArrayList<>(16);

    private BilliardsPhysics physics;
    private BukkitTask physicsTask;

    public BilliardTable(World world, int y, int xMin, int zMin, int xMax, int zMax) {
        this.world = world;
        this.xMin = xMin;
        this.zMin = zMin;
        this.y = y;
        this.xMax = xMax;
        this.zMax = zMax;
        this.isXShortAxis = (xMax - xMin) <= (zMax - zMin);

        physics = new BilliardsPhysics(balls);
        physicsTask = physics.runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), 0, 1);
    }

    public Location getCenter() {
        return new Location(world, (xMin + xMax) * .5, y, (zMin + zMax) * .5);
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

        cueBall = new BilliardBall(this, .5, .5, new Speed(0, 0));

        balls.forEach(BilliardBall::remove);
        balls.clear();
        balls.add(cueBall);
    }

    public void clear() {
        balls.forEach(BilliardBall::remove);
        balls.clear();
        if (physicsTask != null) {
            physicsTask.cancel();
        }
    }

    public void shootCueBall() {
        if (cueBall == null) {
            spawnCueBall();
        }
        double randomAngle = Math.random() * 2 * Math.PI;
        Vector randomDirection = new Vector(Math.cos(randomAngle), 0, Math.sin(randomAngle));
        randomDirection = new Vector(Math.cos(.25 * Math.PI), 0, Math.sin(.25 * Math.PI));
        randomDirection.multiply(CUE_BALL_SPEED);
        cueBall.setSpeed(randomDirection.getX(), randomDirection.getZ());
        ParticleShapes.drawLine(cueBall.getLocation(), cueBall.getLocation().clone().add(randomDirection.multiply(2)), 10);
    }


    // TODO this is flipping over the diagonal, instead of rotating
    // In one of the two cases, x or z needs to be negative (figure it out)
    public Location getLocationInWorld(double x, double z) {
        // BOTH multiples are TABLE_SHORT_SIZE so that x and z are scaled the same!
        if (isXShortAxis) {
            return new Location(world, xMin + (x * TABLE_SHORT_SIZE) , y, zMin + (z * TABLE_SHORT_SIZE));
        } else {
            return new Location(world, xMin + (z * TABLE_SHORT_SIZE), y, zMin + (x * TABLE_SHORT_SIZE));
        }
    }

    public void markPhysicsForCollisionUpdate() {
        physics.markForCollisionUpdate();
    }

    public void shootMultiBall() {
        balls.forEach(BilliardBall::remove);
        balls.clear();

        for (int i = 1; i <= 3; ++i) {
            BilliardBall newBall = new BilliardBall(this, .5, .5 * i, new Speed(0, 0));
            double randomAngle = Math.random() * 2 * Math.PI;
            Vector randomDirection = new Vector(Math.cos(randomAngle), 0, Math.sin(randomAngle)).multiply(CUE_BALL_SPEED);
            newBall.setSpeed(randomDirection.getX(), randomDirection.getZ());
            balls.add(newBall);
        }
    }
}
