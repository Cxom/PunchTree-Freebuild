package net.punchtree.freebuild.billiards;

public class BilliardsPhysics {

    // a pool ball is 2¼"
    // a pool table is 88 inches, a pool table is 8 blocks, so a block in game is 11 inches
    // a block in game is 16 pixels
    private static final double BALL_RADIUS = (2.25 / 11) * 16;

    // corner pocket should be between 6.545 pixels and 6.727 pixels
    // it's oriented diagonally
    // so XxX should be between 4.628 and 4.757 - halfway would be 4.69262

}
