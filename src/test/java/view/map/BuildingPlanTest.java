package view.map;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BuildingPlanTest {
    private static List<String> resource(String path) throws IOException {
        try (InputStream in = BuildingPlanTest.class.getResourceAsStream(path)) {
            assertNotNull(in, path);
            return Arrays.asList(new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n"));
        }
    }

    @Test
    void onlyRealForecourtsArePaved() throws IOException {
        TileGrid grid = TileGrid.of(resource("/maps/campus_map.txt"));
        Set<BuildingStyle> forecourts = Set.of(BuildingStyle.RED_BRICK_BAYS, BuildingStyle.ROBARTS,
                BuildingStyle.CONCRETE_RIBBON, BuildingStyle.BAHEN_LOUVRES);
        for (BuildingPlan plan : BuildingPlan.parse(resource("/maps/segment_buildings.txt"), grid, grid.getRows())) {
            if (forecourts.contains(plan.getStyle()) || plan.getBottom() + 1 >= grid.getRows()) {
                continue;
            }
            int paved = 0;
            for (int col = plan.getLeft(); col <= plan.getRight(); col++) {
                paved += grid.getTile(col, plan.getBottom() + 1) == 'p' ? 1 : 0;
            }
            assertTrue(paved <= 2, plan.getStyle() + " at column " + plan.getLeft() + " has a plaza " + paved
                    + " tiles wide");
        }
    }

    @Test
    void noBuildingPicturesArePlaced() throws IOException {
        assertTrue(SpriteLayer.parse(resource("/maps/campus_sprites.txt")).isEmpty());
    }

    @Test
    void parsesFootprintDoorStyleAndSign() {
        TileGrid grid = TileGrid.of(List.of(
                "......",
                ".###..",
                ".#H#..",
                "......"));
        List<BuildingPlan> plans = BuildingPlan.parse(List.of("2 1 1 RED_BRICK_BAYS Innis Residence"), grid, 4);
        BuildingPlan plan = plans.get(0);
        assertEquals(1, plan.getLeft());
        assertEquals(1, plan.getTop());
        assertEquals(3, plan.getRight());
        assertEquals(2, plan.getBottom());
        assertEquals(1, plan.getFacadeRows());
        assertEquals(BuildingStyle.RED_BRICK_BAYS, plan.getStyle());
        assertEquals("Innis Residence", plan.getSign());
        assertEquals(2, plan.getDoorColumn());
    }

    @Test
    void dashMeansNoSignAndNoDoorIsMinusOne() {
        TileGrid grid = TileGrid.of(List.of("##", "##"));
        BuildingPlan plan = BuildingPlan.parse(List.of("0 0 1 GLASS_FINS -"), grid, 2).get(0);
        assertEquals("", plan.getSign());
        assertEquals(-1, plan.getDoorColumn());
    }

    @Test
    void badLinesNameTheirLineNumber() {
        TileGrid grid = TileGrid.of(List.of("##", "##"));
        IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class,
                () -> BuildingPlan.parse(List.of("", "0 0 1 PINK_CASTLE x"), grid, 2));
        assertTrue(unknown.getMessage().contains("line 2"), unknown.getMessage());
        IllegalArgumentException shortLine = assertThrows(IllegalArgumentException.class,
                () -> BuildingPlan.parse(List.of("0 0 1"), grid, 2));
        assertTrue(shortLine.getMessage().contains("line 1"), shortLine.getMessage());
        IllegalArgumentException notABuilding = assertThrows(IllegalArgumentException.class,
                () -> BuildingPlan.parse(List.of("5 5 1 GLASS_FINS x"), grid, 2));
        assertTrue(notABuilding.getMessage().contains("line 1"), notABuilding.getMessage());
    }

    @Test
    void bundledFileDescribesEveryBuildingOnce() throws IOException {
        TileGrid grid = TileGrid.of(resource("/maps/campus_map.txt"));
        List<BuildingPlan> plans = BuildingPlan.parse(resource("/maps/segment_buildings.txt"), grid, grid.getRows());
        assertEquals(28, plans.size());
        Set<String> footprints = new HashSet<>();
        for (BuildingPlan plan : plans) {
            assertTrue(footprints.add(plan.getLeft() + "," + plan.getTop()), "footprint described twice");
            assertTrue(plan.getFacadeRows() < plan.getBottom() - plan.getTop() + 1, "front wall must leave a roof");
        }
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                if (BuildingPlan.isBuildingTile(grid.getTile(col, row))) {
                    final int c = col;
                    final int r = row;
                    assertTrue(plans.stream().anyMatch(p -> p.contains(c, r)),
                            "building tile without a style at column " + col + ", row " + row);
                }
            }
        }
    }
}
