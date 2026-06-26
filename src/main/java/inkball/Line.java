package inkball;

import java.util.ArrayList;
import java.util.List;
import processing.core.PVector;

/**
 * line drawn by the player.
 */
public class Line {
    private List<PVector> points;

    /**
     * Constructs a new Line.
     */
    public Line() {
        points = new ArrayList<>();
    }

    /**
     * Adds a point to the line.
     *
     * @param x x location.
     * @param y y location.
     */
    public void addPoint(float x, float y) {
        points.add(new PVector(x, y));
    }

    /**
     * Gets the list of points in the line.
     *
     * @return A list of PVector points.
     */
    public List<PVector> getPoints() {
        return points;
    }
}
