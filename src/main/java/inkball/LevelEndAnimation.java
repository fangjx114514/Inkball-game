package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Moves yellow tiles at level end.
 */
public class LevelEndAnimation {
    private int boardSize;
    private int pathLength;
    private int firstIndex;
    private int secondIndex;

    /**
     * Creates the edge animation.
     *
     * @param boardSize board width in tiles.
     */
    public LevelEndAnimation(int boardSize) {
        this.boardSize = boardSize;
        this.pathLength = 4 * (boardSize - 1);
        reset();
    }

    /**
     * Resets both yellow tiles.
     */
    public void reset() {
        firstIndex = 0;
        secondIndex = 2 * (boardSize - 1);
    }

    /**
     * Moves both yellow tiles.
     */
    public void advance() {
        firstIndex = (firstIndex + 1) % pathLength;
        secondIndex = (secondIndex + 1) % pathLength;
    }

    /**
     * Gets first tile position.
     *
     * @return first tile x and y.
     */
    public int[] getFirstPosition() {
        return getPosition(firstIndex);
    }

    /**
     * Gets second tile position.
     *
     * @return second tile x and y.
     */
    public int[] getSecondPosition() {
        return getPosition(secondIndex);
    }

    /**
     * Draws the yellow tiles.
     *
     * @param app main app.
     * @param image yellow tile image.
     * @param yOffset board y offset.
     */
    public void draw(PApplet app, PImage image, int yOffset) {
        drawTile(app, image, getFirstPosition(), yOffset);
        drawTile(app, image, getSecondPosition(), yOffset);
    }

    private void drawTile(PApplet app, PImage image, int[] position, int yOffset) {
        int x = position[0] * App.CELLSIZE;
        int y = position[1] * App.CELLSIZE + yOffset;
        app.image(image, x, y, App.CELLSIZE, App.CELLSIZE);
    }

    private int[] getPosition(int index) {
        if (index < boardSize) {
            return new int[] {index, 0};
        }

        index -= boardSize;
        if (index < boardSize - 1) {
            return new int[] {boardSize - 1, index + 1};
        }

        index -= boardSize - 1;
        if (index < boardSize - 1) {
            return new int[] {boardSize - 2 - index, boardSize - 1};
        }

        index -= boardSize - 1;
        return new int[] {0, boardSize - 2 - index};
    }
}
