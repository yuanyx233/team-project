package view.map;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroundPainterTest {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;

    private static final TileGrid GRID = TileGrid.of(List.of(
            "..--pp==",
            "..--pp==",
            "..--pp=="));

    @Test
    void paintsThirtyTwoArtPixelsPerTile() {
        BufferedImage image = GroundPainter.paint(GRID, 3);
        assertEquals(8 * TILE, image.getWidth());
        assertEquals(3 * TILE, image.getHeight());
    }

    @Test
    void sameGridGivesSamePicture() {
        BufferedImage a = GroundPainter.paint(GRID, 3);
        BufferedImage b = GroundPainter.paint(GRID, 3);
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                assertEquals(a.getRGB(x, y), b.getRGB(x, y), "pixel " + x + "," + y);
            }
        }
    }

    @Test
    void grassSidewalkPlazaAndRoadLookDifferent() {
        BufferedImage image = GroundPainter.paint(GRID, 3);
        int[][] averages = {
                average(image, 0, 1), average(image, 2, 1), average(image, 4, 1), average(image, 6, 1)};
        for (int i = 0; i < averages.length; i++) {
            for (int j = i + 1; j < averages.length; j++) {
                assertTrue(distance(averages[i], averages[j]) > 25,
                        "tile kinds " + i + " and " + j + " look too similar");
            }
        }
    }

    @Test
    void onlyRequestedRowsArePainted() {
        assertEquals(2 * TILE, GroundPainter.paint(GRID, 2).getHeight());
    }

    private static int[] average(BufferedImage image, int col, int row) {
        long r = 0;
        long g = 0;
        long b = 0;
        int n = 0;
        for (int y = row * TILE + 8; y < row * TILE + TILE - 8; y++) {
            for (int x = col * TILE + 8; x < col * TILE + TILE - 8; x++) {
                int rgb = image.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += rgb & 0xFF;
                n++;
            }
        }
        return new int[] {(int) (r / n), (int) (g / n), (int) (b / n)};
    }

    private static double distance(int[] a, int[] b) {
        return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2) + Math.pow(a[2] - b[2], 2));
    }
}
