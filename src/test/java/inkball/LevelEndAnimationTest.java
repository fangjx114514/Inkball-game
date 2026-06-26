package inkball;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LevelEndAnimationTest {

    @Test
    public void testStartsAtCorners() {
        LevelEndAnimation animation = new LevelEndAnimation(18);

        assertArrayEquals(new int[] {0, 0}, animation.getFirstPosition(), "first tile starts top left.");
        assertArrayEquals(new int[] {17, 17}, animation.getSecondPosition(), "second tile starts bottom right.");
    }

    @Test
    public void testMovesClockwise() {
        LevelEndAnimation animation = new LevelEndAnimation(18);

        animation.advance();

        assertArrayEquals(new int[] {1, 0}, animation.getFirstPosition(), "first tile moves right.");
        assertArrayEquals(new int[] {16, 17}, animation.getSecondPosition(), "second tile moves left.");
    }
}
