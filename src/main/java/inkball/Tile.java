package inkball;

import processing.core.PApplet;

/**
 * Abstract class for all tiles on the game board.
 */
public abstract class Tile {
    protected int x, y; // Grid coordinates in cells

    /**
     * Constructs a Tile with the specified coordinates.
     *
     * @param x     x location.
     * @param y     y location.
     */
    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Draws the tile on the screen.
     *
     * @param app     PApplet instance used for drawing.
     * @param yOffset vertical offset.
     */
    public abstract void draw(PApplet app, int yOffset);
}