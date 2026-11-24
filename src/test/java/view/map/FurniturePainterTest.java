package view.map;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FurniturePainterTest {
    @Test
    void furnitureBesideASidewalkOrRoadIsALamp() {
        TileGrid grid = TileGrid.of(List.of(
                ".o-",
                "=o.",
                "pop"));
        assertEquals(FurniturePainter.Kind.LAMP, FurniturePainter.kindOf(grid, 1, 0));
        assertEquals(FurniturePainter.Kind.LAMP, FurniturePainter.kindOf(grid, 1, 1));
        assertNotEquals(FurniturePainter.Kind.LAMP, FurniturePainter.kindOf(grid, 1, 2));
    }

    @Test
    void furnitureBesideAFoodTruckIsAQueuingStudent() {
        TileGrid grid = TileGrid.of(List.of(
                "-oF",
                "Fo."));
        assertEquals(FurniturePainter.Kind.STUDENT, FurniturePainter.kindOf(grid, 1, 0));
        assertEquals(FurniturePainter.Kind.STUDENT, FurniturePainter.kindOf(grid, 1, 1));
    }

    @Test
    void queuingStudentsFaceTheTruckOthersFaceDown() {
        TileGrid grid = TileGrid.of(List.of(
                "-oF",
                "Fo.",
                "pop"));
        assertEquals(CharacterSheet.Direction.RIGHT, FurniturePainter.studentFacing(grid, 1, 0));
        assertEquals(CharacterSheet.Direction.LEFT, FurniturePainter.studentFacing(grid, 1, 1));
        assertEquals(CharacterSheet.Direction.DOWN, FurniturePainter.studentFacing(grid, 1, 2));
    }

    @Test
    void otherFurnitureVariesByPositionButIsRepeatable() {
        Set<FurniturePainter.Kind> kinds = EnumSet.noneOf(FurniturePainter.Kind.class);
        TileGrid grid = TileGrid.of(List.of("po".repeat(40) + "p"));
        for (int col = 1; col < grid.getColumns(); col += 2) {
            FurniturePainter.Kind kind = FurniturePainter.kindOf(grid, col, 0);
            assertEquals(kind, FurniturePainter.kindOf(grid, col, 0));
            kinds.add(kind);
        }
        assertFalse(kinds.contains(FurniturePainter.Kind.LAMP));
        assertTrue(kinds.size() >= 7, "only " + kinds);
    }

    @Test
    void everyKindDrawsSomethingInItsTile() {
        int tile = GroundPainter.PIXELS_PER_TILE;
        for (FurniturePainter.Kind kind : FurniturePainter.Kind.values()) {
            BufferedImage ground = new BufferedImage(3 * tile, 3 * tile, BufferedImage.TYPE_INT_ARGB);
            BufferedImage canopy = new BufferedImage(3 * tile, 3 * tile, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = ground.createGraphics();
            Graphics2D c = canopy.createGraphics();
            FurniturePainter.paint(g, c, kind, 1, 2);
            g.dispose();
            c.dispose();
            int drawn = 0;
            for (int y = 2 * tile; y < 3 * tile; y++) {
                for (int x = tile; x < 2 * tile; x++) {
                    drawn += (ground.getRGB(x, y) >>> 24) != 0 ? 1 : 0;
                }
            }
            assertTrue(drawn > 20, kind + " drew only " + drawn + " pixels");
        }
    }
}
