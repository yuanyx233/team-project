package view.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Walking frames for one {@link CharacterLook}, composed from the bundled CC0 character art.
 * Down and up frames stack the body with the jeans, shoes and hoodie layers; left and right frames
 * take the smooth side-walk strip (which has no clothes) and dress it by height bands. Clothes are
 * tinted to the look's colours by brightness, skin is toned, and hair, backpack and cap are added.
 * One sprite pixel is one map pixel; the feet stand on the bottom row of each frame.
 */
public final class CharacterSheet {
    /** Walking directions. */
    public enum Direction { DOWN, UP, LEFT, RIGHT }

    public static final int FRAMES = 8;
    public static final int WIDTH = 20;
    public static final int HEIGHT = 35;

    // Layout of the bundled sheets.
    private static final int CELL = 16;
    private static final int BODY_X = 11;
    private static final int DOWN_Y = 10;
    private static final int UP_Y = 80;
    private static final int CLOTHES_SHIFT = 7;
    private static final int SIDE_FRAME = 20;
    private static final int SIDE_TOP = 2;
    /** Per-channel skin scaling for each tone, lightest first. */
    private static final double[][] SKIN = {
            {1.0, 1.0, 1.0}, {0.97, 0.86, 0.72}, {0.84, 0.68, 0.52}, {0.64, 0.47, 0.35}, {0.47, 0.33, 0.25}};
    private static final Map<CharacterLook, CharacterSheet> CACHE = new HashMap<>();

    private final BufferedImage[][] frames;

    private CharacterSheet(BufferedImage[][] frames) {
        this.frames = frames;
    }

    /** The composed sheet for a look, built once and then reused. */
    public static synchronized CharacterSheet of(CharacterLook look) {
        return CACHE.computeIfAbsent(look, CharacterSheet::compose);
    }

    /** Composes a fresh sheet for a look. */
    public static CharacterSheet compose(CharacterLook look) {
        CharacterArt art = CharacterArt.get();
        BufferedImage[][] frames = new BufferedImage[Direction.values().length][FRAMES];
        for (int i = 0; i < FRAMES; i++) {
            frames[Direction.DOWN.ordinal()][i] = front(art, look, DOWN_Y, i, CharacterHair.View.FRONT);
            frames[Direction.UP.ordinal()][i] = front(art, look, UP_Y, i, CharacterHair.View.BACK);
            BufferedImage right = side(art, look, i);
            frames[Direction.RIGHT.ordinal()][i] = right;
            frames[Direction.LEFT.ordinal()][i] = mirror(right);
        }
        return new CharacterSheet(frames);
    }

    /** Frame {@code index} (0-7, wrapping) of the walk in a direction. */
    public BufferedImage frame(Direction direction, int index) {
        return frames[direction.ordinal()][Math.floorMod(index, FRAMES)];
    }

    /**
     * The standing pose: the first front or back frame, and for side views the stride frame with the
     * feet together (the side-walk strip starts mid-stride).
     */
    public BufferedImage standing(Direction direction) {
        boolean side = direction == Direction.LEFT || direction == Direction.RIGHT;
        return frame(direction, side ? SIDE_STANDING : 0);
    }

    private static final int SIDE_STANDING = 2;

    // ------------------------------------------------------------------ down / up

    private static BufferedImage front(CharacterArt art, CharacterLook look, int rowY, int index,
                                       CharacterHair.View view) {
        BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int x = BODY_X + index * CELL;
        int offset = (WIDTH - CELL) / 2;
        copy(art.getBody(), x, rowY, CELL, HEIGHT, frame, offset, look, Layer.SKIN);
        int cx = x + CLOTHES_SHIFT;
        int cy = rowY + CLOTHES_SHIFT;
        copy(art.getJeans(), cx, cy, CELL, HEIGHT, frame, offset, look, Layer.TROUSERS);
        copy(art.getShoes(), cx, cy, CELL, HEIGHT, frame, offset, look, Layer.SHOES);
        copy(art.getHoodie(), cx, cy, CELL, HEIGHT, frame, offset, look, Layer.HOODIE);
        CharacterHair.dress(frame, look, view);
        return frame;
    }

    private enum Layer { SKIN, TROUSERS, SHOES, HOODIE }

    /** Copies a region of a layer onto the frame, recolouring it for the look. */
    private static void copy(BufferedImage source, int sx, int sy, int width, int height, BufferedImage frame,
                             int dx, CharacterLook look, Layer layer) {
        double reference = layer == Layer.SKIN ? 0 : averageBrightness(source);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int px = sx + x;
                int py = sy + y;
                if (px >= source.getWidth() || py >= source.getHeight()) {
                    continue;
                }
                int argb = source.getRGB(px, py);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                int recoloured = layer == Layer.SKIN ? skin(argb, look.getSkinTone())
                        : tint(argb, colourOf(look, layer), reference);
                frame.setRGB(dx + x, y, recoloured);
            }
        }
    }

    private static Color colourOf(CharacterLook look, Layer layer) {
        switch (layer) {
            case TROUSERS:
                return look.getTrousers();
            case SHOES:
                return look.getShoes();
            default:
                return look.getHoodie();
        }
    }

    // ------------------------------------------------------------------ left / right

    /** A right-facing side frame: the bare side-walk strip dressed by height bands. */
    private static BufferedImage side(CharacterArt art, CharacterLook look, int index) {
        BufferedImage strip = art.getSideWalk();
        BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int top = HEIGHT;
        int bottom = 0;
        for (int y = 0; y < strip.getHeight(); y++) {
            for (int x = 0; x < SIDE_FRAME; x++) {
                if ((strip.getRGB(index * SIDE_FRAME + x, y) >>> 24) != 0) {
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        double hoodieRef = averageBrightness(art.getHoodie());
        double trouserRef = averageBrightness(art.getJeans());
        double shoeRef = averageBrightness(art.getShoes());
        for (int y = 0; y < strip.getHeight(); y++) {
            for (int x = 0; x < SIDE_FRAME; x++) {
                int argb = strip.getRGB(index * SIDE_FRAME + x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                int fromTop = y - top;
                int colour;
                if (fromTop < 13) {
                    colour = skin(argb, look.getSkinTone());
                } else if (fromTop < 21) {
                    colour = tint(argb, look.getHoodie(), hoodieRef * 1.25);
                } else if (y < bottom - 1) {
                    colour = tint(argb, look.getTrousers(), trouserRef * 1.25);
                } else {
                    colour = tint(argb, look.getShoes(), shoeRef * 1.25);
                }
                frame.setRGB(x, y + SIDE_TOP, colour);
            }
        }
        CharacterHair.dress(frame, look, CharacterHair.View.SIDE);
        return frame;
    }

    private static BufferedImage mirror(BufferedImage image) {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(image, image.getWidth(), 0, -image.getWidth(), image.getHeight(), null);
        g.dispose();
        return out;
    }

    // ------------------------------------------------------------------ colour

    private static int brightness(int argb) {
        return (((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 3;
    }

    private static final Map<BufferedImage, Double> BRIGHTNESS = new HashMap<>();

    /** Mean brightness of a layer's opaque pixels, so tinting keeps its light and dark folds. */
    private static synchronized double averageBrightness(BufferedImage layer) {
        return BRIGHTNESS.computeIfAbsent(layer, image -> {
            long sum = 0;
            int count = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    if ((argb >>> 24) != 0) {
                        sum += brightness(argb);
                        count++;
                    }
                }
            }
            return count == 0 ? 128.0 : sum / (double) count;
        });
    }

    /** Recolours a pixel to {@code colour}, keeping how much lighter or darker it is than the layer average. */
    static int tint(int argb, Color colour, double reference) {
        double factor = brightness(argb) / reference;
        int r = clamp(colour.getRed() * factor);
        int g = clamp(colour.getGreen() * factor);
        int b = clamp(colour.getBlue() * factor);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** Tones a skin pixel; eyes, outlines and anything not skin-coloured are left alone. */
    static int skin(int argb, int tone) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        if (!(r > g && g > b && r - b > 30)) {
            return argb;
        }
        double[] scale = SKIN[tone];
        return (argb & 0xFF000000) | (clamp(r * scale[0]) << 16) | (clamp(g * scale[1]) << 8) | clamp(b * scale[2]);
    }

    private static int clamp(double value) {
        return (int) Math.max(0, Math.min(255, Math.round(value)));
    }
}
