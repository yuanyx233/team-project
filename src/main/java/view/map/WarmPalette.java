package view.map;

import java.awt.image.BufferedImage;

/**
 * Gives map artwork a warmer, softer look: colours are pulled slightly toward grey
 * (so the lawn green is less neon) and then shifted toward red and away from blue.
 * Applied once when the artwork is loaded, so drawing each frame costs nothing extra.
 */
public final class WarmPalette {
    /** How far each colour moves toward its own grey (0 keeps it, 1 makes it grey). */
    private static final double DESATURATE = 0.18;
    /** Overall darkening, so bright greens and pale paving read less harsh. */
    private static final double DIM = 0.95;

    private WarmPalette() {
    }

    /** Returns a warmed copy of {@code image}; the original is left unchanged. */
    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = warm(pixels[i]);
        }
        out.setRGB(0, 0, width, height, pixels, 0, width);
        return out;
    }

    /** Warms one ARGB pixel, keeping its alpha. */
    public static int warm(int argb) {
        int alpha = argb >>> 24;
        double r = (argb >> 16) & 0xFF;
        double g = (argb >> 8) & 0xFF;
        double b = argb & 0xFF;
        double grey = 0.299 * r + 0.587 * g + 0.114 * b;
        r += (grey - r) * DESATURATE;
        g += (grey - g) * DESATURATE;
        b += (grey - b) * DESATURATE;
        int red = clamp((r * 1.04 + 8) * DIM);
        int green = clamp((g * 0.98 + 3) * DIM);
        int blue = clamp((b * 0.84 + 2) * DIM);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int clamp(double value) {
        return (int) Math.max(0, Math.min(255, Math.round(value)));
    }
}
