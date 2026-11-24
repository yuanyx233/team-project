package entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampusMapTest {
    private static final List<String> VALID = List.of(
            "##########",
            "#RL#XH...#",
            "#........#",
            "##########");

    @Test
    void validLayoutExposesSize() {
        CampusMap map = CampusMap.parse(VALID);
        assertEquals(10, map.getWidth());
        assertEquals(4, map.getHeight());
    }

    @Test
    void wallsAndTreesAreSolidGroundAndDoorsAreNot() {
        CampusMap map = CampusMap.parse(List.of(
                "#T.-=R",
                "....LX",
                "H.....",
                "......"));
        assertTrue(map.isSolid(0, 0));
        assertTrue(map.isSolid(1, 0));
        assertFalse(map.isSolid(2, 0));
        assertFalse(map.isSolid(3, 0));
        assertFalse(map.isSolid(4, 0));
        assertFalse(map.isSolid(5, 0));
    }

    @Test
    void furnitureIsSolidAndPlazaIsNot() {
        CampusMap map = CampusMap.parse(List.of(
                "op#RLXH",
                "......."));
        assertTrue(map.isSolid(0, 0));
        assertFalse(map.isSolid(1, 0));
        assertEquals(CampusMap.FURNITURE, map.tileAt(0, 0));
        assertEquals(CampusMap.PLAZA, map.tileAt(1, 0));
    }

    @Test
    void foodTrucksAndParkedCarsAreSolid() {
        CampusMap map = CampusMap.parse(List.of(
                "Fcc#RLXH",
                "F======."));
        assertTrue(map.isSolid(0, 0));
        assertTrue(map.isSolid(0, 1));
        assertTrue(map.isSolid(1, 0));
        assertTrue(map.isSolid(2, 0));
        assertEquals(CampusMap.FOOD_TRUCK, map.tileAt(0, 0));
        assertEquals(CampusMap.PARKED_CAR, map.tileAt(1, 0));
    }

    @Test
    void outsideTheMapIsSolid() {
        CampusMap map = CampusMap.parse(VALID);
        assertTrue(map.isSolid(-1, 2));
        assertTrue(map.isSolid(10, 2));
        assertTrue(map.isSolid(3, -1));
        assertTrue(map.isSolid(3, 4));
    }

    @Test
    void doorPositionsAreFound() {
        CampusMap map = CampusMap.parse(VALID);
        assertEquals(new TilePosition(1, 1), map.doorOf(Building.REGISTRAR));
        assertEquals(new TilePosition(2, 1), map.doorOf(Building.LECTURE_HALL));
        assertEquals(new TilePosition(4, 1), map.doorOf(Building.EXAM_CENTRE));
        assertEquals(new TilePosition(5, 1), map.doorOf(Building.RESIDENCE));
    }

    @Test
    void buildingAtDoorTile() {
        CampusMap map = CampusMap.parse(VALID);
        assertEquals(Building.EXAM_CENTRE, map.buildingAtTile(4, 1).orElseThrow());
        assertTrue(map.buildingAtTile(6, 1).isEmpty());
        assertTrue(map.buildingAtTile(-1, -1).isEmpty());
    }

    @Test
    void unevenRowsAreRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CampusMap.parse(List.of(
                "#RLXH#",
                "......",
                ".....")));
        assertTrue(e.getMessage().contains("row 3"), e.getMessage());
    }

    @Test
    void unknownCharacterIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CampusMap.parse(List.of(
                "#RLXH?",
                "......")));
        assertTrue(e.getMessage().contains("'?'"), e.getMessage());
    }

    @Test
    void missingDoorIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CampusMap.parse(List.of(
                "#RLX.#",
                "......")));
        assertTrue(e.getMessage().contains(Building.RESIDENCE.getDisplayName()), e.getMessage());
    }

    @Test
    void duplicateDoorIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CampusMap.parse(List.of(
                "#RLXHR",
                "......")));
        assertTrue(e.getMessage().contains(Building.REGISTRAR.getDisplayName()), e.getMessage());
    }

    @Test
    void doorWithBlockedTileBelowIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> CampusMap.parse(List.of(
                "#RLXH.",
                "....#.")));
        assertTrue(e.getMessage().contains(Building.RESIDENCE.getDisplayName()), e.getMessage());
    }

    @Test
    void emptyMapIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> CampusMap.parse(List.of()));
    }
}
