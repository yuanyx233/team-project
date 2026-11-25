package view.map;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpriteLayerTest {
    @Test
    void parsesOnePlacementPerLine() {
        List<SpriteLayer.Placement> placements = SpriteLayer.parse(List.of(
                "bahen.png 112 1992 240 168", "", "robarts.png 64 362 304 294"));

        assertEquals(2, placements.size());
        SpriteLayer.Placement bahen = placements.get(0);
        assertEquals("bahen.png", bahen.getFile());
        assertEquals(112, bahen.getX());
        assertEquals(1992, bahen.getY());
        assertEquals(240, bahen.getWidth());
        assertEquals(168, bahen.getHeight());
    }

    @Test
    void malformedLineNamesItsLineNumber() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SpriteLayer.parse(List.of("bahen.png 1 2 3 4", "robarts.png 64 x")));
        assertTrue(error.getMessage().contains("line 2"), error.getMessage());
    }

    @Test
    void spriteIsInFrontOfAWalkerStandingBehindIt() {
        SpriteLayer.Placement sprite = SpriteLayer.parse(List.of("bahen.png 0 100 50 60")).get(0);

        assertTrue(sprite.isInFrontOf(150));
    }

    @Test
    void spriteIsBehindAWalkerStandingInFrontOfIt() {
        SpriteLayer.Placement sprite = SpriteLayer.parse(List.of("bahen.png 0 100 50 60")).get(0);

        assertFalse(sprite.isInFrontOf(160));
        assertFalse(sprite.isInFrontOf(200));
    }
}
