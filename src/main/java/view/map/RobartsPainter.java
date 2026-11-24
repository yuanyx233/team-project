package view.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Robarts Library has its own painter because its look comes from its massing, which the shared
 * facade-and-roof template cannot show. Seen in 3/4 view from St. George it is drawn as: blank concrete
 * towers at both ends; a dark roof whose front edge dips into a V (the triangular "peacock" plan); two
 * wings of dense vertical concrete fins with narrow windows between them, the left lit and the right
 * in shade; a central volume projecting forward with a column of stacked windows; a heavy band where
 * the upper floors overhang a recessed ground floor; and the glass entrance pavilion at the lower right
 * with ROBARTS above it. All sizes scale with the footprint.
 */
final class RobartsPainter {
    static final Color ROOF = new Color(0x4E, 0x4A, 0x5C);
    static final Color WINDOW = new Color(0x3A, 0x34, 0x40);
    static final Color LIT = new Color(0xE8, 0xB8, 0x68);
    private static final Color ROOF_SEAM = new Color(0x44, 0x40, 0x52);
    private static final Color RIM = new Color(0xE6, 0xD6, 0xC8);
    private static final Color CONCRETE = new Color(0xCD, 0xB8, 0xA8);
    private static final Color CONCRETE_LIGHT = new Color(0xE2, 0xD0, 0xC2);
    private static final Color CONCRETE_SHADE = new Color(0xB0, 0x9C, 0x8E);
    private static final Color CONCRETE_DARK = new Color(0x8E, 0x7C, 0x72);
    private static final Color RECESS_LIT = new Color(0x7A, 0x6A, 0x66);
    private static final Color RECESS_SHADE = new Color(0x5E, 0x52, 0x50);
    private static final Color GROUND = new Color(0x3C, 0x36, 0x3A);
    private static final Color GLASS = new Color(0x3E, 0x58, 0x72);
    private static final Color GLASS_LIGHT = new Color(0x7E, 0x9E, 0xB8);
    private static final Color OUTLINE = new Color(0x34, 0x2C, 0x2C);
    private static final int FIN_PITCH = 12;
    private static final int STOREY = 36;

    private RobartsPainter() {
    }

    /** Draws Robarts over its footprint. */
    static void paint(BufferedImage image, BuildingPlan plan) {
        int tile = GroundPainter.PIXELS_PER_TILE;
        int x0 = plan.getLeft() * tile;
        int y0 = plan.getTop() * tile;
        int w = (plan.getRight() - plan.getLeft() + 1) * tile;
        int h = (plan.getBottom() - plan.getTop() + 1) * tile;
        Graphics2D g = image.createGraphics();
        try {
            g.clipRect(x0, y0, w, h);
            g.translate(x0, y0);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            new Layout(g, w, h, plan).draw();
        } finally {
            g.dispose();
        }
    }

    /** Proportions of the building within its footprint, in local art pixels. */
    private static final class Layout {
        private final Graphics2D g;
        private final BuildingPlan plan;
        private final int w;
        private final int h;
        private final int tower;
        private final int vTop;
        private final int vBottom;
        private final int prowLeft;
        private final int prowRight;
        private final int prowTop;
        private final int band;
        private final int pavilionLeft;
        private final int pavilionRight;

        Layout(Graphics2D g, int w, int h, BuildingPlan plan) {
            this.g = g;
            this.plan = plan;
            this.w = w;
            this.h = h;
            tower = Math.max(24, w * 11 / 100);
            vTop = Math.max(24, h * 25 / 100);
            vBottom = h * 48 / 100;
            int prow = Math.max(28, w * 16 / 100);
            prowLeft = w / 2 - prow / 2;
            prowRight = prowLeft + prow;
            prowTop = vBottom - h * 8 / 100;
            band = h - Math.max(44, h * 21 / 100);
            pavilionRight = w - tower - 6;
            pavilionLeft = pavilionRight - Math.max(40, w * 18 / 100);
        }

        void draw() {
            fill(CONCRETE, 0, 0, w, h);
            roof();
            wings();
            prow();
            towers();
            overhang();
            pavilion();
            g.setColor(OUTLINE);
            g.drawRect(0, 0, w - 1, h - 1);
        }

        private void fill(Color c, int x, int y, int fw, int fh) {
            g.setColor(c);
            g.fillRect(x, y, fw, fh);
        }

        /** The y of the roof's front edge at local x (the V between the towers). */
        private int edgeAt(int x) {
            int mid = w / 2;
            double t = Math.abs(x - mid) / (double) (mid - tower);
            return (int) Math.round(vBottom + (vTop - vBottom) * Math.min(1, t));
        }

        private void roof() {
            int[] xs = {tower, w - tower, w - tower, w / 2, tower};
            int[] ys = {10, 10, vTop, vBottom, vTop};
            g.setColor(ROOF);
            g.fillPolygon(xs, ys, 5);
            g.setColor(ROOF_SEAM);
            for (int y = 26; y < vBottom - 10; y += 22) {
                for (int x = tower + 6; x < w - tower - 6; x++) {
                    if (y < edgeAt(x) - 8 && Math.floorMod(x, 4) != 0) {
                        g.fillRect(x, y, 1, 1);
                    }
                }
            }
            // Rooftop plant rooms near the back.
            for (int k = 0; k < 2; k++) {
                int bx = k == 0 ? tower + (w - 2 * tower) / 5 : w - tower - (w - 2 * tower) / 5 - 34;
                fill(CONCRETE_LIGHT, bx, 18, 34, 10);
                fill(CONCRETE_SHADE, bx, 28, 34, 10);
                g.setColor(OUTLINE);
                g.drawRect(bx, 18, 33, 19);
            }
            // Concrete parapet following the V.
            g.setColor(RIM);
            for (int t = 0; t < 6; t++) {
                g.drawLine(tower, vTop + t, w / 2, vBottom + t);
                g.drawLine(w / 2, vBottom + t, w - tower, vTop + t);
            }
            g.setColor(OUTLINE);
            g.drawLine(tower, vTop + 6, w / 2, vBottom + 6);
            g.drawLine(w / 2, vBottom + 6, w - tower, vTop + 6);
        }

        private void wings() {
            for (int x = tower; x < w - tower; x++) {
                if (x >= prowLeft && x < prowRight) {
                    continue;
                }
                boolean left = x < w / 2;
                Color fin = left ? CONCRETE_LIGHT : CONCRETE_SHADE;
                Color recess = left ? RECESS_LIT : RECESS_SHADE;
                int phase = Math.floorMod(x - tower, FIN_PITCH);
                int top = edgeAt(x) + 7;
                boolean isFin = phase < 5;
                fill(isFin ? fin : recess, x, top, 1, band - top);
                if (!isFin && phase >= 7 && phase <= 9) {
                    for (int y = band - STOREY + 6; y > top + 4; y -= STOREY) {
                        int slot = (x - tower) / FIN_PITCH;
                        boolean lit = ArtHash.of(slot, y / STOREY, 500) % 4 == 0;
                        fill(lit ? LIT : WINDOW, x, y, 1, 22);
                    }
                }
                if (phase == 4) {
                    fill(left ? CONCRETE : CONCRETE_DARK, x, top, 1, band - top);
                }
            }
            // Shadow the prow casts on the shaded wing.
            fill(new Color(40, 30, 30, 70), prowRight, prowTop + 10, 10, band - prowTop - 10);
        }

        private void prow() {
            fill(CONCRETE_LIGHT, prowLeft, prowTop, prowRight - prowLeft, 10);
            fill(CONCRETE, prowLeft, prowTop + 10, prowRight - prowLeft, band - prowTop - 10);
            fill(CONCRETE_LIGHT, prowLeft, prowTop + 10, 4, band - prowTop - 10);
            fill(CONCRETE_DARK, prowRight - 5, prowTop + 10, 5, band - prowTop - 10);
            int cw = Math.max(10, (prowRight - prowLeft) / 4);
            int cx = (prowLeft + prowRight) / 2 - cw / 2;
            fill(RECESS_LIT, cx - 3, prowTop + 18, cw + 6, band - prowTop - 24);
            for (int y = prowTop + 22; y + 14 < band - 6; y += 20) {
                boolean lit = ArtHash.of(cx, y, 501) % 3 != 0;
                fill(lit ? LIT : WINDOW, cx, y, cw, 14);
                fill(CONCRETE, cx, y + 14, cw, 2);
            }
            g.setColor(OUTLINE);
            g.drawRect(prowLeft, prowTop, prowRight - prowLeft - 1, band - prowTop - 1);
        }

        private void towers() {
            for (int side = 0; side < 2; side++) {
                int tx = side == 0 ? 0 : w - tower;
                Color face = side == 0 ? CONCRETE_LIGHT : CONCRETE_SHADE;
                fill(RIM, tx, 0, tower, 10);
                fill(face, tx, 10, tower, h - 10);
                g.setColor(side == 0 ? CONCRETE : CONCRETE_DARK);
                for (int y = 10 + 46; y < h; y += 46) {
                    g.drawLine(tx, y, tx + tower - 1, y);
                }
                fill(side == 0 ? CONCRETE_SHADE : CONCRETE_DARK, side == 0 ? tx + tower - 5 : tx, 10, 5, h - 10);
                g.setColor(OUTLINE);
                g.drawRect(tx, 0, tower - 1, h - 1);
                g.drawLine(tx, 10, tx + tower - 1, 10);
            }
        }

        private void overhang() {
            fill(CONCRETE_LIGHT, tower, band, w - 2 * tower, 12);
            fill(CONCRETE_SHADE, tower, band + 10, w - 2 * tower, 2);
            fill(GROUND, tower, band + 12, w - 2 * tower, h - band - 12);
            fill(new Color(0x24, 0x20, 0x24), tower, band + 12, w - 2 * tower, 5);
            for (int x = tower + 20; x < w - tower - 8; x += 40) {
                fill(CONCRETE, x, band + 12, 7, h - band - 12);
                fill(CONCRETE_DARK, x + 7, band + 12, 2, h - band - 12);
            }
            for (int x = tower + 34; x + 14 < pavilionLeft; x += 40) {
                fill(GLASS, x, band + 22, 14, h - band - 26);
                fill(GLASS_LIGHT, x, band + 22, 14, 2);
            }
            g.setColor(OUTLINE);
            g.drawLine(tower, band, w - tower - 1, band);
            g.drawLine(tower, band + 12, w - tower - 1, band + 12);
        }

        private void pavilion() {
            int top = band - 8;
            int pw = pavilionRight - pavilionLeft;
            fill(new Color(0xD8, 0xD4, 0xCE), pavilionLeft - 2, top - 6, pw + 4, 6);
            fill(GLASS, pavilionLeft, top, pw, h - top);
            fill(LIT, pavilionLeft + 4, h - 28, pw - 8, 12);
            g.setColor(new Color(0x2E, 0x40, 0x54));
            for (int x = pavilionLeft + 10; x < pavilionRight; x += 10) {
                g.drawLine(x, top, x, h - 1);
            }
            g.drawLine(pavilionLeft, top + (h - top) / 2, pavilionRight - 1, top + (h - top) / 2);
            g.setColor(GLASS_LIGHT);
            for (int k = 0; k < pw; k += 3) {
                g.fillRect(pavilionLeft + k, top + 4 + k / 3, 1, 1);
            }
            g.setColor(OUTLINE);
            g.drawRect(pavilionLeft - 2, top - 6, pw + 3, h - top + 5);
            String text = plan.getSign().isEmpty() ? "" : plan.getSign().toUpperCase();
            if (!text.isEmpty()) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                FontMetrics metrics = g.getFontMetrics();
                // Cast into the overhang band just left of the pavilion, dark on light concrete.
                int tx = Math.max(tower + 4, pavilionLeft - 10 - metrics.stringWidth(text));
                g.setColor(new Color(0x5A, 0x4C, 0x46));
                g.drawString(text, tx, band + 10);
            }
        }
    }
}
