package data_access;

import entity.Building;
import entity.CampusMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampusMapResourceLoaderTest {
    @Test
    void bundledMapLoadsWithAllDoors() {
        CampusMap map = CampusMapResourceLoader.load(CampusMapResourceLoader.DEFAULT_MAP);
        assertEquals(80, map.getWidth());
        assertEquals(176, map.getHeight());
        for (Building building : Building.values()) {
            assertNotNull(map.doorOf(building));
        }
    }

    @Test
    void missingResourceFailsWithItsName() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> CampusMapResourceLoader.load("/maps/missing.txt"));
        assertTrue(e.getMessage().contains("/maps/missing.txt"), e.getMessage());
    }
}
