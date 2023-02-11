package net.punchtree.freebuild.towerdefense;

import java.util.Objects;

/**
 * Used to represent a specific point along a path (used by minions)
 */
final class PathPosition {
    private final Path path;
    private int segmentIndex;
    private double segmentPosition;
    private boolean atEnd = false;

    PathPosition(Path path, int segmentIndex, double pathSegmentPosition) {
        this.path = path;
        this.segmentIndex = segmentIndex;
        this.segmentPosition = pathSegmentPosition;
    }

    public int segmentIndex() {
        return segmentIndex;
    }

    public double segmentPosition() {
        return segmentPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PathPosition) obj;
        return this.segmentIndex == that.segmentIndex &&
                Double.doubleToLongBits(this.segmentPosition) == Double.doubleToLongBits(that.segmentPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentIndex, segmentPosition);
    }

    @Override
    public String toString() {
        return "PathPosition[" +
                "pathSegmentIndex=" + segmentIndex + ", " +
                "pathSegmentPosition=" + segmentPosition + ']';
    }

    public void advance(double speed) {
        if (atEnd) return;

        segmentPosition += speed;
        double segmentLength = path.getSegmentLength(segmentIndex);
        if (segmentPosition > segmentLength) {
            segmentPosition -= segmentLength;
            ++segmentIndex;
        }

        if (path.isAtEnd(this)) {
            atEnd = true;
        }
    }
}
