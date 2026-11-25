package data_access;

import entity.Building;
import entity.CampusMap;
import entity.TilePosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the hand-edited entrance segment (rows 0-84) of the bundled campus map. */
class EntranceSegmentLayoutTest {
    private static final int LAST_ROW = 84;
    private static final int LAST_DENSE_COL = 48;
    private static final int WINDOW_COLS = 15;
    private static final int WINDOW_ROWS = 10;

    private final CampusMap map = CampusMapResourceLoader.load(CampusMapResourceLoader.DEFAULT_MAP);

    private boolean isBuilding(int col, int row) {
        char c = map.tileAt(col, row);
        return c == CampusMap.WALL || map.buildingAtTile(col, row).isPresent();
    }

    @Test
    void noBuildingCrossesTheSeam() {
        for (int col = 0; col < map.getWidth(); col++) {
            assertFalse(isBuilding(col, LAST_ROW) && isBuilding(col, LAST_ROW + 1),
                    "building crosses rows " + LAST_ROW + "/" + (LAST_ROW + 1) + " at column " + col);
        }
    }

    @Test
    void everyDoorIsReachableFromTheSpawn() {
        TilePosition home = map.doorOf(Building.RESIDENCE);
        boolean[][] seen = new boolean[map.getHeight()][map.getWidth()];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {home.getCol(), home.getRow() + 1});
        seen[home.getRow() + 1][home.getCol()] = true;
        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] tile = queue.poll();
            for (int[] step : steps) {
                int col = tile[0] + step[0];
                int row = tile[1] + step[1];
                if (!map.isSolid(col, row) && !seen[row][col]) {
                    seen[row][col] = true;
                    queue.add(new int[] {col, row});
                }
            }
        }
        for (Building building : Building.values()) {
            TilePosition door = map.doorOf(building);
            assertTrue(seen[door.getRow() + 1][door.getCol()], building.getDisplayName() + " is not reachable");
        }
    }

    @Test
    void examCentreStandsOnStGeorge() {
        TilePosition door = map.doorOf(Building.EXAM_CENTRE);
        assertTrue(door.getCol() >= 34 && door.getCol() <= 42, "Exam Centre door at column " + door.getCol());
    }

    @Test
    void furnitureNeverBlocksADoor() {
        for (Building building : Building.values()) {
            TilePosition door = map.doorOf(building);
            assertNotEquals(CampusMap.FURNITURE, map.tileAt(door.getCol(), door.getRow() + 1),
                    "furniture below the door of " + building.getDisplayName());
        }
    }

    @Test
    void everyScreenOfTheSegmentHasSomethingToLookAt() {
        for (int top = 0; top + WINDOW_ROWS - 1 <= LAST_ROW; top++) {
            for (int left = 0; left + WINDOW_COLS - 1 <= LAST_DENSE_COL; left++) {
                int walkable = 0;
                int furniture = 0;
                int plaza = 0;
                for (int row = top; row < top + WINDOW_ROWS; row++) {
                    for (int col = left; col < left + WINDOW_COLS; col++) {
                        char c = map.tileAt(col, row);
                        walkable += map.isSolid(col, row) ? 0 : 1;
                        furniture += c == CampusMap.FURNITURE ? 1 : 0;
                        plaza += c == CampusMap.PLAZA ? 1 : 0;
                    }
                }
                if (walkable * 2 >= WINDOW_COLS * WINDOW_ROWS) {
                    assertTrue(furniture >= 2 || plaza >= 6, "empty screen at column " + left + ", row " + top
                            + " (furniture " + furniture + ", plaza " + plaza + ")");
                }
            }
        }
    }
}
