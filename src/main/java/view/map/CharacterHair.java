package view.map;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Puts hair, backpack and cap on a composed character frame. Hair pieces come from the bundled
 * hair sheet (rectangles below, inclusive, in sheet pixels); the backpack and cap are drawn here.
 * Everything is placed relative to the head found in the frame itself.
 */
final class CharacterHair {
    /** Which side of the head is visible. */
    enum View { FRONT, SIDE, BACK }

    /** [style][colour][view] = {x0, y0, x1, y1} in the hair sheet. Styles follow {@link CharacterLook.HairStyle}. */
    private static final int[][][][] PIECES = {
            { // SHORT: the male style, three colours
                    {{27, 170, 40, 181}, {29, 189, 40, 200}, {28, 207, 40, 219}},
                    {{69, 170, 82, 181}, {71, 189, 82, 200}, {70, 207, 82, 219}},
                    {{112, 170, 125, 181}, {114, 189, 125, 200}, {113, 207, 125, 219}},
            },
            { // LONG: first female style, four colours
                    {{27, 37, 45, 59}, {30, 72, 43, 95}, {29, 106, 43, 128}},
                    {{79, 37, 97, 59}, {82, 72, 95, 95}, {81, 106, 95, 128}},
                    {{134, 37, 152, 59}, {137, 72, 150, 95}, {136, 106, 150, 128}},
                    {{191, 37, 209, 59}, {194, 72, 207, 95}, {193, 106, 207, 128}},
            },
            { // WAVY: second female style, four colours
                    {{50, 37, 66, 59}, {53, 72, 67, 95}, {53, 106, 70, 128}},
                    {{102, 37, 118, 59}, {105, 72, 119, 95}, {105, 106, 122, 128}},
                    {{157, 37, 173, 59}, {160, 72, 174, 95}, {160, 106, 177, 128}},
                    {{214, 37, 230, 59}, {217, 72, 231, 95}, {217, 106, 234, 128}},
            },
    };
    private static final Color OUTLINE = new Color(0x30, 0x26, 0x28);

    private CharacterHair() {
    }

    /**
     * Adds hair and accessories to a frame. Side frames always face right here (left frames are
     * mirrored afterwards), which is also how the sheet's side hair pieces are drawn.
     */
    static void dress(BufferedImage frame, CharacterLook look, View view) {
        int top = headTop(frame);
        int[] cols = headColumns(frame, top);
        int left = cols[0];
        int right = cols[1];
        int centre = (left + right) / 2;
        if (look.getBackpack() != null && view != View.BACK) {
            backpack(frame, look.getBackpack(), view, top, left, right, centre);
        }
        int[] box = PIECES[look.getHairStyle().ordinal()][look.getHairColour()][view.ordinal()];
        BufferedImage hair = CharacterArt.get().getHair();
        int width = box[2] - box[0] + 1;
        int height = box[3] - box[1] + 1;
        int x0 = left + (right - left + 1 - width) / 2;
        int y0 = top - 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = hair.getRGB(box[0] + x, box[1] + y);
                if ((argb >>> 24) != 0) {
                    set(frame, x0 + x, y0 + y, argb);
                }
            }
        }
        if (look.getBackpack() != null && view == View.BACK) {
            backpack(frame, look.getBackpack(), view, top, left, right, centre);
        }
        if (look.getCap() != null) {
            cap(frame, look.getCap(), view, top, left, right, centre);
        }
    }

    private static int headTop(BufferedImage frame) {
        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                if ((frame.getRGB(x, y) >>> 24) != 0) {
                    return y;
                }
            }
        }
        return 0;
    }

    /** Leftmost and rightmost opaque columns in the ten rows from the top of the head. */
    private static int[] headColumns(BufferedImage frame, int top) {
        int left = frame.getWidth();
        int right = 0;
        for (int y = top; y < Math.min(frame.getHeight(), top + 10); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                if ((frame.getRGB(x, y) >>> 24) != 0) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
            }
        }
        return new int[] {left, right};
    }

    private static void backpack(BufferedImage frame, Color colour, View view, int top, int left, int right,
                                 int centre) {
        int strap = colour.darker().getRGB();
        if (view == View.FRONT) {
            for (int y = top + 14; y <= top + 20; y++) {
                set(frame, centre - 4, y, strap);
                set(frame, centre + 4, y, strap);
            }
        } else if (view == View.BACK) {
            fill(frame, centre - 5, top + 14, centre + 5, top + 24, colour.getRGB());
            fill(frame, centre - 4, top + 17, centre + 4, top + 17, colour.brighter().getRGB());
            outline(frame, centre - 5, top + 14, centre + 5, top + 24);
        } else {
            // Side frames face right before mirroring, so the pack sits on the left of the body.
            int back = left + 1;
            fill(frame, back - 3, top + 14, back, top + 23, colour.getRGB());
            outline(frame, back - 3, top + 14, back, top + 23);
        }
    }

    private static void cap(BufferedImage frame, Color colour, View view, int top, int left, int right,
                            int centre) {
        int crown = colour.getRGB();
        int brim = colour.darker().getRGB();
        fill(frame, left + 1, top - 1, right - 1, top + 3, crown);
        fill(frame, centre - 1, top - 2, centre + 1, top - 2, crown);
        outline(frame, left + 1, top - 1, right - 1, top + 3);
        if (view == View.FRONT) {
            fill(frame, left, top + 4, right, top + 4, brim);
        } else if (view == View.SIDE) {
            fill(frame, centre, top + 4, right + 3, top + 4, brim);
        }
    }

    private static void fill(BufferedImage frame, int x0, int y0, int x1, int y1, int argb) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                set(frame, x, y, argb);
            }
        }
    }

    private static void outline(BufferedImage frame, int x0, int y0, int x1, int y1) {
        int dark = OUTLINE.getRGB();
        for (int x = x0; x <= x1; x++) {
            set(frame, x, y0, dark);
            set(frame, x, y1, dark);
        }
        for (int y = y0; y <= y1; y++) {
            set(frame, x0, y, dark);
            set(frame, x1, y, dark);
        }
    }

    private static void set(BufferedImage frame, int x, int y, int argb) {
        if (x >= 0 && y >= 0 && x < frame.getWidth() && y < frame.getHeight()) {
            frame.setRGB(x, y, argb);
        }
    }
}
