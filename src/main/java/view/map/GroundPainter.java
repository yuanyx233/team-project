package view.map;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Paints the ground of the campus map from its tile grid: textured grass, concrete sidewalk
 * slabs, stone plaza pavers and asphalt, at {@link #PIXELS_PER_TILE} art pixels per tile.
 * Every pixel is chosen from its position alone, so the same grid always gives the same picture.
 */
public final class GroundPainter {
    public static final int PIXELS_PER_TILE = 32;

    /** What lies under a tile, for choosing its texture. */
    enum Ground { GRASS, SIDEWALK, PLAZA, ROAD }

    private static final int[] GRASS = {0x4E843A, 0x5C9640, 0x6CA446, 0x7EB24E, 0x92BE58};
    private static final int[] SLAB = {0xC4BCAE, 0xCEC6B8, 0xD6CEC0};
    private static final int SLAB_JOINT = 0xA8A092;
    private static final int SLAB_EDGE = 0xDED8CC;
    private static final int[] PAVER = {0xC4AA88, 0xCEB692, 0xBCA280, 0xD6C09C};
    private static final int PAVER_JOINT = 0x9E8668;
    private static final int ASPHALT = 0x625E60;
    private static final int ASPHALT_PATCH = 0x686465;
    private static final int ASPHALT_DARK = 0x545054;
    private static final int ASPHALT_LIGHT = 0x7A7674;

    private GroundPainter() {
    }

    /**
     * Paints the first {@code rows} rows of the grid.
     * @param grid the tile grid
     * @param rows how many rows, from the top, to paint
     * @return an opaque image {@code columns * 32} wide and {@code rows * 32} high
     */
    public static BufferedImage paint(TileGrid grid, int rows) {
        int width = grid.getColumns() * PIXELS_PER_TILE;
        int height = rows * PIXELS_PER_TILE;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                Ground ground = groundOf(grid, col, row);
                for (int y = row * PIXELS_PER_TILE; y < (row + 1) * PIXELS_PER_TILE; y++) {
                    for (int x = col * PIXELS_PER_TILE; x < (col + 1) * PIXELS_PER_TILE; x++) {
                        pixels[y * width + x] = texture(ground, x, y);
                    }
                }
            }
        }
        GroundDetails.paint(image, grid, rows);
        return image;
    }

    /** The ground under a tile. Furniture stands on paving when it touches a plaza, otherwise on grass. */
    static Ground groundOf(TileGrid grid, int col, int row) {
        switch (grid.tileOrGrass(col, row)) {
            case '-':
                return Ground.SIDEWALK;
            case 'p':
                return Ground.PLAZA;
            case '=':
            case 'F':
            case 'c':
                return Ground.ROAD;
            case 'o':
                return touchesPlaza(grid, col, row) ? Ground.PLAZA : Ground.GRASS;
            default:
                return Ground.GRASS;
        }
    }

    private static boolean touchesPlaza(TileGrid grid, int col, int row) {
        return grid.tileOrGrass(col - 1, row) == 'p' || grid.tileOrGrass(col + 1, row) == 'p'
                || grid.tileOrGrass(col, row - 1) == 'p' || grid.tileOrGrass(col, row + 1) == 'p';
    }

    static int texture(Ground ground, int x, int y) {
        switch (ground) {
            case SIDEWALK:
                return sidewalk(x, y);
            case PLAZA:
                return plaza(x, y);
            case ROAD:
                return road(x, y);
            default:
                return grass(x, y);
        }
    }

    /** Patchy grass: broad light and dark areas plus fine dither. */
    static int grass(int x, int y) {
        double n = ArtHash.smooth(x, y, 80, 1) * 0.65 + ArtHash.smooth(x, y, 18, 2) * 0.35
                + (ArtHash.unit(x, y, 3) - 0.5) * 0.22;
        return GRASS[Math.max(0, Math.min(GRASS.length - 1, (int) (n * GRASS.length)))];
    }

    /** Square concrete slabs, each a slightly different grey, with joints and speckles. */
    private static int sidewalk(int x, int y) {
        int slab = 16;
        if (x % slab == slab - 1 || y % slab == slab - 1) {
            return SLAB_JOINT;
        }
        if (y % slab == 0) {
            return SLAB_EDGE;
        }
        double speck = ArtHash.unit(x, y, 7);
        if (speck < 0.05) {
            return SLAB_JOINT;
        }
        if (speck > 0.975) {
            return SLAB_EDGE;
        }
        return SLAB[ArtHash.of(x / slab, y / slab, 4) % SLAB.length];
    }

    /** Warm stone pavers laid in a staggered pattern. */
    private static int plaza(int x, int y) {
        int course = y / 8;
        int shifted = x + (course % 2) * 8;
        if (y % 8 == 7 || shifted % 16 == 15) {
            return PAVER_JOINT;
        }
        int colour = PAVER[ArtHash.of(shifted / 16, course, 5) % PAVER.length];
        return y % 8 == 0 ? brighten(colour, 12) : colour;
    }

    /** Asphalt with soft patches and grit. */
    private static int road(int x, int y) {
        double grit = ArtHash.unit(x, y, 8);
        if (grit < 0.08) {
            return ASPHALT_DARK;
        }
        if (grit > 0.95) {
            return ASPHALT_LIGHT;
        }
        return ArtHash.smooth(x, y, 48, 6) > 0.6 ? ASPHALT_PATCH : ASPHALT;
    }

    static int brighten(int rgb, int amount) {
        int r = Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) + amount));
        int g = Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) + amount));
        int b = Math.min(255, Math.max(0, (rgb & 0xFF) + amount));
        return (r << 16) | (g << 8) | b;
    }
}
