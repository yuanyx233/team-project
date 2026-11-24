package view.map;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Decides how each tree tile is drawn so the map is not walled in by trees. Large solid blocks of
 * tree tiles (scenery the player cannot enter, such as the map edges) become clipped hedges along
 * their edges and open lawn inside, with only a few trees, bushes and flowerbeds. Single trees and
 * small groups stay trees. Everything is decided from neighbouring tiles and position hashes.
 */
final class GreeneryPainter {
    /** How a tree tile is drawn. */
    enum Role { TREE, HEDGE, LAWN }

    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final Color OUTLINE = new Color(0x22, 0x3A, 0x24);
    private static final Color HEDGE_TOP = new Color(0x6A, 0x9E, 0x48);
    private static final Color HEDGE_TOP_LIGHT = new Color(0x86, 0xB6, 0x58);
    private static final Color HEDGE_FRONT = new Color(0x3E, 0x6E, 0x36);
    private static final Color HEDGE_FRONT_DARK = new Color(0x30, 0x58, 0x2C);
    private static final Color SHADOW = new Color(30, 40, 20, 70);
    private static final int[][] BUSHES = {
            {0x2C4A28, 0x3E6A36, 0x5A8C44, 0x7AAA54},
            {0x5A2220, 0x8A2E2A, 0xB8443A, 0xD86A50},
    };
    private static final Color[] FLOWERS = {new Color(0xF0, 0xE2, 0x78), new Color(0xEC, 0x96, 0xAA),
            new Color(0xB0, 0x96, 0xE8), new Color(0xF6, 0xF6, 0xEC), new Color(0xE8, 0x6A, 0x4A)};

    private GreeneryPainter() {
    }

    private static boolean treeOrEdge(TileGrid grid, int col, int row) {
        if (col < 0 || row < 0 || col >= grid.getColumns() || row >= grid.getRows()) {
            return true;
        }
        return grid.getTile(col, row) == 'T';
    }

    private static boolean walkable(TileGrid grid, int col, int row) {
        if (col < 0 || row < 0 || col >= grid.getColumns() || row >= grid.getRows()) {
            return false;
        }
        char tile = grid.getTile(col, row);
        return tile == '.' || tile == '-' || tile == '=' || tile == 'p';
    }

    /** The role of the tree tile at ({@code col}, {@code row}). */
    static Role roleOf(TileGrid grid, int col, int row) {
        int[][] sides = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int trees = 0;
        boolean facesWalkway = false;
        for (int[] side : sides) {
            trees += treeOrEdge(grid, col + side[0], row + side[1]) ? 1 : 0;
            facesWalkway |= walkable(grid, col + side[0], row + side[1]);
        }
        if (trees >= 4) {
            return Role.LAWN;
        }
        if (trees == 3 && facesWalkway) {
            return Role.HEDGE;
        }
        return Role.TREE;
    }

    /** Whether a tree rises from this tree tile (always for trees, sometimes for hedges and lawn). */
    static boolean hasTree(TileGrid grid, int col, int row) {
        switch (roleOf(grid, col, row)) {
            case HEDGE:
                return ArtHash.of(col, row, 150) % 4 == 0;
            case LAWN:
                return ArtHash.of(col, row, 151) % 100 < 5;
            default:
                return true;
        }
    }

    /** Tree shape: street trees beside roads and sidewalks are round; others vary by position. */
    static TreePainter.Shape shapeOf(TileGrid grid, int col, int row) {
        int[][] sides = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] side : sides) {
            char tile = grid.tileOrGrass(col + side[0], row + side[1]);
            if (tile == '-' || tile == '=') {
                return TreePainter.Shape.ROUND;
            }
        }
        int pick = ArtHash.of(col, row, 152) % 6;
        if (pick == 4) {
            return TreePainter.Shape.POPLAR;
        }
        if (pick == 5) {
            return TreePainter.Shape.SPRUCE;
        }
        return TreePainter.Shape.ROUND;
    }

    /** Draws the tree tile at ({@code col}, {@code row}) according to its role. */
    static void paint(Graphics2D ground, Graphics2D canopy, TileGrid grid, int col, int row) {
        Role role = roleOf(grid, col, row);
        if (hasTree(grid, col, row)) {
            TreePainter.paint(ground, canopy, col, row, shapeOf(grid, col, row));
        }
        if (role == Role.HEDGE) {
            hedge(ground, grid, col, row);
        } else if (role == Role.LAWN && !hasTree(grid, col, row)) {
            int roll = ArtHash.of(col, row, 153) % 100;
            if (roll < 16) {
                bush(ground, col, row);
            } else if (roll < 26) {
                flowerbed(ground, col, row);
            }
        }
    }

    private static boolean isHedge(TileGrid grid, int col, int row) {
        return treeOrEdge(grid, col, row) && col >= 0 && row >= 0 && col < grid.getColumns()
                && row < grid.getRows() && roleOf(grid, col, row) == Role.HEDGE;
    }

    /**
     * A clipped box hedge on the tile. Neighbouring hedge tiles join up: a horizontal run reads as one
     * long hedge with its front face showing, a vertical run as one long top face that shows its front
     * only at its southern end.
     */
    private static void hedge(Graphics2D g, TileGrid grid, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        boolean left = isHedge(grid, col - 1, row);
        boolean right = isHedge(grid, col + 1, row);
        boolean up = isHedge(grid, col, row - 1);
        boolean down = isHedge(grid, col, row + 1);
        int x0 = left ? x : x + 3;
        int x1 = right ? x + TILE : x + TILE - 3;
        int topStart = up ? y : y + 6;
        int frontStart = down ? y + TILE : y + 16;
        int bottom = down ? y + TILE : y + 29;
        g.setColor(SHADOW);
        if (!down) {
            g.fillRect(x0 + 3, y + TILE - 3, x1 - x0, 3);
        }
        if (!right) {
            g.fillRect(x1, topStart + 4, 4, bottom - topStart - 4);
        }
        g.setColor(HEDGE_TOP);
        g.fillRect(x0, topStart, x1 - x0, frontStart - topStart);
        if (!down) {
            g.setColor(HEDGE_FRONT);
            g.fillRect(x0, frontStart, x1 - x0, bottom - frontStart);
        }
        for (int i = 0; i < 30; i++) {
            int px = x + ArtHash.of(col * 31 + i, row, 154) % TILE;
            int py = y + ArtHash.of(col, row * 31 + i, 155) % TILE;
            if (px >= x0 && px < x1 - 1 && py >= topStart && py < bottom) {
                g.setColor(py < frontStart ? HEDGE_TOP_LIGHT : HEDGE_FRONT_DARK);
                g.fillRect(px, py, 2, 1);
            }
        }
        g.setColor(OUTLINE);
        if (!up) {
            g.drawLine(x0, topStart - 1, x1 - 1, topStart - 1);
        }
        if (!down) {
            g.drawLine(x0, frontStart, x1 - 1, frontStart);
            g.drawLine(x0, bottom, x1 - 1, bottom);
        }
        if (!left) {
            g.drawLine(x0, topStart, x0, bottom);
        }
        if (!right) {
            g.drawLine(x1 - 1, topStart, x1 - 1, bottom);
        }
    }

    private static void bush(Graphics2D g, int col, int row) {
        int[] palette = BUSHES[ArtHash.of(col, row, 156) % BUSHES.length];
        int cx = col * TILE + TILE / 2 + ArtHash.of(col, row, 157) % 9 - 4;
        int cy = row * TILE + TILE / 2 + 4;
        g.setColor(SHADOW);
        g.fillOval(cx - 15, cy + 4, 32, 9);
        int[][] lobes = {{-7, 0, 9}, {7, 0, 9}, {0, -5, 10}};
        for (int[] lobe : lobes) {
            g.setColor(new Color(palette[0]));
            g.fillOval(cx + lobe[0] - lobe[2] - 1, cy + lobe[1] - lobe[2] - 1, lobe[2] * 2 + 2, lobe[2] * 2 + 2);
        }
        for (int[] lobe : lobes) {
            g.setColor(new Color(palette[2]));
            g.fillOval(cx + lobe[0] - lobe[2], cy + lobe[1] - lobe[2], lobe[2] * 2, lobe[2] * 2);
        }
        g.setColor(new Color(palette[3]));
        g.fillOval(cx - 8, cy - 12, 9, 6);
        g.fillOval(cx - 13, cy - 5, 6, 4);
        g.setColor(new Color(palette[1]));
        g.fillOval(cx + 3, cy + 3, 9, 5);
    }

    private static void flowerbed(Graphics2D g, int col, int row) {
        int x = col * TILE + 3;
        int y = row * TILE + 10;
        g.setColor(new Color(0x6E, 0x4A, 0x30));
        g.fillOval(x, y, TILE - 6, 14);
        g.setColor(new Color(0x52, 0x36, 0x22));
        g.drawOval(x, y, TILE - 7, 13);
        for (int i = 0; i < 12; i++) {
            int px = x + 3 + ArtHash.of(col * 13 + i, row, 158) % (TILE - 12);
            int py = y + 2 + ArtHash.of(col, row * 13 + i, 159) % 9;
            g.setColor(new Color(0x4E, 0x84, 0x3A));
            g.fillRect(px, py + 2, 1, 2);
            g.setColor(FLOWERS[ArtHash.of(col, row, 160 + i % 3) % FLOWERS.length]);
            g.fillRect(px, py, 2, 2);
        }
    }
}
