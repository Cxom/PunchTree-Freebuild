package net.punchtree.freebuild.billiards;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.util.armorstand.ArmorStandUtils;
import net.punchtree.freebuild.util.particle.ParticleShapes;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class BilliardBall {

    private static final double COLLISION_ENERGY_LOSS = 1;

    public static final Color RESIN_WHITE = Color.fromRGB(240, 232, 218);

    // a pool ball is 2¼"
    // a pool table is 88 inches, a pool table is 8 blocks, so a block in game is 11 inches
    // a block in game is 16 pixels
    static final double BALL_RADIUS = (2.25 / 44.) / 2.;
    private static final double X_MAX = 1;
    private static final double Z_MAX = 2;

    private BilliardTable table;
    private BallStyle ballStyle;
    private double x;
    private double z;
    private Speed speed;

    private ArmorStand stand;

    private Vector particleLineDirection;

    public BilliardBall(BilliardTable table, BallStyle ballStyle, double x, double z, Speed speed) {
        this.table = table;
        this.ballStyle = ballStyle;
        this.x = x;
        this.z = z;
        this.speed = speed;

        Location spawnLocation = getLocation();

        stand = spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setCanTick(false);
            ArmorStandUtils.resetPose(stand);
            stand.setItem(EquipmentSlot.HAND, BilliardsItems.CUE_BALL);
            stand.addScoreboardTag("billiards");
        });

        double randomAngle = Math.random() * 2 * Math.PI;
        this.particleLineDirection = new Vector(Math.cos(randomAngle), 0, Math.sin(randomAngle));
        particleLineDirection.normalize().multiply(BALL_RADIUS * BilliardTable.TABLE_SHORT_SIZE);
    }

    public BilliardBall(BilliardTable table, BallStyle ballStyle, BilliardTable.TablePosition tablePosition, Speed speed) {
        this(table, ballStyle, tablePosition.x(), tablePosition.z(), speed);
    }

    public void remove() {
        stand.remove();
    }

    public Location getLocation() {
        return table.getLocationInWorld(x, z);
    }

    public void collide(BilliardBall otherBall, double time) {
        move(speed.getX() * time, speed.getZ() * time);
        otherBall.move(otherBall.speed.getX() * time, otherBall.speed.getZ() * time);

        double theta = Math.atan2(otherBall.z - z, otherBall.x - x);

        double v1 = speed.getComponent(theta);
        double v2 = otherBall.speed.getComponent(theta);

        speed.addComponent(theta, (-v1+v2) * COLLISION_ENERGY_LOSS);
        otherBall.speed.addComponent(theta, (-v2+v1) * COLLISION_ENERGY_LOSS);
    }

    public void move(double time) {
        move(speed.getX() * time, speed.getZ() * time);
    }

    private void move(double x, double z) {
        this.x += x;
        this.z += z;

        if (this.x < BALL_RADIUS) {
            this.x = 2 * BALL_RADIUS - this.x;
            speed.flipX();
            table.markPhysicsForCollisionUpdate();
        }

        if (this.x > X_MAX - BALL_RADIUS) {
            this.x = 2 * (X_MAX - BALL_RADIUS) - this.x;
            speed.flipX();
            table.markPhysicsForCollisionUpdate();
        }

        if (this.z < BALL_RADIUS) {
            this.z = 2 * BALL_RADIUS - this.z;
            speed.flipZ();
            table.markPhysicsForCollisionUpdate();
        }

        if (this.z > Z_MAX - BALL_RADIUS) {
            this.z = 2 * (Z_MAX - BALL_RADIUS) - this.z;
            speed.flipZ();
            table.markPhysicsForCollisionUpdate();
        }
    }
    
    void updateDisplay() {
        Location loc = getLocation();
        stand.teleport(loc);
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(ballStyle.color));
        ParticleShapes.drawCircle(loc, BALL_RADIUS * BilliardTable.TABLE_SHORT_SIZE, 8);
        if (ballStyle.hasStripe) {
            ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(BilliardBall.RESIN_WHITE));
            Location lineStart = loc.clone().add(particleLineDirection);
            Location lineEnd = loc.clone().subtract(particleLineDirection);
            ParticleShapes.drawLine(lineStart, lineEnd, 4);
        }
    }

    public double calculateNextCollision(BilliardBall otherBall) {
        double dx = x - otherBall.x;
        double dz = z - otherBall.z;
        double dvx = speed.getX() - otherBall.speed.getX();
        double dvz = speed.getZ() - otherBall.speed.getZ();

        if (dvx == 0 && dvz == 0) {
            // balls are moving in parallel
            return Double.POSITIVE_INFINITY;
        }

        double dSpeed = dvx * dvx + dvz * dvz;
        double b_half = (dx * dvx + dz * dvz);
        double c = Math.pow((BALL_RADIUS + BALL_RADIUS), 2) * dSpeed - Math.pow(dvx * dz - dvz * dx, 2);

        if (c < 0) {
            // balls are moving away from each other
            return Double.POSITIVE_INFINITY;
        }

        double start = (-b_half - Math.sqrt(c)) / dSpeed;
        double end = (-b_half + Math.sqrt(c)) / dSpeed;

        if (end < 0) {
            // no positive zero means no collision
            return Double.POSITIVE_INFINITY;
        }

        if (start + end < 0) {
            // Large approximation of the fact that the balls will not collide?
            // End is positive, so start must be MORE negative (midpoint/extrema occurs below 0)
            // I don't know what this means in turns of approximation
            return Double.POSITIVE_INFINITY;
        }

        if (start < 0) {
            // balls are already colliding
            // one positive zero because end >= 0
            return 0;
        }

        // two non-negative zeros, return the smaller
        return start;
    }

    public void setSpeed(double x, double z) {
        this.speed = new Speed(x, z);
    }
}
