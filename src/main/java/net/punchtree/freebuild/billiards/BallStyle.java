package net.punchtree.freebuild.billiards;

import org.bukkit.Color;

public enum BallStyle {

    CUE(BilliardBall.RESIN_WHITE, false),
    ONE(Color.YELLOW, false),
    TWO(Color.BLUE, false),
    THREE(Color.RED, false),
    FOUR(Color.PURPLE, false),
    FIVE(Color.ORANGE, false),
    SIX(Color.GREEN, false),
    SEVEN(Color.fromRGB(156, 74, 47), false),
    EIGHT(Color.BLACK, false),
    NINE(Color.YELLOW, true),
    TEN(Color.BLUE, true),
    ELEVEN(Color.RED, true),
    TWELVE(Color.PURPLE, true),
    THIRTEEN(Color.ORANGE, true),
    FOURTEEN(Color.GREEN, true),
    FIFTEEN(Color.fromRGB(156, 74, 47), true);

    final Color color;
    final boolean hasStripe;

    BallStyle(Color color, boolean hasStripe) {
        this.color = color;
        this.hasStripe = hasStripe;
    }
}
