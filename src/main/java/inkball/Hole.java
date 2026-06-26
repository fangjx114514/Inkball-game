package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Hole on the game board and balls can be captured.
 */
public class Hole extends Tile {
    private PImage image;
    private String color;

    /**
     * Constructs a new Hole with the specified parameters.
     *
     * @param x     x-coordinate.
     * @param y     y-coordinate.
     * @param color color code of the hole.
     * @param image hole image.
     */
    public Hole(int x, int y, String color, PImage image) {
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
        app.image(image, screenX, screenY, App.CELLSIZE * 2, App.CELLSIZE * 2); // A hole has 2x2 area
    }
}