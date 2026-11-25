package view.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Draws a campus building in 3/4 view from its {@link BuildingPlan}: the roof seen from above in the top
 * rows of the footprint, then the front wall in the bottom {@code facadeRows} rows with the style's
 * material, windows, entrance and name sign. Everything stays inside the footprint and is chosen from
 * position hashes, so the same plan always gives the same picture.
 */
final class BuildingPainter {
    private static final int TILE = GroundPainter.PIXELS_PER_TILE;
    /** Height of one upper storey and of the ground floor, in art pixels. */
    private static final int STOREY = 40;
    private static final int GROUND_FLOOR = 44;
    private static final Color OUTLINE = new Color(0x34, 0x2C, 0x2C);
    private static final Color GLASS = new Color(0x3E, 0x56, 0x6E);
    private static final Color GLASS_LIGHT = new Color(0x7E, 0x9E, 0xB8);
    private static final Color GLINT = new Color(0xC4, 0xDA, 0xE6);
    private static final Color WARM = new Color(0xE8, 0xC0, 0x78);
    private static final Color DOORWAY = new Color(0x2A, 0x26, 0x28);
    private static final Color DOOR_GLASS = new Color(0x2A, 0x3A, 0x4E);
    private static final Color RECESS = new Color(0x38, 0x38, 0x3E);
    private static final Color SIGN = new Color(0x22, 0x34, 0x58);
    private static final Color LOUVRE = new Color(0x3A, 0x3E, 0x4C);
    private static final Color TERRACOTTA = new Color(0xC0, 0x64, 0x40);
    private static final Color METAL = new Color(0xC4, 0xC8, 0xCC);

    private BuildingPainter() {
    }

    /** Draws the building onto an image that covers the map from its top-left corner. */
    static void paint(BufferedImage image, BuildingPlan plan) {
        if (plan.getStyle() == BuildingStyle.ROBARTS) {
            RobartsPainter.paint(image, plan);
            return;
        }
        new Job(image, plan).run();
    }

    /** The state for drawing one building. */
    private static final class Job {
        private final BufferedImage image;
        private final BuildingStyle style;
        private final BuildingPlan plan;
        private final int x0;
        private final int x1;
        private final int y0;
        private final int yb;
        private final int wallTop;
        private final int doorX;
        private final Graphics2D g;
        private int entranceTop;

        Job(BufferedImage image, BuildingPlan plan) {
            this.image = image;
            this.plan = plan;
            this.style = plan.getStyle();
            x0 = plan.getLeft() * TILE;
            x1 = (plan.getRight() + 1) * TILE;
            y0 = plan.getTop() * TILE;
            yb = (plan.getBottom() + 1) * TILE;
            wallTop = yb - plan.getFacadeRows() * TILE;
            doorX = plan.getDoorColumn() >= 0 ? plan.getDoorColumn() * TILE + TILE / 2 : (x0 + x1) / 2;
            entranceTop = yb - GROUND_FLOOR;
            g = image.createGraphics();
            g.clipRect(x0, y0, x1 - x0, yb - y0);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }

        void run() {
            try {
                roof();
                wall();
                upperWindows();
                groundFloor();
                entrance();
                sign();
                g.setColor(OUTLINE);
                g.drawRect(x0, wallTop, x1 - x0 - 1, yb - wallTop - 1);
                g.drawRect(x0, y0, x1 - x0 - 1, yb - y0 - 1);
            } finally {
                g.dispose();
            }
        }

        // ------------------------------------------------------------------ helpers

        private void set(int x, int y, Color colour) {
            if (x >= x0 && x < x1 && y >= y0 && y < yb) {
                image.setRGB(x, y, colour.getRGB());
            }
        }

        private Color shade(Color c, int amount) {
            return new Color(clamp(c.getRed() + amount), clamp(c.getGreen() + amount), clamp(c.getBlue() + amount),
                    c.getAlpha());
        }

        private int clamp(int v) {
            return Math.max(0, Math.min(255, v));
        }

        private void fill(Color colour, int x, int y, int w, int h) {
            g.setColor(colour);
            g.fillRect(x, y, w, h);
        }

        private void box(int x, int y, int w, int h) {
            g.setColor(OUTLINE);
            g.drawRect(x, y, w - 1, h - 1);
        }

        private int upperTop() {
            return wallTop + 8;
        }

        private int upperBottom() {
            return yb - GROUND_FLOOR;
        }

        // ------------------------------------------------------------------ roofs

        private void roof() {
            switch (style.roof) {
                case SLATE:
                case SLATE_GABLES:
                case HOUSE_GABLE:
                case GOTHIC_TOWER:
                    slateRoof();
                    break;
                default:
                    flatRoof();
                    break;
            }
            if (style.tallChimney) {
                int cx = x0 + 18;
                fill(style.wall, cx, y0 + 4, 14, wallTop - y0 - 4);
                fill(shade(style.wall, -26), cx + 10, y0 + 4, 4, wallTop - y0 - 4);
                fill(style.trim, cx - 2, y0 + 2, 18, 4);
                fill(DOORWAY, cx + 3, y0 + 2, 8, 2);
                box(cx, y0 + 4, 14, wallTop - y0 - 4);
            }
        }

        private void flatRoof() {
            Color roof = style.roofColour;
            int roofHeight = wallTop - y0;
            for (int y = y0; y < wallTop; y++) {
                for (int x = x0; x < x1; x++) {
                    double n = ArtHash.unit(x, y, 400);
                    set(x, y, n < 0.06 ? shade(roof, -12) : n > 0.95 ? shade(roof, 10) : roof);
                }
            }
            g.setColor(shade(roof, -7));
            for (int x = x0 + 32; x < x1 - 4; x += 32) {
                g.drawLine(x, y0 + 6, x, wallTop - 6);
            }
            for (int y = y0 + 32; y < wallTop - 6; y += 32) {
                g.drawLine(x0 + 6, y, x1 - 6, y);
            }
            fill(shade(roof, 28), x0, y0, x1 - x0, 4);
            fill(shade(roof, 18), x0, y0, 4, roofHeight);
            fill(shade(roof, -20), x1 - 4, y0, 4, roofHeight);
            fill(shade(roof, -26), x0 + 4, y0 + 4, x1 - x0 - 8, 2);
            fill(shade(roof, 30), x0, wallTop - 5, x1 - x0, 5);
            fill(shade(roof, -30), x0, wallTop - 1, x1 - x0, 1);
            if (style.roof == BuildingStyle.Roof.TRIANGLE) {
                raisedTriangle(roof);
            }
            if (style.roof == BuildingStyle.Roof.FLAT_PENTHOUSE && roofHeight >= 56) {
                penthouse(roofHeight);
            }
            if (style.roof == BuildingStyle.Roof.DOME) {
                dome();
            }
            for (int uy = y0 + 14; uy + 18 < wallTop - 8; uy += 52) {
                for (int ux = x0 + 14; ux + 30 < x1 - 8; ux += 70) {
                    int pick = ArtHash.of(ux, uy, 401) % 4;
                    if (style.roof == BuildingStyle.Roof.TRIANGLE) {
                        continue;
                    }
                    if (pick == 0) {
                        rooftopUnit(ux, uy);
                    } else if (pick == 1) {
                        skylight(ux, uy);
                    } else if (pick == 2) {
                        vent(ux + 10, uy + 4);
                    }
                }
            }
        }

        private void penthouse(int roofHeight) {
            int pw = Math.max(48, (x1 - x0) * 2 / 5);
            int ph = Math.min(roofHeight - 24, 48);
            int px = (x0 + x1 - pw) / 2;
            int py = y0 + 12;
            fill(shade(style.wall, 14), px, py, pw, ph / 2);
            fill(shade(style.wall, -10), px, py + ph / 2, pw, ph - ph / 2);
            for (int x = px + 8; x + 8 < px + pw; x += 18) {
                fill(GLASS, x, py + ph / 2 + 5, 8, 8);
            }
            box(px, py, pw, ph);
        }

        private void dome() {
            int r = 22;
            int cx = x1 - 40;
            int cy = wallTop - 12;
            g.setColor(shade(style.wall, -8));
            g.fillRect(cx - r - 2, cy - 4, 2 * r + 4, 10);
            g.setColor(METAL);
            g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 180);
            g.setColor(shade(METAL, 24));
            g.fillArc(cx - r + 4, cy - r + 4, r, r, 90, 90);
            g.setColor(DOORWAY);
            g.fillRect(cx - 3, cy - r + 2, 6, r - 2);
            g.setColor(OUTLINE);
            g.drawArc(cx - r, cy - r, 2 * r, 2 * r, 0, 180);
            g.drawLine(cx - r, cy, cx + r, cy);
        }

        private void rooftopUnit(int ux, int uy) {
            fill(new Color(40, 36, 30, 70), ux + 3, uy + 3, 24, 16);
            fill(new Color(0xD2, 0xD2, 0xCE), ux, uy, 24, 8);
            fill(new Color(0xA4, 0xA6, 0xA6), ux, uy + 8, 24, 8);
            g.setColor(new Color(0x80, 0x82, 0x84));
            for (int gx = ux + 3; gx < ux + 22; gx += 4) {
                g.drawLine(gx, uy + 10, gx, uy + 14);
            }
            box(ux, uy, 24, 16);
        }

        private void skylight(int ux, int uy) {
            fill(new Color(0xB8, 0xB6, 0xB0), ux - 1, uy - 1, 32, 14);
            fill(GLASS, ux + 1, uy + 1, 28, 10);
            fill(GLASS_LIGHT, ux + 1, uy + 1, 28, 2);
            g.setColor(new Color(0xB8, 0xB6, 0xB0));
            for (int x = ux + 8; x < ux + 29; x += 7) {
                g.drawLine(x, uy + 1, x, uy + 10);
            }
            box(ux - 1, uy - 1, 32, 14);
        }

        private void vent(int vx, int vy) {
            g.setColor(new Color(40, 36, 30, 70));
            g.fillOval(vx + 2, vy + 4, 9, 5);
            g.setColor(new Color(0x9A, 0x9C, 0xA0));
            g.fillOval(vx, vy, 8, 8);
            g.setColor(new Color(0x6E, 0x70, 0x74));
            g.fillOval(vx + 2, vy + 2, 4, 4);
            g.setColor(OUTLINE);
            g.drawOval(vx, vy, 7, 7);
        }

        /** Robarts' raised, peacock-like triangular roof mass in ribbed concrete. */
        private void raisedTriangle(Color roof) {
            int apexX = (x0 + x1) / 2;
            int apexY = y0 + 10;
            int baseY = wallTop - 8;
            int[] xs = {x0 + 10, apexX, x1 - 10};
            int[] ys = {baseY, apexY, baseY};
            g.setColor(shade(style.wall, 6));
            g.fillPolygon(xs, ys, 3);
            g.setColor(shade(style.wall, -14));
            g.fillPolygon(new int[] {apexX, x1 - 10, apexX}, new int[] {apexY, baseY, baseY}, 3);
            g.setColor(shade(style.wall, -22));
            for (int y = apexY + 6; y < baseY; y += 4) {
                double t = (y - apexY) / (double) (baseY - apexY);
                int half = (int) ((apexX - x0 - 10) * t);
                g.drawLine(apexX - half, y, apexX + half, y);
            }
            g.setColor(OUTLINE);
            g.drawPolygon(xs, ys, 3);
            g.drawLine(apexX, apexY, apexX, baseY);
            fill(shade(style.wall, 20), apexX - 18, apexY + 18, 36, 18);
            fill(shade(style.wall, -18), apexX - 18, apexY + 30, 36, 6);
            box(apexX - 18, apexY + 18, 36, 18);
        }

        private void slateRoof() {
            Color slate = style.roofColour;
            int ridge = y0 + Math.max(6, (wallTop - y0) / 3);
            for (int y = y0; y < wallTop; y++) {
                int course = (y - y0) / 6;
                for (int x = x0; x < x1; x++) {
                    boolean back = y < ridge;
                    Color c = back ? shade(slate, -14) : slate;
                    if ((y - y0) % 6 == 5 || ((x + (course % 2) * 5) % 10 == 9 && !back)) {
                        c = shade(c, -18);
                    }
                    set(x, y, c);
                }
            }
            fill(shade(slate, 26), x0, ridge - 2, x1 - x0, 3);
            fill(shade(slate, -34), x0, wallTop - 3, x1 - x0, 3);
            if (style.chimneys) {
                for (int cx : new int[] {x0 + 14, x1 - 30}) {
                    fill(style.wall, cx, ridge - 22, 16, 22);
                    fill(shade(style.wall, -24), cx + 11, ridge - 22, 5, 22);
                    fill(style.trim, cx - 2, ridge - 25, 20, 4);
                    box(cx, ridge - 22, 16, 22);
                }
            }
            if (style.roof == BuildingStyle.Roof.SLATE_GABLES) {
                gable(x0 + (x1 - x0) / 4, 28, Math.min(44, wallTop - ridge));
                gable(x0 + 3 * (x1 - x0) / 4, 28, Math.min(44, wallTop - ridge));
            } else if (style.roof == BuildingStyle.Roof.HOUSE_GABLE) {
                gable((x0 + x1) / 2, Math.min(40, (x1 - x0) / 2 - 6), Math.min(56, wallTop - y0 - 4));
            } else if (style.roof == BuildingStyle.Roof.GOTHIC_TOWER) {
                gothicTower();
            }
        }

        /** A wall-coloured gable rising from the top of the front wall into the roof. */
        private void gable(int gx, int half, int height) {
            g.setColor(style.wall);
            g.fillPolygon(new int[] {gx - half, gx, gx + half}, new int[] {wallTop, wallTop - height, wallTop}, 3);
            g.setColor(style.trim);
            g.drawLine(gx - half - 2, wallTop, gx, wallTop - height - 2);
            g.drawLine(gx, wallTop - height - 2, gx + half + 2, wallTop);
            fill(GLASS, gx - 5, wallTop - height / 2 - 4, 10, 12);
            fill(style.trim, gx - 1, wallTop - height / 2 - 4, 2, 12);
            g.setColor(OUTLINE);
            g.drawLine(gx - half - 3, wallTop, gx, wallTop - height - 3);
            g.drawLine(gx, wallTop - height - 3, gx + half + 3, wallTop);
        }

        /** Knox College's central stone tower with battlements and a pointed window. */
        private void gothicTower() {
            int w = 48;
            int tx = doorX - w / 2;
            int top = y0 + 4;
            for (int y = top + 8; y < wallTop; y++) {
                for (int x = tx; x < tx + w; x++) {
                    set(x, y, texture(x, y));
                }
            }
            for (int x = tx; x < tx + w; x += 8) {
                fill(style.wall, x, top, 5, 8);
                box(x, top, 5, 8);
            }
            fill(shade(style.wall, -24), tx + w - 5, top + 8, 5, wallTop - top - 8);
            pointedWindow(doorX - 6, top + 18, 12, Math.min(30, wallTop - top - 26));
            box(tx, top + 8, w, wallTop - top - 8);
        }

        // ------------------------------------------------------------------ wall

        private void wall() {
            for (int y = wallTop; y < yb; y++) {
                for (int x = x0; x < x1; x++) {
                    set(x, y, texture(x, y));
                }
            }
            fill(style.cornice, x0, wallTop, x1 - x0, style.cornice.equals(style.trim) ? 5 : 9);
            fill(shade(style.cornice, -30), x0, wallTop + (style.cornice.equals(style.trim) ? 5 : 9), x1 - x0, 1);
            fill(shade(style.wall, 14), x0, wallTop + 6, 2, yb - wallTop - 6);
            fill(shade(style.wall, -18), x1 - 3, wallTop + 6, 3, yb - wallTop - 6);
        }

        private Color texture(int x, int y) {
            Color base = style.wall;
            Color dark = style.wallDark;
            if (style.faceted) {
                int segment = (x - x0) / Math.max(48, (x1 - x0) / 5);
                base = shade(base, segment % 2 == 0 ? 8 : -12);
                dark = shade(dark, segment % 2 == 0 ? 8 : -12);
            }
            int ly = y - wallTop;
            switch (style.texture) {
                case BRICK: {
                    int course = ly / 4;
                    int shifted = x + (course % 2) * 4;
                    if (ly % 4 == 3 || shifted % 8 == 7) {
                        return dark;
                    }
                    return shade(base, ArtHash.of(shifted / 8, course, 402) % 13 - 6);
                }
                case STONE: {
                    int course = ly / 8;
                    int shifted = x + (course % 2) * 8;
                    if (ly % 8 == 7 || shifted % 16 == 15) {
                        return dark;
                    }
                    return shade(base, ArtHash.of(shifted / 16, course, 403) % 15 - 7);
                }
                case PANELS: {
                    if (ly % 20 == 19 || (x - x0) % 32 == 31) {
                        return dark;
                    }
                    double n = ArtHash.unit(x, y, 404);
                    return n < 0.05 ? shade(base, -10) : n > 0.96 ? shade(base, 8) : base;
                }
                case RIBBED: {
                    if (Math.floorMod(ly, 4) == 3) {
                        return dark;
                    }
                    if (Math.floorMod(ly, 4) == 0) {
                        return shade(base, 8);
                    }
                    double n = ArtHash.unit(x, y, 407);
                    return n < 0.04 ? shade(base, -8) : base;
                }
                case GLASS: {
                    if ((x - x0) % 16 == 15 || ly % 20 == 19) {
                        return dark;
                    }
                    boolean streak = Math.floorMod(x - y, 37) < 3;
                    return streak ? GLASS_LIGHT : base;
                }
                default: {
                    double n = ArtHash.unit(x, y, 405);
                    return n < 0.05 ? shade(base, -9) : n > 0.96 ? shade(base, 8) : base;
                }
            }
        }

        // ------------------------------------------------------------------ windows

        private void upperWindows() {
            int top = upperTop();
            int bottom = upperBottom();
            switch (style.windows) {
                case FINS:
                    fill(GLASS, x0 + 2, top, x1 - x0 - 4, bottom - top);
                    for (int fx = x0 + 4; fx < x1 - 4; fx += 8) {
                        fill(style.trim, fx, top, 3, bottom - top);
                        fill(shade(style.trim, -70), fx + 3, top, 1, bottom - top);
                    }
                    return;
                case BAYS:
                    for (int bx = x0 + 14; bx + 28 < x1; bx += 64) {
                        fill(style.trim, bx - 2, top - 2, 32, bottom - top + 2);
                        fill(GLASS, bx, top, 28, bottom - top - 2);
                        for (int y = top + STOREY - 2; y < bottom; y += STOREY) {
                            fill(shade(GLASS, -14), bx, y, 28, 3);
                        }
                        fill(GLASS_LIGHT, bx + 2, top + 2, 4, bottom - top - 8);
                        box(bx - 2, top - 2, 32, bottom - top + 2);
                    }
                    return;
                case DEEP_FINS:
                    deepFins(top, bottom);
                    return;
                case ANGULAR:
                    angularGlass(top, bottom);
                    return;
                case TALL_FRAMED:
                    tallFramed(top, bottom);
                    return;
                case LOUVRE_BOX:
                    louvreBox(top, bottom);
                    return;
                case PILASTERS:
                    pilasters(top, bottom);
                    return;
                case STRIPS:
                    for (int sx = x0 + 12; sx + 14 < x1 - 8; sx += 30) {
                        fill(shade(style.wall, -40), sx - 2, top, 18, bottom - top);
                        fill(DOOR_GLASS, sx, top + 2, 14, bottom - top - 4);
                        for (int y = top + 12; y < bottom - 2; y += 12) {
                            fill(shade(DOOR_GLASS, 26), sx, y, 14, 1);
                        }
                        fill(GLASS_LIGHT, sx + 1, top + 2, 2, bottom - top - 4);
                    }
                    return;
                default:
                    break;
            }
            for (int floorTop = top; floorTop + STOREY <= bottom + 2; floorTop += STOREY) {
                int floor = (floorTop - top) / STOREY;
                switch (style.windows) {
                    case RIBBON:
                        fill(GLASS, x0 + 8, floorTop + 10, x1 - x0 - 16, 16);
                        fill(GLASS_LIGHT, x0 + 8, floorTop + 10, x1 - x0 - 16, 3);
                        g.setColor(shade(style.wall, -50));
                        for (int x = x0 + 8; x < x1 - 8; x += 10) {
                            g.drawLine(x, floorTop + 10, x, floorTop + 25);
                        }
                        fill(shade(style.wall, 22), x0 + 6, floorTop + 26, x1 - x0 - 12, 2);
                        break;
                    case SPARSE:
                        for (int x = x0 + 20; x + 14 < x1 - 10; x += 34) {
                            if (ArtHash.of(x / 34, floor, 408) % 3 != 0) {
                                continue;
                            }
                            fill(shade(style.wall, -50), x - 3, floorTop + 9, 20, 20);
                            fill(GLASS, x, floorTop + 12, 14, 14);
                            fill(shade(style.wall, -80), x, floorTop + 12, 14, 3);
                        }
                        break;
                    case GRID:
                        for (int x = x0 + 8; x + 10 < x1 - 6; x += 18) {
                            window(x, floorTop + 12, 10, 16, true);
                        }
                        break;
                    case LARGE:
                        for (int x = x0 + 8; x + 26 < x1 - 6; x += 36) {
                            window(x, floorTop + 6, 26, 26, true);
                        }
                        break;
                    case DEEP_SET:
                        for (int x = x0 + 10; x + 14 < x1 - 6; x += 28) {
                            fill(shade(style.wall, -60), x - 3, floorTop + 6, 20, 26);
                            window(x, floorTop + 9, 14, 20, false);
                        }
                        break;
                    case POINTED:
                        for (int x = x0 + 12; x + 12 < x1 - 8; x += 26) {
                            if (style.roof == BuildingStyle.Roof.GOTHIC_TOWER && Math.abs(x + 6 - doorX) < 30) {
                                continue;
                            }
                            pointedWindow(x, floorTop + 6, 12, 24);
                        }
                        break;
                    default:
                        for (int x = x0 + 10; x + 12 < x1 - 6; x += 24) {
                            window(x, floorTop + 8, 12, 20, true);
                        }
                        break;
                }
            }
        }

        /** One window: frame in the trim colour, glass with a glint, sometimes lit or with blinds. */
        private void window(int x, int y, int w, int h, boolean framed) {
            if (framed) {
                fill(style.trim, x - 2, y - 2, w + 4, h + 4);
            }
            int pick = ArtHash.of(x, y, 406) % 10;
            Color glass = pick == 0 ? WARM : GLASS;
            fill(glass, x, y, w, h);
            if (pick == 1) {
                fill(new Color(0xD8, 0xD2, 0xC4), x, y, w, h / 2);
            } else {
                fill(pick == 0 ? shade(WARM, 20) : GLASS_LIGHT, x, y, w, Math.max(2, h / 6));
            }
            set(x + 2, y + h / 2, GLINT);
            set(x + 3, y + h / 2 - 1, GLINT);
            if (w >= 12 && framed) {
                fill(style.trim, x + w / 2 - 1, y, 2, h);
            }
            fill(shade(style.wall, 26), x - 2, y + h + 1, w + 4, 2);
            int pad = framed ? 2 : 0;
            box(x - pad, y - pad, w + 2 * pad, h + 2 * pad);
        }

        private void pointedWindow(int x, int y, int w, int h) {
            int half = w / 2;
            fill(style.trim, x - 2, y + half, w + 4, h - half + 2);
            g.setColor(style.trim);
            g.fillPolygon(new int[] {x - 2, x + half, x + w + 2}, new int[] {y + half, y - 3, y + half}, 3);
            fill(GLASS, x, y + half, w, h - half);
            g.setColor(GLASS);
            g.fillPolygon(new int[] {x, x + half, x + w}, new int[] {y + half, y, y + half}, 3);
            fill(style.trim, x + half - 1, y + 2, 2, h - 2);
            fill(style.trim, x, y + h / 2 + 4, w, 1);
            set(x + 2, y + h - 6, GLINT);
            g.setColor(OUTLINE);
            g.drawPolygon(new int[] {x - 2, x + half, x + w + 1}, new int[] {y + half, y - 3, y + half}, 3);
            g.drawRect(x - 2, y + half, w + 3, h - half + 1);
        }

        private void deepFins(int top, int bottom) {
            int glassBoxLeft = x1 - Math.max(48, (x1 - x0) / 4);
            fill(DOOR_GLASS, x0 + 4, top, glassBoxLeft - x0 - 4, bottom - top);
            for (int y = top + STOREY - 4; y < bottom; y += STOREY) {
                fill(shade(style.wall, -10), x0 + 4, y, glassBoxLeft - x0 - 4, 6);
            }
            for (int fx = x0 + 6; fx + 10 < glassBoxLeft; fx += 20) {
                fill(style.wall, fx, top - 2, 10, bottom - top + 2);
                fill(shade(style.wall, 16), fx, top - 2, 2, bottom - top + 2);
                fill(shade(style.wall, -50), fx + 10, top, 5, bottom - top);
            }
            glassBox(glassBoxLeft, top - 6, x1 - 4 - glassBoxLeft, yb - top + 4);
        }

        private void glassBox(int x, int y, int w, int h) {
            fill(GLASS, x, y, w, h);
            for (int gx = x + 10; gx < x + w; gx += 12) {
                fill(shade(GLASS, -18), gx, y, 1, h);
            }
            for (int gy = y + 14; gy < y + h; gy += 20) {
                fill(shade(GLASS, -18), x, gy, w, 1);
            }
            for (int k = 0; k < w; k += 3) {
                set(x + k, y + 6 + k / 2, GLASS_LIGHT);
            }
            fill(WARM, x + 4, y + h - 26, w - 8, 10);
            box(x, y, w, h);
        }

        private void angularGlass(int top, int bottom) {
            fill(style.wall, x0 + 2, top, x1 - x0 - 4, bottom - top);
            for (int y = top; y < bottom; y += 20) {
                for (int x = x0 + 2; x < x1 - 2; x += 16) {
                    if (ArtHash.of(x, y, 409) % 5 == 0) {
                        fill(TERRACOTTA, x, y, 16, 20);
                        fill(shade(TERRACOTTA, -26), x, y + 17, 16, 3);
                    }
                }
            }
            g.setColor(style.trim);
            for (int x = x0 + 2; x < x1; x += 16) {
                g.drawLine(x, top, x, bottom);
            }
            for (int k = -(bottom - top); k < x1 - x0; k += 48) {
                g.drawLine(x0 + k, bottom, x0 + k + (bottom - top), top);
                g.drawLine(x0 + k + 1, bottom, x0 + k + 1 + (bottom - top), top);
            }
        }

        private void tallFramed(int top, int bottom) {
            for (int floorTop = top; floorTop + STOREY <= bottom + 2; floorTop += STOREY) {
                for (int x = x0 + 12; x + 14 < x1 - 8; x += 28) {
                    window(x, floorTop + 4, 14, 32, false);
                }
            }
            for (int fx = x0 + 4; fx < x1 - 4; fx += 28) {
                fill(style.trim, fx, top - 4, 5, yb - top);
                fill(shade(style.trim, -40), fx + 5, top - 4, 1, yb - top);
            }
        }

        /** Bahen: plain towers at both ends, a dark louvred box over a lit glazed band in the middle. */
        private void louvreBox(int top, int bottom) {
            int towerW = Math.max(40, (x1 - x0) / 5);
            int bx = x0 + towerW;
            int bw = x1 - x0 - 2 * towerW;
            fill(shade(style.wall, 10), x0, wallTop, towerW, yb - wallTop);
            fill(shade(style.wall, 10), x1 - towerW, wallTop, towerW, yb - wallTop);
            fill(shade(style.wall, -24), x0 + towerW - 4, wallTop, 4, yb - wallTop);
            fill(shade(style.wall, -24), x1 - 4, wallTop, 4, yb - wallTop);
            for (int y = wallTop + 46; y < yb; y += 46) {
                fill(style.wallDark, x0, y, towerW, 1);
                fill(style.wallDark, x1 - towerW, y, towerW, 1);
            }
            int louvreBottom = top + (bottom - top) * 3 / 5;
            fill(LOUVRE, bx, top, bw, louvreBottom - top);
            g.setColor(shade(LOUVRE, 18));
            for (int y = top + 2; y < louvreBottom; y += 3) {
                g.drawLine(bx, y, bx + bw - 1, y);
            }
            fill(GLASS, bx, louvreBottom, bw, bottom - louvreBottom);
            fill(WARM, bx + 2, louvreBottom + 6, bw - 4, bottom - louvreBottom - 10);
            for (int x = bx; x < bx + bw; x += 32) {
                fill(shade(LOUVRE, -12), x, top, 3, bottom - top);
            }
            box(bx, top, bw, bottom - top);
        }

        private void pilasters(int top, int bottom) {
            for (int x = x0 + 16; x + 16 < x1 - 8; x += 32) {
                window(x, top + 6, 16, bottom - top - 14, true);
            }
            for (int px = x0 + 4; px < x1 - 4; px += 32) {
                fill(style.trim, px, top - 2, 7, yb - top - 8);
                fill(shade(style.trim, -40), px + 7, top - 2, 1, yb - top - 8);
                fill(shade(style.trim, 10), px - 1, top - 4, 9, 3);
            }
        }

        // ------------------------------------------------------------------ ground floor

        private void groundFloor() {
            int top = yb - GROUND_FLOOR;
            if (style.entrance == BuildingStyle.Entrance.COLONNADE) {
                fill(RECESS, x0 + 4, top + 4, x1 - x0 - 8, GROUND_FLOOR - 10);
                fill(shade(RECESS, -14), x0 + 4, top + 4, x1 - x0 - 8, 4);
                for (int x = x0 + 18; x < x1 - 30; x += 22) {
                    fill(GLASS, x, top + 14, 14, 18);
                    fill(GLASS_LIGHT, x, top + 14, 14, 3);
                }
                for (int cx = x0 + 6; cx < x1 - 6; cx += 32) {
                    fill(style.wall, cx, top + 4, 5, GROUND_FLOOR - 10);
                    fill(shade(style.wall, -30), cx + 5, top + 4, 1, GROUND_FLOOR - 10);
                }
                steps(x0, x1 - x0);
                return;
            }
            if (style.windows == BuildingStyle.Windows.DEEP_FINS || style.windows == BuildingStyle.Windows.LOUVRE_BOX
                    || style.windows == BuildingStyle.Windows.TALL_FRAMED) {
                if (style.windows == BuildingStyle.Windows.LOUVRE_BOX) {
                    int towerW = Math.max(40, (x1 - x0) / 5);
                    fill(GLASS, x0 + towerW, top + 6, x1 - x0 - 2 * towerW, GROUND_FLOOR - 16);
                    fill(WARM, x0 + towerW + 2, top + 12, x1 - x0 - 2 * towerW - 4, 8);
                    fill(shade(style.wall, -6), x0 + towerW, yb - 12, x1 - x0 - 2 * towerW, 12);
                    g.setColor(new Color(0x60, 0x6A, 0x7C));
                    g.drawLine(x0 + towerW, yb - 20, x1 - towerW, yb - 20);
                    for (int x = x0 + towerW; x < x1 - towerW; x += 12) {
                        g.drawLine(x, yb - 20, x, yb - 12);
                    }
                }
                return;
            }
            if (style.cantilever) {
                fill(shade(style.wall, -70), x0 + 2, top, x1 - x0 - 4, 6);
                fill(shade(style.wall, -40), x0 + 2, top + 6, x1 - x0 - 4, 4);
            }
            if (style.windows == BuildingStyle.Windows.SPARSE || style.windows == BuildingStyle.Windows.STRIPS
                    || style.windows == BuildingStyle.Windows.PILASTERS) {
                return;
            }
            boolean glassFront = style.entrance == BuildingStyle.Entrance.GLASS_CANOPY;
            for (int x = x0 + 10; x + 20 < x1 - 6; x += glassFront ? 30 : 26) {
                if (Math.abs(x + 10 - doorX) < 34) {
                    continue;
                }
                if (style.windows == BuildingStyle.Windows.POINTED) {
                    pointedWindow(x, top + 10, 12, 24);
                } else {
                    window(x, top + 12, glassFront ? 22 : 14, 20, !glassFront);
                }
            }
        }

        private void steps(int x, int w) {
            for (int i = 0; i < 3; i++) {
                fill(shade(style.wall, 12 - i * 10), x, yb - 6 + i * 2, w, 2);
            }
        }

        // ------------------------------------------------------------------ entrances

        private void entrance() {
            switch (style.entrance) {
                case STONE_ARCH: {
                    int w = 36;
                    int left = doorX - w / 2;
                    entranceTop = yb - 46;
                    fill(style.trim, left, entranceTop + 12, w, 46 - 12);
                    g.setColor(style.trim);
                    g.fillOval(left, entranceTop, w, 26);
                    fill(DOORWAY, doorX - 11, entranceTop + 14, 22, 32);
                    g.setColor(DOORWAY);
                    g.fillOval(doorX - 11, entranceTop + 6, 22, 18);
                    fill(shade(DOORWAY, 24), doorX - 1, entranceTop + 16, 2, 30);
                    fill(shade(style.trim, -20), left - 2, yb - 4, w + 4, 4);
                    break;
                }
                case GOTHIC_ARCH: {
                    int w = 32;
                    int left = doorX - w / 2;
                    entranceTop = yb - 52;
                    fill(style.trim, left, yb - 36, w, 36);
                    g.setColor(style.trim);
                    g.fillPolygon(new int[] {left, doorX, left + w}, new int[] {yb - 36, yb - 54, yb - 36}, 3);
                    fill(DOORWAY, doorX - 10, yb - 32, 20, 32);
                    g.setColor(DOORWAY);
                    g.fillPolygon(new int[] {doorX - 10, doorX, doorX + 10}, new int[] {yb - 32, yb - 46, yb - 32}, 3);
                    fill(shade(DOORWAY, 24), doorX - 1, yb - 40, 2, 40);
                    g.setColor(OUTLINE);
                    g.drawPolygon(new int[] {left, doorX, left + w - 1}, new int[] {yb - 36, yb - 54, yb - 36}, 3);
                    break;
                }
                case PORTICO: {
                    int w = 52;
                    int left = doorX - w / 2;
                    entranceTop = yb - 58;
                    fill(DOORWAY, doorX - 10, yb - 34, 20, 34);
                    for (int i = 0; i < 4; i++) {
                        int cx = left + 3 + i * 15;
                        fill(style.trim, cx, yb - 44, 5, 40);
                        fill(shade(style.trim, -40), cx + 4, yb - 44, 1, 40);
                    }
                    g.setColor(style.trim);
                    g.fillPolygon(new int[] {left - 2, doorX, left + w + 2}, new int[] {yb - 44, yb - 58, yb - 44}, 3);
                    fill(style.trim, left - 2, yb - 46, w + 4, 3);
                    g.setColor(OUTLINE);
                    g.drawPolygon(new int[] {left - 2, doorX, left + w + 2}, new int[] {yb - 44, yb - 58, yb - 44}, 3);
                    fill(shade(style.trim, -16), left - 4, yb - 4, w + 8, 4);
                    break;
                }
                case PORCH: {
                    int w = 44;
                    int left = doorX - w / 2;
                    entranceTop = yb - 46;
                    fill(DOORWAY, doorX - 9, yb - 34, 18, 34);
                    fill(shade(DOORWAY, 40), doorX + 4, yb - 18, 2, 2);
                    fill(style.roofColour, left - 2, yb - 44, w + 4, 8);
                    fill(shade(style.roofColour, -20), left - 2, yb - 37, w + 4, 2);
                    for (int px : new int[] {left, left + w - 4}) {
                        fill(style.trim, px, yb - 36, 4, 32);
                    }
                    fill(style.trim, left, yb - 20, w, 2);
                    fill(shade(style.trim, -24), left - 4, yb - 4, w + 8, 4);
                    box(left - 2, yb - 44, w + 4, 8);
                    break;
                }
                case GRAND_STAIRS: {
                    int w = 64;
                    int left = doorX - w / 2;
                    entranceTop = yb - 50;
                    fill(style.trim, left + 8, yb - 50, w - 16, 6);
                    fill(DOORWAY, doorX - 12, yb - 42, 24, 34);
                    fill(shade(DOORWAY, 24), doorX - 1, yb - 40, 2, 32);
                    for (int i = 0; i < 4; i++) {
                        fill(shade(style.trim, -8 * i), left + 6 - i * 3, yb - 8 + i * 2, w - 12 + i * 6, 2);
                    }
                    for (int cx : new int[] {left + 8, left + w - 13}) {
                        fill(style.trim, cx, yb - 44, 5, 36);
                        fill(shade(style.trim, -40), cx + 4, yb - 44, 1, 36);
                    }
                    break;
                }
                case RECESSED: {
                    int w = Math.min(72, x1 - x0 - 24);
                    int left = Math.max(x0 + 6, Math.min(x1 - 6 - w, doorX - w / 2));
                    entranceTop = yb - 42;
                    fill(RECESS, left, yb - 40, w, 40);
                    fill(shade(RECESS, -16), left, yb - 40, w, 6);
                    for (int x = left + 4; x + 10 <= left + w - 4; x += 14) {
                        fill(DOOR_GLASS, x, yb - 32, 10, 32);
                        fill(GLASS_LIGHT, x, yb - 32, 10, 2);
                    }
                    fill(WARM, left + 6, yb - 30, 6, 6);
                    box(left, yb - 40, w, 40);
                    break;
                }
                case TERRACE_STAIR: {
                    entranceTop = yb - 40;
                    fill(DOOR_GLASS, doorX - 13, yb - 34, 26, 34);
                    fill(GLASS_LIGHT, doorX - 13, yb - 34, 26, 3);
                    fill(shade(DOOR_GLASS, 30), doorX - 1, yb - 31, 2, 31);
                    break;
                }
                case COLONNADE: {
                    entranceTop = yb - GROUND_FLOOR;
                    fill(DOOR_GLASS, doorX - 13, yb - 36, 26, 30);
                    fill(GLASS_LIGHT, doorX - 13, yb - 36, 26, 3);
                    fill(shade(DOOR_GLASS, 30), doorX - 1, yb - 33, 2, 27);
                    break;
                }
                default: {
                    entranceTop = yb - 42;
                    fill(shade(style.wall, -50), doorX - 16, yb - 36, 32, 36);
                    fill(DOOR_GLASS, doorX - 14, yb - 34, 28, 34);
                    fill(GLASS_LIGHT, doorX - 14, yb - 34, 28, 3);
                    fill(shade(DOOR_GLASS, 30), doorX - 1, yb - 31, 2, 31);
                    Color canopy = style.texture == BuildingStyle.Texture.GLASS ? style.trim : shade(style.wall, -30);
                    fill(canopy, doorX - 22, yb - 42, 44, 5);
                    fill(shade(canopy, -40), doorX - 22, yb - 37, 44, 1);
                    break;
                }
            }
        }

        private void sign() {
            String text = plan.getSign();
            if (text.isEmpty()) {
                return;
            }
            if (style.carvedSign) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                FontMetrics metrics = g.getFontMetrics();
                String upper = text.toUpperCase();
                int w = metrics.stringWidth(upper);
                int sx = Math.max(x0 + 4, Math.min(x1 - 4 - w, doorX - w / 2));
                int sy = entranceTop - 6;
                g.setColor(shade(style.wall, 24));
                g.drawString(upper, sx + 1, sy + 1);
                g.setColor(shade(style.wall, -90));
                g.drawString(upper, sx, sy);
                return;
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
            FontMetrics metrics = g.getFontMetrics();
            int w = metrics.stringWidth(text) + 8;
            int h = 12;
            int sx = Math.max(x0 + 4, Math.min(x1 - 4 - w, doorX - w / 2));
            int sy = Math.max(wallTop + 8, entranceTop - h - 3);
            fill(SIGN, sx, sy, w, h);
            fill(shade(SIGN, 40), sx, sy, w, 1);
            g.setColor(Color.WHITE);
            g.drawString(text, sx + 4, sy + 9);
            box(sx, sy, w, h);
        }
    }
}
