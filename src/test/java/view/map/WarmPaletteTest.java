package view.map;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class WarmPaletteTest {
    private static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    private static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    private static int blue(int argb) {
        return argb & 0xFF;
    }

    @Test
    void neutralGreyBecomesWarm() {
        int out = WarmPalette.warm(0xFF808080);
        assertTrue(red(out) > blue(out), "red should exceed blue after warming");
    }

    @Test
    void brightGreenIsToneDown() {
        int in = 0xFF62B248; // the old lawn green
        int out = WarmPalette.warm(in);
        int spreadBefore = green(in) - blue(in);
        int spreadAfter = green(out) - blue(out);
        assertTrue(spreadAfter < spreadBefore + 20, "green should not get more saturated");
        assertTrue(green(out) <= green(in), "green should not get brighter");
    }

    @Test
    void alphaIsKept() {
        assertEquals(0x00, WarmPalette.warm(0x00808080) >>> 24);
        assertEquals(0x7F, WarmPalette.warm(0x7F808080) >>> 24);
    }

    @Test
    void channelsStayInRange() {
        int white = WarmPalette.warm(0xFFFFFFFF);
        int black = WarmPalette.warm(0xFF000000);
        for (int c : new int[] {red(white), green(white), blue(white), red(black), green(black), blue(black)}) {
            assertTrue(c >= 0 && c <= 255);
        }
    }

    @Test
    void applyReturnsWarmedCopyOfSameSize() {
        BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFF808080);
        image.setRGB(1, 0, 0x00000000);
        BufferedImage out = WarmPalette.apply(image);
        assertEquals(2, out.getWidth());
        assertEquals(1, out.getHeight());
        assertEquals(WarmPalette.warm(0xFF808080), out.getRGB(0, 0));
        assertEquals(0xFF808080, image.getRGB(0, 0), "source image is not modified");
    }
}
