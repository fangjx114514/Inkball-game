package inkball;

import java.util.*;
import processing.core.PApplet;
import processing.core.PImage;


/**
 * Ball in the game, including movement, wall collisions,
 * hole attraction, and capture logic.
 */
public class Ball extends Tile {

    private PImage image;
    public float posX, posY; // Position in pixels
    public float vx, vy; // Velocity
    private Random ran; // random object
    public int radius; // balls radius in pixel
    public boolean is_captured; // capture ball flag
    public float originalSize; // Original diameter
    public float currentSize;  // Current diameter
    private String color;
    public boolean correctCapture;
    public Hole collidedHole;
    public String correctColor;
    public String wrongColor;
    
    /**
     * Constructs a Ball with the specified parameters.
     *
     * @param x       x-coordinate.
     * @param y       y-coordinate.
     * @param image   ball image.
     * @param color   color code of the ball.
     * @param yOffset vertical offset.
     */
    public Ball(int x, int y, PImage image, String color, int yOffset) {
        super(x, y);
        this.image = image;
        this.posX = x * App.CELLSIZE; // Initialize position in pixels
        this.posY = y * App.CELLSIZE + yOffset;
        this.radius = 12;
        this.originalSize = 24; // Diameter
        this.currentSize = originalSize;
        this.is_captured = false;
        this.color = color;
        this.correctCapture = false;
        this.correctColor = null;
        this.wrongColor = null;

        // Initialize velocities
        int[] random_num = {-2, 2};
        this.ran = new Random(); //random objects
        int index_x = ran.nextInt(2);
        int index_y = ran.nextInt(2);
        this.vx = random_num[index_x];
        this.vy = random_num[index_y];
    }
    
    public String get_color() {
        return color;
    }

    public String getCorrectColor() {
        return this.correctColor;
    }

    public String get_wrongColor() {
        return this.wrongColor;
    }

    /**
     * Updates the ball's position, checks for 
     * collisions, and handles captures.
     *
     * @param grid   game grid.
     * @param yOffset offset.
     * @param app    main app instance.
     */
    public void update_position(Tile[][] grid, int yOffset, App app) {
        posX += vx;
        posY += vy;
        attraction_to_ball(grid, yOffset);
        check_collision(grid, yOffset, app);
        
        boolean is_captured = check_hole_collision(grid, yOffset);
        if (is_captured) {
            this.is_captured = true;
            if (this.color.equals(collidedHole.get_color())) { // ball and hole has same color, success
                this.correctCapture = true;

                if (this.color.equals("0")) {
                    this.correctColor = "grey";
                }
                else if (this.color.equals("1")) {
                    this.correctColor = "orange";
                }
                else if (this.color.equals("2")) {
                    this.correctColor = "blue";
                }
                else if (this.color.equals("3")) {
                    this.correctColor = "green";
                }
                else if (this.color.equals("4")) {
                    this.correctColor = "yellow";
                }

                // System.out.println("success catch with ball color char: ball" + this.color);
            }

            else if (!this.color.equals(collidedHole.get_color()) && collidedHole.get_color().equals("0")) { // balls goes to grey wall, success
                this.correctCapture = true;
                if (this.color.equals("0")) {
                    this.correctColor = "grey";
                }
                else if (this.color.equals("1")) {
                    this.correctColor = "orange";
                }
                else if (this.color.equals("2")) {
                    this.correctColor = "blue";
                }
                else if (this.color.equals("3")) {
                    this.correctColor = "green";
                }
                else if (this.color.equals("4")) {
                    this.correctColor = "yellow";
                }
                // System.out.println("success catch with ball color char: ball" + this.color + " goes to grey hole");
            }

            else if (this.color.equals("0")) { // grey ball goes to any hole rather than grey hole, success
                this.correctCapture = true;
                this.correctColor = "grey";
            
                // System.out.println("success catch with ball color char: ball" + this.color + " goes to hole " + holecolor);
            }

            else {
                this.correctCapture = false;
                if (this.color.equals("1")) {
                    this.wrongColor = "orange";
                }
                else if (this.color.equals("2")) {
                    this.wrongColor = "blue";
                }
                else if (this.color.equals("3")) {
                    this.wrongColor = "green";
                }
                else if (this.color.equals("4")) {
                    this.wrongColor = "yellow";
                }
                // System.out.println("unsuccess catch: ball" + this.wrongColor + " goes to the wrong hole");
            }
        }
    }

    private void check_collision(Tile[][] grid, int yOffset, App app) {
        Map<Wall, Float> distances = get_distance(grid, yOffset);

        for (Map.Entry<Wall, Float> entry : distances.entrySet()) {
            Wall wall = entry.getKey();
            float distance = entry.getValue();

            // The wall's center position
            float wallX = wall.x * App.CELLSIZE + App.CELLSIZE / 2f;
            float wallY = wall.y * App.CELLSIZE + App.CELLSIZE / 2f + yOffset;

            // Check for collision
            if (distance <= radius + App.CELLSIZE / 2f) {
                // Determine the side of collision
                float dx = posX - wallX;
                float dy = posY - wallY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    // Horizontal collision
                    vx = -vx;
                    // Adjust position to prevent sticking
                    if (dx > 0) {
                        posX = wallX + App.CELLSIZE / 2f + radius;
                    } else {
                        posX = wallX - App.CELLSIZE / 2f - radius;
                    }
                } else {
                    // Vertical collision
                    vy = -vy;
                    // Adjust position to prevent sticking
                    if (dy > 0) {
                        posY = wallY + App.CELLSIZE / 2f + radius;
                    } else {
                        posY = wallY - App.CELLSIZE / 2f - radius;
                    }
                }
                String wallColor = wall.get_color();
                if (!wallColor.equals("0")) {
                    // change the ball's color
                    String sprite_name = "ball" + wallColor;
                    PImage new_ball_sprite = app.getSprite(sprite_name);
                    this.image = new_ball_sprite;
                    this.color = wallColor;
                }
            }
        }
    }

    private HashMap<Wall, Float> get_distance(Tile[][] grid, int yOffset) {
        // Map to store distances to walls, keyed by the wall tile
        HashMap<Wall, Float> distances = new HashMap<>();

        // Convert ball's position to grid coordinates
        int gridX = (int) posX / App.CELLSIZE;
        int gridY = (int) (posY - yOffset) / App.CELLSIZE; // Adjust for yOffset

        // Define the range to check around the ball's current position
        int startX = Math.max(0, gridX - 1);
        int endX = Math.min(grid[0].length - 1, gridX + 1);
        int startY = Math.max(0, gridY - 1);
        int endY = Math.min(grid.length - 1, gridY + 1);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Tile tile = grid[y][x];
                if (tile instanceof Wall) {
                    Wall wall = (Wall) tile;
                    // Get the wall's position in pixels
                    float wallX = x * App.CELLSIZE;
                    float wallY = y * App.CELLSIZE + yOffset;
                    // Calculate the distance between the ball's center and the wall's center
                    float dx = posX - (wallX + App.CELLSIZE / 2f);// make sure float type
                    float dy = posY - (wallY + App.CELLSIZE / 2f);
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    distances.put(wall, distance);
                }
            }
        }
        return distances;
    }

    private boolean check_hole_collision(Tile[][] grid, int yOffset) {
        boolean sizeAdjusted = false;
        // Loop through the grid to find holes
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                Tile tile = grid[row][col];
                if (tile instanceof Hole) {
                    Hole hole = (Hole) tile;
                    
                    float holeCenterX = (hole.x + 1) * App.CELLSIZE;
                    float holeCenterY = (hole.y + 1) * App.CELLSIZE + yOffset;
                    
                    // Calculate distance between ball's center and hole's center
                    float dx = posX - holeCenterX;
                    float dy = posY - holeCenterY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    
                    
                    float catch_distance = this.radius - 4; // 8
                    // Check if the distance is less than or equal to the distance that the hole should catch
                    if (distance <= catch_distance) {
                        this.collidedHole = hole;
                        return true; // is catched
                    } else if (distance <= 24) {
                        // Adjust size based on distance
                        float minSize = 5; // Minimum size when at the hole's center
                        float sizeFactor = distance / 24; // Result of this variable should between 0 to 1
                        float newSize = minSize + (originalSize - minSize) * sizeFactor;
                        currentSize = newSize;
                        sizeAdjusted = true;
                    }
                }
            }
        }
        if (!sizeAdjusted) {
            // Reset size to original if ball is not close to any hole
            currentSize = originalSize;
        }
        return false; // No collision with any hole
    }

    private void attraction_to_ball(Tile[][] grid, int yOffset) {
        // Loop through the grid to find holes
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                Tile tile = grid[row][col];
                if (tile instanceof Hole) {
                    Hole hole = (Hole) tile;

                    // Calculate the vector from the ball to the hole
                    float holeCenterX = (hole.x + 1) * App.CELLSIZE; // thanks for ed 2646
                    float holeCenterY = (hole.y + 1) * App.CELLSIZE + yOffset;

                    float dx = holeCenterX - posX;
                    float dy = holeCenterY - posY;

                    // Calculate the distance between the ball and the hole
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    // Apply attraction only if within a certain range
                    if (distance <= 24) { 
                    
                        // Normalize the direction vector
                        float length = distance;
                        float nx = dx / length;
                        float ny = dy / length;

                        // Calculate the attraction force
                        float ax = nx * 0.15f;
                        float ay = ny * 0.15f;

                        // Apply the acceleration
                        vx += ax;
                        vy += ay;
                    }
                }
            }
        }
    }

    @Override
    public void draw(PApplet app, int yOffset) {
        float drawX = posX - currentSize / 2;
        float drawY = posY - currentSize / 2;
        app.image(image, drawX, drawY, currentSize, currentSize);
    }
}
