package view.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterArtTest {
    @Test
    void allBundledImagesLoad() {
        CharacterArt art = CharacterArt.get();
        assertEquals(360, art.getBody().getWidth());
        assertEquals(244, art.getHair().getWidth());
        assertEquals(536, art.getHoodie().getWidth());
        assertEquals(536, art.getJeans().getWidth());
        assertEquals(536, art.getShoes().getWidth());
        assertEquals(160, art.getSideWalk().getWidth());
    }

    @Test
    void creditsFileIsBundled() {
        assertNotNull(CharacterArt.class.getResource("/characters/CREDITS.md"));
    }

    @Test
    void missingImageIsNamed() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> CharacterArt.load("/characters/missing.png"));
        assertTrue(e.getMessage().contains("/characters/missing.png"), e.getMessage());
    }
}
