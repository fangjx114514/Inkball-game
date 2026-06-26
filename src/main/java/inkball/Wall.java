package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a wall on the game board
 * that can change ball colors.
 */
public class Wall extends Tile {
    private PImage image;
    private String color;

    /**
     * A new Wall with the specified parameters.
     *
     * @param x     x location.
     * @param y     y location.
     * @param image wall image.
     * @param color color code of the wall.
     */
    public Wall(int x, int y, PImage image, String color) {
        super(x, y);
        this.image = image;
        this.color = color;
    }

    public String get_color() {
        return color;
        
    }

    @Override
    public void draw(PApplet app, int yOffset) {
        int screenX = x * App.CELLSIZE;
        int screenY = y * App.CELLSIZE + yOffset;
        app.image(image, screenX, screenY, App.CELLSIZE, App.CELLSIZE);
    }
}