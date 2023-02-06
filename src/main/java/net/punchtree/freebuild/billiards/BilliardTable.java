package net.punchtree.freebuild.billiards;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.util.particle.ParticleShapes;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class BilliardTable {

    record TablePosition(double x, double z) {}

    private static final double CUE_BALL_SPEED = .02;
    private static final double SHOT_FORCE_MULTIPLIER = 0.025;
    private static final double SHOT_REACH = 4.5;
    private static final double HIT_BOX_HEIGHT = BilliardBall.BALL_RADIUS * 2;
    private static final double HIT_BOX_HALF_WIDTH = BilliardBall.BALL_RADIUS;

    static final int TABLE_SHORT_SIZE = 8;
    static final int TABLE_LONG_SIZE = TABLE_SHORT_SIZE * 2;

    private final World world;
    private final int y, xMin, zMin, xMax, zMax;
    private final boolean isXShortAxis;

    private BilliardBall cueBall;

    private List<BilliardBall> balls = new ArrayList<>(16);

    private BilliardsPhysics physics;
    private BukkitTask physicsTask;

    private static final List<TablePosition> TRIANGLE_RACK_POSITIONS = new ArrayList<>(15);
    static {
        TablePosition FOOT_SPOT = new TablePosition(0.5, 1.5);
        TRIANGLE_RACK_POSITIONS.add(FOOT_SPOT);
        // 30 60 90 triangle to next row
        // balls are touching, so hypotenuse is 2 * BALL_RADIUS
        // So down is BALL_RADIUS * sqrt(3) and left is BALL_RADIUS
        double sqrt3 = Math.sqrt(3);
        TablePosition SECOND_ROW_START = new TablePosition(FOOT_SPOT.x() - BilliardBall.BALL_RADIUS, FOOT_SPOT.z() + (BilliardBall.BALL_RADIUS * sqrt3));
        TRIANGLE_RACK_POSITIONS.add(SECOND_ROW_START);
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(SECOND_ROW_START.x() + 2 * BilliardBall.BALL_RADIUS, SECOND_ROW_START.z()));
        TablePosition THIRD_ROW_START = new TablePosition(SECOND_ROW_START.x() - BilliardBall.BALL_RADIUS, SECOND_ROW_START.z() + (BilliardBall.BALL_RADIUS * sqrt3));
        TRIANGLE_RACK_POSITIONS.add(THIRD_ROW_START);
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(THIRD_ROW_START.x() + 2 * BilliardBall.BALL_RADIUS, THIRD_ROW_START.z()));
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(THIRD_ROW_START.x() + 4 * BilliardBall.BALL_RADIUS, THIRD_ROW_START.z()));
        TablePosition FOURTH_ROW_START = new TablePosition(THIRD_ROW_START.x() - BilliardBall.BALL_RADIUS, THIRD_ROW_START.z() + (BilliardBall.BALL_RADIUS * sqrt3));
        TRIANGLE_RACK_POSITIONS.add(FOURTH_ROW_START);
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FOURTH_ROW_START.x() + 2 * BilliardBall.BALL_RADIUS, FOURTH_ROW_START.z()));
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FOURTH_ROW_START.x() + 4 * BilliardBall.BALL_RADIUS, FOURTH_ROW_START.z()));
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FOURTH_ROW_START.x() + 6 * BilliardBall.BALL_RADIUS, FOURTH_ROW_START.z()));
        TablePosition FIFTH_ROW_START = new TablePosition(FOURTH_ROW_START.x() - BilliardBall.BALL_RADIUS, FOURTH_ROW_START.z() + (BilliardBall.BALL_RADIUS * sqrt3));
        TRIANGLE_RACK_POSITIONS.add(FIFTH_ROW_START);
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FIFTH_ROW_START.x() + 2 * BilliardBall.BALL_RADIUS, FIFTH_ROW_START.z()));
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FIFTH_ROW_START.x() + 4 * BilliardBall.BALL_RADIUS, FIFTH_ROW_START.z()));
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FIFTH_ROW_START.x() + 6 * BilliardBall.BALL_RADIUS, FIFTH_ROW_START.z()));
        TRIANGLE_RACK_POSITIONS.add(new TablePosition(FIFTH_ROW_START.x() + 8 * BilliardBall.BALL_RADIUS, FIFTH_ROW_START.z()));
    }

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
        return getLocationInWorld(.5, 1);
    }

    public World getWorld() {
        return world;
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

    public void takeShot(BilliardsShot shot) {
        // This makes a shot aimed straight down still resolve in the direction the player is facing
        // Perhaps it should choose a random direction instead?
        Location playerLocationLookingLevel = shot.source().getLocation().clone();
        playerLocationLookingLevel.setPitch(0);

        Vector shotLineStart = shot.source().getEyeLocation().toVector();
        Vector shotDirection = shot.source().getLocation().getDirection().normalize();
        Vector shotLineEnd = shotLineStart.clone().add(shotDirection.multiply(SHOT_REACH));
        Vector shotDirectionHorizontal = playerLocationLookingLevel.getDirection();

        if (shotDirectionHorizontal.getY() != 0) {
            throw new IllegalArgumentException("Shot direction must be horizontal");
        }

        Location pointOnPlane = cueBall.getLocation();
        Vector planeForward = new Vector(-shotDirectionHorizontal.getX(), 0, -shotDirectionHorizontal.getZ());

        if ( !lineCrossesTargetPlane(pointOnPlane, planeForward, shotLineStart, shotLineEnd) ) {
            return;
        }
        Vector intersection = getPlaneLineIntersection(pointOnPlane, planeForward, shotLineStart, shotLineEnd);

        // Check x and z distances from base of ball (ball location)
        if (intersection.getY() > y + HIT_BOX_HEIGHT) {
            return;
        }
        if (intersection.setY(y).distanceSquared(pointOnPlane.toVector()) > HIT_BOX_HALF_WIDTH * HIT_BOX_HALF_WIDTH) {
            return;
        }

        // HIT!!!!

        shotDirectionHorizontal.normalize(); // TODO this is going to fire in LOCAL rotation, not global!!!
        // This means the puck will not shoot the way the player is facing,
        shotDirectionHorizontal.multiply(shot.force() * SHOT_FORCE_MULTIPLIER);
        cueBall.setSpeed(shotDirectionHorizontal.getX(), shotDirectionHorizontal.getZ());
    }

    /**
     * THIS IS DIRECTIONAL
     */
    public static boolean lineCrossesTargetPlane(Location pointOnPlane, Vector planeForward, Vector start, Vector end) {
        Vector lastDifference = pointOnPlane.toVector().subtract(start);
        Vector currentDifference = pointOnPlane.toVector().subtract(end);
        return lastDifference.dot(planeForward) < 0 && currentDifference.dot(planeForward) >= 0;
    }

    public static Vector getPlaneLineIntersection(Location pointOnPlane, Vector planeForward, Vector lineStart, Vector lineEnd) {
        // Calculate plane intersection
        Vector lineDirection = lineEnd.clone().subtract(lineStart);
        Vector lineEndToTarget = pointOnPlane.toVector().subtract(lineStart);
        double t = lineEndToTarget.dot(planeForward) / lineDirection.dot(planeForward);
        Vector intersection = lineStart.clone().add(lineDirection.multiply(t));
        return intersection;
    }

    @Override
    public String toString() {
        return String.format("world:%s y:%d min:(%d,%d) max:(%d,%d)", world.getName(), y, xMin, zMin, xMax, zMax);
    }

    public void spawnCueBall() {
        if (cueBall != null) {
            balls.remove(cueBall);
            cueBall.remove();
        }

        cueBall = new BilliardBall(this, BallStyle.CUE, .5, .5, new Speed(0, 0));
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
            BilliardBall newBall = new BilliardBall(this, BallStyle.CUE, .5, .5 * i, new Speed(0, 0));
            double randomAngle = Math.random() * 2 * Math.PI;
            Vector randomDirection = new Vector(Math.cos(randomAngle), 0, Math.sin(randomAngle)).multiply(CUE_BALL_SPEED);
            newBall.setSpeed(randomDirection.getX(), randomDirection.getZ());
            balls.add(newBall);
        }
    }

    public void rackEightBall() {
        balls.forEach(BilliardBall::remove);
        balls.clear();

        // Set fixed balls
        // TODO static factory methods on this class, instead of constructor calls?
        BilliardBall oneBall = new BilliardBall(this, BallStyle.ONE, TRIANGLE_RACK_POSITIONS.get(0), new Speed(0, 0));
        BilliardBall eightBall = new BilliardBall(this, BallStyle.EIGHT, TRIANGLE_RACK_POSITIONS.get(4), new Speed(0, 0));

        balls.add(oneBall);
        balls.add(eightBall);

        // We exclude ONE from solids because it must always be at the top of the rack
        List<BallStyle> solidsList = Arrays.asList(
                BallStyle.TWO,
                BallStyle.THREE,
                BallStyle.FOUR,
                BallStyle.FIVE,
                BallStyle.SIX,
                BallStyle.SEVEN);
        List<BallStyle> stripesList = Arrays.asList(BallStyle.NINE,
                BallStyle.TEN,
                BallStyle.ELEVEN,
                BallStyle.TWELVE,
                BallStyle.THIRTEEN,
                BallStyle.FOURTEEN,
                BallStyle.FIFTEEN);

        Collections.shuffle(solidsList);
        Collections.shuffle(stripesList);

        Queue<BallStyle> solidsQueue = new ArrayDeque<>(
                solidsList);
        Queue<BallStyle> stripesQueue = new ArrayDeque<>(
                stripesList);

        // Algorithm 2 - wider variety of racks, conforms to tournament rules without being more specific
        // Choose random balls for the corners
        Queue<BallStyle> randomStyle = Math.random() >= 0.5 ? solidsQueue : stripesQueue;
        Queue<BallStyle> otherStyle = randomStyle == solidsQueue ? stripesQueue : solidsQueue;
        BilliardBall leftCorner = new BilliardBall(this, randomStyle.poll(), TRIANGLE_RACK_POSITIONS.get(10), new Speed(0, 0));
        BilliardBall rightCorner = new BilliardBall(this, otherStyle.poll(), TRIANGLE_RACK_POSITIONS.get(14), new Speed(0, 0));
        balls.add(leftCorner);
        balls.add(rightCorner);

        // Oop, actually just hardcode a simple rack for now, while algo is in development
        balls.add(new BilliardBall(this, stripesQueue.poll(), TRIANGLE_RACK_POSITIONS.get(1), new Speed(0, 0)));
        balls.add(new BilliardBall(this, solidsQueue.poll(), TRIANGLE_RACK_POSITIONS.get(2), new Speed(0, 0)));
        balls.add(new BilliardBall(this, solidsQueue.poll(), TRIANGLE_RACK_POSITIONS.get(3), new Speed(0, 0)));
        balls.add(new BilliardBall(this, stripesQueue.poll(), TRIANGLE_RACK_POSITIONS.get(5), new Speed(0, 0)));
        balls.add(new BilliardBall(this, stripesQueue.poll(), TRIANGLE_RACK_POSITIONS.get(6), new Speed(0, 0)));
        balls.add(new BilliardBall(this, solidsQueue.poll(), TRIANGLE_RACK_POSITIONS.get(7), new Speed(0, 0)));
        balls.add(new BilliardBall(this, stripesQueue.poll(), TRIANGLE_RACK_POSITIONS.get(8), new Speed(0, 0)));
        balls.add(new BilliardBall(this, solidsQueue.poll(), TRIANGLE_RACK_POSITIONS.get(9), new Speed(0, 0)));
        balls.add(new BilliardBall(this, stripesQueue.poll(), TRIANGLE_RACK_POSITIONS.get(11), new Speed(0, 0)));
        balls.add(new BilliardBall(this, solidsQueue.poll(), TRIANGLE_RACK_POSITIONS.get(12), new Speed(0, 0)));
        balls.add(new BilliardBall(this, stripesQueue.poll(), TRIANGLE_RACK_POSITIONS.get(13), new Speed(0, 0)));

        if (!solidsQueue.isEmpty() || !stripesQueue.isEmpty()) {
            throw new IllegalStateException("Dummy hardcoded rack is not right!");
        }

        spawnCueBall();

//        Map<Integer, BallStyle> placement = new HashMap<>();
//        placement.put(0, BallStyle.ONE);
//        placement.put(4, BallStyle.EIGHT);

//        boolean valid = verifyPlacement(placement);
    }

    private void findPlacement(Map<Integer, BallStyle> placementMap, Queue<BallStyle> solid, Queue<BallStyle> striped) {
        Queue<Integer> remainingPositions = new ArrayDeque();
        for (int i = 0; i <= 14; ++i) {
            if (!placementMap.containsKey(i)) {
                remainingPositions.add(i);
            }
        }
        findPlacement(placementMap, remainingPositions, solid, striped);
    }

    private void findPlacement(Map<Integer, BallStyle> placementMap, Queue<Integer> remainingPositions, Queue<BallStyle> solid, Queue<BallStyle> striped) {

    }

    private static final Map<Integer, List<Integer>> ADJACENCY_LIST = new HashMap<>(15);
    static {
        ADJACENCY_LIST.put(0, List.of(1, 2));
        ADJACENCY_LIST.put(1, List.of(0, 2, 3, 4));
        ADJACENCY_LIST.put(2, List.of(0, 1, 4, 5));
        ADJACENCY_LIST.put(3, List.of(1, 4, 6, 7));
        ADJACENCY_LIST.put(4, List.of(1, 2, 3, 5, 7, 8));
        ADJACENCY_LIST.put(5, List.of(2, 4, 8, 9));
        ADJACENCY_LIST.put(6, List.of(3, 7, 10, 11));
        ADJACENCY_LIST.put(7, List.of(3, 4, 6, 8, 11, 12));
        ADJACENCY_LIST.put(8, List.of(4, 5, 7, 9, 12, 13));
        ADJACENCY_LIST.put(9, List.of(5, 8, 13, 14));
        ADJACENCY_LIST.put(10, List.of(6, 11));
        ADJACENCY_LIST.put(11, List.of(6, 7, 10, 12));
        ADJACENCY_LIST.put(12, List.of(7, 8, 11, 13));
        ADJACENCY_LIST.put(13, List.of(8, 9, 12, 14));
        ADJACENCY_LIST.put(14, List.of(9, 13));
    }

    private boolean verifyPlacement(Map<Integer, BallStyle> placements) {
        for (Map.Entry<Integer, BallStyle> placement : placements.entrySet()) {
            int position = placement.getKey();
            BallStyle style = placement.getValue();
            if (position == 4) continue; // Don't check the eight ball
            int same = 0, different = 0;
            for (int neighborPosition : ADJACENCY_LIST.get(position)) {
                if (neighborPosition == 4 || !placements.containsKey(neighborPosition)) {
                    // if the neighbor is the 8 ball, or hasn't been selected yet, skip it
                    continue;
                }
                BallStyle neighborStyle = placements.get(neighborPosition);
                if (neighborStyle.hasStripe == style.hasStripe) {
                    ++same;
                } else {
                    ++different;
                }
            }
            if (same > different) return false;
        }
        return true;
    }
}
