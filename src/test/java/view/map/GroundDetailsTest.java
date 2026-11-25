package view.map;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroundDetailsTest {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;

    private static TileGrid repeat(String row, int times) {
        return TileGrid.of(new ArrayList<>(Collections.nCopies(times, row)));
    }

    @Test
    void longVerticalRoadGetsAYellowCentreLine() {
        BufferedImage image = GroundPainter.paint(repeat("--====--", 10), 10);
        int centre = 4 * TILE;
        int yellow = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = centre - 4; x < centre + 4; x++) {
                yellow += (image.getRGB(x, y) & 0xFFFFFF) == GroundDetails.YELLOW ? 1 : 0;
            }
        }
        assertTrue(yellow > image.getHeight(), "expected a dashed yellow line, found " + yellow + " pixels");
    }

    @Test
    void fourLaneCrossStreetGetsStreetcarTracksInItsMiddleLanes() {
        TileGrid grid = TileGrid.of(List.of(
                "------------",
                "============",
                "============",
                "============",
                "============",
                "------------"));
        BufferedImage image = GroundPainter.paint(grid, 6);
        int x = 5 * TILE + 7;
        assertEquals(GroundDetails.RAIL, image.getRGB(x, 2 * TILE + 10) & 0xFFFFFF);
        assertEquals(GroundDetails.RAIL, image.getRGB(x, 3 * TILE + 22) & 0xFFFFFF);
        assertNotEquals(GroundDetails.RAIL, image.getRGB(x, 1 * TILE + 10) & 0xFFFFFF);
        // Embedded track, not a railway: the bed between the rails is plain, with no sleepers.
        for (int tx = 4 * TILE; tx < 5 * TILE; tx++) {
            assertEquals(GroundDetails.TRACK_BED, image.getRGB(tx, 2 * TILE + 16) & 0xFFFFFF, "sleeper at " + tx);
        }
    }

    @Test
    void roadNextToSidewalkGetsACurb() {
        BufferedImage image = GroundPainter.paint(repeat("--====--", 10), 10);
        int x = 2 * TILE;
        assertEquals(GroundDetails.CURB_LIGHT, image.getRGB(x, 5 * TILE + 10) & 0xFFFFFF);
    }

    @Test
    void roadsNextToAnIntersectionGetCrosswalks() {
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            rows.add("..====..");
        }
        rows.add("========");
        rows.add("========");
        for (int i = 0; i < 6; i++) {
            rows.add("..====..");
        }
        BufferedImage image = GroundPainter.paint(TileGrid.of(rows), rows.size());
        int paint = 0;
        for (int y = 5 * TILE; y < 6 * TILE; y++) {
            for (int x = 2 * TILE; x < 6 * TILE; x++) {
                paint += (image.getRGB(x, y) & 0xFFFFFF) == GroundDetails.PAINT ? 1 : 0;
            }
        }
        assertTrue(paint > 4 * TILE * TILE / 5, "expected zebra stripes, found " + paint + " paint pixels");
    }
}
