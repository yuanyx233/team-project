package view.map;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VehiclePainterTest {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;

    private static int drawn(BufferedImage image, int col, int row) {
        int count = 0;
        for (int y = row * TILE; y < (row + 1) * TILE; y++) {
            for (int x = col * TILE; x < (col + 1) * TILE; x++) {
                count += (image.getRGB(x, y) >>> 24) != 0 ? 1 : 0;
            }
        }
        return count;
    }

    @Test
    void foodTruckCoversBothOfItsTiles() {
        BufferedImage ground = new BufferedImage(4 * TILE, 4 * TILE, BufferedImage.TYPE_INT_ARGB);
        BufferedImage canopy = new BufferedImage(4 * TILE, 4 * TILE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ground.createGraphics();
        Graphics2D c = canopy.createGraphics();
        VehiclePainter.foodTruck(g, c, 2, 1);
        g.dispose();
        c.dispose();
        assertTrue(drawn(ground, 2, 1) > TILE * TILE / 2);
        assertTrue(drawn(ground, 2, 2) > TILE * TILE / 2);
    }

    @Test
    void carCoversBothOfItsTiles() {
        BufferedImage ground = new BufferedImage(4 * TILE, 4 * TILE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ground.createGraphics();
        VehiclePainter.car(g, 1, 2);
        g.dispose();
        assertTrue(drawn(ground, 1, 2) > TILE * TILE / 3);
        assertTrue(drawn(ground, 2, 2) > TILE * TILE / 3);
    }

    @Test
    void vehiclesStandOnRoad() {
        TileGrid grid = TileGrid.of(List.of("Fcc="));
        assertEquals(GroundPainter.Ground.ROAD, GroundPainter.groundOf(grid, 0, 0));
        assertEquals(GroundPainter.Ground.ROAD, GroundPainter.groundOf(grid, 1, 0));
    }
}
