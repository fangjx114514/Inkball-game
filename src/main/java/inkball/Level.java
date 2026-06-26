package inkball;

import java.util.List;
import java.util.ArrayList;

/**
 * Stores level scoring, timer, layout, and spawn data
 * read from the JSON configuration file.
 */
public class Level {
        String layout;
        int time;
        float spawn_interval;
        float scoreIncrease;
        float scoreDecrease;
        List<String> ballsList;
        Tile[][] mapGrid;
        List<String> initialBallsList;

        /**
         * Level with the specified parameters.
         *
         * @param layout        filename of the level layout.
         * @param time          time limit for.
         * @param spawn_interval spawn interval for balls.
         * @param scoreIncrease score increase modifier.
         * @param scoreDecrease score decrease modifier.
         * @param ballsList     list of balls to spawn for current level.
         */
        public Level(String layout, int time, float spawn_interval, float scoreIncrease, float scoreDecrease, List<String> ballsList) {
            this.layout = layout;
            this.time = time;
            this.spawn_interval = spawn_interval;
            this.scoreIncrease = scoreIncrease;
            this.scoreDecrease = scoreDecrease;
            this.mapGrid = null;
            this.ballsList = new ArrayList<>(ballsList);
            this.initialBallsList = new ArrayList<>(ballsList);
        }

        /**
         * reset balls spawn list queue
         */
        public void resetBallsList() {
            this.ballsList = new ArrayList<>(initialBallsList);
        }

    }
