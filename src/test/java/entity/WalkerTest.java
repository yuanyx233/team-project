package entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WalkerTest {
    private static final double EPS = 1e-6;
    private static final int T = Walker.TILE_SIZE;

    /** 10×6 tiles, doors on row 0, one wall at column 5, row 3. */
    private static final CampusMap MAP = CampusMap.parse(List.of(
            "RLXH......",
            "..........",
            "..........",
            ".....#....",
            "..........",
            ".........."));

    @Test
    void walksFourTilesPerSecond() {
        Walker walker = new Walker(2 * T, 2 * T);
        walker.move(1, 0, 0.5, MAP);
        assertEquals(2 * T + 2 * T, walker.getFootX(), EPS);
        assertEquals(2 * T, walker.getFootY(), EPS);
    }

    @Test
    void noInputDoesNotMove() {
        Walker walker = new Walker(2 * T, 2 * T);
        walker.move(0, 0, 1.0, MAP);
        assertEquals(2 * T, walker.getFootX(), EPS);
        assertEquals(2 * T, walker.getFootY(), EPS);
    }

    @Test
    void diagonalIsNoFasterThanStraight() {
        Walker walker = new Walker(2 * T, 2 * T);
        walker.move(1, 1, 0.25, MAP);
        double dx = walker.getFootX() - 2 * T;
        double dy = walker.getFootY() - 2 * T;
        assertEquals(Walker.SPEED * 0.25, Math.hypot(dx, dy), EPS);
        assertEquals(dx, dy, EPS);
    }

    @Test
    void stopsAtRightMapEdge() {
        Walker walker = new Walker(8 * T, 5 * T);
        walker.move(1, 0, 2.0, MAP);
        assertEquals(10 * T - Walker.HITBOX_WIDTH / 2.0, walker.getFootX(), EPS);
    }

    @Test
    void stopsAtLeftAndBottomMapEdges() {
        Walker walker = new Walker(T, 5 * T);
        walker.move(-1, 1, 2.0, MAP);
        assertEquals(Walker.HITBOX_WIDTH / 2.0, walker.getFootX(), EPS);
        assertEquals(6 * T, walker.getFootY(), EPS);
    }

    @Test
    void stopsAtTheEdgeOfAWall() {
        // Feet on row 3, left of the wall at column 5.
        Walker walker = new Walker(3 * T, 3 * T + 12);
        walker.move(1, 0, 1.0, MAP);
        assertEquals(5 * T - Walker.HITBOX_WIDTH / 2.0, walker.getFootX(), EPS);
    }

    @Test
    void stopsWhenWalkingUpIntoAWall() {
        // Below the wall at column 5, row 3.
        Walker walker = new Walker(5 * T + 8, 5 * T);
        walker.move(0, -1, 1.0, MAP);
        assertEquals(4 * T + Walker.HITBOX_HEIGHT, walker.getFootY(), EPS);
    }

    @Test
    void slidesAlongAWallOnTheFreeAxis() {
        Walker walker = new Walker(5 * T - Walker.HITBOX_WIDTH / 2.0, 3 * T + 12);
        walker.move(1, 1, 0.05, MAP);
        assertEquals(5 * T - Walker.HITBOX_WIDTH / 2.0, walker.getFootX(), EPS);
        assertTrue(walker.getFootY() > 3 * T + 12);
    }

    @Test
    void reportsTheDoorItOverlaps() {
        Walker walker = new Walker(1 * T + 8, 1 * T + 2);
        assertEquals(Building.LECTURE_HALL, walker.buildingAtDoor(MAP).orElseThrow());
    }

    @Test
    void reportsNoDoorAwayFromDoors() {
        Walker walker = new Walker(2 * T, 3 * T);
        assertTrue(walker.buildingAtDoor(MAP).isEmpty());
    }

    @Test
    void placedBelowADoorStandsOnTheTileBelowWithoutTouchingTheDoor() {
        Walker walker = new Walker(0, 0);
        walker.placeBelow(MAP.doorOf(Building.RESIDENCE));
        assertEquals(3 * T + T / 2.0, walker.getFootX(), EPS);
        assertTrue(walker.getFootY() - Walker.HITBOX_HEIGHT >= T);
        assertTrue(walker.getFootY() <= 2 * T);
        assertTrue(walker.buildingAtDoor(MAP).isEmpty());
    }
}
