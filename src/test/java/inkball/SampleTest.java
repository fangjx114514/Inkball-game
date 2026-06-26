package inkball;

import processing.core.PVector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import processing.event.KeyEvent;

public class SampleTest {


    static App app;

    @BeforeAll
    public static void setup() {
        // System.setProperty("java.awt.headless", "true");
        app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.noLoop(); 
        app.delay(1000);
    }

    @Test
    public void testInitialGameState() { // Testing the initial game state
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);
    
        assertNotNull(app, "successfully created game."); 
        assertEquals(0, app.getScore(), "Initial score should be 0.");
    }

    @Test
    public void testStartNewLevel() { // testing startNewLevel in app
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);

        app.startNewLevel(0);
        assertEquals(0, app.getScore(), "initial score is 0");

        // check the current level index is set correctly
        assertEquals(0, app.currentLevelIndex, "Current level index should be set to 0.");
        // check the spawners and active_balls are correct
        assertNotNull(app.spawners, "Spawners should be collected when starting a new level.");
        assertFalse(app.activeBalls.isEmpty(), "Active balls should be collected when starting a new level.");
        // Testing load map grip
        assertNotNull(app.levelList.get(0).mapGrid, "Map grid should be loaded for the new level.");
    }
    
    @Test
    public void testScoreResetOnNewLevel() { // test score if level changed
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);

        app.currentScore = 100;  // set score for testing
        app.startNewLevel(0);    // restart at first level, which should reset score
        assertEquals(0, app.getScore(), "Score should be reset when starting at first level.");

        app.currentScore = 100;
        app.startNewLevel(1);
        assertEquals(100, app.getScore(), "Score should be carried over to the next level.");
    }

    @Test
    public void testSprite() { // test if load sprite correctly
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);
        // loading a sprite
        assertNotNull(app.getSprite("ball0"), "Known sprite should be loaded successfully.");

        // if loading an unknown sprite and expect an RuntimeException
        assertThrows(RuntimeException.class, () -> app.getSprite("unknown"), "Should throw an exception for unknown sprites.");
    }

    @Test
    public void testStartNewLine() { //testing startNewLine method
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);
        float startX = 100.0f;
        float startY = 100.0f;

        app.start_new_line(startX, startY); // call the method with input
        
        assertNotNull(app.currentLine, "A new line should be created.");
        assertFalse(app.currentLine.getPoints().isEmpty(), "Line should have at least one point.");

        PVector point = app.currentLine.getPoints().get(0);
        assertEquals(startX, point.x, "first point should match the input.");
    }

    @Test
    public void testTimeDecrease() { // timer should keep count down when calling draw method
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);
        app.remaining_time = 10;
        int initialTime = app.millis();
        app.draw();  // Simulate draw
        int afterDrawTime = app.millis();
        assertTrue(app.remaining_time < 10, "Remaining time should decrease.");
    }

    @Test
    public void testTimesUP() { // times up should be included
        app.remaining_time = 0.1f;
        app.draw();
        assertTrue(app.timesUp, "timesUp flag should be true.");
    }

    @Test
    public void testLevelTransition() { 
        // level end before next

        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);

        app.currentLevelIndex = 0; // set current level be 0
        app.activeBalls.clear();  // clear active balls manually
        app.levelList.get(0).ballsList.clear();  // clear ballList manually
        app.remaining_time = 2;
        app.lastUpdateTime = app.millis();
        app.draw();  // after no balls, start level end
        assertTrue(app.levelEnded, "level end bonus should start.");
        assertEquals(0, app.currentLevelIndex, "level should not change immediately.");

        app.updateLevelEnd(App.LEVEL_END_TICK);
        assertEquals(1, app.getScore(), "score should increase by remaining time.");

        app.updateLevelEnd(App.LEVEL_END_TICK);
        assertEquals(1, app.currentLevelIndex, "should go to the next level when conditions are met.");
        assertFalse(app.levelEnded, "level end bonus should finish.");
    }

    @Test
    public void testGameEndAfterBonus() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000);

        app.noLoop();
        app.currentLevelIndex = app.levelList.size() - 1;
        app.remaining_time = 1;
        app.startLevelEnd();

        assertTrue(app.levelEnded, "level end bonus should start.");
        app.updateLevelEnd(App.LEVEL_END_TICK);
        assertTrue(app.gameEnded, "game should end after final bonus.");
    }



    @Test
    public void simpleTest() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000); // delay is to give time to initialise stuff before drawing begins
    }
}

// gradle run						Run the program
// gradle test						Run the testcases

// Please ensure you leave comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report: 
// gradle test jacocoTestReport
