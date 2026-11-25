package view.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalkAnimationTest {
    @Test
    void startsStandingFacingDown() {
        WalkAnimation walk = new WalkAnimation();
        assertEquals(CharacterSheet.Direction.DOWN, walk.getFacing());
        assertEquals(0, walk.getFrame());
    }

    @Test
    void walkingRightFacesRightAndAdvances() {
        WalkAnimation walk = new WalkAnimation();
        walk.update(1, 0, 0.25);
        assertEquals(CharacterSheet.Direction.RIGHT, walk.getFacing());
        assertTrue(walk.getFrame() > 0);
    }

    @Test
    void cycleRunsAtAboutTenFramesPerSecond() {
        WalkAnimation walk = new WalkAnimation();
        walk.update(0, 1, 0.35);
        assertEquals(3, walk.getFrame());
        walk.update(0, 1, 0.6);
        assertEquals((int) (0.95 * 10) % CharacterSheet.FRAMES, walk.getFrame());
    }

    @Test
    void stoppingShowsStandingFrameAndKeepsFacing() {
        WalkAnimation walk = new WalkAnimation();
        walk.update(0, -1, 0.3);
        walk.update(0, 0, 0.1);
        assertEquals(CharacterSheet.Direction.UP, walk.getFacing());
        assertEquals(0, walk.getFrame());
        assertFalse(walk.isMoving());
    }

    @Test
    void horizontalWinsWhenMovingDiagonally() {
        WalkAnimation walk = new WalkAnimation();
        walk.update(-1, 1, 0.1);
        assertEquals(CharacterSheet.Direction.LEFT, walk.getFacing());
    }
}
