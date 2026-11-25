package view.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws street furniture tiles: a lamp where the furniture stands beside a sidewalk or road,
 * otherwise a bench, a set of recycling bins or a bike rack chosen from its position.
 * Anything taller than its own tile (the lamp's upper pole and head) goes on the canopy image,
 * so it covers the player walking behind it.
 */
final class FurniturePainter {
    /** The kinds of street furniture. */
    enum Kind { LAMP, BENCH, BIN, BIKE_RACK, NOTICE_BOARD, PICNIC_TABLE, STUDENT, PLANTER, HYDRANT }

    /** Props for furniture that is not a lamp or a queue, weighted by repetition. */
    private static final Kind[] PROPS = {Kind.BENCH, Kind.BENCH, Kind.BIN, Kind.BIKE_RACK, Kind.NOTICE_BOARD,
            Kind.PICNIC_TABLE, Kind.STUDENT, Kind.STUDENT, Kind.PLANTER, Kind.HYDRANT};
    private static final Color UOFT_BLUE = new Color(0x1E, 0x3C, 0x78);

    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final Color OUTLINE = new Color(0x34, 0x28, 0x2A);
    private static final Color SHADOW = new Color(36, 30, 40, 80);
    private static final Color IRON = new Color(0x48, 0x4A, 0x54);
    private static final Color IRON_LIGHT = new Color(0x6E, 0x72, 0x80);
    private static final Color LAMP_GLOW = new Color(0xFA, 0xE6, 0xA0);
    private static final Color WOOD = new Color(0xA8, 0x70, 0x44);
    private static final Color WOOD_LIGHT = new Color(0xC4, 0x8A, 0x56);
    private static final Color STEEL = new Color(0x9A, 0x9C, 0xA6);
    private static final Color[] BINS = {new Color(0x34, 0x62, 0xAA), new Color(0x3C, 0x8C, 0x50),
            new Color(0x5A, 0x5A, 0x60)};
    private static final Color[] BIKES = {new Color(0xC4, 0x46, 0x3C), new Color(0x3C, 0x6E, 0xAA),
            new Color(0xE6, 0xBE, 0x46), new Color(0x78, 0x50, 0x96)};

    private FurniturePainter() {
    }

    /** Which piece of furniture stands on tile ({@code col}, {@code row}). */
    static Kind kindOf(TileGrid grid, int col, int row) {
        char left = grid.tileOrGrass(col - 1, row);
        char right = grid.tileOrGrass(col + 1, row);
        if (left == 'F' || right == 'F') {
            return Kind.STUDENT;
        }
        if (left == '-' || left == '=' || right == '-' || right == '=') {
            return Kind.LAMP;
        }
        return PROPS[ArtHash.of(col, row, 100) % PROPS.length];
    }

    /** Draws the furniture on tile ({@code col}, {@code row}) of the grid, choosing its kind and facing. */
    static void paint(Graphics2D ground, Graphics2D canopy, TileGrid grid, int col, int row) {
        paint(ground, canopy, kindOf(grid, col, row), col, row, studentFacing(grid, col, row));
    }

    /** Draws one piece of furniture on its tile; a student faces down. */
    static void paint(Graphics2D ground, Graphics2D canopy, Kind kind, int col, int row) {
        paint(ground, canopy, kind, col, row, CharacterSheet.Direction.DOWN);
    }

    private static void paint(Graphics2D ground, Graphics2D canopy, Kind kind, int col, int row,
                              CharacterSheet.Direction facing) {
        int x = col * TILE;
        int y = row * TILE;
        switch (kind) {
            case LAMP:
                lamp(ground, canopy, x, y, ArtHash.of(col, row, 102) % 2 == 0);
                break;
            case BENCH:
                bench(ground, x, y);
                break;
            case BIN:
                bins(ground, x, y);
                break;
            case NOTICE_BOARD:
                noticeBoard(ground, x, y, col, row);
                break;
            case PICNIC_TABLE:
                picnicTable(ground, x, y);
                break;
            case STUDENT:
                student(ground, canopy, x, y, col, row, facing);
                break;
            case PLANTER:
                planter(ground, x, y, col, row);
                break;
            case HYDRANT:
                hydrant(ground, x, y);
                break;
            default:
                bikeRack(ground, x, y, col, row);
                break;
        }
    }

    private static void lamp(Graphics2D ground, Graphics2D canopy, int x, int y, boolean banner) {
        if (banner) {
            int bx = x + TILE / 2 + 2;
            canopy.setColor(UOFT_BLUE);
            canopy.fillRect(bx, y - 30, 9, 17);
            canopy.setColor(Color.WHITE);
            canopy.fillRect(bx + 2, y - 27, 5, 1);
            canopy.fillRect(bx + 3, y - 25, 3, 4);
            canopy.fillRect(bx + 2, y - 19, 5, 1);
            canopy.setColor(OUTLINE);
            canopy.drawRect(bx, y - 30, 8, 16);
        }
        int cx = x + TILE / 2;
        int base = y + TILE - 6;
        ground.setColor(SHADOW);
        ground.fillOval(cx - 9, base - 3, 18, 7);
        ground.setColor(IRON);
        ground.fillRect(cx - 5, base - 5, 10, 5);
        ground.fillRect(cx - 2, y, 4, base - y);
        ground.setColor(IRON_LIGHT);
        ground.fillRect(cx - 2, y, 1, base - y);
        canopy.setColor(IRON);
        canopy.fillRect(cx - 2, y - 34, 4, 34);
        canopy.setColor(IRON_LIGHT);
        canopy.fillRect(cx - 2, y - 34, 1, 34);
        canopy.setColor(OUTLINE);
        canopy.fillRect(cx - 9, y - 44, 18, 4);
        canopy.setColor(IRON);
        canopy.fillRect(cx - 8, y - 43, 16, 2);
        canopy.setColor(LAMP_GLOW);
        canopy.fillRect(cx - 6, y - 40, 12, 6);
        canopy.setColor(OUTLINE);
        canopy.drawRect(cx - 7, y - 41, 13, 7);
    }

    private static void bench(Graphics2D g, int x, int y) {
        g.setColor(SHADOW);
        g.fillRect(x + 3, y + TILE - 6, TILE - 2, 5);
        g.setColor(WOOD);
        g.fillRect(x + 2, y + 8, TILE - 4, 6);
        g.fillRect(x + 2, y + 17, TILE - 4, 6);
        g.setColor(WOOD_LIGHT);
        g.fillRect(x + 2, y + 8, TILE - 4, 2);
        g.fillRect(x + 2, y + 17, TILE - 4, 2);
        g.setColor(IRON);
        g.fillRect(x + 4, y + 14, 3, 13);
        g.fillRect(x + TILE - 7, y + 14, 3, 13);
        g.setColor(OUTLINE);
        g.drawRect(x + 2, y + 8, TILE - 5, 5);
        g.drawRect(x + 2, y + 17, TILE - 5, 5);
    }

    private static void bins(Graphics2D g, int x, int y) {
        g.setColor(SHADOW);
        g.fillRect(x + 4, y + TILE - 5, TILE - 4, 4);
        for (int i = 0; i < BINS.length; i++) {
            int bx = x + 2 + i * 10;
            Color colour = BINS[i];
            g.setColor(colour);
            g.fillRect(bx, y + 10, 8, 18);
            g.setColor(colour.darker());
            g.fillRect(bx - 1, y + 7, 10, 4);
            g.setColor(new Color(0xE6, 0xE6, 0xE6));
            g.fillRect(bx + 2, y + 15, 4, 3);
            g.setColor(OUTLINE);
            g.drawRect(bx, y + 10, 7, 17);
        }
    }

    private static void bikeRack(Graphics2D g, int x, int y, int col, int row) {
        int base = y + TILE - 4;
        g.setColor(SHADOW);
        g.fillRect(x + 1, base - 2, TILE - 2, 4);
        g.setColor(STEEL);
        for (int i = 0; i < 2; i++) {
            int rx = x + 3 + i * 14;
            g.drawArc(rx, base - 22, 12, 16, 0, 180);
            g.drawLine(rx, base - 14, rx, base);
            g.drawLine(rx + 12, base - 14, rx + 12, base);
        }
        Color bike = BIKES[ArtHash.of(col, row, 101) % BIKES.length];
        int bx = x + 4;
        g.setColor(OUTLINE);
        g.drawOval(bx - 2, base - 11, 10, 10);
        g.drawOval(bx + 14, base - 11, 10, 10);
        g.setColor(bike);
        g.drawLine(bx + 3, base - 6, bx + 10, base - 15);
        g.drawLine(bx + 10, base - 15, bx + 19, base - 6);
        g.drawLine(bx + 3, base - 6, bx + 12, base - 6);
        g.drawLine(bx + 12, base - 6, bx + 10, base - 15);
        g.drawLine(bx + 19, base - 6, bx + 18, base - 17);
        g.setColor(OUTLINE);
        g.fillRect(bx + 7, base - 18, 6, 2);
        g.fillRect(bx + 16, base - 19, 5, 2);
    }

    private static void noticeBoard(Graphics2D g, int x, int y, int col, int row) {
        g.setColor(SHADOW);
        g.fillRect(x + 4, y + TILE - 5, TILE - 4, 4);
        g.setColor(new Color(0x5A, 0x40, 0x2C));
        g.fillRect(x + 5, y + 16, 3, 13);
        g.fillRect(x + TILE - 8, y + 16, 3, 13);
        g.setColor(new Color(0xB4, 0x8A, 0x5A));
        g.fillRect(x + 2, y + 2, TILE - 4, 17);
        g.setColor(new Color(0xD2, 0xB0, 0x7C));
        g.fillRect(x + 4, y + 5, TILE - 8, 12);
        Color[] papers = {new Color(0xF6, 0xF0, 0xE2), new Color(0xF6, 0xD6, 0x78), new Color(0xAA, 0xD2, 0xEC),
                new Color(0xEC, 0xA0, 0xAA), new Color(0xC8, 0xE8, 0xB4)};
        for (int i = 0; i < 4; i++) {
            Color paper = papers[ArtHash.of(col, row, 120 + i) % papers.length];
            int px = x + 5 + i * 6;
            int py = y + 6 + (i % 2) * 3;
            g.setColor(paper);
            g.fillRect(px, py, 5, 7);
            g.setColor(new Color(0xC8, 0x34, 0x30));
            g.fillRect(px + 2, py, 1, 1);
        }
        g.setColor(UOFT_BLUE);
        g.fillRect(x + 1, y, TILE - 2, 3);
        g.setColor(OUTLINE);
        g.drawRect(x + 2, y + 2, TILE - 5, 16);
    }

    private static void picnicTable(Graphics2D g, int x, int y) {
        g.setColor(SHADOW);
        g.fillRect(x + 4, y + 8, TILE - 4, TILE - 8);
        g.setColor(WOOD);
        g.fillRect(x + 3, y + 3, TILE - 6, 4);
        g.fillRect(x + 3, y + 24, TILE - 6, 4);
        g.setColor(WOOD_LIGHT);
        g.fillRect(x + 2, y + 10, TILE - 4, 11);
        g.setColor(WOOD);
        for (int i = 1; i < 4; i++) {
            g.drawLine(x + 2, y + 10 + i * 3, x + TILE - 3, y + 10 + i * 3);
        }
        g.setColor(OUTLINE);
        g.drawRect(x + 2, y + 10, TILE - 5, 10);
        g.drawRect(x + 3, y + 3, TILE - 7, 3);
        g.drawRect(x + 3, y + 24, TILE - 7, 3);
    }

    /** Which way a student on this tile faces: towards a food truck beside them, otherwise down the screen. */
    static CharacterSheet.Direction studentFacing(TileGrid grid, int col, int row) {
        if (grid.tileOrGrass(col + 1, row) == 'F') {
            return CharacterSheet.Direction.RIGHT;
        }
        if (grid.tileOrGrass(col - 1, row) == 'F') {
            return CharacterSheet.Direction.LEFT;
        }
        return CharacterSheet.Direction.DOWN;
    }

    /**
     * A standing student, drawn from the same composed characters as the player, one art pixel per
     * sprite pixel. The figure is taller than its tile: the part above the tile goes on the canopy
     * image so it covers a player walking behind, the rest on the ground image.
     */
    private static void student(Graphics2D ground, Graphics2D canopy, int x, int y, int col, int row,
                                CharacterSheet.Direction facing) {
        CharacterLook look = CharacterLook.forNumber(ArtHash.of(col, row, 300));
        BufferedImage frame = CharacterSheet.of(look).standing(facing);
        int left = x + (TILE - CharacterSheet.WIDTH) / 2 + ArtHash.of(col, row, 130) % 5 - 2;
        int top = y + TILE - 3 - CharacterSheet.HEIGHT;
        ground.setColor(SHADOW);
        ground.fillOval(left + 3, y + TILE - 6, CharacterSheet.WIDTH - 6, 5);
        Graphics2D below = (Graphics2D) ground.create();
        below.clipRect(x - TILE, y, 3 * TILE, TILE);
        below.drawImage(frame, left, top, null);
        below.dispose();
        Graphics2D above = (Graphics2D) canopy.create();
        above.clipRect(x - TILE, y - TILE, 3 * TILE, TILE);
        above.drawImage(frame, left, top, null);
        above.dispose();
    }

    private static void planter(Graphics2D g, int x, int y, int col, int row) {
        g.setColor(SHADOW);
        g.fillRect(x + 6, y + TILE - 4, TILE - 6, 4);
        g.setColor(new Color(0xC4, 0xBE, 0xB4));
        g.fillRect(x + 4, y + 16, TILE - 8, 6);
        g.setColor(new Color(0xA0, 0x9A, 0x90));
        g.fillRect(x + 4, y + 22, TILE - 8, 7);
        int[] leaves = {0x3C6C34, 0x528A3C, 0x76AA4A};
        for (int i = 0; i < 70; i++) {
            int dx = ArtHash.of(col * 7 + i, row, 140) % 22;
            int dy = ArtHash.of(col, row * 7 + i, 141) % 14;
            if ((dx - 11) * (dx - 11) / 121.0 + (dy - 8) * (dy - 8) / 64.0 <= 1) {
                g.setColor(new Color(leaves[(dy < 6 ? 2 : dy < 10 ? 1 : 0)]));
                g.fillRect(x + 5 + dx, y + 3 + dy, 2, 2);
            }
        }
        if (ArtHash.of(col, row, 142) % 2 == 0) {
            g.setColor(new Color(0xEC, 0x96, 0xAA));
            g.fillRect(x + 10, y + 7, 2, 2);
            g.fillRect(x + 18, y + 5, 2, 2);
            g.fillRect(x + 21, y + 11, 2, 2);
        }
        g.setColor(OUTLINE);
        g.drawRect(x + 4, y + 16, TILE - 9, 12);
    }

    private static void hydrant(Graphics2D g, int x, int y) {
        Color red = new Color(0xC8, 0x34, 0x2C);
        g.setColor(SHADOW);
        g.fillOval(x + 9, y + TILE - 7, 16, 6);
        g.setColor(red);
        g.fillRect(x + 12, y + 12, 9, 16);
        g.fillRect(x + 9, y + 17, 15, 4);
        g.setColor(red.darker());
        g.fillRect(x + 11, y + 9, 11, 4);
        g.fillRect(x + 11, y + 26, 11, 3);
        g.setColor(new Color(0xE6, 0x7A, 0x6A));
        g.fillRect(x + 13, y + 13, 2, 12);
        g.setColor(OUTLINE);
        g.drawRect(x + 12, y + 12, 8, 15);
    }
}
