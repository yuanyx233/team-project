package view.map;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreeneryPainterTest {
    /** A 10x10 block of tree tiles inside a ring of lawn. */
    private static TileGrid block() {
        List<String> rows = new ArrayList<>();
        rows.add("............");
        for (int i = 0; i < 10; i++) {
            rows.add(".TTTTTTTTTT.");
        }
        rows.add("............");
        return TileGrid.of(rows);
    }

    @Test
    void largeBlockIsThinnedOut() {
        TileGrid grid = block();
        int trees = 0;
        for (int row = 1; row <= 10; row++) {
            for (int col = 1; col <= 10; col++) {
                trees += GreeneryPainter.hasTree(grid, col, row) ? 1 : 0;
            }
        }
        assertTrue(trees < 100 / 3, "too many trees: " + trees);
    }

    @Test
    void blockEdgeFacingLawnIsAHedgeAndInsideIsLawn() {
        TileGrid grid = block();
        assertEquals(GreeneryPainter.Role.HEDGE, GreeneryPainter.roleOf(grid, 5, 1));
        assertEquals(GreeneryPainter.Role.HEDGE, GreeneryPainter.roleOf(grid, 1, 5));
        assertEquals(GreeneryPainter.Role.LAWN, GreeneryPainter.roleOf(grid, 5, 5));
    }

    @Test
    void mapEdgeCountsAsTrees() {
        TileGrid grid = TileGrid.of(List.of(
                "TTTTT",
                "....."));
        assertEquals(GreeneryPainter.Role.HEDGE, GreeneryPainter.roleOf(grid, 2, 0));
    }

    @Test
    void singleTreesAndSmallGroupsStayTrees() {
        TileGrid grid = TileGrid.of(List.of(
                ".....",
                ".T.TT",
                "....."));
        assertEquals(GreeneryPainter.Role.TREE, GreeneryPainter.roleOf(grid, 1, 1));
        assertEquals(GreeneryPainter.Role.TREE, GreeneryPainter.roleOf(grid, 3, 1));
        assertTrue(GreeneryPainter.hasTree(grid, 1, 1));
    }

    @Test
    void streetTreesAreRound() {
        TileGrid grid = TileGrid.of(List.of(
                "-T=",
                "-T=",
                "-T=",
                "-T="));
        for (int row = 0; row < 4; row++) {
            assertEquals(TreePainter.Shape.ROUND, GreeneryPainter.shapeOf(grid, 1, row));
        }
    }

    @Test
    void lawnTreesComeInSeveralShapes() {
        TileGrid grid = TileGrid.of(List.of(".T.T.T.T.T.T.T.T.T.T.T.T.T.T.T.T.T.T.T.T."));
        java.util.Set<TreePainter.Shape> shapes = java.util.EnumSet.noneOf(TreePainter.Shape.class);
        for (int col = 1; col < grid.getColumns(); col += 2) {
            shapes.add(GreeneryPainter.shapeOf(grid, col, 0));
        }
        assertEquals(java.util.EnumSet.allOf(TreePainter.Shape.class), shapes);
    }
}
