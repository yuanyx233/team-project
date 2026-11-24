package view.map;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Draws parked vehicles: food trucks (two tiles tall, parked along the west curb with the serving
 * hatch facing the sidewalk) and cars (two tiles wide, parked along a cross street).
 * Colours are chosen from the vehicle's position, so the same map always looks the same.
 */
final class VehiclePainter {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final Color OUTLINE = new Color(0x2E, 0x26, 0x2A);
    private static final Color SHADOW = new Color(20, 18, 24, 90);
    private static final Color GLASS = new Color(0x2E, 0x3E, 0x52);
    private static final Color GLINT = new Color(0x9C, 0xC4, 0xDC);
    private static final Color TYRE = new Color(0x22, 0x20, 0x24);
    private static final Color WARM_LIGHT = new Color(0xF8, 0xD8, 0x8A);
    private static final Color[] TRUCKS = {new Color(0xF2, 0xC9, 0x4C), new Color(0xE0, 0x5A, 0x47),
            new Color(0x4A, 0x90, 0xC2), new Color(0x5A, 0xAE, 0x6A), new Color(0xF0, 0x95, 0x4A),
            new Color(0xE8, 0x8A, 0xAE)};
    private static final Color[] AWNINGS = {new Color(0xD2, 0x3C, 0x34), new Color(0x2E, 0x6E, 0xB4),
            new Color(0x3A, 0x8C, 0x4A)};
    private static final Color[] CARS = {new Color(0xC4, 0x4A, 0x3C), new Color(0x3C, 0x6E, 0xAA),
            new Color(0xE6, 0xE6, 0xE0), new Color(0x8A, 0x8E, 0x96), new Color(0x4E, 0x8A, 0x5A),
            new Color(0xE6, 0xBE, 0x46)};

    private VehiclePainter() {
    }

    /** A food truck whose top tile is ({@code col}, {@code row}); it also fills the tile below. */
    static void foodTruck(Graphics2D ground, Graphics2D canopy, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        Color body = TRUCKS[ArtHash.of(col, row, 110) % TRUCKS.length];
        Color awning = AWNINGS[ArtHash.of(col, row, 111) % AWNINGS.length];
        ground.setColor(SHADOW);
        ground.fillRect(x + 8, y + 8, 24, 2 * TILE - 6);
        // Wheels peeking out at both sides.
        ground.setColor(TYRE);
        for (int wy : new int[] {y + 12, y + 46}) {
            ground.fillRect(x + 3, wy, 3, 8);
            ground.fillRect(x + 26, wy, 3, 8);
        }
        // Body, then a lighter roof with vents and a roof sign.
        ground.setColor(body);
        ground.fillRoundRect(x + 5, y + 3, 22, 2 * TILE - 6, 6, 6);
        ground.setColor(body.brighter());
        ground.fillRect(x + 8, y + 18, 16, 2 * TILE - 24);
        ground.setColor(new Color(0xB4, 0xB6, 0xBC));
        ground.fillRect(x + 10, y + 24, 6, 5);
        ground.fillRect(x + 12, y + 48, 8, 6);
        ground.setColor(new Color(0x78, 0x7A, 0x82));
        ground.drawLine(x + 10, y + 26, x + 15, y + 26);
        ground.setColor(Color.WHITE);
        ground.fillRect(x + 17, y + 31, 6, 14);
        ground.setColor(awning);
        ground.fillRect(x + 18, y + 33, 4, 2);
        ground.fillRect(x + 18, y + 37, 4, 2);
        ground.fillRect(x + 18, y + 41, 4, 2);
        // Cab at the north end: windscreen and headlights.
        ground.setColor(GLASS);
        ground.fillRect(x + 8, y + 6, 16, 7);
        ground.setColor(GLINT);
        ground.drawLine(x + 10, y + 11, x + 14, y + 7);
        ground.setColor(WARM_LIGHT);
        ground.fillRect(x + 7, y + 3, 3, 2);
        ground.fillRect(x + 22, y + 3, 3, 2);
        // Serving side: warm hatch light under a striped awning folded out over the boulevard.
        ground.setColor(WARM_LIGHT);
        ground.fillRect(x + 5, y + 22, 3, 24);
        for (int i = 0; i < 24; i += 4) {
            ground.setColor(i % 8 == 0 ? awning : new Color(0xF4, 0xF0, 0xE6));
            ground.fillRect(x - 2, y + 22 + i, 7, 4);
        }
        ground.setColor(OUTLINE);
        ground.drawRect(x - 2, y + 22, 7, 23);
        ground.drawRoundRect(x + 5, y + 3, 21, 2 * TILE - 7, 6, 6);
        // A chalk menu board standing on the boulevard.
        ground.setColor(new Color(0x5A, 0x40, 0x2C));
        ground.fillRect(x - 13, y + 50, 2, 8);
        ground.fillRect(x - 6, y + 50, 2, 8);
        ground.setColor(new Color(0x2C, 0x34, 0x30));
        ground.fillRect(x - 14, y + 42, 11, 10);
        ground.setColor(new Color(0xF0, 0xE8, 0xC8));
        ground.drawLine(x - 12, y + 44, x - 6, y + 44);
        ground.drawLine(x - 12, y + 47, x - 8, y + 47);
        ground.setColor(new Color(0xE6, 0x82, 0x5A));
        ground.drawLine(x - 12, y + 49, x - 7, y + 49);
        // A little steam rising from the roof vent.
        canopy.setColor(new Color(240, 240, 236, 150));
        canopy.fillOval(x + 9, y + 14, 7, 6);
        canopy.fillOval(x + 12, y + 8, 6, 5);
    }

    /** A car whose left tile is ({@code col}, {@code row}); it also fills the tile to the right. */
    static void car(Graphics2D g, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        Color body = CARS[ArtHash.of(col, row, 112) % CARS.length];
        g.setColor(SHADOW);
        g.fillRoundRect(x + 7, y + 20, 58, 10, 8, 8);
        g.setColor(TYRE);
        g.fillRect(x + 10, y + 25, 9, 5);
        g.fillRect(x + 45, y + 25, 9, 5);
        g.fillRect(x + 10, y + 6, 9, 3);
        g.fillRect(x + 45, y + 6, 9, 3);
        g.setColor(body);
        g.fillRoundRect(x + 4, y + 8, 56, 20, 10, 10);
        g.setColor(body.brighter());
        g.fillRect(x + 8, y + 9, 48, 3);
        g.setColor(body.darker());
        g.fillRect(x + 6, y + 23, 52, 4);
        // Cabin with windscreen, rear window and side windows.
        g.setColor(body.brighter());
        g.fillRoundRect(x + 19, y + 10, 26, 12, 6, 6);
        g.setColor(GLASS);
        g.fillRect(x + 16, y + 11, 4, 10);
        g.fillRect(x + 44, y + 11, 4, 10);
        g.fillRect(x + 21, y + 19, 22, 3);
        g.setColor(GLINT);
        g.drawLine(x + 45, y + 12, x + 46, y + 14);
        g.setColor(WARM_LIGHT);
        g.fillRect(x + 58, y + 12, 2, 3);
        g.fillRect(x + 58, y + 21, 2, 3);
        g.setColor(new Color(0xC8, 0x30, 0x2C));
        g.fillRect(x + 4, y + 12, 2, 3);
        g.fillRect(x + 4, y + 21, 2, 3);
        g.setColor(OUTLINE);
        g.drawRoundRect(x + 4, y + 8, 55, 19, 10, 10);
    }
}
