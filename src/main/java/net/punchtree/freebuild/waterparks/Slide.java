package net.punchtree.freebuild.waterparks;

import java.util.ArrayList;
import java.util.List;

public class Slide {

    private final List<SlideSegment> segments = new ArrayList<>();

    public void addSegment(SlideSegment startSegment) {
        segments.add(startSegment);
    }

    public void addSegment(SlideSegmentType segmentType) {
        addSegment(SlideSegmentFactory.createSegment(getEndingSeam(), segmentType));
    }

    private SlideSeam getEndingSeam() {
        return segments.get(segments.size() - 1).getEndingSeam();
    }

    public void draw() {
        segments.forEach(SlideSegment::draw);
    }

}
