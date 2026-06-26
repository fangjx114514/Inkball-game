package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.core.PVector;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.util.*;

/**
 * Main class for the Inkball game.
 * <p>
 * This class extends PApplet and handles the game setup, game loop, event handling, and rendering.
 * </p>
 */
public class App extends PApplet {
    public static final int CELLSIZE = 32;
    public static final int CELLHEIGHT = 32;
    public static final int CELLAVG = 32;
    public static final int TOPBAR = 0;
    public static int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;
    public static final int INITIAL_PARACHUTES = 1;
    public static final int FPS = 30;
    public static final float LEVEL_END_TICK = 0.067f;
    public String configPath;

    //new things added
    private HashMap<String, PImage> sprites = new HashMap<>();
    List<Level> levelList;
    Map<String, Integer> scoreIncreaseMap;
    Map<String, Integer> scoreDecreaseMap;
    public int currentLevelIndex = 0;
    private Tile[][] grid;
    public int yOffset = 2 * CELLSIZE;
    private Map<String, String> colorToSprite;
    private Map<String, String> spriteToColor;
    private Map<String, String> colorToNumber;
    public List<Spawner> spawners;
    public List<Ball> activeBalls;
    public boolean isPaused = false;
    public boolean levelEnded = false;
    public boolean gameEnded = false;
    private int pausedTime = 0;
    public static Random random = new Random();
    private LevelEndAnimation levelEndAnimation;
    private float levelEndTimeCounter = 0;


    // timer stuffs
    public float remaining_time; // time remaining in seconds
    public float remaining_time2; // interval
    public boolean timesUp; // Flag to indicate when time is up
    public float lastUpdateTime; // last update time in milliseconds
    public float lastSpawnUpdateTime;

    //lines stuff
    public List<Line> store_lines;
    public Line currentLine;
    public boolean dragging = false;


    //scores stuff
    public int currentScore = 0;


    /** 
     * Constructor called App.
     * Initializes the config file path.
     */
    public App() {
        this.configPath = "config.json";
        this.levelEndAnimation = new LevelEndAnimation(18);
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    
    /**
     * Get the sprite image for the given sprite name.
     * 
     * @param s Sprite name in string.
     * @return PImage associated with the sprite.
     */
    public PImage getSprite(String s) {
        PImage result = sprites.get(s);
        if (result == null) {
            try {
                result = loadImage(URLDecoder.decode(this.getClass().getResource(s+".png").getPath(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            sprites.put(s, result);
        }
        return result;
    }

    public int getScore() { // helper method for testing
        return currentScore;
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);

        // three hashmap to move around the name easily
        colorToSprite = new HashMap<>();
        colorToSprite.put("grey", "ball0");
        colorToSprite.put("orange", "ball1");
        colorToSprite.put("blue", "ball2");
        colorToSprite.put("green", "ball3");
        colorToSprite.put("yellow", "ball4");

        colorToNumber = new HashMap<>();
        colorToNumber.put("grey", "0");
        colorToNumber.put("orange", "1");
        colorToNumber.put("blue", "2");
        colorToNumber.put("green", "3");
        colorToNumber.put("yellow", "4");

        spriteToColor = new HashMap<>();
        spriteToColor.put("ball0", "grey");
        spriteToColor.put("ball1", "orange");
        spriteToColor.put("ball2", "blue");
        spriteToColor.put("ball3", "green");
        spriteToColor.put("ball4", "yellow");


        store_lines = new ArrayList<>();

        String[] sprites = new String[]{
            "tile",
            "wall0", "wall1", "wall2", "wall3", "wall4",
            "hole0", "hole1", "hole2", "hole3", "hole4",
            "ball0", "ball1", "ball2", "ball3", "ball4",
            "entrypoint",
        };

        for (int i = 0; i < sprites.length; i++) {
            getSprite(sprites[i]);
        }

        levelList = new ArrayList<>();
        scoreIncreaseMap = new HashMap<>();
        scoreDecreaseMap = new HashMap<>();
        spawners = new ArrayList<>();
        activeBalls = new ArrayList<>();
        levelEndAnimation.reset();
        levelEnded = false;
        gameEnded = false;
		JSONObject jsonFile = loadJSONObject(configPath);
        JSONArray levelsArray = jsonFile.getJSONArray("levels");

        for (int i = 0; i < levelsArray.size(); i++) {

            JSONObject levelObject = levelsArray.getJSONObject(i);

            String layout = levelObject.getString("layout");
            int time = levelObject.getInt("time");
            float spawn_interval = levelObject.getInt("spawn_interval");
            float scoreIncrease = levelObject.getFloat("score_increase_from_hole_capture_modifier");
            float scoreDecrease = levelObject.getFloat("score_decrease_from_wrong_hole_modifier");

            //read balls
            JSONArray balls = levelObject.getJSONArray("balls");
            ArrayList<String> ballsList = new ArrayList<String>();
            for (int n = 0; n < balls.size(); n++){
                ballsList.add(balls.getString(n));
            }
            
            Level level = new Level(layout, time, spawn_interval, scoreIncrease, scoreDecrease, ballsList);
            levelList.add(level); // all level object stored here
        }

        Level currentLevel = levelList.get(currentLevelIndex);
        currentLevel.mapGrid = loadMapGrid(currentLevel.layout);

        collectInitialBalls(currentLevel.mapGrid);

        JSONObject scoreIncreaseObj = jsonFile.getJSONObject("score_increase_from_hole_capture");
        for (Object keyObj : scoreIncreaseObj.keys()) {
            String key = (String) keyObj;
            int value = scoreIncreaseObj.getInt(key);
            scoreIncreaseMap.put(key, value);
        }
        // {orange=50, green=50, blue=50, yellow=100, grey=70}

        JSONObject scoreDecreaseObj = jsonFile.getJSONObject("score_decrease_from_wrong_hole");
        for (Object keyObj : scoreDecreaseObj.keys()) {
            String key = (String) keyObj;
            int value = scoreDecreaseObj.getInt(key);
            scoreDecreaseMap.put(key, value);
        }
        // {orange=25, green=25, blue=25, yellow=100, grey=0}

        //setup timer
        remaining_time = currentLevel.time;
        remaining_time2 = currentLevel.spawn_interval;
        timesUp = false;
        lastUpdateTime = millis();
        lastSpawnUpdateTime = millis();
        collectSpawners(currentLevel.mapGrid);

    }
    
    private void collectSpawners(Tile[][] board) {
        spawners.clear(); // Clear any existing spawners
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Tile tile = board[row][col];
                if (tile instanceof Spawner) {
                    spawners.add((Spawner) tile);
                }
            }
        }
    }

    private void spawnNextBall() {

        Level currentLevel = levelList.get(currentLevelIndex);
        List<String> ballsList = currentLevel.ballsList;
        // Get the next ball color and remove it from the list
        if (!ballsList.isEmpty() && !spawners.isEmpty()) {
            String colorName = ballsList.remove(0);
            String spriteName = colorToSprite.get(colorName);
            String colorNumber = colorToNumber.get(colorName);

            if (spriteName != null) {
                PImage ballImage = getSprite(spriteName);
                Spawner spawner = spawners.get(random.nextInt(spawners.size())); // randomly select a spawner
                int spawnX = spawner.getX(); // getter method in spawner class to get x and y
                int spawnY = spawner.getY();
                Ball newBall = new Ball(spawnX, spawnY, ballImage, colorNumber, yOffset); // Create a ball at the spawner's position
                activeBalls.add(newBall);
            }
        }
    }

    
    public Tile[][] loadMapGrid(String layoutFilename) {

        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(layoutFilename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int gridRows = 18; // size
        int gridCols = 18;
    
        // initialize the grid with empty spaces
        Tile[][] grid = new Tile[gridRows][gridCols];
        // add grey wall around the grid borders
        for (int row = 0; row < gridRows; row++) {
            grid[row][0] = new Wall(0, row, getSprite("wall0"), "0"); // Left wall
            grid[row][gridCols - 1] = new Wall(gridCols - 1, row, getSprite("wall0"), "0"); // Right wall
        }

        for (int col = 0; col < gridCols; col++) {
            grid[0][col] = new Wall(col, 0, getSprite("wall0"), "0"); // Top wall
            grid[gridRows - 1][col] = new Wall(col, gridRows - 1, getSprite("wall0"), "0"); // Bottom wall
        }

        for (int row = 0; row < lines.size() && row < gridRows; row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length() && col < gridCols; col++) {
                char tileChar = line.charAt(col);
                int x = col;
                int y = row;

                if (tileChar == 'X') {
                    grid[y][x] = new Wall(x, y, getSprite("wall0"), "0");
                } else if (tileChar == '1') {
                    grid[y][x] = new Wall(x, y, getSprite("wall1"), "1");
                } else if (tileChar == '2') {
                    grid[y][x] = new Wall(x, y, getSprite("wall2"), "2");
                } else if (tileChar == '3') {
                    grid[y][x] = new Wall(x, y, getSprite("wall3"), "3");
                } else if (tileChar == '4') {
                    grid[y][x] = new Wall(x, y, getSprite("wall4"), "4");
                } else if (tileChar == 'S') {
                    grid[y][x] = new Spawner(x, y, getSprite("entrypoint"));
                } else if (tileChar == 'H') {
                    if (col + 1 < line.length()) {
                        char holeColorChar = line.charAt(col + 1);
                        String holeSpriteName = "hole" + holeColorChar;
                        Hole hole = new Hole(x, y, Character.toString(holeColorChar), getSprite(holeSpriteName));

                        grid[y][x] = hole;
                        if (x + 1 < gridCols) {
                            grid[y][x + 1] = hole;
                        }
                        if (y + 1 < gridRows) {
                            grid[y + 1][x] = hole;
                            if (x + 1 < gridCols) {
                                grid[y + 1][x + 1] = hole;
                            }
                        }
                        col++;
                    }
                } else if (tileChar == 'B') {
                    if (col + 1 < line.length()) {
                        char ballColorChar = line.charAt(col + 1);
                        String ballSpriteName = "ball" + ballColorChar;
                        Ball newBall = new Ball(x, y, getSprite(ballSpriteName),Character.toString(ballColorChar), yOffset);
                        grid[y][x] = newBall;
                        col++;
                    }
                }
            }
        }
        return grid;
    }
    
    private void collectInitialBalls(Tile[][] grid) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                Tile tile = grid[row][col];
                if (tile instanceof Ball) {
                    Ball ball = (Ball) tile;
                    activeBalls.add(ball);
                }
            }
        }
    }
    
    /**
     * Starts a new level with the given level index.
     * 
     * @param levelIndex represent the index of next level to start.
     */
    public void startNewLevel(int levelIndex) {
        currentLevelIndex = levelIndex;
        Level currentLevel = levelList.get(currentLevelIndex);
        spawners.clear();
        activeBalls.clear();
        store_lines.clear();
        levelEnded = false;
        gameEnded = false;
        levelEndTimeCounter = 0;
        levelEndAnimation.reset();

        currentLevel.mapGrid = loadMapGrid(currentLevel.layout);
        remaining_time = currentLevel.time;
        remaining_time2 = currentLevel.spawn_interval;
        
        collectInitialBalls(currentLevel.mapGrid);
        collectSpawners(currentLevel.mapGrid);
        if (levelIndex == 0) {
            currentScore = 0;
        }
    }

    /**
     * Starts level end bonus.
     */
    public void startLevelEnd() {
        levelEnded = true;
        remaining_time2 = 0;
        levelEndTimeCounter = 0;
        levelEndAnimation.reset();
        if (remaining_time <= 0) {
            finishLevelEnd();
        }
    }

    /**
     * Updates level end bonus.
     *
     * @param timePassed seconds passed.
     */
    public void updateLevelEnd(float timePassed) {
        if (!levelEnded) {
            return;
        }

        if (remaining_time <= 0) {
            finishLevelEnd();
            return;
        }

        levelEndTimeCounter += timePassed;
        while (levelEndTimeCounter >= LEVEL_END_TICK && levelEnded) {
            levelEndTimeCounter -= LEVEL_END_TICK;
            currentScore++;
            remaining_time -= 1;
            if (remaining_time < 0) {
                remaining_time = 0;
            }
            levelEndAnimation.advance();
            if (remaining_time <= 0) {
                finishLevelEnd();
            }
        }
    }

    /**
     * Draws level end tiles.
     */
    public void drawLevelEndTiles() {
        if (levelEnded) {
            levelEndAnimation.draw(this, getSprite("wall4"), yOffset);
        }
    }

    /**
     * Finishes level end bonus.
     */
    private void finishLevelEnd() {
        levelEnded = false;
        levelEndTimeCounter = 0;
        remaining_time = 0;
        currentLevelIndex++;
        if (currentLevelIndex < levelList.size()) {
            startNewLevel(currentLevelIndex);
        } else {
            gameEnded = true;
            noLoop();
        }
    }

    /**
     * Starts a new line with hitboxes added.
     * 
     * @param x x coordinate where the line starts.
     * @param y x coordinate where the line starts.
     */
    public void start_new_line(float x, float y) {
        currentLine = new Line();
        add_hitbox(x, y);
    }

    /**
     * Adds a point to the current line being drawn.
     * 
     * @param x y coordinate to add point.
     * @param y y coordinate to add point.
     */
    public void add_hitbox(float x, float y) {
        if (currentLine != null) {
            if (y < yOffset) { // stop drawing at top bar
                return;
            }
            float y_with_offset = y - yOffset;
            currentLine.addPoint(x, y_with_offset);
        }
    }

    /**
     * Removes a line if the mouse is close to it 
     * (associate with mouse event e).
     * 
     * @param x x-coordinate of the mouse position.
     * @param y y-coordinate of the mouse position.
     */
    public void remove_line(float x, float y) {
        float adjustedY = y - yOffset;
        PVector mousePos = new PVector(x, adjustedY);

        for (int i = 0; i < store_lines.size(); i++) {
            Line line = store_lines.get(i);
            List<PVector> points = line.getPoints();

            // Check if the mouse is close to any point in the line
            for (PVector point : points) {
                if (mousePos.dist(point) <= 10) {
                    // Mouse is close enough to a point in this line; remove the line
                    store_lines.remove(i);
                    return; //exit the method
                }
            }
        }
    }

    /**
     * Finishes the current line being drawn and adds it to 
     * the store_lines list so that balls can collide with
     * it.
     */
    public void finishLine() {
        if (currentLine != null) {
            store_lines.add(currentLine); // make sure line stay on the board
            currentLine = null;
        }
    }

    /**
     * Draw line on the screen.
     * 
     * @param l line to draw.
     */
    public void drawPlayerLine(Line l) {
        List<PVector> points = l.getPoints();
        if (points.size() > 1) {
            for (int i = 0; i < points.size() - 1; i++) {
                PVector p1 = points.get(i);
                PVector p2 = points.get(i + 1);
                // adjust for yOffset when drawing
                float y1 = p1.y + yOffset;
                float y2 = p2.y + yOffset;
                line(p1.x, y1, p2.x, y2);
            }
        }
    }

    /**
     * Checks collisions between ball and lines.
     * 
     * @param ball ball object to check.
     */
    public void checkLineCollisions(Ball ball) {
        // Adjust the ball's future position based on its velocity
        float futureX = ball.posX + ball.vx;
        float futureY = ball.posY + ball.vy;
        Line lineToRemove = null;

        for (Line line : store_lines) {
            List<PVector> points = line.getPoints();

            // Check each segment of the line
            for (int i = 0; i < points.size() - 1; i++) {
                PVector p1 = points.get(i);
                PVector p2 = points.get(i + 1);

                // Adjust points for yOffset
                float x1 = p1.x;
                float y1 = p1.y + yOffset;
                float x2 = p2.x;
                float y2 = p2.y + yOffset;

                // Check for collision with the segment
                if (isCollideWithLine(futureX, futureY, ball.radius, x1, y1, x2, y2)) {
                    // Handle collision
                    handleCollisionWithLine(ball, x1, y1, x2, y2);
                    lineToRemove = line;
                    break; // Exit after handling one collision
                }
            }
            if (lineToRemove != null) {
                break; // Exit the outer loop
            }
        }
        // Remove the line outside of the loop to avoid modifying the collection during iteration
        if (lineToRemove != null) {
            store_lines.remove(lineToRemove);
        }
    }
    
    /**
     * Checks if a ball collides with a line based on 
     * the distance of ball's center and lines.
     * 
     * @param ballX x-coordinate of the ball.
     * @param ballY y-coordinate of the ball.
     * @param radius ball's radius.
     * @param x1 x-coordinate of the first point.
     * @param y1 y-coordinate of the first point.
     * @param x2 x-coordinate of the second point.
     * @param y2 y-coordinate of the second point.
     * @return return true only if the ball collides with the line.
     */
    public boolean isCollideWithLine(float ballX, float ballY, float radius, float x1, float y1, float x2, float y2) {
        // Calculate distances
        float distanceP1ToBall = dist(x1, y1, ballX, ballY);
        float distanceP2ToBall = dist(x2, y2, ballX, ballY);
        float distanceP1ToP2 = dist(x1, y1, x2, y2);

        // Check if the sum of distances is less than the line segment length plus radius
        if (distanceP1ToBall + distanceP2ToBall <= distanceP1ToP2 + radius) {
            return true; // collision detected
        } else {
            return false; // no collision
        }
    }

    /**
     * Handles the collision between ball and line 
     * by reflecting the ball's velocity.
     * 
     * @param ball The ball involved in the collision.
     * @param x1 x-coordinate of the first point.
     * @param y1 y-coordinate of the first point.
     * @param x2 x-coordinate of the second point.
     * @param y2 y-coordinate of the second point.
     */
    public void handleCollisionWithLine(Ball ball, float x1, float y1, float x2, float y2) {
        // Calculate the direction vector of the line segment
        float dx = x2 - x1;
        float dy = y2 - y1;

        // Calculate the two normal vectors
        PVector normal1 = new PVector(-dy, dx);
        PVector normal2 = new PVector(dy, -dx);

        // normalize
        normal1.normalize(); // ed 2150
        normal2.normalize();

        // Determine which normal to use (closer to the ball)
        PVector midpoint = new PVector((x1 + x2) / 2, (y1 + y2) / 2);
        PVector testPoint1 = PVector.add(midpoint, normal1);
        PVector testPoint2 = PVector.add(midpoint, normal2);

        float distance1 = dist(ball.posX, ball.posY, testPoint1.x, testPoint1.y);
        float distance2 = dist(ball.posX, ball.posY, testPoint2.x, testPoint2.y);

        PVector normal;
        if (distance1 < distance2) {
            normal = normal1;
        } else {
            normal = normal2;
        }

        // Reflect the ball's velocity vector over the normal vector
        PVector velocity = new PVector(ball.vx, ball.vy);

        float dotProduct = velocity.dot(normal);
        PVector reflection = PVector.sub(velocity, PVector.mult(normal, 2 * dotProduct));

        // Update the ball's velocity
        ball.vx = reflection.x;
        ball.vy = reflection.y;
    }

    private void calculate_score(Ball ball) {
        Level currentLevel = levelList.get(currentLevelIndex);
        float scoreModifier;

        if (ball.correctCapture) {
            String color = ball.getCorrectColor(); // get the color string
            float baseScoreIncrease = scoreIncreaseMap.get(color);
            scoreModifier = currentLevel.scoreIncrease;
            float scoreIncrease = Math.round(baseScoreIncrease * scoreModifier);
            currentScore += scoreIncrease; // update the current score
        } else {
            String color = ball.get_wrongColor();
            float baseScoreDecrease = scoreDecreaseMap.get(color); 
            scoreModifier = currentLevel.scoreDecrease;
            float scoreDecrease = Math.round(baseScoreDecrease * scoreModifier);
            currentScore -= scoreDecrease;
            currentLevel.ballsList.add(color); // put it back to queue
        }
    }

    /**
     * Restarts the game by reset all the flags,
     * variables and start at first level with 
     * calling startNewLevel.
     */
    public void restartGame() {

        currentLevelIndex = 0;
        currentScore = 0;
        timesUp = false;
        isPaused = false;
        levelEnded = false;
        gameEnded = false;
        levelEndTimeCounter = 0;
        levelEndAnimation.reset();
        lastUpdateTime = millis();
        lastSpawnUpdateTime = millis();
        activeBalls.clear();
        store_lines.clear();
        for (Level level : levelList) {
            level.resetBallsList();
        }
        startNewLevel(currentLevelIndex);
        loop();
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (event.getKey() == ' ') {
            isPaused = !isPaused; 
            if (isPaused) {
                pausedTime = millis(); 
            } else {
                // adjust the timers based on the time spent paused
                int timeSpentPaused = millis() - pausedTime;
                lastUpdateTime += timeSpentPaused;
                lastSpawnUpdateTime += timeSpentPaused;
            }
        }

        if (event.getKey() == 'r' || event.getKey() == 'R') {
            restartGame();
        }
    }

    /**
     * Receive key released signal from the keyboard.
     * (not used)
     */
	@Override
    public void keyReleased(){
        
    }

    /**
     * mouse pressed events.
     * 
     * @param e mouse event.
     */
    @Override
    public void mousePressed(MouseEvent e) {

        if (mouseY < yOffset) {
            // Ignore mouse dragging in the top bar area
            return;
        }

        if (e.getButton() == LEFT) {
            dragging = false; // reset the flag when mouse is pressed
        } else if (e.getButton() == RIGHT) {
            remove_line(mouseX, mouseY);
        }
        
    }
	
    /**
     * mouse dragged events used for drawing lines.
     * 
     * @param e mouse event.
     */
	@Override
    public void mouseDragged(MouseEvent e) {
        // add line segments to player-drawn line object if left mouse button is held
        if (mouseY < yOffset) {
            if (dragging) {
                finishLine();
                dragging = false;
            }
        }

		if (mouseButton == LEFT) {
            if (!dragging) {
                // start a new line
                start_new_line(mouseX, mouseY);
                dragging = true;
            } else { // mouse is already dragging
                add_hitbox(mouseX, mouseY);//add hitbox
            }
        } 
    }

    /**
     * mouse released events.
     * 
     * @param e mouse event.
     */
    @Override
    public void mouseReleased(MouseEvent e) {

        if (mouseY < yOffset) {
            // ignore mouse releases in the top bar area
            return;
        }

		if (e.getButton() == LEFT) {
            if (dragging) {
                finishLine();
                dragging = false; // Reset the flag
            } else { // when mouse was clicked but not dragging
                currentLine = null; // make sure currentLine is null
            }
        }
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {

        background(200,200,200);
        fill(0);
        rect(15, 15, 150, 35);

        if (gameEnded || currentLevelIndex >= levelList.size()) {
            textSize(23);
            text("=== ENDED ===", 200, 35);
            noLoop();
            return;
        }

        Level currentLevel = levelList.get(currentLevelIndex); // current level index
        List<String> ballsList = currentLevel.ballsList;
        int ballsToDraw = Math.min(5, ballsList.size());

        Tile[][] board = currentLevel.mapGrid;

        // go through first five balls and draw them
        for (int i = 0; i < ballsToDraw; i++) {
            String colorName = ballsList.get(i); // THIS LINE HAS INDEX OUT OF BOUND
            String spriteName = colorToSprite.get(colorName);

            if (spriteName != null) {
                PImage ballImage = getSprite(spriteName);
                float xPosition = 15 + 4 + i * (24 + 6);
                // a is rectangle x, b represents initial interval, i represents index,
                // 24 is balls diameter and 6 is interval between balls
                float yPosition = 15 + 11 / 2.0f;
                // Center the ball vertically in the rectangle
                image(ballImage, xPosition, yPosition, 24, 24);
            }
        }

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                int x = col * CELLSIZE;
                int y = row * CELLSIZE + yOffset;
                image(getSprite("tile"), x, y, CELLSIZE, CELLSIZE);

                Tile tile = board[row][col];
                if (tile != null && !(tile instanceof Ball)) {
                    tile.draw(this, yOffset);
                }
            }
        }

        if (!isPaused) {
            int currentTime = millis();
            float time_passed = (currentTime - lastUpdateTime) / 1000.0f; // Time since last frame in seconds

            if (levelEnded) {
                updateLevelEnd(time_passed);
                lastUpdateTime = currentTime;
            } else {
                remaining_time -= time_passed; // Decrease remaining game time
                if (remaining_time <= 0) {
                    remaining_time = 0;
                    timesUp = true;
                }

                // Update the spawn timer based on time passed since last frame
                remaining_time2 -= time_passed; // Decrease time until next spawn
                if (remaining_time2 <= 0.0f) {
                    if (!currentLevel.ballsList.isEmpty()) {
                        spawnNextBall(); // Spawn a new ball
                        remaining_time2 += currentLevel.spawn_interval; // reset
                    } else {
                        // no more balls to spawn, spawn_interval stop at 0
                        remaining_time2 = 0;
                    }
                }

                lastUpdateTime = currentTime; // Update last update time  

                for (int i = 0; i < activeBalls.size(); i++) { // Update and draw active balls
                    Ball ball = activeBalls.get(i);
                    if (ball.is_captured) {
                        activeBalls.remove(i);
                        calculate_score(ball);
                        i--; // Adjust index after removal
                    } else {
                        ball.update_position(board, yOffset, this);
                        checkLineCollisions(ball);
                        ball.draw(this, yOffset);
                    }
                }
            }
        }

        else {
            textSize(23);
            text("*** PAUSED ***", 200, 35);
            for (Ball ball : activeBalls) {
                ball.draw(this, yOffset);
            }
        }          

        strokeWeight(10);
        for (Line l : store_lines) {
            drawPlayerLine(l);
        }

        // Draw the current line being drawn
        if (currentLine != null) { // make sure line is shown on time
            drawPlayerLine(currentLine);
        }

        if (!levelEnded && !gameEnded && !timesUp && activeBalls.isEmpty() && currentLevel.ballsList.isEmpty()) {
            startLevelEnd();
        }

        drawLevelEndTiles();

        textSize(20);
        text("Time: " + (int)remaining_time, 450, 23);
        text("Score: " + currentScore, 450, 43);
        textSize(17);
        String formattedTime = String.format("%.1f", remaining_time2);
        text(formattedTime, 175, 50);

		
        if (timesUp) {
            textSize(23);
            text("=== TIME'S UP ===", 200, 35);
            noLoop();
            return;
        }

        if (gameEnded) {
            textSize(23);
            text("=== ENDED ===", 200, 35);
            noLoop();
            return;
        }

    }

    /**
     * This is main method to start the inkball app.
     * 
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
