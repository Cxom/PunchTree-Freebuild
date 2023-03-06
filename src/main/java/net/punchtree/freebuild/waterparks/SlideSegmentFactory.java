package net.punchtree.freebuild.waterparks;

import net.punchtree.freebuild.waterparks.segment.CurveSlideSegment;
import net.punchtree.freebuild.waterparks.segment.StraightSlideSegment;

public class SlideSegmentFactory {
    public static SlideSegment createStartSegment(SlideSeam startSlideSeam) {
        // Just a straight segment
        return new StraightSlideSegment(startSlideSeam);
    }

    public static SlideSegment createSegment(SlideSeam startSlideSeam, SlideSegmentType straight) {
        return switch (straight) {
            case STRAIGHT -> new StraightSlideSegment(startSlideSeam);
            case CURVE_LEFT -> new CurveSlideSegment(startSlideSeam, true, true);
            case CURVE_RIGHT -> new CurveSlideSegment(startSlideSeam, false, true);
            default -> throw new IllegalArgumentException("Unknown segment type: " + straight);
        };
    }
}
