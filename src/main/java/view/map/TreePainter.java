package view.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws one tree on its tile in one of three shapes: a round leafy tree, a tall narrow poplar or a
 * spruce. The trunk and ground shadow go on the ground image. The leaves are split: the part above
 * the tree's own tile goes on the canopy image, which the map draws over the player, and the part
 * inside the tile (a spruce's lowest branches) on the ground image. A few leaf images per shape and
 * colour are built once and reused; each tree picks one from its position.
 */
final class TreePainter {
    /** Tree shapes. */
    enum Shape { ROUND, POPLAR, SPRUCE }

    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    private static final int VARIANTS = 3;
    /** Leaf palettes: outline, shade, base, light. */
    private static final int[][] ROUND_PALETTES = {
            {0x284E2E, 0x3C6C34, 0x528A3C, 0x76AA4A},
            {0x46462A, 0x6E702C, 0x949638, 0xC4BC5C},
            {0x224834, 0x326240, 0x487E4A, 0x689C58},
            {0x6E3C1E, 0xB06024, 0xDE9234, 0xF8C860},
    };
    private static final int[][] POPLAR_PALETTES = {
            {0x5A4A1E, 0xA8862E, 0xD2AE3E, 0xF0D46A},
            {0x2E4A28, 0x4A6E32, 0x6A9240, 0x8EB656},
    };
    private static final int[][] SPRUCE_PALETTES = {
            {0x1A3226, 0x284A34, 0x386244, 0x52805A},
    };
    private static final Color TRUNK = new Color(0x78, 0x50, 0x34);
    private static final Color TRUNK_SHADE = new Color(0x5C, 0x3C, 0x2A);
    private static final Color TRUNK_LIGHT = new Color(0x96, 0x68, 0x44);
    private static final Color OUTLINE = new Color(0x34, 0x26, 0x22);
    private static final Color SHADOW = new Color(30, 40, 20, 80);
    private static final BufferedImage[][] ROUND = build(ROUND_PALETTES, Shape.ROUND);
    private static final BufferedImage[][] POPLAR = build(POPLAR_PALETTES, Shape.POPLAR);
    private static final BufferedImage[][] SPRUCE = build(SPRUCE_PALETTES, Shape.SPRUCE);

    private TreePainter() {
    }

    /** Draws a round tree on tile ({@code col}, {@code row}). */
    static void paint(Graphics2D ground, Graphics2D canopy, int col, int row) {
        paint(ground, canopy, col, row, Shape.ROUND);
    }

    /**
     * Draws a tree of the given shape on tile ({@code col}, {@code row}).
     * @param ground graphics of the ground image (32 art pixels per tile)
     * @param canopy graphics of the canopy image, same size as the ground image
     */
    static void paint(Graphics2D ground, Graphics2D canopy, int col, int row, Shape shape) {
        int cx = col * TILE + TILE / 2 + ArtHash.of(col, row, 90) % 7 - 3;
        int base = row * TILE + TILE - 4;
        int trunkHeight = shape == Shape.SPRUCE ? 14 : shape == Shape.POPLAR ? 30 : 44;
        int trunkHalf = shape == Shape.ROUND ? 7 : 4;
        ground.setColor(SHADOW);
        int shadowHalf = shape == Shape.ROUND ? 34 : 22;
        ground.fillOval(cx - shadowHalf, base - 9, shadowHalf * 2, 18);
        ground.setColor(TRUNK);
        ground.fillRect(cx - trunkHalf, base - trunkHeight, trunkHalf * 2, trunkHeight);
        ground.setColor(TRUNK_SHADE);
        ground.fillRect(cx + trunkHalf / 2, base - trunkHeight, trunkHalf - trunkHalf / 2, trunkHeight);
        ground.setColor(TRUNK_LIGHT);
        ground.fillRect(cx - trunkHalf, base - trunkHeight, 2, trunkHeight - 4);
        if (shape == Shape.ROUND) {
            ground.setColor(TRUNK);
            ground.fillPolygon(new int[] {cx - 15, cx - 7, cx - 7}, new int[] {base + 2, base - 10, base + 2}, 3);
            ground.setColor(TRUNK_SHADE);
            ground.fillPolygon(new int[] {cx + 14, cx + 6, cx + 6}, new int[] {base + 2, base - 10, base + 2}, 3);
        }
        ground.setColor(OUTLINE);
        ground.fillRect(cx - trunkHalf - 2, base - trunkHeight, 2, trunkHeight);
        ground.fillRect(cx + trunkHalf, base - trunkHeight, 2, trunkHeight);
        ground.fillRect(cx - trunkHalf - 4, base + 2, trunkHalf * 2 + 8, 2);

        BufferedImage leaves = pick(shape, col, row);
        int left = cx - leaves.getWidth() / 2;
        int bottomGap = shape == Shape.SPRUCE ? 6 : shape == Shape.POPLAR ? 26 : 32;
        int top = base - bottomGap - leaves.getHeight();
        int tileTop = row * TILE;
        Graphics2D below = (Graphics2D) ground.create();
        below.clipRect(left, tileTop, leaves.getWidth(), TILE);
        below.drawImage(leaves, left, top, null);
        below.dispose();
        Graphics2D above = (Graphics2D) canopy.create();
        above.clipRect(left, top, leaves.getWidth(), Math.max(0, tileTop - top));
        above.drawImage(leaves, left, top, null);
        above.dispose();
    }

    private static BufferedImage pick(Shape shape, int col, int row) {
        BufferedImage[][] set = shape == Shape.SPRUCE ? SPRUCE : shape == Shape.POPLAR ? POPLAR : ROUND;
        int palette = shape == Shape.ROUND && ArtHash.of(col, row, 91) % 5 == 0
                ? 3 : ArtHash.of(col, row, 92) % (shape == Shape.ROUND ? 3 : set.length);
        return set[palette][ArtHash.of(col, row, 93) % VARIANTS];
    }

    private static BufferedImage[][] build(int[][] palettes, Shape shape) {
        BufferedImage[][] images = new BufferedImage[palettes.length][VARIANTS];
        for (int p = 0; p < palettes.length; p++) {
            for (int v = 0; v < VARIANTS; v++) {
                images[p][v] = shade(mask(shape, v), palettes[p], v, shape);
            }
        }
        return images;
    }

    /** The outline of one leaf image as a mask. */
    private static boolean[][] mask(Shape shape, int variant) {
        int width;
        int height;
        if (shape == Shape.POPLAR) {
            width = 40;
            height = 116;
        } else if (shape == Shape.SPRUCE) {
            width = 60;
            height = 92;
        } else {
            width = 96;
            height = 92;
        }
        boolean[][] mask = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mask[y][x] = inShape(shape, variant, x, y, width, height);
            }
        }
        return mask;
    }

    private static boolean inShape(Shape shape, int variant, int x, int y, int width, int height) {
        if (shape == Shape.POPLAR) {
            double t = y / (double) height;
            double half = (width / 2.0 - 2) * Math.sin(Math.PI * Math.min(1, t * 1.15)) + 1;
            half += (ArtHash.of(variant, y / 6, 98) % 3) - 1;
            return Math.abs(x - width / 2.0) <= half;
        }
        if (shape == Shape.SPRUCE) {
            // Three stacked tiers, each a triangle wider than the one above it.
            int[][] tiers = {{0, 40, 14}, {24, 66, 22}, {48, height, 29}};
            for (int[] tier : tiers) {
                if (y >= tier[0] && y < tier[1]) {
                    double t = (y - tier[0]) / (double) (tier[1] - tier[0]);
                    double half = 3 + (tier[2] - 3) * t + (ArtHash.of(variant, y / 4, 99) % 3) - 1;
                    if (Math.abs(x - width / 2.0) <= half) {
                        return true;
                    }
                }
            }
            return false;
        }
        int[][] blobs = {{48, 52, 38}, {26, 56, 24}, {70, 56, 24}, {36, 30, 24}, {62, 30, 24}, {48, 20, 20}};
        for (int b = 0; b < blobs.length; b++) {
            int bx = blobs[b][0] + ArtHash.of(variant, b, 94) % 9 - 4;
            int by = blobs[b][1] + ArtHash.of(variant, b, 95) % 9 - 4;
            int radius = blobs[b][2];
            double dy = (y - by) * 1.08;
            if ((x - bx) * (x - bx) + dy * dy <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    /** Colours a mask: lit upper-left rim, shaded lower-right rim, leaf clumps and a dark outline. */
    private static BufferedImage shade(boolean[][] mask, int[] palette, int variant, Shape shape) {
        int height = mask.length;
        int width = mask[0].length;
        int outline = palette[0];
        int shadeColour = palette[1];
        int base = palette[2];
        int light = palette[3];
        int rim = shape == Shape.ROUND ? 8 : 5;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!mask[y][x]) {
                    continue;
                }
                int colour = base;
                if (!inside(mask, x - rim, y - rim)) {
                    colour = light;
                } else if (!inside(mask, x + rim - 2, y + rim + 2)) {
                    colour = shadeColour;
                }
                if (shape == Shape.SPRUCE && !inside(mask, x, y + 3)) {
                    colour = shadeColour;
                }
                image.setRGB(x, y, 0xFF000000 | colour);
            }
        }
        int clumps = width * height / 150;
        for (int i = 0; i < clumps; i++) {
            int x = 4 + ArtHash.of(variant, i, 96) % (width - 8);
            int y = 4 + ArtHash.of(variant, i, 97) % (height - 8);
            int colour = y < height * 0.55 ? light : shadeColour;
            boolean vertical = shape == Shape.POPLAR;
            for (int d = -2; d <= 3; d++) {
                int px = vertical ? x : x + d;
                int py = vertical ? y + d : y;
                if (inside(mask, px - 3, py) && inside(mask, px + 3, py)
                        && inside(mask, px, py - 3) && inside(mask, px, py + 3)) {
                    image.setRGB(px, py, 0xFF000000 | colour);
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (mask[y][x] && (!inside(mask, x - 1, y) || !inside(mask, x + 1, y)
                        || !inside(mask, x, y - 1) || !inside(mask, x, y + 1))) {
                    image.setRGB(x, y, 0xFF000000 | outline);
                }
            }
        }
        return image;
    }

    private static boolean inside(boolean[][] mask, int x, int y) {
        return y >= 0 && y < mask.length && x >= 0 && x < mask[0].length && mask[y][x];
    }
}
