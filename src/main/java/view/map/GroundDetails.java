package view.map;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Second pass over the painted ground: road paint (centre lines, bike lanes, crosswalks),
 * curbs, grass spilling over paving edges, and small flat details such as flowers, stones,
 * fallen leaves, cracks and manholes. Like the textures, every choice comes from position hashes.
 */
final class GroundDetails {
    static final int PAINT = 0xE2DECE;
    static final int YELLOW = 0xDEBA5C;
    static final int CURB_LIGHT = 0xD2CCC2;
    static final int RAIL = 0x5A5C62;
    static final int TRACK_BED = 0x77736F;
    private static final int RAIL_SHINE = 0xA8AAAE;
    private static final int RAIL_GROOVE = 0x3E3C40;
    private static final int CURB_SHADE = 0x4A4648;
    private static final int CURB_FAR = 0xB4AEA4;
    private static final int GRASS_EDGE = 0x42703A;
    private static final int GRASS_FILL = 0x6CA446;
    private static final int GRASS_DARK = 0x4E843A;
    private static final int GRASS_TIP = 0x92BE58;
    private static final int[] FLOWERS = {0xF0E278, 0xF6F6EC, 0xEC96AA, 0xB096E8};
    private static final int[] LEAVES = {0xD88A3A, 0xC4642E, 0xE8B048};
    private static final int STONE = 0x9A948C;
    private static final int STONE_LIGHT = 0xC2BCB2;
    private static final int CRACK = 0x9A9284;
    private static final int MANHOLE = 0x4E4A4C;
    private static final int MANHOLE_RIM = 0x7E7A78;
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final int LONG_RUN = 4;

    private enum RoadKind { VERTICAL, HORIZONTAL, INTERSECTION }

    private final TileGrid grid;
    private final int rows;
    private final int width;
    private final int[] pixels;

    private GroundDetails(BufferedImage image, TileGrid grid, int rows) {
        this.grid = grid;
        this.rows = rows;
        this.width = image.getWidth();
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    /** Paints all details onto an image made by {@link GroundPainter}. */
    static void paint(BufferedImage image, TileGrid grid, int rows) {
        GroundDetails details = new GroundDetails(image, grid, rows);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                details.paintTile(col, row);
            }
        }
    }

    private void paintTile(int col, int row) {
        char tile = grid.tileOrGrass(col, row);
        GroundPainter.Ground ground = GroundPainter.groundOf(grid, col, row);
        if (ground == GroundPainter.Ground.ROAD) {
            paintRoad(col, row);
        } else if (ground == GroundPainter.Ground.SIDEWALK || ground == GroundPainter.Ground.PLAZA) {
            paintPavingDetails(col, row, ground);
            paintGrassEdges(col, row);
        } else if (tile == '.') {
            paintGrassDetails(col, row);
        }
    }

    // ------------------------------------------------------------------ roads

    /**
     * Road tiles, parked vehicles, and one-tile walls standing in a lane (the old map's trucks),
     * which keep lane paint aligned across them.
     */
    private boolean isRoadLike(int col, int row) {
        char tile = grid.tileOrGrass(col, row);
        if (tile == '=' || tile == 'F' || tile == 'c') {
            return true;
        }
        return tile == '#' && (grid.tileOrGrass(col - 1, row) == '=' || grid.tileOrGrass(col + 1, row) == '=');
    }

    private int run(int col, int row, int dCol, int dRow) {
        int length = 1;
        for (int step = 1; step < 64 && isRoadLike(col + dCol * step, row + dRow * step); step++) {
            length++;
        }
        for (int step = 1; step < 64 && isRoadLike(col - dCol * step, row - dRow * step); step++) {
            length++;
        }
        return length;
    }

    private RoadKind roadKind(int col, int row) {
        int vertical = run(col, row, 0, 1);
        int horizontal = run(col, row, 1, 0);
        if (vertical > LONG_RUN && horizontal > LONG_RUN) {
            return RoadKind.INTERSECTION;
        }
        return vertical >= horizontal ? RoadKind.VERTICAL : RoadKind.HORIZONTAL;
    }

    private boolean isIntersection(int col, int row) {
        return grid.tileOrGrass(col, row) == '=' && roadKind(col, row) == RoadKind.INTERSECTION;
    }

    private void paintRoad(int col, int row) {
        RoadKind kind = roadKind(col, row);
        // Manholes first, so lane paint and track beds are drawn over them rather than under.
        if (kind != RoadKind.INTERSECTION && ArtHash.of(col, row, 31) % 29 == 0) {
            manhole(col, row);
        }
        if (kind == RoadKind.VERTICAL) {
            int first = col;
            while (isRoadLike(first - 1, row)) {
                first--;
            }
            int last = col;
            while (isRoadLike(last + 1, row)) {
                last++;
            }
            boolean crosswalk = isIntersection(col, row - 1) || isIntersection(col, row + 1)
                    || sidewalkContinues(first - 1, row, -1, 0) && sidewalkContinues(last + 1, row, 1, 0);
            if (crosswalk) {
                zebra(col, row, true);
            } else {
                lanes(col, row, first, last, row, true);
            }
        } else if (kind == RoadKind.HORIZONTAL) {
            int first = row;
            while (isRoadLike(col, first - 1)) {
                first--;
            }
            int last = row;
            while (isRoadLike(col, last + 1)) {
                last++;
            }
            boolean crosswalk = isIntersection(col - 1, row) || isIntersection(col + 1, row)
                    || sidewalkContinues(col, first - 1, 0, -1) && sidewalkContinues(col, last + 1, 0, 1);
            if (crosswalk) {
                zebra(col, row, false);
            } else {
                lanes(col, row, first, last, col, false);
            }
        }
        curbs(col, row);
    }

    /**
     * True when a sidewalk leads away from the road here for at least four tiles, i.e. a footway
     * meets the road head-on, so pedestrians cross at this spot. Sidewalks running alongside the
     * road do not count.
     */
    private boolean sidewalkContinues(int col, int row, int dCol, int dRow) {
        for (int step = 0; step < 4; step++) {
            if (grid.tileOrGrass(col + dCol * step, row + dRow * step) != '-') {
                return false;
            }
        }
        return true;
    }

    /**
     * Lane paint across a road band from {@code first} to {@code last} (columns for a vertical road,
     * rows for a horizontal one): a dashed yellow centre line, and white bike-lane lines on wide roads.
     */
    private void lanes(int col, int row, int first, int last, int along, boolean vertical) {
        int centre = (first + last + 1) * TILE / 2;
        int x0 = col * TILE;
        int y0 = row * TILE;
        // A four-lane cross street (College) carries streetcar tracks in its middle lanes instead of bike lanes.
        boolean streetcar = !vertical && last - first == 3;
        boolean trackLane = streetcar && (row == first + 1 || row == first + 2);
        for (int i = 0; i < TILE; i++) {
            int a = (vertical ? y0 : x0) + i;
            if (trackLane) {
                // Streetcar track set into the road: a smoother, lighter bed with two grooved rails.
                for (int y = y0 + 8; y <= y0 + 24; y++) {
                    set(a, y, TRACK_BED);
                }
                for (int rail : new int[] {10, 22}) {
                    set(a, y0 + rail, RAIL);
                    set(a, y0 + rail - 1, RAIL_SHINE);
                    set(a, y0 + rail + 1, RAIL_GROOVE);
                }
            }
            if (Math.floorMod(a, TILE) < 20) {
                for (int offset : new int[] {-3, -2, 1, 2}) {
                    across(col, row, centre + offset, a, vertical, YELLOW);
                }
            }
            if (last - first >= 3 && !streetcar) {
                across(col, row, (first + 1) * TILE - 1, a, vertical, PAINT);
                across(col, row, last * TILE, a, vertical, PAINT);
            }
        }
        boolean bikeLane = last - first >= 3 && !streetcar
                && (vertical ? col == first || col == last : row == first || row == last);
        if (bikeLane && Math.floorMod(along, 8) == 3) {
            bikeSymbol(x0 + TILE / 2, y0 + TILE / 2);
        }
    }

    /** Sets one pixel of a line that runs along the road, if it falls inside this tile. */
    private void across(int col, int row, int acrossPos, int alongPos, boolean vertical, int colour) {
        int x = vertical ? acrossPos : alongPos;
        int y = vertical ? alongPos : acrossPos;
        if (x >= col * TILE && x < (col + 1) * TILE && y >= row * TILE && y < (row + 1) * TILE) {
            set(x, y, colour);
        }
    }

    private void zebra(int col, int row, boolean verticalRoad) {
        for (int y = 4; y < TILE - 4; y++) {
            for (int x = 4; x < TILE - 4; x++) {
                int stripe = verticalRoad ? x : y;
                if (stripe % 8 < 4) {
                    set(col * TILE + x, row * TILE + y, PAINT);
                }
            }
        }
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {
                if (ArtHash.unit(col * TILE + x, row * TILE + y, 33) < 0.12 && get(col * TILE + x, row * TILE + y) == PAINT) {
                    set(col * TILE + x, row * TILE + y, 0xC4C0B4);
                }
            }
        }
    }

    private void bikeSymbol(int cx, int cy) {
        ring(cx - 6, cy + 2, 4, PAINT);
        ring(cx + 6, cy + 2, 4, PAINT);
        for (int i = -6; i <= 6; i++) {
            set(cx + i, cy - 2 + Math.abs(i) / 3, PAINT);
        }
        set(cx + 3, cy - 5, PAINT);
        set(cx + 4, cy - 5, PAINT);
    }

    private void manhole(int col, int row) {
        int cx = col * TILE + 8 + ArtHash.of(col, row, 32) % 16;
        int cy = row * TILE + 16;
        for (int dy = -6; dy <= 6; dy++) {
            for (int dx = -6; dx <= 6; dx++) {
                int d = dx * dx + dy * dy;
                if (d <= 36) {
                    set(cx + dx, cy + dy, d >= 25 ? MANHOLE_RIM : (dy % 3 == 0 ? MANHOLE_RIM : MANHOLE));
                }
            }
        }
    }

    private void curbs(int col, int row) {
        int x0 = col * TILE;
        int y0 = row * TILE;
        for (int i = 0; i < TILE; i++) {
            if (!isRoadLike(col, row - 1)) {
                set(x0 + i, y0, CURB_LIGHT);
                set(x0 + i, y0 + 1, CURB_LIGHT);
                set(x0 + i, y0 + 2, CURB_SHADE);
            }
            if (!isRoadLike(col, row + 1)) {
                set(x0 + i, y0 + TILE - 2, CURB_FAR);
                set(x0 + i, y0 + TILE - 1, CURB_FAR);
            }
            if (!isRoadLike(col - 1, row)) {
                set(x0, y0 + i, CURB_LIGHT);
                set(x0 + 1, y0 + i, CURB_LIGHT);
                set(x0 + 2, y0 + i, CURB_SHADE);
            }
            if (!isRoadLike(col + 1, row)) {
                set(x0 + TILE - 2, y0 + i, CURB_FAR);
                set(x0 + TILE - 1, y0 + i, CURB_FAR);
            }
        }
    }

    // ------------------------------------------------------------------ paving and grass

    private boolean isLawn(int col, int row) {
        char tile = grid.tileOrGrass(col, row);
        return (tile == '.' || tile == 'T' || tile == 'o')
                && GroundPainter.groundOf(grid, col, row) == GroundPainter.Ground.GRASS;
    }

    /** Grass tufts growing over the edge of a paved tile wherever it touches lawn. */
    private void paintGrassEdges(int col, int row) {
        int x0 = col * TILE;
        int y0 = row * TILE;
        int[][] sides = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] side : sides) {
            if (!isLawn(col + side[0], row + side[1])) {
                continue;
            }
            for (int k = 0; k < TILE; k++) {
                int depth = ArtHash.of(x0 + k * (side[1] != 0 ? 1 : 0), y0 + k * (side[0] != 0 ? 1 : 0),
                        40 + side[0] * 3 + side[1]) % 5;
                for (int j = 0; j <= depth; j++) {
                    int x;
                    int y;
                    if (side[1] == -1) {
                        x = x0 + k;
                        y = y0 + j;
                    } else if (side[1] == 1) {
                        x = x0 + k;
                        y = y0 + TILE - 1 - j;
                    } else if (side[0] == -1) {
                        x = x0 + j;
                        y = y0 + k;
                    } else {
                        x = x0 + TILE - 1 - j;
                        y = y0 + k;
                    }
                    set(x, y, j == depth ? GRASS_EDGE : GRASS_FILL);
                }
            }
        }
    }

    private void paintPavingDetails(int col, int row, GroundPainter.Ground ground) {
        int x0 = col * TILE;
        int y0 = row * TILE;
        double roll = ArtHash.unit(col, row, 50);
        if (ground == GroundPainter.Ground.SIDEWALK && roll < 0.07) {
            int x = x0 + 6 + ArtHash.of(col, row, 51) % 18;
            int y = y0 + 6 + ArtHash.of(col, row, 52) % 16;
            for (int i = 0; i < 7; i++) {
                set(x + i, y + i / 2 + (i % 3 == 0 ? 1 : 0), CRACK);
            }
        } else if (roll > 0.9) {
            leaf(x0 + 6 + ArtHash.of(col, row, 53) % 20, y0 + 6 + ArtHash.of(col, row, 54) % 20, col, row);
        }
    }

    private void paintGrassDetails(int col, int row) {
        int x0 = col * TILE;
        int y0 = row * TILE;
        int tufts = 2 + ArtHash.of(col, row, 60) % 3;
        for (int t = 0; t < tufts; t++) {
            int x = x0 + 3 + ArtHash.of(col, row, 61 + t) % 26;
            int y = y0 + 4 + ArtHash.of(col, row, 71 + t) % 25;
            set(x - 2, y - 2, GRASS_DARK);
            set(x + 2, y - 2, GRASS_DARK);
            set(x - 1, y - 1, GRASS_DARK);
            set(x + 1, y - 1, GRASS_DARK);
            set(x, y, GRASS_DARK);
            set(x - 2, y - 3, GRASS_TIP);
            set(x + 2, y - 3, GRASS_TIP);
        }
        double roll = ArtHash.unit(col, row, 80);
        if (roll < 0.10) {
            int colour = FLOWERS[ArtHash.of(col, row, 81) % FLOWERS.length];
            int x = x0 + 4 + ArtHash.of(col, row, 82) % 18;
            int y = y0 + 4 + ArtHash.of(col, row, 83) % 18;
            int[][] spots = {{0, 0}, {6, 2}, {2, 6}, {8, 8}, {4, 3}};
            for (int[] spot : spots) {
                set(x + spot[0], y + spot[1], colour);
                set(x + spot[0] + 1, y + spot[1], colour);
                set(x + spot[0], y + spot[1] + 1, colour);
                set(x + spot[0] + 1, y + spot[1] + 1, GRASS_TIP);
                set(x + spot[0], y + spot[1] + 2, GRASS_DARK);
            }
        } else if (roll < 0.14) {
            int x = x0 + 8 + ArtHash.of(col, row, 84) % 14;
            int y = y0 + 10 + ArtHash.of(col, row, 85) % 12;
            for (int dy = 0; dy < 4; dy++) {
                for (int dx = 0; dx < 6; dx++) {
                    if (!((dy == 0 || dy == 3) && (dx == 0 || dx == 5))) {
                        set(x + dx, y + dy, dy == 0 ? STONE_LIGHT : STONE);
                    }
                }
            }
        } else if (roll < 0.20) {
            leaf(x0 + 6 + ArtHash.of(col, row, 86) % 20, y0 + 6 + ArtHash.of(col, row, 87) % 20, col, row);
        }
    }

    private void leaf(int x, int y, int col, int row) {
        int colour = LEAVES[ArtHash.of(col, row, 88) % LEAVES.length];
        set(x, y, colour);
        set(x + 1, y, colour);
        set(x + 1, y + 1, colour);
        set(x + 2, y + 1, colour);
        set(x + 2, y + 2, 0x8A5A2E);
    }

    // ------------------------------------------------------------------ pixels

    private void ring(int cx, int cy, int radius, int colour) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int d = dx * dx + dy * dy;
                if (d <= radius * radius && d > (radius - 1) * (radius - 1)) {
                    set(cx + dx, cy + dy, colour);
                }
            }
        }
    }

    private int get(int x, int y) {
        return pixels[y * width + x] & 0xFFFFFF;
    }

    private void set(int x, int y, int colour) {
        if (x >= 0 && x < width && y >= 0 && y < rows * TILE) {
            pixels[y * width + x] = colour;
        }
    }
}
