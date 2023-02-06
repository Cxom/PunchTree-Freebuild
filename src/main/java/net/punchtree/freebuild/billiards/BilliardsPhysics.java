package net.punchtree.freebuild.billiards;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BilliardsPhysics extends BukkitRunnable {

    private static final double TIME_STEP = 1;

    private List<BilliardBall> balls;
    private boolean doCollisionUpdate = false;
    private double timeUntilNextCollision;
    private BilliardBall first, second;

    public BilliardsPhysics(List<BilliardBall> balls) {
        this.balls = balls;
    }

    @Override
    public void run() {
        double timePassedThisStep = 0;
        while (timePassedThisStep + timeUntilNextCollision < TIME_STEP) {
            for (BilliardBall ball : balls) {
                if (ball == first) {
                    ball.collide(second, timeUntilNextCollision);
                } else if (ball != second) {
                    ball.move(timeUntilNextCollision);
                }
            }
            timePassedThisStep += timeUntilNextCollision;
            doCollisionUpdate();
        }
        // adding time passed this step makes this zoom up to the last collision that happened during this tick
        // when it begins calculating for the next tick
        timeUntilNextCollision += timePassedThisStep;
        timeUntilNextCollision -= TIME_STEP;

        for(BilliardBall ball : balls) {
            ball.move(TIME_STEP - timePassedThisStep);
        }

        balls.forEach(BilliardBall::updateDisplay);

        if (doCollisionUpdate) {
            doCollisionUpdate();
        }
    }

    private void doCollisionUpdate() {
        timeUntilNextCollision = Double.POSITIVE_INFINITY;
        for (int i = 0; i < balls.size(); ++i) {
            for (int j = i+1; j < balls.size(); ++j) {
                double timeUntilCollision = balls.get(i).calculateNextCollision(balls.get(j));
                if (timeUntilCollision < timeUntilNextCollision) {
                    timeUntilNextCollision = timeUntilCollision;
                    first = balls.get(i);
                    second = balls.get(j);
                }
            }
        }

        doCollisionUpdate = false;
    }

    public void markForCollisionUpdate() {
        this.doCollisionUpdate = true;
    }

    // corner pocket should be between 6.545 pixels and 6.727 pixels
    // it's oriented diagonally
    // so XxX should be between 4.628 and 4.757 - halfway would be 4.69262

}
