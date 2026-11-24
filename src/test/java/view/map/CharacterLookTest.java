package view.map;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CharacterLookTest {
    @Test
    void sameNumberGivesSameLook() {
        assertEquals(CharacterLook.forNumber(42), CharacterLook.forNumber(42));
    }

    @Test
    void studentsVary() {
        Set<Color> hoodies = new HashSet<>();
        Set<CharacterLook> looks = new HashSet<>();
        int backpacks = 0;
        int caps = 0;
        for (int n = 0; n < 20; n++) {
            CharacterLook look = CharacterLook.forNumber(n);
            hoodies.add(look.getHoodie());
            looks.add(look);
            backpacks += look.getBackpack() != null ? 1 : 0;
            caps += look.getCap() != null ? 1 : 0;
        }
        assertTrue(hoodies.size() >= 5, "hoodie colours: " + hoodies.size());
        assertTrue(looks.size() >= 15, "distinct looks: " + looks.size());
        assertTrue(backpacks > 0 && backpacks < 20, "backpacks: " + backpacks);
        assertTrue(caps > 0 && caps < 20, "caps: " + caps);
    }

    @Test
    void playerWearsUofTBlue() {
        assertEquals(CharacterLook.UOFT_BLUE, CharacterLook.player().getHoodie());
    }

    @Test
    void hairColourIsValidForItsStyle() {
        for (int n = 0; n < 50; n++) {
            CharacterLook look = CharacterLook.forNumber(n);
            assertTrue(look.getHairColour() >= 0 && look.getHairColour() < look.getHairStyle().colours());
            assertTrue(look.getSkinTone() >= 0 && look.getSkinTone() < CharacterLook.SKIN_TONES);
        }
    }
}
