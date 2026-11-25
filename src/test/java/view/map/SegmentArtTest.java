package view.map;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SegmentArtTest {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final TileGrid GRID = TileGrid.of(List.of(
            "......",
            ".##...",
            ".#H..T",
            "......",
            "--o---",
            "======"));

    private static BufferedImage backdrop() {
        BufferedImage image = new BufferedImage(6 * 16, 6 * 16, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, (x * 7 + y * 13) & 0xFFFFFF);
            }
        }
        return image;
    }

    @Test
    void imagesCoverOnlyTheRequestedRows() {
        SegmentArt art = SegmentArt.paint(GRID, 4, backdrop());
        assertEquals(6 * TILE, art.getGround().getWidth());
        assertEquals(4 * TILE, art.getGround().getHeight());
        assertEquals(4 * TILE, art.getCanopy().getHeight());
        assertEquals(4, art.getRows());
    }

    @Test
    void buildingTilesShowTheOldArtworkEnlargedCrisply() {
        BufferedImage old = backdrop();
        SegmentArt art = SegmentArt.paint(GRID, 6, old);
        for (int y = 1 * TILE; y < 3 * TILE; y++) {
            for (int x = 1 * TILE; x < 2 * TILE; x++) {
                assertEquals(old.getRGB(x / 2, y / 2) & 0xFFFFFF, art.getGround().getRGB(x, y) & 0xFFFFFF);
            }
        }
    }

    @Test
    void plannedBuildingsAreDrawnInJavaNotCopied() {
        BufferedImage old = backdrop();
        List<BuildingPlan> plans = BuildingPlan.parse(List.of("1 1 1 GEORGIAN_BRICK Test"), GRID, 6);
        SegmentArt art = SegmentArt.paint(GRID, 6, old, plans);
        int copied = 0;
        for (int y = 1 * TILE; y < 3 * TILE; y++) {
            for (int x = 1 * TILE; x < 2 * TILE; x++) {
                copied += (old.getRGB(x / 2, y / 2) & 0xFFFFFF) == (art.getGround().getRGB(x, y) & 0xFFFFFF) ? 1 : 0;
            }
        }
        assertTrue(copied < TILE * TILE / 10, "building still copied from the old backdrop: " + copied);
    }

    @Test
    void treesPutLeavesOnTheCanopyLayer() {
        SegmentArt art = SegmentArt.paint(GRID, 6, backdrop());
        int leaves = 0;
        for (int y = 0; y < 2 * TILE; y++) {
            for (int x = 4 * TILE; x < 6 * TILE; x++) {
                leaves += (art.getCanopy().getRGB(x, y) >>> 24) == 0xFF ? 1 : 0;
            }
        }
        assertTrue(leaves > TILE * TILE, "expected a canopy above the tree, found " + leaves);
    }

    @Test
    void paintingIsRepeatable() {
        SegmentArt a = SegmentArt.paint(GRID, 6, backdrop());
        SegmentArt b = SegmentArt.paint(GRID, 6, backdrop());
        for (int y = 0; y < a.getGround().getHeight(); y += 3) {
            for (int x = 0; x < a.getGround().getWidth(); x += 3) {
                assertEquals(a.getGround().getRGB(x, y), b.getGround().getRGB(x, y));
                assertEquals(a.getCanopy().getRGB(x, y), b.getCanopy().getRGB(x, y));
            }
        }
    }
}
