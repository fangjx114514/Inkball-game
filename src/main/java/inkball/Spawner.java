package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Spawner on the game board, which spawn new balls.
 */
public class Spawner extends Tile {
    private PImage image;

    /**
     * Spawner with the specified parameters.
     *
     * @param x     x location.
     * @param y     y location.
     * @param image spawner's image.
     */
    public Spawner(int x, int y, PImage image) {
        super(x, y);
        this.image = image;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
    
    @Override
    public void draw(PApplet app, int yOffset) {
        int screenX = x * App.CELLSIZE;
        int screenY = y * App.CELLSIZE + yOffset; // Apply yOffset here
        app.image(image, screenX, screenY, App.CELLSIZE, App.CELLSIZE);
    }
}