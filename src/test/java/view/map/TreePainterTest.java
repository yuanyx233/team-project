package view.map;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class TreePainterTest {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;

    @Test
    void canopyIsDrawnAboveTheTreeTileAndTrunkOnTheGround() {
        BufferedImage ground = new BufferedImage(6 * TILE, 6 * TILE, BufferedImage.TYPE_INT_RGB);
        BufferedImage canopy = new BufferedImage(6 * TILE, 6 * TILE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ground.createGraphics();
        Graphics2D c = canopy.createGraphics();
        TreePainter.paint(g, c, 3, 4);
        g.dispose();
        c.dispose();

        int opaqueAbove = 0;
        for (int y = 0; y < 4 * TILE; y++) {
            for (int x = 0; x < canopy.getWidth(); x++) {
                opaqueAbove += (canopy.getRGB(x, y) >>> 24) == 0xFF ? 1 : 0;
            }
        }
        assertTrue(opaqueAbove > 2 * TILE * TILE, "canopy above the tree tile, found " + opaqueAbove);

        int trunk = 0;
        for (int y = 4 * TILE; y < 5 * TILE; y++) {
            for (int x = 3 * TILE; x < 4 * TILE; x++) {
                trunk += (ground.getRGB(x, y) & 0xFFFFFF) != 0 ? 1 : 0;
            }
        }
        assertTrue(trunk > 0, "trunk drawn on the ground in the tree's own tile");
    }

    @Test
    void canopyLeavesTheTreeTileItselfClear() {
        BufferedImage ground = new BufferedImage(6 * TILE, 6 * TILE, BufferedImage.TYPE_INT_RGB);
        BufferedImage canopy = new BufferedImage(6 * TILE, 6 * TILE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ground.createGraphics();
        Graphics2D c = canopy.createGraphics();
        TreePainter.paint(g, c, 3, 4);
        g.dispose();
        c.dispose();
        for (int y = 4 * TILE + 4; y < 5 * TILE; y++) {
            for (int x = 3 * TILE; x < 4 * TILE; x++) {
                assertEquals(0, canopy.getRGB(x, y) >>> 24, "canopy must not cover the trunk tile at " + x + "," + y);
            }
        }
    }
}
