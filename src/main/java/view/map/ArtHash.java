package view.map;

/**
 * Repeatable pseudo-random numbers from a position, so painted textures never depend on
 * the order things are painted in. The same inputs always give the same output.
 */
public final class ArtHash {
    private ArtHash() {
    }

    /** A well-mixed non-negative integer for the position and salt. */
    public static int of(int x, int y, int salt) {
        int h = x * 0x27D4EB2D + y * 0x165667B1 + salt * 0x9E3779B9;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;
        return h & 0x7FFFFFFF;
    }

    /** A number in [0, 1) for the position and salt. */
    public static double unit(int x, int y, int salt) {
        return of(x, y, salt) / (double) 0x80000000L;
    }

    /**
     * Smooth value noise in [0, 1): random values on a grid of the given cell size,
     * blended smoothly in between, so neighbouring pixels change gradually.
     */
    public static double smooth(int x, int y, int cell, int salt) {
        int gx = Math.floorDiv(x, cell);
        int gy = Math.floorDiv(y, cell);
        double fx = fade((x - gx * cell) / (double) cell);
        double fy = fade((y - gy * cell) / (double) cell);
        double top = lerp(unit(gx, gy, salt), unit(gx + 1, gy, salt), fx);
        double bottom = lerp(unit(gx, gy + 1, salt), unit(gx + 1, gy + 1, salt), fx);
        return lerp(top, bottom, fy);
    }

    private static double fade(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
