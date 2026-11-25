package view.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The Java-painted art for the entrance segment (the top rows of the map, where the demo route runs):
 * a ground image and a canopy image, both at {@link GroundPainter#PIXELS_PER_TILE} art pixels per tile.
 * The map draws the ground under the player and the canopy (tree tops, lamp heads) over the player.
 * Buildings keep their existing artwork, copied from the old 16 px backdrop and enlarged without blurring.
 */
public final class SegmentArt {
    /** Rows 0-84: from the residence to the far sidewalk of the cross street past Sidney Smith Hall. */
    public static final int ROWS = 85;

    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final int OLD_TILE = 16;
    private static final Color BUILDING_SHADOW = new Color(30, 34, 24, 80);

    private final BufferedImage ground;
    private final BufferedImage canopy;
    private final int rows;

    private SegmentArt(BufferedImage ground, BufferedImage canopy, int rows) {
        this.ground = ground;
        this.canopy = canopy;
        this.rows = rows;
    }

    /**
     * Paints the first {@code rows} rows of the grid.
     * @param grid the tile grid
     * @param rows how many rows, from the top, to paint
     * @param backdrop the old backdrop at 16 px per tile, used for building tiles; may be null
     */
    public static SegmentArt paint(TileGrid grid, int rows, BufferedImage backdrop) {
        return paint(grid, rows, backdrop, List.of());
    }

    /**
     * Paints the first {@code rows} rows of the grid. Buildings with a plan are drawn in Java by
     * {@link BuildingPainter}; any other building tile copies the old backdrop.
     */
    public static SegmentArt paint(TileGrid grid, int rows, BufferedImage backdrop, List<BuildingPlan> plans) {
        BufferedImage ground = GroundPainter.paint(grid, rows);
        BufferedImage canopy = new BufferedImage(ground.getWidth(), ground.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ground.createGraphics();
        Graphics2D c = canopy.createGraphics();
        try {
            paintBuildingShadows(g, grid, rows);
            if (backdrop != null) {
                copyBuildings(ground, grid, rows, backdrop);
            }
            for (BuildingPlan plan : plans) {
                BuildingPainter.paint(ground, plan);
            }
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < grid.getColumns(); col++) {
                    char tile = grid.getTile(col, row);
                    if (tile == 'T') {
                        GreeneryPainter.paint(g, c, grid, col, row);
                    } else if (tile == 'o') {
                        FurniturePainter.paint(g, c, grid, col, row);
                    } else if (tile == 'F' && grid.tileOrGrass(col, row - 1) != 'F') {
                        VehiclePainter.foodTruck(g, c, col, row);
                    } else if (tile == 'c' && grid.tileOrGrass(col - 1, row) != 'c') {
                        VehiclePainter.car(g, col, row);
                    }
                }
            }
        } finally {
            g.dispose();
            c.dispose();
        }
        return new SegmentArt(ground, canopy, rows);
    }

    private static boolean isBuilding(TileGrid grid, int col, int row) {
        char tile = grid.tileOrGrass(col, row);
        return tile == '#' || tile == 'R' || tile == 'L' || tile == 'X' || tile == 'H';
    }

    /** A soft shadow to the right of and below each building, as if lit from the upper left. */
    private static void paintBuildingShadows(Graphics2D g, TileGrid grid, int rows) {
        g.setColor(BUILDING_SHADOW);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                if (!isBuilding(grid, col, row)) {
                    continue;
                }
                if (!isBuilding(grid, col + 1, row)) {
                    g.fillRect((col + 1) * TILE, row * TILE + (isBuilding(grid, col, row - 1) ? 0 : 10), 10, TILE);
                }
                if (!isBuilding(grid, col, row + 1)) {
                    g.fillRect(col * TILE + 8, (row + 1) * TILE, TILE, 8);
                }
            }
        }
    }

    private static void copyBuildings(BufferedImage ground, TileGrid grid, int rows, BufferedImage backdrop) {
        int scale = TILE / OLD_TILE;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                if (!isBuilding(grid, col, row)) {
                    continue;
                }
                for (int y = row * TILE; y < (row + 1) * TILE; y++) {
                    for (int x = col * TILE; x < (col + 1) * TILE; x++) {
                        int sx = x / scale;
                        int sy = y / scale;
                        if (sx < backdrop.getWidth() && sy < backdrop.getHeight()) {
                            ground.setRGB(x, y, backdrop.getRGB(sx, sy));
                        }
                    }
                }
            }
        }
    }

    public BufferedImage getGround() {
        return ground;
    }

    public BufferedImage getCanopy() {
        return canopy;
    }

    public int getRows() {
        return rows;
    }
}
