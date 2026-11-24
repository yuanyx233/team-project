package view.map;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class CharacterSheetTest {
    private static int lowestOpaqueRow(BufferedImage frame) {
        for (int y = frame.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < frame.getWidth(); x++) {
                if ((frame.getRGB(x, y) >>> 24) != 0) {
                    return y;
                }
            }
        }
        return -1;
    }

    @Test
    void composesEightFramesInFourDirections() {
        CharacterSheet sheet = CharacterSheet.compose(CharacterLook.player());
        for (CharacterSheet.Direction direction : CharacterSheet.Direction.values()) {
            for (int i = 0; i < CharacterSheet.FRAMES; i++) {
                BufferedImage frame = sheet.frame(direction, i);
                assertEquals(CharacterSheet.WIDTH, frame.getWidth());
                assertEquals(CharacterSheet.HEIGHT, frame.getHeight());
                int lowest = lowestOpaqueRow(frame);
                assertTrue(lowest >= CharacterSheet.HEIGHT - 3, direction + " " + i + " feet at " + lowest);
            }
        }
    }

    @Test
    void sameLookGivesSameFrames() {
        CharacterLook look = CharacterLook.forNumber(7);
        CharacterSheet a = CharacterSheet.compose(look);
        CharacterSheet b = CharacterSheet.compose(look);
        for (CharacterSheet.Direction direction : CharacterSheet.Direction.values()) {
            for (int i = 0; i < CharacterSheet.FRAMES; i++) {
                for (int y = 0; y < CharacterSheet.HEIGHT; y++) {
                    for (int x = 0; x < CharacterSheet.WIDTH; x++) {
                        assertEquals(a.frame(direction, i).getRGB(x, y), b.frame(direction, i).getRGB(x, y));
                    }
                }
            }
        }
    }

    @Test
    void hoodieColourIsApplied() {
        Color red = new Color(0xB8, 0x3C, 0x3A);
        CharacterLook look = new CharacterLook(0, CharacterLook.HairStyle.SHORT, 0, red,
                new Color(0x4A, 0x5C, 0x8E), Color.WHITE, null, null);
        BufferedImage frame = CharacterSheet.compose(look).frame(CharacterSheet.Direction.DOWN, 0);
        int close = 0;
        int opaque = 0;
        for (int y = 15; y <= 21; y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                int argb = frame.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                opaque++;
                Color c = new Color(argb);
                double distance = Math.abs(c.getRed() - red.getRed()) + Math.abs(c.getGreen() - red.getGreen())
                        + Math.abs(c.getBlue() - red.getBlue());
                close += distance < 120 ? 1 : 0;
            }
        }
        assertTrue(close * 2 > opaque, "torso pixels close to the hoodie colour: " + close + " of " + opaque);
    }

    @Test
    void composedSheetsAreCachedPerLook() {
        assertSame(CharacterSheet.of(CharacterLook.player()), CharacterSheet.of(CharacterLook.player()));
    }
}
