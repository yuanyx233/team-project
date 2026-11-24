package view.map;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingPainterTest {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;

    /** A 8x7-tile building at column 2, row 2 on a 12x11 canvas, door at column 5. */
    private static BuildingPlan plan(BuildingStyle style) {
        return new BuildingPlan(2, 2, 9, 8, 4, style, "Test Hall", 5);
    }

    private static BufferedImage canvas() {
        return new BufferedImage(12 * TILE, 11 * TILE, BufferedImage.TYPE_INT_ARGB);
    }

    @Test
    void sameBuildingGivesSamePicture() {
        BufferedImage a = canvas();
        BufferedImage b = canvas();
        BuildingPainter.paint(a, plan(BuildingStyle.GEORGIAN_BRICK));
        BuildingPainter.paint(b, plan(BuildingStyle.GEORGIAN_BRICK));
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                assertEquals(a.getRGB(x, y), b.getRGB(x, y));
            }
        }
    }

    @Test
    void everyStyleStaysInsideItsFootprint() {
        for (BuildingStyle style : BuildingStyle.values()) {
            BufferedImage image = canvas();
            BuildingPainter.paint(image, plan(style));
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    boolean inside = x >= 2 * TILE && x < 10 * TILE && y >= 2 * TILE && y < 9 * TILE;
                    if (!inside) {
                        assertEquals(0, image.getRGB(x, y) >>> 24, style + " drew outside at " + x + "," + y);
                    }
                }
            }
        }
    }

    @Test
    void stylesLookClearlyDifferent() {
        List<int[]> averages = new ArrayList<>();
        for (BuildingStyle style : BuildingStyle.values()) {
            BufferedImage image = canvas();
            BuildingPainter.paint(image, plan(style));
            long r = 0;
            long g = 0;
            long b = 0;
            int n = 0;
            for (int y = 5 * TILE; y < 9 * TILE; y++) {
                for (int x = 2 * TILE; x < 10 * TILE; x++) {
                    int argb = image.getRGB(x, y);
                    r += (argb >> 16) & 0xFF;
                    g += (argb >> 8) & 0xFF;
                    b += argb & 0xFF;
                    n++;
                }
            }
            averages.add(new int[] {(int) (r / n), (int) (g / n), (int) (b / n)});
        }
        List<int[]> distinct = new ArrayList<>();
        for (int[] average : averages) {
            boolean close = distinct.stream().anyMatch(d -> Math.abs(d[0] - average[0])
                    + Math.abs(d[1] - average[1]) + Math.abs(d[2] - average[2]) < 30);
            if (!close) {
                distinct.add(average);
            }
        }
        assertTrue(distinct.size() >= 10, "only " + distinct.size() + " clearly different wall colours");
    }

    /** Robarts at its real size on the map: 19 x 18 tiles with a 7-row front wall. */
    private static BufferedImage robarts() {
        BufferedImage image = new BufferedImage(19 * TILE, 18 * TILE, BufferedImage.TYPE_INT_ARGB);
        BuildingPainter.paint(image, new BuildingPlan(0, 0, 18, 17, 7, BuildingStyle.ROBARTS, "Robarts", -1));
        return image;
    }

    private static int lowestRoofRow(BufferedImage image, int x) {
        int lowest = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            if ((image.getRGB(x, y) & 0xFFFFFF) == (RobartsPainter.ROOF.getRGB() & 0xFFFFFF)) {
                lowest = y;
            }
        }
        return lowest;
    }

    @Test
    void robartsRoofEdgeFormsAV() {
        BufferedImage image = robarts();
        int width = image.getWidth();
        int besideTower = lowestRoofRow(image, width * 22 / 100);
        int middle = lowestRoofRow(image, width / 2 - 70);
        assertTrue(besideTower > 0 && middle > besideTower + 40,
                "roof edge should dip towards the middle: " + besideTower + " vs " + middle);
    }

    @Test
    void robartsWingsAreFinnedAndEndTowersBlank() {
        BufferedImage image = robarts();
        int width = image.getWidth();
        int y = image.getHeight() - 150;
        int changes = 0;
        boolean wasLight = false;
        for (int x = width * 13 / 100; x < width * 38 / 100; x++) {
            int argb = image.getRGB(x, y);
            int brightness = (((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 3;
            boolean light = brightness > 150;
            if (light != wasLight) {
                changes++;
                wasLight = light;
            }
        }
        assertTrue(changes >= 20, "fins and recesses should alternate along the wing, changes " + changes);
        for (int ty = image.getHeight() / 3; ty < image.getHeight() - 140; ty++) {
            for (int tx = 4; tx < width / 10; tx++) {
                int rgb = image.getRGB(tx, ty) & 0xFFFFFF;
                assertFalse(rgb == (RobartsPainter.WINDOW.getRGB() & 0xFFFFFF)
                        || rgb == (RobartsPainter.LIT.getRGB() & 0xFFFFFF), "window in end tower at " + tx + "," + ty);
            }
        }
    }

    @Test
    void doorColumnShowsADarkDoorwayOnTheBottomRow() {
        for (BuildingStyle style : BuildingStyle.values()) {
            BufferedImage image = canvas();
            BuildingPainter.paint(image, plan(style));
            int dark = 0;
            for (int y = 8 * TILE; y < 9 * TILE; y++) {
                for (int x = 5 * TILE; x < 6 * TILE; x++) {
                    int argb = image.getRGB(x, y);
                    int brightness = (((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 3;
                    dark += brightness < 75 ? 1 : 0;
                }
            }
            assertTrue(dark > TILE * TILE / 5, style + " doorway too light: " + dark);
        }
    }
}
